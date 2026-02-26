package be.intecbrussel.shoppinglist.service;

import be.intecbrussel.shoppinglist.model.FoodAtHome;

import java.util.List;

public interface FoodAtHomeService {
    FoodAtHome saveFoodAtHome(FoodAtHome foodAtHome);
    List<FoodAtHome> findAllFoodAtHomes();
    FoodAtHome findFoodAtHome(long foodAtHomeId);
    FoodAtHome updateFoodAtHome(FoodAtHome foodAtHome, long foodAtHomeId);
    void deleteFoodAtHome(long foodAtHomeId);
}
