package be.intecbrussel.shoppinglist.configuration;


import be.intecbrussel.shoppinglist.model.*;
import be.intecbrussel.shoppinglist.repository.FoodRepository;
import be.intecbrussel.shoppinglist.repository.FoodStorageRepository;
//import be.intecbrussel.shoppinglist.repository.FoodUntouchedRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.List;

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

            Food bloemkool01 = new FoodUntouched(0, "bloemkool", "(vorige week, op)"
                    , Helper.days2date(-3), 800, kelder);
            foodRepository.save(bloemkool01);

            Food bloemkool02 = FoodUntouched.foodUntouchedBuilder()
                    .name("bloemkool")
                    //.remarks(null)
                    .bestBeforeEnd(Helper.days2date(3))
                    .quantityPerPackage(750)
                    .storage(kelder)
                    .build();
            foodRepository.save(bloemkool02);

            Food miso01 = FoodUntouched.foodUntouchedBuilder()
                    .name("miso licht")
                    //.remarks(null)
                    .bestBeforeEnd(Helper.days2date(90))
                    .quantityPerPackage(300)
                    .storage(koelkast)
                    .build();
            foodRepository.save(miso01);


            // Open existing fresh miso package (=copy from FoodUntouched to FoodTouched,
            // with less content and shorter useBy date,
            // and disable/delete FoodUntouched version of it.
            Food openedMiso = FoodTouched.foodTouchedBuilder()
                    .name(miso01.getName())
                    .useBy(Helper.days2date(30))
                    .amountLeft(miso01.get)
                    .storage(koelkast)
                    .build();
            foodRepository.save(openedMiso);


            //

            List<Food> foods01 = foodRepository.findAll();
            for(Food food : foods01) {
                System.out.println(food);
            }

        };
    }

}
