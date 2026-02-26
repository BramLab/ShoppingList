package be.intecbrussel.shoppinglist.service;

import be.intecbrussel.shoppinglist.model.UserHome;

import java.util.List;

public interface FoodAtHomeService {
    UserHome saveFoodAtHome(UserHome userHome);
    List<UserHome> findAllFoodAtHomes();
    UserHome findFoodAtHome(long foodAtHomeId);
    UserHome updateFoodAtHome(UserHome userHome, long foodAtHomeId);
    void deleteFoodAtHome(long foodAtHomeId);
}
