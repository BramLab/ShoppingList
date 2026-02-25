//package be.intecbrussel.shoppinglist.model;
//
//import be.intecbrussel.shoppinglist.repository.FoodOriginalRepository;
//import be.intecbrussel.shoppinglist.repository.FoodRepository;
//import be.intecbrussel.shoppinglist.repository.StorageRepository;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.context.annotation.Bean;
//
//import static org.junit.jupiter.api.Assertions.*;
//
////@DataJpaTest
//class FoodOriginalTest {
//
//    FoodRepository foodRepository; // work together -> test cooperation.
//    StorageRepository storageRepository;
//    FoodOriginalRepository foodOriginalRepository;
//
//    @Bean
//    @BeforeEach
//    void setUp() {
//
//        Storage kelder = new Storage(0, "Kelder", null);
//        storageRepository.save(kelder);
//        Storage koelkast = new Storage(0, "Koelkast", null);
//        storageRepository.save(koelkast);
//
//        FoodOriginal bloemkool01 = FoodOriginal.foodOriginalBuilder()
//                .name("bloemkool")
//                .remarks("bloemkool01")
//                .bestBeforeEnd(Helper.days2date(4))
//                .original_ml_g(750)
//                .build();
//        foodOriginalRepository.save(bloemkool01);
//
//        FoodOriginal bloemkool02 = FoodOriginal.foodOriginalBuilder()
//                .name("bloemkool")
//                .remarks("bloemkool02")
//                .bestBeforeEnd(Helper.days2date(4))
//                .original_ml_g(750)
//                .build();
//        foodOriginalRepository.save(bloemkool02);
//        bloemkool02.setUseBy(Helper.days2date(2));
//        bloemkool02.setRemaining_ml_g(0d);
//        foodOriginalRepository.save(bloemkool02);
//
//    }
//
//    @AfterEach
//    void tearDown() {
//    }
//
//    @Test
//    void testGetId() {
//        // Arrange Act Assert
//        //Food foodsTestBloemkool02 = foodRepository.findById(1);
//
//        Food f01 = foodRepository.findById(1L).orElse(null);
//        if (f01 != null) {
//            System.out.println(f01);
//        }
//
//        System.out.println("bloemkool02.getOriginal_ml_g(): " + f01.getId());
//
//    }
//
//    @Test
//    void testGetName() {
//    }
//
//    @Test
//    void testGetRemarks() {
//    }
//
//    @Test
//    void testSetId() {
//    }
//
//    @Test
//    void testSetName() {
//    }
//
//    @Test
//    void testSetRemarks() {
//    }
//
//    @Test
//    void testEquals() {
//    }
//
//    @Test
//    void testCanEqual() {
//    }
//
//    @Test
//    void testHashCode() {
//    }
//
//    @Test
//    void testToString() {
//    }
//
//    @Test
//    void testGetCreatedAt() {
//    }
//
//    @Test
//    void testGetUpdatedAt() {
//    }
//
//    @Test
//    void testSetCreatedAt() {
//    }
//
//    @Test
//    void testSetUpdatedAt() {
//    }
//
//    @Test
//    void testEquals1() {
//    }
//
//    @Test
//    void testCanEqual1() {
//    }
//
//    @Test
//    void testHashCode1() {
//    }
//
//    @Test
//    void testToString1() {
//    }
//
//    @Test
//    void getUseBy() {
//    }
//
//    @Test
//    void setOriginal_ml_g() {
//    }
//
//    @Test
//    void setRemaining_ml_g() {
//    }
//
//    @Test
//    void getBestBeforeEnd() {
//    }
//
//    @Test
//    void getOriginal_ml_g() {
//    }
//
//    @Test
//    void getRemaining_ml_g() {
//    }
//
//    @Test
//    void setBestBeforeEnd() {
//    }
//
//    @Test
//    void setUseBy() {
//    }
//
//    @Test
//    void testEquals2() {
//    }
//
//    @Test
//    void testCanEqual2() {
//    }
//
//    @Test
//    void testHashCode2() {
//    }
//
//    @Test
//    void testToString2() {
//    }
//
//    @Test
//    void foodOriginalBuilder() {
//    }
//}