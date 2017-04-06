select lt.label_text, s.set_id
from diet.set s inner join diet.label l on s.setname_label_id=l.label_id
inner join diet.label_text lt on l.label_id=lt.label_id
where s.set_id = 652

select manufacturer_id from diet.food where manufacturer_id is not null

select *
from diet.label_text lt inner join diet."label" l on lt.label_id = l.label_id
where l.label_id = 29528


INSERT INTO diet.food_sets (food_id, set_id) VALUES (14792, 652);


INSERT INTO diet.set (set_id, setname_label_id, ontol_id, parentset_id, sort_order) VALUES (652, 30955, 34, NULL, 0);






select * from diet.nutrient

select _lt.label_text
from diet.label_text _lt inner join diet.label _l on _lt.label_id = _l.label_id
inner join diet.unit _u on _l.label_id = _u.unit_label_id










select * from diet.label_text

insert into diet.food(foodname_label_id, sd_id)values(50000, 180);
insert into diet.label(label_id, label_type_code)values(50000, 180);

delete from diet.label where label_id = 50000
delete from diet.food where foodname_label_id = 50000


select _lt.label_text, _fu.scale
from diet.label_text _lt inner join diet.label _l on _lt.label_id = _l.label_id
inner join diet.unit _u on _l.label_id = _u.unit_label_id
inner join diet.food_units _fu on _u.unit_id = _fu.unit_id

select _lt.label_text
from diet.label_text _lt inner join diet.label _l on _lt.label_id = _l.label_id
inner join diet.source _s on _l.label_id = _s.source_label_id
where _lt.lang_id = 2


select _fs.food_id from diet.food_source _fs where source_link_no like '%35052%'




CREATE TYPE diet.file_nutr as(
	NDB_No integer,
	Nutr_No integer,
	Nutr_Val double precision
);

CREATE OR REPLACE FUNCTION diet.nutrient_loader(nutrients diet.file_nutr[]) RETURNS int4 AS $printStrings$
DECLARE
	var_i integer;
	var_food_id integer;
BEGIN
	for var_i in 0..nutrients.count loop
		
		var_food_id := (select _fs.food_id from diet.food_source _fs where source_link_no like '%' || nutrients[i].NDB_No || '%');
		
		insert into diet.food_content(food_id, source_id, nutr_id, fc_quantity)
		values(var_food_id, 1, nutrients[i].Nutr_No, nutrients[i].Nutr_Val);
		
	end loop;
	
	return var_i; -- return number of nutrients inserted
	
END;
$printStrings$ LANGUAGE plpgsql;




CREATE OR REPLACE FUNCTION diet.food_loader(NDB_No integer, Shrt_Desc text, Long_Desc text, Refuse_percent integer) RETURNS int4 AS $printStrings$
DECLARE
var_i integer;
var_food_id integer;
var_label_id integer;
BEGIN

var_food_id := nextval('diet.food_food_id_seq');
var_label_id := nextval('diet.label_label_id_seq');
                
insert into diet.label (label_id, label_type_code) values (var_label_id, 3);	-- 3 is food name
insert into diet.label_text (label_id, lang_id, label_text, label_long_text) values (var_label_id, 2, Shrt_Desc, Long_Desc); -- 2 is english
insert into diet.food (food_id, foodname_label_id, sd_id) values (var_food_id, var_label_id, 36); -- sd_id -> security desc
                
insert into diet.food_source(food_id, source_id, content_unit_id, content_unit_q, refuse_percent, source_link_no)
values(var_food_id, 1, 15, 10, Refuse_percent, 'USDA:SR28:' || NDB_No || ' SOTE:null'); -- VERSION is from java code
           
return var_i; -- return number of foods inserted
                
END;
$printStrings$ LANGUAGE plpgsql;












