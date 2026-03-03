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
            HomeService homeService,
            AuthService authService,
            StorageTypeService storageTypeService,
            FoodService foodService,
            StoredFoodService storedFoodService
    ) {
        return args -> {

            // ── Homes ─────────────────────────────────────────────────────────────────
            HomeResponse home01 = homeService.saveHome(new HomeRequest("home01"));
            HomeResponse home02 = homeService.saveHome(new HomeRequest("home02"));

            // ── Users ─────────────────────────────────────────────────────────────────
            authService.registerUser(new RegisterRequest(0, "user01", "u1@g.c", Role.ADMIN,  "hashed01?"));
            authService.registerUser(new RegisterRequest(0, "user02", "u2@g.c", Role.NORMAL, "hashed02?"));
            authService.registerUser(new RegisterRequest(0, "user03", "u3@g.c", Role.NORMAL, "hashed03?"));

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

            // ── Stored foods ──────────────────────────────────────────────────────────
            storedFoodService.saveStoredFood(new StoredFoodRequest(home01.id(), bloemkool01.id(),   kelder.id(),       0));
            storedFoodService.saveStoredFood(new StoredFoodRequest(home01.id(), bloemkool02.id(),   kelder.id(),       1));
            storedFoodService.saveStoredFood(new StoredFoodRequest(home01.id(), miso01.id(),        koelkast.id(),     1));
            storedFoodService.saveStoredFood(new StoredFoodRequest(home01.id(), melkAmandel01.id(), voorraadkast.id(), 12));
            storedFoodService.saveStoredFood(new StoredFoodRequest(home01.id(), melkKoe01.id(),     voorraadkast.id(), 9));
            storedFoodService.saveStoredFood(new StoredFoodRequest(home01.id(), melkSoja01.id(),    voorraadkast.id(), 20));

            // ── Verification output ───────────────────────────────────────────────────
            System.out.println("*** all active foods:");
            foodService.findAllFoods().forEach(System.out::println);

            System.out.println("*** all storedFoods:");
            storedFoodService.findAllStoredFoods().forEach(System.out::println);

            System.out.println("*** end of config file ***");
        };
    }
}
