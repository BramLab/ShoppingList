select * from food;


SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA='shoppinglist' AND TABLE_NAME='food'
order by COLUMN_NAME asc;

# spring:             dtype (subclass name, e.g. FoodUntouched)
# audit:              created_at, updated_at
# food:               id, name, typical_unit, remarks
# fooduntouched:      best_before_end, quantity_per_package, how_many, food_storage_id
# FoodTouched:        use_by, amount_left, storage_location
# FoodIngredient:     quantity



