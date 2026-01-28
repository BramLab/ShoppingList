package be.intecbrussel.shoppinglist.configuration;


import be.intecbrussel.shoppinglist.model.Foodproduct;
import be.intecbrussel.shoppinglist.model.InStore;
import be.intecbrussel.shoppinglist.repository.FoodproductRepository;
import be.intecbrussel.shoppinglist.repository.InStoreRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.time.LocalDate;

@Configuration
@EnableJpaAuditing
public class Config {

    @Bean
    CommandLineRunner dataLoader_commandLineRunner(
            FoodproductRepository foodproductRepository,
            InStoreRepository inStoreRepository) {
        return args -> {

            Foodproduct fpSpitskool = new Foodproduct(0,"spitskool");
            Foodproduct fpMiso = new Foodproduct(0,"miso");
            Foodproduct fpSesam = new Foodproduct(0,"sesamzaad, ongeroosterd");
            foodproductRepository.save(fpSpitskool);
            foodproductRepository.save(fpMiso);
            foodproductRepository.save(fpSesam);

            InStore isSpitskool = new InStore(fpSpitskool, LocalDate.now().plusDays(10),2.3);
            InStore isMiso = new InStore(fpMiso, LocalDate.now().plusDays(90),0.35);
            InStore isSesam = new InStore(fpSesam, LocalDate.now().plusDays(360),0.2);
            inStoreRepository.save(isSpitskool);
            inStoreRepository.save(isMiso);
            inStoreRepository.save(isSesam);

        };
    }

}
