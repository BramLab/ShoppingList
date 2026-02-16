package be.intecbrussel.shoppinglist.configuration;


import be.intecbrussel.shoppinglist.model.*;
import be.intecbrussel.shoppinglist.repository.FoodRepository;
import be.intecbrussel.shoppinglist.repository.FoodStorageRepository;
//import be.intecbrussel.shoppinglist.repository.FoodUntouchedRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

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
                    , Helper.days2date(-3), 800, 0, kelder);
            foodRepository.save(bloemkool01);// Inferred type 'S' for type parameter 'S' is not within its bound; should extend
            Food bloemkool02 = FoodUntouched.foodUntouchedBuilder()
                    .name("bloemkool")
                    //.remarks(null)
                    .bestBeforeEnd(Helper.days2date(3))
                    .quantityPerPackage(750)
                    .howMany(1)
                    .storage(kelder)
                    .build();
            foodRepository.save(bloemkool02);
            Food miso01 = FoodUntouched.foodUntouchedBuilder()
                    .name("miso licht")
                    //.remarks(null)
                    .bestBeforeEnd(Helper.days2date(90))
                    .quantityPerPackage(300)
                    .howMany(1)
                    .storage(koelkast)
                    .build();
            foodRepository.save(miso01);

        };
    }

}
