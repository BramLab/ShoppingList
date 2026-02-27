package be.intecbrussel.shoppinglist.service;

import be.intecbrussel.shoppinglist.model.Home;

import java.util.List;

public interface HomeService {
    Home saveHome(Home home);
    List<Home> findAllHomes();
    Home findHome(long foodAtHomeId);
    Home updateHome(Home home, long foodAtHomeId);
    void deleteHome(long foodAtHomeId);
}
