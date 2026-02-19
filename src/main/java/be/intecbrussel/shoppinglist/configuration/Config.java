package be.intecbrussel.shoppinglist.configuration;


import be.intecbrussel.shoppinglist.model.*;
import be.intecbrussel.shoppinglist.repository.FoodRepository;
import be.intecbrussel.shoppinglist.repository.FoodStorageRepository;
//import be.intecbrussel.shoppinglist.repository.FoodUntouchedRepository;

import jakarta.websocket.Session;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Configuration
@EnableJpaAuditing
public class Config {

    @Bean
    CommandLineRunner dataLoader_commandLineRunner(
            FoodRepository foodRepository,
            FoodStorageRepository foodStorageRepository
//            ,FoodUntouchedRepository foodUntouchedRepository
    ) {
        return args -> {

            Storage kelder = new Storage(0, "Kelder", null);
            foodStorageRepository.save(kelder);
            Storage koelkast = new Storage(0, "Koelkast", null);
            foodStorageRepository.save(koelkast);

            Food bloemkool01 = new FoodUntouched(0, "bloemkool", "(vorige week eigenlijk al op)"
                    , Helper.days2date(-3), 800, kelder);
            foodRepository.save(bloemkool01);

            Food bloemkool02 = FoodUntouched.foodUntouchedBuilder()
                    .name("bloemkool")
                    //.remarks(null)
                    .bestBeforeEnd(Helper.days2date(3))
                    .ml_g_inPackage(750)
                    .storage(kelder)
                    .build();
            foodRepository.save(bloemkool02);

            Food miso01 = FoodUntouched.foodUntouchedBuilder()
                    .name("miso licht")
                    //.remarks(null)
                    .bestBeforeEnd(Helper.days2date(90))
                    .ml_g_inPackage(300)
                    .storage(koelkast)
                    .build();
            foodRepository.save(miso01);

            // Now open existing fresh miso package
            // => copy from FoodUntouched to FoodTouched,
            // with less content and shorter useBy date,
            // and disable/delete FoodUntouched version of it.
            // Then soft delete original.
            List<FoodIngredient> foodIngredients01  = new ArrayList<FoodIngredient>();
            FoodIngredient foodIngredient01 = new FoodIngredient(1.0, miso01);
            foodIngredients01.add(foodIngredient01);
            foodRepository.save(foodIngredient01);

            Food openedMiso = FoodTouched.foodTouchedBuilder()
                    .name(miso01.getName())
                    .useBy(Helper.days2date(30))
                    .ml_g_Left( ((FoodUntouched)miso01).getMl_g_inPackage() - 30)
                    .foodIngredients(foodIngredients01)
                    .build();
            foodRepository.save(openedMiso);
            foodRepository.deleteById(miso01.getId());

            List<Food> foods01 = foodRepository.findAll();
            for(Food food : foods01) {
                System.out.println(food);
            }

            //Optional<Food> f = foodRepository.findById(4L);
            //System.out.println(f);

            Food f = foodRepository.findById(5L).orElse(null);
            if (f != null) {
                System.out.println(f);
            }

            //other: Session.createSQLQuery()

        };
    }

}
