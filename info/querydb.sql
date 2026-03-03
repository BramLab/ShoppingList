select * from user;
select * from home;
select * from storage_type;
select * from stored_food;
select * from food;
select * from food_original;

select #*,
      u.id, u.username
    , h.id, h.name
    , sf.id
    , st.id, st.name
    , f.id, f.name
    , fo.best_before_end, fo.original_ml_g, fo.use_by ,fo.remaining_ml_g
from home h
left join user u on u.home_id = h.id
left join stored_food sf on sf.home_id = h.id
left join storage_type st on sf.storage_type_id = st.id
left join food f on sf.food_id = f.id
left join food_original fo on f.id = fo.food_id
where u.id = 1
order by u.id, h.id, sf.id, st.id
;



select
    #Audit:
    created_at, updated_at,
    #SoftDelete:
    deleted_at,
    #Class type:
    dtype,
    #Food:
    id,name,remarks,
    #FoodOriginal:
    best_before_end, original_ml_g, use_by, remaining_ml_g,
    #FoodUntouched:#FoodTouched:# best_before_end, ml_g_left  use_by, ml_g_left, #food_ingredients,
    #FoodIngredient:
    quantity, ingredient_id,
    #FoodSubstitute:
    food_original, food_alternative, recipe_id, aspect
from food;

# All column names from "food" table:
SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA='shoppinglist' AND TABLE_NAME='food'
order by COLUMN_NAME asc;

# #Drop tables:
# SELECT table_name FROM information_schema.tables WHERE table_schema = 'shopppinglist';
# drop table if exists food_food_ingredients;
# drop table if exists food_in_house;
# drop table if exists food_in_storage;
# drop table if exists food_storage;
# drop table if exists food_substitute;
# drop table if exists stored_food;
# drop table if exists storage;
# drop table if exists food;
# drop table if exists recipe;
# SELECT table_name FROM information_schema.tables WHERE table_schema = 'shopppinglist';
