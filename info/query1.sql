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









select * from diet.food_units

select _lt.label_text, _lt.label_id
from diet.unit _u inner join diet.label _l on _u.unit_label_id = _l.label_id
inner join diet.label_text _lt on _l.label_id = _lt.label_id
order by _lt.label_id

select _f.food_id, _fs.source_link_no
from diet.food _f inner join diet.food_source _fs on _f.food_id = _fs.food_id
order by _fs.source_link_no asc

select _lt.label_text, _lt.label_long_text
from diet.food _f inner join diet.label _l on _f.foodname_label_id = _l.label_id
inner join diet.label_text _lt on _l.label_id = _lt.label_id
where _f.food_id = 15131

select _fs.food_id from diet.food_source _fs where source_link_no like '%35052%'

select _f.foodname_label_id from diet.food _f where food_id = 15770


select * from minta.label_text order by label_id desc

select * from minta.food_source order by source_link_no desc









