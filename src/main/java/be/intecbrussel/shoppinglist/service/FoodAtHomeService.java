package be.intecbrussel.shoppinglist.service;

import be.intecbrussel.shoppinglist.model.Home;

import java.util.List;

public interface FoodAtHomeService {
    Home saveFoodAtHome(Home home);
    List<Home> findAllFoodAtHomes();
    Home findFoodAtHome(long foodAtHomeId);
    Home updateFoodAtHome(Home home, long foodAtHomeId);
    void deleteFoodAtHome(long foodAtHomeId);
}
