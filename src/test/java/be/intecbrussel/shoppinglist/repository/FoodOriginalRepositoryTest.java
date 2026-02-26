package be.intecbrussel.shoppinglist.repository;

import be.intecbrussel.shoppinglist.model.Food;
import be.intecbrussel.shoppinglist.model.FoodOriginal;
import be.intecbrussel.shoppinglist.model.Util;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class FoodOriginalRepositoryTest {

    @Autowired
    FoodOriginalRepository foodOriginalRepository;

    @Autowired
    FoodRepository foodRepository;

    private FoodOriginal bloemkool;

    @BeforeEach
    void setUp() {
        bloemkool = FoodOriginal.foodOriginalBuilder()
                .name("bloemkool")
                .remarks("bloemkool01")
                .bestBeforeEnd(Util.days2date(4))
                .original_ml_g(750)
                .build();
        foodOriginalRepository.save(bloemkool);
    }

    @AfterEach
    void tearDown() {
        foodOriginalRepository.deleteAll();
    }

    @Test
    void testSaveAndFindById() {
        Food found = foodRepository.findById(bloemkool.getId()).orElse(null);
        assertNotNull(found);
        assertEquals("bloemkool", found.getName());
    }

    @Test
    void testRemainingMlGSetToNegativeWhenZero() {
        bloemkool.setRemaining_ml_g(0d);
        foodOriginalRepository.save(bloemkool);

        FoodOriginal saved = foodOriginalRepository.findById(bloemkool.getId()).orElseThrow();
        assertEquals(-1d, saved.getRemaining_ml_g());
    }

    @Test
    void testUseByFallsBackToBestBeforeEnd() {
        assertNull(bloemkool.getUseBy()); // confirm useBy was never set
        assertEquals(bloemkool.getBestBeforeEnd(), bloemkool.getUseBy());
    }

    @Test
    void testSetOriginal_ml_g_AlsoSetsRemaining() {
        bloemkool.setOriginal_ml_g(500d);
        assertEquals(500d, bloemkool.getRemaining_ml_g());
    }
}