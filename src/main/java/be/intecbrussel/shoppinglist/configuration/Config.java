package be.intecbrussel.shoppinglist.configuration;


import be.intecbrussel.shoppinglist.model.*;
import be.intecbrussel.shoppinglist.repository.FoodOriginalRepository;
import be.intecbrussel.shoppinglist.repository.FoodRepository;
//import be.intecbrussel.shoppinglist.repository.FoodUntouchedRepository;
import be.intecbrussel.shoppinglist.repository.StorageRepository;
//import be.intecbrussel.shoppinglist.repository.FoodUntouchedRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.List;

//@Component, @Controller

@Configuration
@EnableJpaAuditing
public class Config {

    @Bean
    CommandLineRunner dataLoader_commandLineRunner(
            FoodRepository foodRepository,
            StorageRepository storageRepository
            , FoodOriginalRepository foodOriginalRepository
//            ,FoodUntouchedRepository foodUntouchedRepository,
//            IngredientRepository ingredientRepository
    ) {
        return args -> {

            Storage kelder = new Storage(0, "Kelder", null);
            storageRepository.save(kelder);
            Storage koelkast = new Storage(0, "Koelkast", null);
            storageRepository.save(koelkast);

            FoodOriginal bloemkool01 = FoodOriginal.foodOriginalBuilder()
                    .name("bloemkool")
                    .bestBeforeEnd(Helper.days2date(3))
                    .original_ml_g(750)
                    .build();
            foodOriginalRepository.save(bloemkool01);

            bloemkool01.setUseBy(Helper.days2date(1));
            bloemkool01.setRemaining_ml_g(200);
            foodOriginalRepository.save(bloemkool01);

//            FoodUntouched bloemkool01 = new FoodUntouched(0, "bloemkool", "(vorige week eigenlijk al op)"
//                    , Helper.days2date(-3), 800);
//            foodUntouchedRepository.save(bloemkool01);//Inferred type 'S' for type parameter 'S' is not within its bound; should extend 'be.intecbrussel.shoppinglist.model.FoodUntouched'
//
//            Food bloemkool02 = FoodUntouched.foodUntouchedBuilder()
//                    .name("bloemkool")
//                    .bestBeforeEnd(Helper.days2date(3))
//                    .ml_g_inPackage(750)
//                    .build();
//            foodRepository.save(bloemkool02);
//
//            Food miso01 = FoodUntouched.foodUntouchedBuilder()
//                    .name("miso licht")
//                    .bestBeforeEnd(Helper.days2date(90))
//                    .ml_g_inPackage(300)
//                    .build();
//            foodRepository.save(miso01);
//
//            Food melkSoja01 = FoodUntouched.foodUntouchedBuilder()
//                    .name("melk soja")
//                    .bestBeforeEnd(Helper.days2date(90))
//                    .ml_g_inPackage(10000)
//                    .build();
//            foodRepository.save(melkSoja01);
//
//            Food melkAmandel01 = FoodUntouched.foodUntouchedBuilder()
//                    .name("melk amandel")
//                    .bestBeforeEnd(Helper.days2date(120))
//                    .ml_g_inPackage(10000)
//                    .build();
//            foodRepository.save(melkAmandel01);
//
//            Food melkKoe01 = FoodUntouched.foodUntouchedBuilder()
//                    .name("melk koe")
//                    .bestBeforeEnd(Helper.days2date(90))
//                    .ml_g_inPackage(10000)
//                    .build();
//            foodRepository.save(melkKoe01);

            // Now open existing fresh miso package
            // => copy from FoodUntouched to FoodTouched,
            // with less content and shorter useBy date,
            // and disable/delete FoodUntouched version of it.
            // Then soft delete original.
//            FoodIngredient foodIngredient01 = new FoodIngredient(0, miso01);
//            ingredientRepository.save(foodIngredient01);
//            List<FoodIngredient> foodIngredients01  = new ArrayList<FoodIngredient>();
//            foodIngredients01.add(foodIngredient01);
//
//            Food openedMiso = FoodTouched.foodTouchedBuilder()
//                    .name(miso01.getName())
//                    .useBy(Helper.days2date(30))
//                    .ml_g_Left( ((FoodUntouched)miso01).getMl_g_inPackage() - 30)
//                    .foodIngredients(foodIngredients01)
//                    .build();
//            foodRepository.save(openedMiso);
//            foodRepository.deleteById(miso01.getId());


            System.out.println("*** all active foods:");
            List<Food> foods01 = foodRepository.findAll();
            for(Food food : foods01) {
                System.out.println(food);
            }

//            System.out.println("*** food id 5:");
//            Food f01 = foodRepository.findById(8L).orElse(null);
//            if (f01 != null) {
//                System.out.println(f01);
//            }
//
//            Food f02 = foodRepository.findByName("miso licht").orElse(null);
//            if (f02 != null) {
//                System.out.println(f02);
//            }
//
//            Storage s01 = storageRepository.findByName("kelder").orElse(null);
//            if (s01 != null) {
//                System.out.println(s01);
//            }

            System.out.println("*** the end ***");
        };
    }

}
