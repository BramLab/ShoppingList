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

// @SpringBootApplication annotation. This annotation represents:
// @Configuration, @EnableAutoConfiguration and @ComponentScan

@Configuration
@EnableJpaAuditing
public class Config {

    @Bean
    CommandLineRunner dataLoader_commandLineRunner(
              UserHome userHome
            , FoodRepository foodRepository
            , StorageRepository storageRepository
            , FoodOriginalRepository foodOriginalRepository
    ) {
        return args -> {

            StorageType kelder = new StorageType(0, "Kelder", null);
            storageRepository.save(kelder);
            StorageType koelkast = new StorageType(0, "Koelkast", null);
            storageRepository.save(koelkast);

            FoodOriginal bloemkool01 = FoodOriginal.foodOriginalBuilder()
                    .name("bloemkool")
                    .remarks("bloemkool01")
                    .bestBeforeEnd(Util.days2date(4))
                    .original_ml_g(750)
                    .build();
            foodOriginalRepository.save(bloemkool01);



            FoodOriginal bloemkool02 = FoodOriginal.foodOriginalBuilder()
                    .name("bloemkool")
                    .remarks("bloemkool02")
                    .bestBeforeEnd(Util.days2date(4))
                    .original_ml_g(750)
                    .build();
            foodOriginalRepository.save(bloemkool02);
            bloemkool02.setUseBy(Util.days2date(2));
            bloemkool02.setRemaining_ml_g(0d);
            foodOriginalRepository.save(bloemkool02);

            FoodOriginal miso01 = FoodOriginal.foodOriginalBuilder()
                    .name("miso licht")
                    .bestBeforeEnd(Util.days2date(90))
                    .original_ml_g(300)
                    .build();
            foodRepository.save(miso01);

            FoodOriginal melkSoja01 = FoodOriginal.foodOriginalBuilder()
                    .name("melk soja")
                    .bestBeforeEnd(Util.days2date(90))
                    .original_ml_g(1000)
                    .build();
            foodRepository.save(melkSoja01);

            FoodOriginal melkAmandel01 = FoodOriginal.foodOriginalBuilder()
                    .name("melk amandel")
                    .bestBeforeEnd(Util.days2date(120))
                    .original_ml_g(1000)
                    .build();
            foodRepository.save(melkAmandel01);

            FoodOriginal melkKoe01 = FoodOriginal.foodOriginalBuilder()
                    .name("melk koe")
                    .bestBeforeEnd(Util.days2date(90))
                    .original_ml_g(1000)
                    .build();
            foodRepository.save(melkKoe01);

            // Now open existing fresh miso package
            // => copy from FoodUntouched to FoodTouched,
            // with less content and shorter useBy date,
            // and disable/delete FoodUntouched version of it.
            // Then (soft) delete original.
//            FoodIngredient foodIngredient01 = new FoodIngredient(0, miso01);
//            ingredientRepository.save(foodIngredient01);
//            List<FoodIngredient> foodIngredients01  = new ArrayList<FoodIngredient>();
//            foodIngredients01.add(foodIngredient01);
            //
//            Food openedMiso = FoodTouched.foodOriginalBuilder()
//                    .name(miso01.getName())
//                    .useBy(Helper.days2date(30))
//                    .ml_g_Left( ((FoodUntouched)miso01).getMl_g_inPackage() - 30)
//                    .foodIngredients(foodIngredients01)
//                    .build();
//            foodRepository.save(openedMiso);
//            foodRepository.deleteById(miso01.getId());




            System.out.println("*** test getters/setters:");
            Food foodsTestBloemkool02 = foodRepository.getOne(bloemkool02.getId());
            System.out.println("bloemkool02.getOriginal_ml_g(): " + bloemkool02.getOriginal_ml_g());
            System.out.println("bloemkool02.getBestBeforeEnd(): " + bloemkool02.getBestBeforeEnd());
            System.out.println("bloemkool02.getRemaining_ml_g(): " + bloemkool02.getRemaining_ml_g());
            System.out.println("bloemkool02.getUseBy(): " + bloemkool02.getUseBy());


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
