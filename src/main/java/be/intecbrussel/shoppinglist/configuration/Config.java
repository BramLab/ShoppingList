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

            FoodStorage kelder = new FoodStorage(0, "Kelder", null);
            foodStorageRepository.save(kelder);
            FoodStorage koelkast = new FoodStorage(0, "Koelkast", null);
            foodStorageRepository.save(koelkast);

            Food bloemkool01 = new FoodUntouched(0, "bloemkool", QuantityUnit.kg, "(vorige week, op)"
                    , Helper.days2date(-3), 800, 0, kelder);
            foodRepository.save(bloemkool01);// Inferred type 'S' for type parameter 'S' is not within its bound; should extend
            Food bloemkool02 = FoodUntouched.foodUntouchedBuilder()
                    .name("bloemkool")
                    .typicalUnit(QuantityUnit.kg)
                    //.remarks(null)
                    .bestBeforeEnd(Helper.days2date(3))
                    .quantityPerPackage(750)
                    .howMany(1)
                    .foodStorage(kelder)
                    .build();
            foodRepository.save(bloemkool02);
            Food miso01 = FoodUntouched.foodUntouchedBuilder()
                    .name("miso licht")
                    .typicalUnit(QuantityUnit.kg)
                    //.remarks(null)
                    .bestBeforeEnd(Helper.days2date(90))
                    .quantityPerPackage(300)
                    .howMany(1)
                    .foodStorage(koelkast)
                    .build();
            foodRepository.save(miso01);



                    //days2date(3)),1.2, StorageLocation.kelder);

//            Food fpMiso = new Food(0,"miso");
//            Food fpSesam = new Food(0,"sesamzaad, ongeroosterd");
//            foodproductRepository.save(fpSpitskool);
//            foodproductRepository.save(fpMiso);
//            foodproductRepository.save(fpSesam);
//
//            FoodUntouched isSpitskool = new FoodUntouched(fpSpitskool, LocalDate.now().plusDays(10),2.3);
//            FoodUntouched isMiso = new FoodUntouched(fpMiso, LocalDate.now().plusDays(90),0.35);
//            FoodUntouched isSesam = new FoodUntouched(fpSesam, LocalDate.now().plusDays(360),0.2);
//            inStoreRepository.save(isSpitskool);
//            inStoreRepository.save(isMiso);
//            inStoreRepository.save(isSesam);

        };
    }

}
