package be.intecbrussel.shoppinglist.configuration;

import be.intecbrussel.shoppinglist.dto.*;
import be.intecbrussel.shoppinglist.model.Role;
import be.intecbrussel.shoppinglist.service.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
public class ConfigViaService {

    @Bean
    @ConditionalOnProperty(name = "app.seed-data", havingValue = "true", matchIfMissing = true)
    CommandLineRunner dataLoader_commandLineRunner(
            AuthService authService,
            StorageTypeService storageTypeService,
            FoodService foodService,
            StoredFoodService storedFoodService
    ) {
        return args -> {

            // ── Users (each registration also creates 1 home) ─────────────────────────
            // user01 and user02 share a home name but each gets their own home row —
            // if you want a shared physical home, one user must register and the other
            // must be added via a separate "join home" flow (out of scope here).
            UserResponse user01 = authService.registerUser(
                    new RegisterRequest(0, "user01", "u1@g.c", Role.ADMIN,  "hashed01?", "home01"));
            UserResponse user02 = authService.registerUser(
                    new RegisterRequest(0, "user02", "u2@g.c", Role.NORMAL, "hashed02?", "home01-user02"));
            UserResponse user03 = authService.registerUser(
                    new RegisterRequest(0, "user03", "u3@g.c", Role.NORMAL, "hashed03?", "home02"));

            // Convenience: use user01's home as the primary home for stored foods
            long primaryHomeId = user01.homeId();

            // ── Storage types ─────────────────────────────────────────────────────────
            StorageTypeResponse kelder       = storageTypeService.saveStorageType(new StorageTypeRequest("Kelder",       null));
            StorageTypeResponse koelkast     = storageTypeService.saveStorageType(new StorageTypeRequest("Koelkast",     null));
            StorageTypeResponse voorraadkast = storageTypeService.saveStorageType(new StorageTypeRequest("Voorraadkast", null));

            // ── Foods ─────────────────────────────────────────────────────────────────
            FoodOriginalResponse bloemkool01 = foodService.saveFood(
                    new FoodOriginalRequest("bloemkool", "bloemkool01", Util.days2date(4), 750));

            FoodOriginalResponse bloemkool02 = foodService.saveFood(
                    new FoodOriginalRequest("bloemkool", "bloemkool02", Util.days2date(4), 750));
            foodService.openPackage(bloemkool02.id(), new OpenPackageRequest(Util.days2date(2), 0));
            foodService.consume(bloemkool02.id(), new ConsumeRequest(750)); // empties → remaining = -1

            FoodOriginalResponse miso01        = foodService.saveFood(new FoodOriginalRequest("miso licht",   null, Util.days2date(90),  300));
            FoodOriginalResponse melkSoja01    = foodService.saveFood(new FoodOriginalRequest("melk soja",    null, Util.days2date(90),  1000));
            FoodOriginalResponse melkAmandel01 = foodService.saveFood(new FoodOriginalRequest("melk amandel", null, Util.days2date(120), 1000));
            FoodOriginalResponse melkKoe01     = foodService.saveFood(new FoodOriginalRequest("melk koe",     null, Util.days2date(90),  1000));

            // ── Stored foods (all linked to user01's home) ────────────────────────────
            storedFoodService.saveStoredFood(new StoredFoodRequest(primaryHomeId, bloemkool01.id(),   kelder.id(),       0));
            storedFoodService.saveStoredFood(new StoredFoodRequest(primaryHomeId, bloemkool02.id(),   kelder.id(),       1));
            storedFoodService.saveStoredFood(new StoredFoodRequest(primaryHomeId, miso01.id(),        koelkast.id(),     1));
            storedFoodService.saveStoredFood(new StoredFoodRequest(primaryHomeId, melkAmandel01.id(), voorraadkast.id(), 12));
            storedFoodService.saveStoredFood(new StoredFoodRequest(primaryHomeId, melkKoe01.id(),     voorraadkast.id(), 9));
            storedFoodService.saveStoredFood(new StoredFoodRequest(primaryHomeId, melkSoja01.id(),    voorraadkast.id(), 20));

            // ── Verification output ───────────────────────────────────────────────────
            System.out.println("*** all active foods:");
            foodService.findAllFoods().forEach(System.out::println);

            System.out.println("*** all storedFoods:");
            storedFoodService.findAllStoredFoods().forEach(System.out::println);

            System.out.println("*** end of config file ***");
        };
    }
}