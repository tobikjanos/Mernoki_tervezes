-- Function: diet.sote_food_loader()

-- DROP FUNCTION diet.sote_food_loader();

CREATE OR REPLACE FUNCTION diet.sote_food_loader()
  RETURNS int4 AS
$BODY$
declare
       var_i integer;
       var_usda_ndb_no varchar(50);
       var_sote_db_no numeric;
       var_food_id integer;
       var_new_food_id integer;
       var_label_id integer;
       var_new_label_id integer;
       var_refuse_percent integer;
       var_ref_rec_id integer;
       var_old_english_label varchar(200);
       var_old_english_long_label varchar(200);
       cursor_food cursor for select * from recept.usda_sote_mapping;
       var_food_rec recept.usda_sote_mapping%ROWTYPE;
begin
     var_i := 0;
     open cursor_food;
     loop
         fetch cursor_food into var_food_rec;
         exit when not found;
         raise notice 'Food name: %', var_food_rec.label_text;
         var_usda_ndb_no := null;
         var_sote_db_no := null;
         var_food_id := null;
         var_label_id := null;
	var_refuse_percent := null;
	var_ref_rec_id :=  null;
--1. megkeresi a food t�bl�ban az usda_ndb_no -t (ez mindig meglesz!)
         select usda_ndb_no, sote_db_no, food_id, foodname_label_id, refuse_percent, ref_rec_id
		into var_usda_ndb_no, var_sote_db_no, var_food_id, var_label_id, var_refuse_percent, var_ref_rec_id
         from diet.food where usda_ndb_no = var_food_rec.usda_ndb_no limit 1;
              --lehet, hogy m�r t�bb ilyen rekord van, pl. paradicsomp�r� 5sz�r,
              --de akkor mind az 5-ben ki van t�ltve az usda_no �s a sote_no is
--2. megn�zi, van-e ebben a rekordban m�r sote_db_no,
         if var_sote_db_no is null then
--ha nincs, be�rja �s felveszi a magyar c�mk�t
		update diet.food set sote_db_no = var_food_rec.sote_db_no
			where food_id=var_food_id;
		insert into diet.label_text (label_id, lang_id, label_text, label_long_text)
			values (var_label_id, 1, var_food_rec.label_text, var_food_rec.label_text);
         else
--3. ennek az usda-�lelmiszernek m�r ismerj�k egy magyar megfeleloj�t �s egy magyar nev�t
--nem �rjuk fel�l, mert akkor a sote-recept nem tal�lja meg
--hanem: �j food rekord
		var_old_english_long_label:=null;
		var_old_english_label:=null;
		select label_text, label_long_text into var_old_english_label, var_old_english_long_label
			from diet.label_text where label_id=var_label_id and lang_id=2;
		var_new_label_id := nextval('diet.label_label_id_seq'::regclass);
		var_new_food_id := nextval('diet.food_food_id_seq'::regclass);
		insert into diet.label (label_id, label_type_code)
			values(var_new_label_id, 3); --3 is food name
		insert into diet.label_text (label_id, lang_id, label_text, label_long_text) --a r�gi angol n�v megtart�sa
			values(var_new_label_id, 2, var_old_english_label, var_old_english_long_label); --2 is english
--az �j magyar n�v
		insert into diet.label_text (label_id, lang_id, label_text, label_long_text) 
			values(var_new_label_id, 1, var_food_rec.label_text, var_food_rec.label_text); --1 is Hungarian
--az �j �lelmiszer
		var_new_food_id := nextval('diet.food_food_id_seq'::regclass);
		insert into diet.food (food_id, usda_ndb_no, sote_db_no, refuse_percent, ref_rec_id, 
			foodname_label_id)
                values (var_new_food_id, var_usda_ndb_no, var_food_rec.sote_db_no, var_refuse_percent, var_ref_rec_id,
			var_new_label_id);
         end if;
         var_i := var_i+1;
     end loop;
     close cursor_food;
     return var_i;  --no. of foods imported
end;
$BODY$
  LANGUAGE 'plpgsql' VOLATILE;
ALTER FUNCTION diet.sote_food_loader() OWNER TO postgres;
COMMENT ON FUNCTION diet.sote_food_loader() IS '
recept.usda_sote_mapping t�bla minden rekordj�ra:
1. megkeresi a food t�bl�ban az usda_ndb_no -t (ez mindig meglesz!)
2. megn�zi, van-e ebben a rekordban m�r sote_db_no,
ha nincs, be�rja �s felveszi a magyar c�mk�t
3. ha van, �j food rekordot csin�l
Futtat�s: select * from diet.sote_food_loader();
K�sz�tette: Vass�nyi Istv�n, 2007. aug.';


















-- Function: diet.sote_recept_excel_loader()

-- DROP FUNCTION diet.sote_recept_excel_loader();

CREATE OR REPLACE FUNCTION diet.sote_recept_excel_loader()
  RETURNS int4 AS
$BODY$
declare
       var_i integer;
       var_usda_ndb_no varchar(50);
       var_sote_db_no numeric;
       var_food_id integer;
       var_new_food_id integer;
       var_rec_food_id integer;
	var_label_id integer;
       var_new_label_id integer;
       var_new_recipe_id integer;
       var_new_recbase_id integer;
	var_rec_id integer;
	var_unit_id integer;
       var_ref_rec_id integer;
       var_old_english_label varchar(200);
       var_old_english_long_label varchar(200);
       cursor_food cursor for select * from recept.alapanyagok_receptje;
       var_food_rec recept.alapanyagok_receptje%ROWTYPE;
begin
     var_i := 0;
     open cursor_food;
     loop
         fetch cursor_food into var_food_rec;
         exit when not found;
--         raise notice 'Food name: %', var_food_rec.recipe_label;
	var_food_id := null;
	var_ref_rec_id :=  null;
--1. megn�zi, van-e ilyen sote_db_no a diet.food t�bl�ban /ha van akkor ez m�r nem az elso rekordja a receptnek/
	select ref_rec_id, food_id into var_ref_rec_id, var_food_id
	from diet.food where sote_db_no = var_food_rec.sote_db_no; --csak 1 tal�lat lehet
	if var_ref_rec_id is null and var_food_id is not null then 
		--hiba, nincs kit�ltve a ref_rec_id
		raise notice 'Hib�s �lelmiszer: %', var_food_id::varchar(50);
	end if;
	if var_ref_rec_id is null then
--2. ha nincs: �j diet.recipe_base �s diet.recipe l�trehoz�sa recipe_label magyar n�vvel
		var_new_label_id := nextval('diet.label_label_id_seq'::regclass);
		insert into diet.label (label_id, label_type_code)
			values(var_new_label_id, 2); --2 is recipe name
		insert into diet.label_text (label_id, lang_id, label_text, label_long_text) 
			values(var_new_label_id, 1, var_food_rec.recipe_label, var_food_rec.recipe_label); --1 is Hungarian
		var_new_recbase_id := nextval('diet.recipe_base_rec_id_seq'::regclass);
		insert into diet.recipe_base (recbase_id, basename_label_id) values (var_new_recbase_id, var_new_label_id);
		var_new_recipe_id := nextval('diet.recipe_rec_id_seq'::regclass);
		insert into diet.recipe (rec_id, recbase_id, recname_label_id) 
			values (var_new_recipe_id, var_new_recbase_id, var_new_label_id);
		var_rec_id := var_new_recipe_id;
--�j diet.food l�trehoz�sa recipe_label magyar n�vvel, ref_rec_id kit�lt�se az �j rec_id-vel
		var_new_food_id := nextval('diet.food_food_id_seq'::regclass);
		raise notice '	�j �tel: %', var_food_rec.recipe_label || ',  id: ' || var_new_food_id::varchar(50);
		insert into diet.food (food_id, ref_rec_id, foodname_label_id, sote_db_no)
			values (var_new_food_id, var_new_recipe_id, var_new_label_id, var_food_rec.sote_db_no);		
	else
--3. ha van, akkor ref_rec_id kiv�tele
		var_rec_id := var_ref_rec_id;
	end if;
--4. ref_rec_id receptj�be �j recipe_content rekord felv�tele
	var_rec_food_id := null;
	select food_id into var_rec_food_id from diet.food where usda_ndb_no=var_food_rec.usda_ndb_no;
	var_unit_id := null;
	raise notice '		�j �lelmiszer �telhez: %', var_food_id::varchar(50);
	select case
             when var_food_rec.unit = 'g'  then 5
             when var_food_rec.unit = 'ml'  then 8567
             when var_food_rec.unit = 'db'  then 3
             when var_food_rec.unit = 'kg'  then 6
             when var_food_rec.unit = 'l'  then 7
             when var_food_rec.unit = 'adag'  then 1
             when var_food_rec.unit = 'csom�'  then 2
             when var_food_rec.unit = 'fej'  then 4
             when var_food_rec.unit = 'csomag'  then 9210
             else null
        into var_unit_id end;
	insert into diet.recipe_content (rec_id, food_id, role_id, quantity, unit_id)
		values (var_rec_id, var_rec_food_id, 8, var_food_rec.quantity, var_unit_id);

     var_i := var_i+1;
     end loop;
     close cursor_food;
     return var_i;  --no. of foods imported
end;
$BODY$
  LANGUAGE 'plpgsql' VOLATILE;
ALTER FUNCTION diet.sote_recept_excel_loader() OWNER TO postgres;
COMMENT ON FUNCTION diet.sote_recept_excel_loader() IS '
recept.alapanyagok_receptje t�bla minden rekordj�ra:
1. megn�zi, van-e ilyen sote_db_no a diet.food t�bl�ban /ha van akkor ez m�r nem az elso rekordja a receptnek/
2. ha nincs: �j diet.recipe_base �s diet.recipe l�trehoz�sa recipe_label magyar n�vvel, 
�j diet.food l�trehoz�sa recipe_label magyar n�vvel, ref_rec_id kit�lt�se az �j rec_id-vel
3. ha van, akkor ref_rec_id kiv�tele
4. ref_rec_id receptj�be �j recipe_content rekord felv�tele
Futtat�s: select * from diet.sote_recept_excel_loader();
K�sz�tette: Vass�nyi Istv�n, 2007. aug.';















-- Function: diet.sote_recept_loader()

-- DROP FUNCTION diet.sote_recept_loader();

CREATE OR REPLACE FUNCTION diet.sote_recept_loader()
  RETURNS int4 AS
$BODY$
declare
	var_i integer;
	var_usda_ndb_no varchar(50);
	var_alapanyag_neve varchar(200);
	var_sote_db_no numeric;
	var_food_id integer;
	var_new_label_id integer;
	var_new_recipe_id integer;
	var_new_recbase_id integer;
	var_unit_id integer;
	cursor_recept cursor for select * from recept.recept;
	var_recept_rec recept.recept%ROWTYPE;
	cursor_reszlet cursor (par_recept_id integer) for select * from recept.recept_reszlet
		where receptid = par_recept_id;
	var_reszlet_rec recept.recept_reszlet%ROWTYPE;
begin
     var_i := 0;
     open cursor_recept;
     loop
         fetch cursor_recept into var_recept_rec;
         exit when not found;
         raise notice 'Recept neve: %', var_recept_rec.etel_neve;

--1. �j diet.recipe_base �s diet.recipe felv�tele
	var_new_label_id := nextval('diet.label_label_id_seq'::regclass);
	insert into diet.label (label_id, label_type_code)
		values(var_new_label_id, 2); --2 is recipe name
	insert into diet.label_text (label_id, lang_id, label_text, label_long_text) 
		values(var_new_label_id, 1, var_recept_rec.etel_neve, var_recept_rec.etel_neve); --1 is Hungarian
	var_new_recbase_id := nextval('diet.recipe_base_rec_id_seq'::regclass);
	insert into diet.recipe_base (recbase_id, basename_label_id) values (var_new_recbase_id, var_new_label_id);
	var_new_recipe_id := nextval('diet.recipe_rec_id_seq'::regclass);
	insert into diet.recipe (rec_id, recbase_id, recname_label_id, sote_rec_id) 
		values (var_new_recipe_id, var_new_recbase_id, var_new_label_id, var_recept_rec.receptid);

--2. a receptre parametriz�lt kurzor nyit�sa a recept.recept_reszlet t�bl�ra
	open cursor_reszlet(var_recept_rec.receptid);
	loop
		fetch cursor_reszlet into var_reszlet_rec;
		exit when not found;
		raise notice '	�sszetevo: %', var_reszlet_rec.anyagid::varchar(50) || ', '||var_reszlet_rec.mennyiseg::varchar(50) || ', '||var_reszlet_rec.mertegysid::varchar(50);

--3. megn�zi, van-e m�r ilyen food a sote_db_no alapj�n
		var_food_id := null;
		select food_id into var_food_id from diet.food where sote_db_no = var_reszlet_rec.anyagid limit 1;
		if var_food_id is null then
-- ha nincs, l�trehozza
			select megnevezes, usda_ndb_no into var_alapanyag_neve, var_usda_ndb_no 
					from recept.alapanyag where sorszam=var_reszlet_rec.anyagid;
			raise notice '		�j alapanyag: %', var_alapanyag_neve;
			var_new_label_id := nextval('diet.label_label_id_seq'::regclass);
			insert into diet.label (label_id, label_type_code)
				values(var_new_label_id, 3); --3 is food name
			insert into diet.label_text (label_id, lang_id, label_text, label_long_text) 
				values(var_new_label_id, 1, var_alapanyag_neve, var_alapanyag_neve); --1 is Hungarian

			var_food_id := nextval('diet.food_food_id_seq'::regclass);
			insert into diet.food (food_id, usda_ndb_no, foodname_label_id, sote_db_no)
				values (var_food_id, var_usda_ndb_no, var_new_label_id, var_reszlet_rec.anyagid);
		end if;
--4. a recipe_content rekord felv�tele
		var_unit_id := null;
		select case
			when var_reszlet_rec.mertegysid = 5  then 5
			when var_reszlet_rec.mertegysid = 3  then 3
			when var_reszlet_rec.mertegysid = 6  then 6
			when var_reszlet_rec.mertegysid = 7  then 7
			when var_reszlet_rec.mertegysid = 1  then 1
			when var_reszlet_rec.mertegysid = 2  then 2
			when var_reszlet_rec.mertegysid = 4  then 4
			when var_reszlet_rec.mertegysid = 8  then 9210
		else null
		into var_unit_id end;
		insert into diet.recipe_content (rec_id, food_id, role_id, quantity, unit_id)
			values (var_new_recipe_id, var_food_id, 8, var_reszlet_rec.mennyiseg, var_unit_id);
	end loop;
	close cursor_reszlet;
     var_i := var_i+1;
     end loop;
     close cursor_recept;
     return var_i;  --no. of foods imported
end;
$BODY$
  LANGUAGE 'plpgsql' VOLATILE;
ALTER FUNCTION diet.sote_recept_loader() OWNER TO postgres;
COMMENT ON FUNCTION diet.sote_recept_loader() IS '
recept.recept t�bla minden rekordj�ra:
1. �j diet.recipe_base �s diet.recipe felv�tele
2. a receptre parametriz�lt kurzor nyit�sa a recept.recept_reszlet t�bl�ra
3. ebben: megn�zi, van-e m�r ilyen food a sote_db_no alapj�n, ha nincs, l�trehozza
4. a recipe_content rekord felv�tele

Futtat�s: select * from diet.sote_recept_loader();
K�sz�tette: Vass�nyi Istv�n, 2007. aug.';













-- Function: diet.usda_decimals_loader()

-- DROP FUNCTION diet.usda_decimals_loader();

CREATE OR REPLACE FUNCTION diet.usda_decimals_loader()
  RETURNS int4 AS
$BODY$
declare
       var_i integer;
       var_decimals integer;
       var_nutr_id integer;
       var_label_id integer;

       cursor_nutr cursor for select * from usda.nutrient;
       var_nutr_rec usda.nutrient%ROWTYPE;
begin
     var_i := 0;
     open cursor_nutr;
     loop
         fetch cursor_nutr into var_nutr_rec;
         exit when not found;
	--select decimals into var_
         update diet.nutrient set decimals=var_nutr_rec.decimals where usda_nutr_no=var_nutr_rec.nutr_no;               
         var_i := var_i+1;
     end loop;
     close cursor_nutr;
     return var_i;  --no. of nutrients imported
end;
$BODY$
  LANGUAGE 'plpgsql' VOLATILE;
ALTER FUNCTION diet.usda_decimals_loader() OWNER TO postgres;












-- Function: diet.usda_food_loader()

-- DROP FUNCTION diet.usda_food_loader();

CREATE OR REPLACE FUNCTION diet.usda_food_loader()
  RETURNS int4 AS
$BODY$
/* New function body */
declare
       var_i integer;
       var_usda_ndb_no varchar(50);
       var_food_id integer;
       var_label_id integer;

       cursor_food cursor for select * from usda.food_des where "Shrt_Desc" is not null;
       var_food_rec usda.food_des%ROWTYPE;
begin
     var_i := 0;
     open cursor_food;
     loop
         fetch cursor_food into var_food_rec;
         exit when not found;
         raise notice 'Food name: %', var_food_rec."Shrt_Desc";
         var_label_id := nextval('diet.label_label_id_seq'::regclass);
         var_food_id := nextval('diet.food_food_id_seq'::regclass);
         insert into diet.label (label_id, label_type_code)
                values(var_label_id, 3); --3 is food name
         insert into diet.label_text (label_id, lang_id, label_text, label_long_text)
                values(var_label_id, 2, var_food_rec."Shrt_Desc", var_food_rec."Long_Desc"); --2 is english
         insert into diet.food (food_id, usda_ndb_no, refuse_percent, foodname_label_id)
                values (var_food_id, var_food_rec."NDB_No", var_food_rec."Refuse", var_label_id);
         var_i := var_i+1;
     end loop;
     --select * from diet.recipe;
     close cursor_food;
     return var_i;  --no. of foods imported
end;
$BODY$
  LANGUAGE 'plpgsql' VOLATILE;
ALTER FUNCTION diet.usda_food_loader() OWNER TO postgres;
COMMENT ON FUNCTION diet.usda_food_loader() IS '
Be�rja a diet.nutrient t�bl�ba az usda.nutrient t�bl�t,
 �s l�trehozza az angol nyelvu c�mk�ket.
Futtat�s: select * from diet.usda_nutrient_loader();
K�sz�tette: Vass�nyi Istv�n, 2007. j�l.';












-- Function: diet.usda_nutrient_loader()

-- DROP FUNCTION diet.usda_nutrient_loader();

CREATE OR REPLACE FUNCTION diet.usda_nutrient_loader()
  RETURNS int4 AS
$BODY$
declare
       var_i integer;
       var_unit_id integer;
       var_nutr_id integer;
       var_label_id integer;

       cursor_nutr cursor for select * from usda.nutrient;
       var_nutr_rec usda.nutrient%ROWTYPE;
begin
     var_i := 0;
     open cursor_nutr;
     loop
         fetch cursor_nutr into var_nutr_rec;
         exit when not found;
         --raise notice 'Nutrient name: %', var_nutr_rec.label_text;
         
         select case
             when var_nutr_rec.unit_label = 'g'  then 5
             when var_nutr_rec.unit_label = 'IU'  then 8
             when var_nutr_rec.unit_label = 'kcal'  then 9
             when var_nutr_rec.unit_label = 'kj'  then 10
             when var_nutr_rec.unit_label = 'mcg'  then 11
             when var_nutr_rec.unit_label = 'mcg_DFE'  then 12
             when var_nutr_rec.unit_label = 'mcg_RAE'  then 13
             when var_nutr_rec.unit_label = 'mg'  then 14
             else null
         into var_unit_id end;
         --raise notice 'Nutrient id: %', var_unit_id;
         if var_unit_id is null then
             raise notice 'Nutrient name: %', var_nutr_rec.label_text;
         end if;

         var_label_id := nextval('diet.label_label_id_seq'::regclass);
         var_nutr_id := nextval('diet.nutrient_nutr_id_seq'::regclass);
         insert into diet.label (label_id, label_type_code)
                values(var_label_id, 4); --4 is nutrient name
         insert into diet.label_text (label_id, lang_id, label_text, label_long_text)
                values(var_label_id, 2, var_nutr_rec.label_text, var_nutr_rec.label_long_text); --2 is english
         insert into diet.nutrient (nutr_id, usda_nutr_no, nutrname_label_id, unit_id)
                values (var_nutr_id, var_nutr_rec.nutr_no, var_label_id, var_unit_id);
         var_i := var_i+1;
     end loop;
     close cursor_nutr;
     return var_i;  --no. of nutrients imported
end;
$BODY$
  LANGUAGE 'plpgsql' VOLATILE;
ALTER FUNCTION diet.usda_nutrient_loader() OWNER TO postgres;
COMMENT ON FUNCTION diet.usda_nutrient_loader() IS '
Be�rja a diet.food t�bl�ba az usda.food_desc t�bl�t,
 �s l�trehozza az angol nyelvu c�mk�ket.
Futtat�s: select * from diet.usda_fod_loader();
K�sz�tette: Vass�nyi Istv�n, 2007. j�l.';











-- Function: diet.usda_weight_loader()

-- DROP FUNCTION diet.usda_weight_loader();

CREATE OR REPLACE FUNCTION diet.usda_weight_loader()
  RETURNS int4 AS
$BODY$
declare
       var_i integer; var_j integer;
       var_usda_ndb_no varchar(50);
       var_label_id integer;
       var_weight_rec_found boolean;
       var_unit_id integer;
       var_default_unit_id integer; var_second_unit_id integer; 
       var_unit_label varchar(200);
       cursor_food cursor for select * from diet.food where usda_ndb_no is not null;
       var_food_rec diet.food%ROWTYPE;
       cursor_weight cursor (par_usda_ndb_no varchar(50)) for select * from usda.weight where usda_ndb_no = par_usda_ndb_no order by seq;
       var_weight_rec usda.weight%ROWTYPE;
begin
     var_i := 0;
     open cursor_food;
     loop
         fetch cursor_food into var_food_rec;
         exit when not found;
--       raise notice 'Food no.: %', var_food_rec.usda_ndb_no;
	 var_default_unit_id := null; var_second_unit_id := null; var_weight_rec_found := false; var_j := 0;
	--1. a food weight rekordjai
	 open cursor_weight(var_food_rec.usda_ndb_no);
	 loop
		fetch cursor_weight into var_weight_rec;
		exit when not found;
		var_weight_rec_found := true;
--		raise notice '	';
--		raise notice '	weights: %', var_weight_rec.seq::varchar(50) || ' ' || var_weight_rec.amount::varchar(50) || ' ' || var_weight_rec.unit_label::varchar(50);
	--2. ha az amount nem 1, akkor r�sze a m�rt.egys�gnek
		if var_weight_rec.amount = 1 then var_unit_label := var_weight_rec.unit_label;
			else  var_unit_label := var_weight_rec.amount::varchar(50) || ' ' || var_weight_rec.unit_label;
		end if;
--		raise notice '	labels: %', var_unit_label;
	 --3. van-e ilyen m�rt�kegys�g?
		var_label_id := null; var_unit_id := null;
		select l.label_id into var_label_id from diet.label_text lt inner join diet.label l on lt.label_id=l.label_id 
			where l.label_type_code=1 --meas.unit
			and lt.lang_id = 2 --english
			and lt.label_text=var_unit_label limit 1;
--		raise notice '	label_id: %', var_label_id;
		if var_label_id is null then --uj c�mke �s m�rt. egys. l�trehoz�sa
			var_label_id := nextval('diet.label_label_id_seq'::regclass);
		        insert into diet.label (label_id, label_type_code) 
				values(var_label_id, 1); --1 a m�rt.egys. t�pusa
			insert into diet.label_text (label_id, lang_id, label_text, label_long_text)
				values(var_label_id, 2, var_unit_label, null); --2 is english
			var_unit_id := nextval('diet.unit_unit_id_seq'::regclass);
--			raise notice '	�J label_id: %', var_label_id;
--			raise notice '	�J unit_id: %', var_unit_id;
			insert into diet.unit (unit_id, unit_label_id) values (var_unit_id, var_label_id);
		else --van m�r ilyen m�rt.egys., unit_id kiolvas�sa
			select unit_id into var_unit_id from diet.unit where unit_label_id=var_label_id;
--			raise notice '	TAL�LT unit_id: %', var_unit_label || ' ' || var_unit_id::varchar(50);
		end if;
		if var_unit_id is null then 
			raise notice 'hianyzo unit_id: %', var_food_rec.usda_ndb_no || var_weight_rec.seq::varchar(50)|| var_weight_rec.unit_label::varchar(50);
		end if;
--		raise notice '	unit_id: %', var_unit_id;
	--unit_id OK, 
	--4. �j weight rekord
		if exists (select * from diet.food_weight where food_id=var_food_rec.food_id and unit_id=var_unit_id) then --kulcs�tk�z�s
			raise notice 'food_no: %' , var_food_rec.usda_ndb_no::varchar(50) ||', unit_id ' || var_unit_id::varchar(50);
		end if;
		insert into diet.food_weight (food_id, unit_id, mass_dkg) 
			values (var_food_rec.food_id, var_unit_id, var_weight_rec.mass_dkg);
	--5. elso k�t unit_id elment�se default-nak
		var_j := var_j+1;
		if var_j = 1 then var_default_unit_id := var_unit_id; end if;
		if var_j = 2 then var_second_unit_id := var_unit_id; end if;
	 end loop;
	 close cursor_weight;
	--food_units �j rekordja
	if var_weight_rec_found = false then --m�g nincs  a food-nak weight rekordja
	--4. dummy weight rekord besz�r�s 
		insert into diet.food_weight (food_id, unit_id, mass_dkg) 
			values (var_food_rec.food_id, 15, 1); -- 1 dkg az 1 dkg
		var_default_unit_id := 15; --dkg a default
	end if;
	insert into diet.food_units (food_id, lang_id, default_unit_id, second_unit_id)
		values (var_food_rec.food_id, 2, var_default_unit_id, var_second_unit_id);
         var_i := var_i+1;
     end loop;
     close cursor_food;
     return var_i;  --no. of weights imported
end; $BODY$
  LANGUAGE 'plpgsql' VOLATILE;
ALTER FUNCTION diet.usda_weight_loader() OWNER TO postgres;
COMMENT ON FUNCTION diet.usda_weight_loader() IS 'A diet.food t�bla minden rekordj�hoz:
1. megn�zi az usda.weight t�bla hozz� tartoz� rekordjait
1.5 ha nincs egy sem, akkor a default_unit a dekagramm, label_id=14569, default_unit_id=15 
2. ha az amount nem 1, akkor az is a m�rt�kegys�g r�sze (msre_desc) pl. 6 mushrooms
3. ha nincs m�g ilyen nevu m�rt�kegys�g, akkor l�trehozza, ha van, lek�rdezi az id-j�t
4. minden usda.weight rekordb�l k�sz�t egy food_weight rekordot, egyet a dkg-mal akkor is, ha nem volt food_weight rekord
5. az elso k�t usda.weight rekord eset�ben kit�lti a food_units default_unit_id �s second_unit_id mezoit 
ha nem volt food_weight rekord, akkor csak a default_unit_id lesz dkg, a m�sik null

Futtat�s: select * from diet.usda_weight_loader();

K�sz�tette: Vass�nyi Istv�n, 2007. j�l.';









CREATE OR REPLACE FUNCTION diet.sote_rec_description_loader()
  RETURNS int4 AS
$BODY$
declare
	var_i integer;
	var_new_label_id integer;
	var_recipe_id integer;
	cursor_recept cursor for select * from public.recept where leiras is not null;
	var_recept_rec public.recept%ROWTYPE;
begin
     var_i := 0;
     open cursor_recept;
     loop
         fetch cursor_recept into var_recept_rec;
         exit when not found;
         raise notice 'Recept neve: %', var_recept_rec.etel_neve;

--1. etel_neve: van-e ilyen nevu �tel a label_text-ben?
--(elvileg minddig kell lennie, pont 1-nek) --> rec_id
--2. a tal�lt recept magyar nev�nek az elov�tele
	var_recipe_id := null;
	select r.rec_id into var_recipe_id
	from diet.label_text lt inner join diet.label l on lt.label_id = l.label_id 
				inner join diet.recipe r on r.recname_label_id = l.label_id
	where l.label_type_code = 2 --type: recipe name
		and lt.lang_id = 1 --magyar nyelvu
		and lt.label_text = var_recept_rec.etel_neve;

	if var_recipe_id is null then --nincs meg a recept
		raise notice '		A recept nem tal�lhat� a menugene adatb�zisban';
	else	--megvan

--3. �j label, label_text l�terehoz�sa (receptn�v, recept t�bla leiras mezoje)
		var_new_label_id := nextval('diet.label_label_id_seq'::regclass);
		insert into diet.label (label_id, label_type_code)
			values(var_new_label_id, 6); --6 is recipe description
		insert into diet.label_text (label_id, lang_id, label_text, label_long_text) 
			values(var_new_label_id, 1, var_recept_rec.etel_neve, var_recept_rec.leiras); --1 is Hungarian

--4. a label hozz�k�t�se a recipe.recdesc_label_id mezoh�z
		update diet.recipe set recdesc_label_id = var_new_label_id
			where rec_id = var_recipe_id;
	end if;
	var_i := var_i+1;
	end loop;
    close cursor_recept;
    return var_i;  --no. of descriptions imported
end;
$BODY$
  LANGUAGE 'plpgsql' VOLATILE;
ALTER FUNCTION diet.sote_recept_loader() OWNER TO postgres;
COMMENT ON FUNCTION diet.sote_recept_loader() IS '
A public.recept t�bla minden rekordj�ra:
1. etel_neve: van-e ilyen nevu �tel a label_text-ben?
(elvileg minddig kell lennie, pont 1-nek) --> rec_id
2. a tal�lt recept magyar nev�nek az elov�tele
3. �j label, label_text l�terehoz�sa (receptn�v, recept t�bla leiras mezoje)
4. a label hozz�k�t�se a recipe.recdesc_label_id mezoh�z

Futtat�s: select * from diet.sote_rec_description_loader();
K�sz�tette: Vass�nyi Istv�n, 2008. feb.';




 _____________ NOD32 2862 (20080210) Inform�ci� _____________

Az �zenetet a NOD32 antivirus system megvizsg�lta.
http://www.nod32.hu
