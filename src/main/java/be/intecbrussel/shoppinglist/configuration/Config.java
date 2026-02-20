package be.intecbrussel.shoppinglist.configuration;


import be.intecbrussel.shoppinglist.model.*;
import be.intecbrussel.shoppinglist.repository.FoodRepository;
import be.intecbrussel.shoppinglist.repository.FoodUntouchedRepository;
import be.intecbrussel.shoppinglist.repository.IngredientRepository;
import be.intecbrussel.shoppinglist.repository.StorageRepository;
//import be.intecbrussel.shoppinglist.repository.FoodUntouchedRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableJpaAuditing
public class Config {

    @Bean
    CommandLineRunner dataLoader_commandLineRunner(
            FoodRepository foodRepository,
            StorageRepository storageRepository,
            FoodUntouchedRepository foodUntouchedRepository,
            IngredientRepository ingredientRepository
    ) {
        return args -> {

            Storage kelder = new Storage(0, "Kelder", null);
            storageRepository.save(kelder);
            Storage koelkast = new Storage(0, "Koelkast", null);
            storageRepository.save(koelkast);

            FoodUntouched bloemkool01 = new FoodUntouched(0, "bloemkool", "(vorige week eigenlijk al op)"
                    , Helper.days2date(-3), 800);
            foodUntouchedRepository.save(bloemkool01);//Inferred type 'S' for type parameter 'S' is not within its bound; should extend 'be.intecbrussel.shoppinglist.model.FoodUntouched'

            Food bloemkool02 = FoodUntouched.foodUntouchedBuilder()
                    .name("bloemkool")
                    //.remarks(null)
                    .bestBeforeEnd(Helper.days2date(3))
                    .ml_g_inPackage(750)
                    //.storage(kelder)
                    .build();
            foodRepository.save(bloemkool02);

            Food miso01 = FoodUntouched.foodUntouchedBuilder()
                    .name("miso licht")
                    //.remarks(null)
                    .bestBeforeEnd(Helper.days2date(90))
                    .ml_g_inPackage(300)
                    //.storage(koelkast)
                    .build();
            foodRepository.save(miso01);

            Food melkSoja01 = FoodUntouched.foodUntouchedBuilder()
                    .name("melk soja")
                    //.remarks(null)
                    .bestBeforeEnd(Helper.days2date(90))
                    .ml_g_inPackage(10000)
                    //.storage(kelder)
                    .build();
            foodRepository.save(melkSoja01);

            Food melkAmandel01 = FoodUntouched.foodUntouchedBuilder()
                    .name("melk amandel")
                    //.remarks(null)
                    .bestBeforeEnd(Helper.days2date(120))
                    .ml_g_inPackage(10000)
                    //.storage(kelder)
                    .build();
            foodRepository.save(melkAmandel01);

            Food melkKoe01 = FoodUntouched.foodUntouchedBuilder()
                    .name("melk koe")
                    //.remarks(null)
                    .bestBeforeEnd(Helper.days2date(90))
                    .ml_g_inPackage(10000)
                    //.storage(kelder)
                    .build();
            foodRepository.save(melkKoe01);

            // Now open existing fresh miso package
            // => copy from FoodUntouched to FoodTouched,
            // with less content and shorter useBy date,
            // and disable/delete FoodUntouched version of it.
            // Then soft delete original.
            FoodIngredient foodIngredient01 = new FoodIngredient(0, miso01);
            ingredientRepository.save(foodIngredient01);




            System.out.println("*** all active foods BEFORE DELETE");
            List<Food> foods00 = foodRepository.findAll();
            for(Food food : foods00) {
                System.out.println(food);
            }

            List<FoodIngredient> foodIngredients01  = new ArrayList<FoodIngredient>();
            foodIngredients01.add(foodIngredient01);

            Food openedMiso = FoodTouched.foodTouchedBuilder()
                    .name(miso01.getName())
                    .useBy(Helper.days2date(30))
                    .ml_g_Left( ((FoodUntouched)miso01).getMl_g_inPackage() - 30)
                    .foodIngredients(foodIngredients01)
                    .build();
            foodRepository.save(openedMiso);
            foodRepository.deleteById(miso01.getId());


            System.out.println("*** all active foods:");
            List<Food> foods01 = foodRepository.findAll();
            for(Food food : foods01) {
                System.out.println(food);
            }

            System.out.println("*** food id 5:");
            Food f01 = foodRepository.findById(8L).orElse(null);
            if (f01 != null) {
                System.out.println(f01);
            }

            Food f02 = foodRepository.findByName("miso licht").orElse(null);
            if (f02 != null) {
                System.out.println(f02);
            }

            Storage s01 = storageRepository.findByName("kelder").orElse(null);
            if (s01 != null) {
                System.out.println(s01);
            }

            System.out.println("*** the end ***");
        };
    }

}
