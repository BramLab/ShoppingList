package be.intecbrussel.shoppinglist.dto;

import be.intecbrussel.shoppinglist.model.Home;

public class HomeMapper {

    public static HomeResponse mapToHomeResponse(Home home) {
        return new HomeResponse(
                home.getId(),
                home.getName()
        );
    }

    public static Home mapToHome(HomeRequest request) {
        Home home = new Home();
        home.setName(request.name());
        return home;
    }
}
