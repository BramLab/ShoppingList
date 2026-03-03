package be.intecbrussel.shoppinglist.service;

import be.intecbrussel.shoppinglist.dto.HomeRequest;
import be.intecbrussel.shoppinglist.dto.HomeResponse;

import java.util.List;

public interface HomeService {
    HomeResponse saveHome(HomeRequest request);
    List<HomeResponse> findAllHomes();
    HomeResponse findHomeById(long id);
    HomeResponse updateHome(long id, HomeRequest request);
    void deleteHome(long id);
}
