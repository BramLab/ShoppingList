package be.intecbrussel.shoppinglist.model;

import java.time.LocalDate;

public class Util {

    static public LocalDate days2date(int days){
        return LocalDate.now().plusDays(days);
    }

}
