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
    #FoodUntouched:
    best_before_end,ml_g_in_package,
    #FoodTouched:
    use_by, ml_g_left,food_ingredients,
    #FoodIngredient:
    quantity, food_id,
    #FoodSubstitute:
    food_original, food_alternative, recipe_id, aspect
from food;



SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA='shoppinglist' AND TABLE_NAME='food'
order by COLUMN_NAME asc;

# spring:             dtype (subclass name, e.g. FoodUntouched)
# audit:              created_at, updated_at
# food:               id, name, typical_unit, remarks
# fooduntouched:      best_before_end, quantity_per_package, how_many, food_storage_id
# FoodTouched:        use_by, amount_left, storage_location
# FoodIngredient:     quantity



