select * from food;

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
