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
--1. megkeresi a food táblában az usda_ndb_no -t (ez mindig meglesz!)
         select usda_ndb_no, sote_db_no, food_id, foodname_label_id, refuse_percent, ref_rec_id
		into var_usda_ndb_no, var_sote_db_no, var_food_id, var_label_id, var_refuse_percent, var_ref_rec_id
         from diet.food where usda_ndb_no = var_food_rec.usda_ndb_no limit 1;
              --lehet, hogy már több ilyen rekord van, pl. paradicsompüré 5ször,
              --de akkor mind az 5-ben ki van töltve az usda_no és a sote_no is
--2. megnézi, van-e ebben a rekordban már sote_db_no,
         if var_sote_db_no is null then
--ha nincs, beírja és felveszi a magyar címkét
		update diet.food set sote_db_no = var_food_rec.sote_db_no
			where food_id=var_food_id;
		insert into diet.label_text (label_id, lang_id, label_text, label_long_text)
			values (var_label_id, 1, var_food_rec.label_text, var_food_rec.label_text);
         else
--3. ennek az usda-élelmiszernek már ismerjük egy magyar megfelelojét és egy magyar nevét
--nem írjuk felül, mert akkor a sote-recept nem találja meg
--hanem: új food rekord
		var_old_english_long_label:=null;
		var_old_english_label:=null;
		select label_text, label_long_text into var_old_english_label, var_old_english_long_label
			from diet.label_text where label_id=var_label_id and lang_id=2;
		var_new_label_id := nextval('diet.label_label_id_seq'::regclass);
		var_new_food_id := nextval('diet.food_food_id_seq'::regclass);
		insert into diet.label (label_id, label_type_code)
			values(var_new_label_id, 3); --3 is food name
		insert into diet.label_text (label_id, lang_id, label_text, label_long_text) --a régi angol név megtartása
			values(var_new_label_id, 2, var_old_english_label, var_old_english_long_label); --2 is english
--az új magyar név
		insert into diet.label_text (label_id, lang_id, label_text, label_long_text) 
			values(var_new_label_id, 1, var_food_rec.label_text, var_food_rec.label_text); --1 is Hungarian
--az új élelmiszer
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
recept.usda_sote_mapping tábla minden rekordjára:
1. megkeresi a food táblában az usda_ndb_no -t (ez mindig meglesz!)
2. megnézi, van-e ebben a rekordban már sote_db_no,
ha nincs, beírja és felveszi a magyar címkét
3. ha van, új food rekordot csinál
Futtatás: select * from diet.sote_food_loader();
Készítette: Vassányi István, 2007. aug.';


















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
--1. megnézi, van-e ilyen sote_db_no a diet.food táblában /ha van akkor ez már nem az elso rekordja a receptnek/
	select ref_rec_id, food_id into var_ref_rec_id, var_food_id
	from diet.food where sote_db_no = var_food_rec.sote_db_no; --csak 1 találat lehet
	if var_ref_rec_id is null and var_food_id is not null then 
		--hiba, nincs kitöltve a ref_rec_id
		raise notice 'Hibás élelmiszer: %', var_food_id::varchar(50);
	end if;
	if var_ref_rec_id is null then
--2. ha nincs: új diet.recipe_base és diet.recipe létrehozása recipe_label magyar névvel
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
--új diet.food létrehozása recipe_label magyar névvel, ref_rec_id kitöltése az új rec_id-vel
		var_new_food_id := nextval('diet.food_food_id_seq'::regclass);
		raise notice '	új étel: %', var_food_rec.recipe_label || ',  id: ' || var_new_food_id::varchar(50);
		insert into diet.food (food_id, ref_rec_id, foodname_label_id, sote_db_no)
			values (var_new_food_id, var_new_recipe_id, var_new_label_id, var_food_rec.sote_db_no);		
	else
--3. ha van, akkor ref_rec_id kivétele
		var_rec_id := var_ref_rec_id;
	end if;
--4. ref_rec_id receptjébe új recipe_content rekord felvétele
	var_rec_food_id := null;
	select food_id into var_rec_food_id from diet.food where usda_ndb_no=var_food_rec.usda_ndb_no;
	var_unit_id := null;
	raise notice '		új élelmiszer ételhez: %', var_food_id::varchar(50);
	select case
             when var_food_rec.unit = 'g'  then 5
             when var_food_rec.unit = 'ml'  then 8567
             when var_food_rec.unit = 'db'  then 3
             when var_food_rec.unit = 'kg'  then 6
             when var_food_rec.unit = 'l'  then 7
             when var_food_rec.unit = 'adag'  then 1
             when var_food_rec.unit = 'csomó'  then 2
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
recept.alapanyagok_receptje tábla minden rekordjára:
1. megnézi, van-e ilyen sote_db_no a diet.food táblában /ha van akkor ez már nem az elso rekordja a receptnek/
2. ha nincs: új diet.recipe_base és diet.recipe létrehozása recipe_label magyar névvel, 
új diet.food létrehozása recipe_label magyar névvel, ref_rec_id kitöltése az új rec_id-vel
3. ha van, akkor ref_rec_id kivétele
4. ref_rec_id receptjébe új recipe_content rekord felvétele
Futtatás: select * from diet.sote_recept_excel_loader();
Készítette: Vassányi István, 2007. aug.';















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

--1. új diet.recipe_base és diet.recipe felvétele
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

--2. a receptre parametrizált kurzor nyitása a recept.recept_reszlet táblára
	open cursor_reszlet(var_recept_rec.receptid);
	loop
		fetch cursor_reszlet into var_reszlet_rec;
		exit when not found;
		raise notice '	Összetevo: %', var_reszlet_rec.anyagid::varchar(50) || ', '||var_reszlet_rec.mennyiseg::varchar(50) || ', '||var_reszlet_rec.mertegysid::varchar(50);

--3. megnézi, van-e már ilyen food a sote_db_no alapján
		var_food_id := null;
		select food_id into var_food_id from diet.food where sote_db_no = var_reszlet_rec.anyagid limit 1;
		if var_food_id is null then
-- ha nincs, létrehozza
			select megnevezes, usda_ndb_no into var_alapanyag_neve, var_usda_ndb_no 
					from recept.alapanyag where sorszam=var_reszlet_rec.anyagid;
			raise notice '		új alapanyag: %', var_alapanyag_neve;
			var_new_label_id := nextval('diet.label_label_id_seq'::regclass);
			insert into diet.label (label_id, label_type_code)
				values(var_new_label_id, 3); --3 is food name
			insert into diet.label_text (label_id, lang_id, label_text, label_long_text) 
				values(var_new_label_id, 1, var_alapanyag_neve, var_alapanyag_neve); --1 is Hungarian

			var_food_id := nextval('diet.food_food_id_seq'::regclass);
			insert into diet.food (food_id, usda_ndb_no, foodname_label_id, sote_db_no)
				values (var_food_id, var_usda_ndb_no, var_new_label_id, var_reszlet_rec.anyagid);
		end if;
--4. a recipe_content rekord felvétele
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
recept.recept tábla minden rekordjára:
1. új diet.recipe_base és diet.recipe felvétele
2. a receptre parametrizált kurzor nyitása a recept.recept_reszlet táblára
3. ebben: megnézi, van-e már ilyen food a sote_db_no alapján, ha nincs, létrehozza
4. a recipe_content rekord felvétele

Futtatás: select * from diet.sote_recept_loader();
Készítette: Vassányi István, 2007. aug.';













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
Beírja a diet.nutrient táblába az usda.nutrient táblát,
 és létrehozza az angol nyelvu címkéket.
Futtatás: select * from diet.usda_nutrient_loader();
Készítette: Vassányi István, 2007. júl.';












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
Beírja a diet.food táblába az usda.food_desc táblát,
 és létrehozza az angol nyelvu címkéket.
Futtatás: select * from diet.usda_fod_loader();
Készítette: Vassányi István, 2007. júl.';











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
	--2. ha az amount nem 1, akkor része a mért.egységnek
		if var_weight_rec.amount = 1 then var_unit_label := var_weight_rec.unit_label;
			else  var_unit_label := var_weight_rec.amount::varchar(50) || ' ' || var_weight_rec.unit_label;
		end if;
--		raise notice '	labels: %', var_unit_label;
	 --3. van-e ilyen mértékegység?
		var_label_id := null; var_unit_id := null;
		select l.label_id into var_label_id from diet.label_text lt inner join diet.label l on lt.label_id=l.label_id 
			where l.label_type_code=1 --meas.unit
			and lt.lang_id = 2 --english
			and lt.label_text=var_unit_label limit 1;
--		raise notice '	label_id: %', var_label_id;
		if var_label_id is null then --uj címke és mért. egys. létrehozása
			var_label_id := nextval('diet.label_label_id_seq'::regclass);
		        insert into diet.label (label_id, label_type_code) 
				values(var_label_id, 1); --1 a mért.egys. típusa
			insert into diet.label_text (label_id, lang_id, label_text, label_long_text)
				values(var_label_id, 2, var_unit_label, null); --2 is english
			var_unit_id := nextval('diet.unit_unit_id_seq'::regclass);
--			raise notice '	ÚJ label_id: %', var_label_id;
--			raise notice '	ÚJ unit_id: %', var_unit_id;
			insert into diet.unit (unit_id, unit_label_id) values (var_unit_id, var_label_id);
		else --van már ilyen mért.egys., unit_id kiolvasása
			select unit_id into var_unit_id from diet.unit where unit_label_id=var_label_id;
--			raise notice '	TALÁLT unit_id: %', var_unit_label || ' ' || var_unit_id::varchar(50);
		end if;
		if var_unit_id is null then 
			raise notice 'hianyzo unit_id: %', var_food_rec.usda_ndb_no || var_weight_rec.seq::varchar(50)|| var_weight_rec.unit_label::varchar(50);
		end if;
--		raise notice '	unit_id: %', var_unit_id;
	--unit_id OK, 
	--4. új weight rekord
		if exists (select * from diet.food_weight where food_id=var_food_rec.food_id and unit_id=var_unit_id) then --kulcsütközés
			raise notice 'food_no: %' , var_food_rec.usda_ndb_no::varchar(50) ||', unit_id ' || var_unit_id::varchar(50);
		end if;
		insert into diet.food_weight (food_id, unit_id, mass_dkg) 
			values (var_food_rec.food_id, var_unit_id, var_weight_rec.mass_dkg);
	--5. elso két unit_id elmentése default-nak
		var_j := var_j+1;
		if var_j = 1 then var_default_unit_id := var_unit_id; end if;
		if var_j = 2 then var_second_unit_id := var_unit_id; end if;
	 end loop;
	 close cursor_weight;
	--food_units új rekordja
	if var_weight_rec_found = false then --még nincs  a food-nak weight rekordja
	--4. dummy weight rekord beszúrás 
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
COMMENT ON FUNCTION diet.usda_weight_loader() IS 'A diet.food tábla minden rekordjához:
1. megnézi az usda.weight tábla hozzá tartozó rekordjait
1.5 ha nincs egy sem, akkor a default_unit a dekagramm, label_id=14569, default_unit_id=15 
2. ha az amount nem 1, akkor az is a mértékegység része (msre_desc) pl. 6 mushrooms
3. ha nincs még ilyen nevu mértékegység, akkor létrehozza, ha van, lekérdezi az id-jét
4. minden usda.weight rekordból készít egy food_weight rekordot, egyet a dkg-mal akkor is, ha nem volt food_weight rekord
5. az elso két usda.weight rekord esetében kitölti a food_units default_unit_id és second_unit_id mezoit 
ha nem volt food_weight rekord, akkor csak a default_unit_id lesz dkg, a másik null

Futtatás: select * from diet.usda_weight_loader();

Készítette: Vassányi István, 2007. júl.';









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

--1. etel_neve: van-e ilyen nevu étel a label_text-ben?
--(elvileg minddig kell lennie, pont 1-nek) --> rec_id
--2. a talált recept magyar nevének az elovétele
	var_recipe_id := null;
	select r.rec_id into var_recipe_id
	from diet.label_text lt inner join diet.label l on lt.label_id = l.label_id 
				inner join diet.recipe r on r.recname_label_id = l.label_id
	where l.label_type_code = 2 --type: recipe name
		and lt.lang_id = 1 --magyar nyelvu
		and lt.label_text = var_recept_rec.etel_neve;

	if var_recipe_id is null then --nincs meg a recept
		raise notice '		A recept nem található a menugene adatbázisban';
	else	--megvan

--3. új label, label_text léterehozása (receptnév, recept tábla leiras mezoje)
		var_new_label_id := nextval('diet.label_label_id_seq'::regclass);
		insert into diet.label (label_id, label_type_code)
			values(var_new_label_id, 6); --6 is recipe description
		insert into diet.label_text (label_id, lang_id, label_text, label_long_text) 
			values(var_new_label_id, 1, var_recept_rec.etel_neve, var_recept_rec.leiras); --1 is Hungarian

--4. a label hozzákötése a recipe.recdesc_label_id mezohöz
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
A public.recept tábla minden rekordjára:
1. etel_neve: van-e ilyen nevu étel a label_text-ben?
(elvileg minddig kell lennie, pont 1-nek) --> rec_id
2. a talált recept magyar nevének az elovétele
3. új label, label_text léterehozása (receptnév, recept tábla leiras mezoje)
4. a label hozzákötése a recipe.recdesc_label_id mezohöz

Futtatás: select * from diet.sote_rec_description_loader();
Készítette: Vassányi István, 2008. feb.';




 _____________ NOD32 2862 (20080210) Információ _____________

Az üzenetet a NOD32 antivirus system megvizsgálta.
http://www.nod32.hu
