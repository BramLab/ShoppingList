package be.intecbrussel.shoppinglist.configuration;

import be.intecbrussel.shoppinglist.model.*;
import be.intecbrussel.shoppinglist.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import java.util.List;

@Configuration
@EnableJpaAuditing
public class Config {

    @Bean
    // Prevent during tests; inserted data interferes with test data; 2 versions:
    // 1) explicit: "seed data is on by default, but tests opt out":
    @ConditionalOnProperty(name = "app.seed-data", havingValue = "true", matchIfMissing = true)
    // @Profile("!test") // 2) shorter version.
    CommandLineRunner dataLoader_commandLineRunner(
              UserHomeRepository userHomeRepository
            , AppUserRepository appUserRepository
            , StorageTypeRepository storageTypeRepository
            , FoodRepository foodRepository
            , FoodOriginalRepository foodOriginalRepository // both this or foodRepository can save FoodOriginal.
            , StoredFoodRepository storedFoodRepository) {
        return args -> {

            Home home01 = new Home(0, "home01");
            userHomeRepository.save(home01);
            Home home02 = new Home(0, "home02");
            userHomeRepository.save(home02);

            AppUser appUser01 = new AppUser(0, "user01", "u1@g.c", UserRole.ADMIN, "hashed01?", home01);
            appUserRepository.save(appUser01);
            AppUser appUser02 = new AppUser(0, "user02", "u2@g.c", UserRole.NORMAL, "hashed02?", home01);
            appUserRepository.save(appUser02);
            AppUser appUser03 = new AppUser(0, "user03", "u3@g.c", UserRole.NORMAL, "hashed03?", home02);
            appUserRepository.save(appUser03);

            StorageType kelder = new StorageType(0, "Kelder", null);
            storageTypeRepository.save(kelder);
            StorageType koelkast = new StorageType(0, "Koelkast", null);
            storageTypeRepository.save(koelkast);
            StorageType voorraadkast = new StorageType(0, "voorraadkast", null);
            storageTypeRepository.save(voorraadkast);

            FoodOriginal bloemkool01 = FoodOriginal.foodOriginalBuilder()
                    .name("bloemkool")
                    .remarks("bloemkool01")
                    .bestBeforeEnd(Util.days2date(4))
                    .original_ml_g(750)
                    .build();
            foodRepository.save(bloemkool01);

            FoodOriginal bloemkool02 = FoodOriginal.foodOriginalBuilder()
                    .name("bloemkool")
                    .remarks("bloemkool02")
                    .bestBeforeEnd(Util.days2date(4))
                    .original_ml_g(750)
                    .build();
            foodRepository.save(bloemkool02);
            bloemkool02.setUseBy(Util.days2date(2));
            bloemkool02.setRemaining_ml_g(0d);
            foodRepository.save(bloemkool02);

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

            StoredFood storedFood01 = new StoredFood(0, home01, bloemkool01,kelder,0);
            storedFoodRepository.save(storedFood01);
            StoredFood storedFood02 = new StoredFood(0, home01, bloemkool02,kelder,1);
            storedFoodRepository.save(storedFood02);
            StoredFood storedFood03 = new StoredFood(0, home01, miso01,koelkast,1);
            storedFoodRepository.save(storedFood03);
            StoredFood storedFood04 = new StoredFood(0, home01, melkAmandel01,voorraadkast,12);
            storedFoodRepository.save(storedFood04);
            StoredFood storedFood05 = new StoredFood(0, home01, melkKoe01,voorraadkast,9);
            storedFoodRepository.save(storedFood05);
            StoredFood storedFood06 = new StoredFood(0, home01, melkSoja01,voorraadkast,20);
            storedFoodRepository.save(storedFood06);

//            System.out.println("*** test getters/setters:");
//            //Food foodsTestBloemkool02 = foodRepository.getOne(bloemkool02.getId());
//            Food foodsTestBloemkool02 = foodRepository.getReferenceById(bloemkool02.getId());
//            System.out.println("bloemkool02.getOriginal_ml_g(): " + bloemkool02.getOriginal_ml_g());
//            System.out.println("bloemkool02.getBestBeforeEnd(): " + bloemkool02.getBestBeforeEnd());
//            System.out.println("bloemkool02.getRemaining_ml_g(): " + bloemkool02.getRemaining_ml_g());
//            System.out.println("bloemkool02.getUseBy(): " + bloemkool02.getUseBy());

            System.out.println("*** all active foods:");
            List<Food> foods01 = foodRepository.findAll();
            for(Food food : foods01) {
                System.out.println(food);
            }

            System.out.println("*** end of config file ***");
        };
    }

}
