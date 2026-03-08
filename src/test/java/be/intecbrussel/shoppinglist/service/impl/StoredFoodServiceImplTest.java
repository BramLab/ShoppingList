package be.intecbrussel.shoppinglist.service.impl;

import be.intecbrussel.shoppinglist.dto.ConsumeRequest;
import be.intecbrussel.shoppinglist.dto.ConsumeResult;
import be.intecbrussel.shoppinglist.model.*;
import be.intecbrussel.shoppinglist.repository.FoodOriginalRepository;
import be.intecbrussel.shoppinglist.repository.StorageTypeRepository;
import be.intecbrussel.shoppinglist.repository.StoredFoodRepository;
import be.intecbrussel.shoppinglist.service.StoredFoodServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StoredFoodServiceImplTest {

    @Mock StoredFoodRepository   storedFoodRepo;
    @Mock FoodOriginalRepository foodOriginalRepo;
    @Mock StorageTypeRepository  storageTypeRepo;

    @InjectMocks
    StoredFoodServiceImpl service;

    // ── Shared fixtures ────────────────────────────────────────────────────────

    private Home         home;
    private StorageType  fridge;
    private Food         milkBase;  // plain Food (no FoodOriginal yet)
    private FoodOriginal milkFo;    // FoodOriginal variant

    @BeforeEach
    void setUp() {
        home     = new Home(1L, "Test home");
        fridge   = new StorageType(1L, "Fridge", null);
        milkBase = new Food(10L, "Milk", "Fresh whole milk");
        milkFo   = new FoodOriginal(10L, "Milk", "Fresh whole milk",
                LocalDate.of(2026, 4, 1), 1000d, null, 1000d);
    }

    /** Make foodOriginalRepo.save() return its argument with an assigned id. */
    private void stubFoodOriginalSave(long idToAssign) {
        when(foodOriginalRepo.save(any())).thenAnswer(inv -> {
            FoodOriginal fo = inv.getArgument(0);
            fo.setId(idToAssign);
            return fo;
        });
    }

    // ── Happy paths ────────────────────────────────────────────────────────────

    @Test
    void consume_fromPackOf12_decrementsQtyAndCreatesOpenedStoredFood() {
        StoredFood pack = new StoredFood(5L, home, milkBase, fridge, 12);
        when(storedFoodRepo.findById(5L)).thenReturn(Optional.of(pack));
        when(storedFoodRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        stubFoodOriginalSave(99L);

        ConsumeRequest req = new ConsumeRequest();
        req.setRemainingMlG(1000d);
        req.setUseBy(LocalDate.of(2026, 3, 20));

        ArgumentCaptor<StoredFood> sfCaptor = ArgumentCaptor.forClass(StoredFood.class);
        ConsumeResult result = service.consume(5L, req);

        // Two StoredFood saves: decremented pack + new opened unit
        verify(storedFoodRepo, times(2)).save(sfCaptor.capture());
        assertThat(sfCaptor.getAllValues().get(0).getQuantity()).isEqualTo(11);

        assertThat(result.getSourceStoredFoodId()).isEqualTo(5L);
        assertThat(result.getSourceRemainingQuantity()).isEqualTo(11);
        assertThat(result.getOpenedRemainingMlG()).isEqualTo(1000d);
        assertThat(result.isOpenedUnitWasEmpty()).isFalse();
        assertThat(result.getOpenedStoredFoodId()).isNotNull();

        StoredFood openedSF = sfCaptor.getAllValues().get(1);
        assertThat(openedSF.getQuantity()).isEqualTo(1);
        assertThat(openedSF.getHome()).isEqualTo(home);
        assertThat(openedSF.getStorageType()).isEqualTo(fridge); // inherited
    }

    @Test
    void consume_lastUnitInPack_deletesSourceStoredFood() {
        StoredFood pack = new StoredFood(6L, home, milkBase, fridge, 1);
        when(storedFoodRepo.findById(6L)).thenReturn(Optional.of(pack));
        when(storedFoodRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        stubFoodOriginalSave(100L);

        ConsumeRequest req = new ConsumeRequest();
        req.setRemainingMlG(500d);
        req.setUseBy(LocalDate.of(2026, 3, 18));

        ConsumeResult result = service.consume(6L, req);

        verify(storedFoodRepo).delete(pack);
        verify(storedFoodRepo, never()).save(pack);

        assertThat(result.getSourceStoredFoodId()).isNull();
        assertThat(result.getSourceRemainingQuantity()).isEqualTo(0);
        assertThat(result.isOpenedUnitWasEmpty()).isFalse();
    }

    @Test
    void consume_emptyOnOpen_softDeletesOpenedFoodOriginal() {
        StoredFood pack = new StoredFood(7L, home, milkBase, fridge, 3);
        when(storedFoodRepo.findById(7L)).thenReturn(Optional.of(pack));
        when(storedFoodRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        stubFoodOriginalSave(101L);

        ConsumeRequest req = new ConsumeRequest();
        req.setRemainingMlG(0d); // empty on open

        ConsumeResult result = service.consume(7L, req);

        // FoodOriginal saved then immediately soft-deleted
        verify(foodOriginalRepo).save(any(FoodOriginal.class));
        verify(foodOriginalRepo).delete(any(FoodOriginal.class));

        // Only the decremented pack is saved; no opened-unit StoredFood row
        verify(storedFoodRepo, times(1)).save(any(StoredFood.class));

        assertThat(result.isOpenedUnitWasEmpty()).isTrue();
        assertThat(result.getOpenedStoredFoodId()).isNull();
        assertThat(result.getSourceRemainingQuantity()).isEqualTo(2);
    }

    @Test
    void consume_storageTypeOverride_usesProvidedStorageType() {
        StorageType pantry = new StorageType(2L, "Pantry", null);
        StoredFood  pack   = new StoredFood(8L, home, milkBase, fridge, 5);

        when(storedFoodRepo.findById(8L)).thenReturn(Optional.of(pack));
        when(storedFoodRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        stubFoodOriginalSave(102L);

        // storageTypeRepo.findById() is the JpaRepository version: findById(Long).
        // The primitive overload has been removed from StorageTypeRepository so there
        // is now exactly one findById method and the stub always matches the call.
        when(storageTypeRepo.findById(2L)).thenReturn(Optional.of(pantry));

        ConsumeRequest req = new ConsumeRequest();
        req.setRemainingMlG(750d);
        req.setStorageTypeId(2L);

        ArgumentCaptor<StoredFood> captor = ArgumentCaptor.forClass(StoredFood.class);
        service.consume(8L, req);

        verify(storedFoodRepo, times(2)).save(captor.capture());
        assertThat(captor.getAllValues().get(1).getStorageType()).isEqualTo(pantry);
    }

    @Test
    void consume_inheritsBestBeforeEndFromFoodOriginal() {
        StoredFood pack = new StoredFood(9L, home, milkFo, fridge, 4);
        when(storedFoodRepo.findById(9L)).thenReturn(Optional.of(pack));
        when(storedFoodRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ArgumentCaptor<FoodOriginal> foCaptor = ArgumentCaptor.forClass(FoodOriginal.class);
        when(foodOriginalRepo.save(foCaptor.capture())).thenAnswer(inv -> {
            FoodOriginal fo = inv.getArgument(0);
            fo.setId(103L);
            return fo;
        });

        ConsumeRequest req = new ConsumeRequest();
        req.setRemainingMlG(800d);

        service.consume(9L, req);

        FoodOriginal savedFo = foCaptor.getValue();
        assertThat(savedFo.getBestBeforeEnd()).isEqualTo(LocalDate.of(2026, 4, 1));
        assertThat(savedFo.getOriginal_ml_g()).isEqualTo(1000d);
        assertThat(savedFo.getRemaining_ml_g()).isEqualTo(800d);
    }

    // ── Error paths ────────────────────────────────────────────────────────────

    @Test
    void consume_unknownStoredFoodId_throwsEntityNotFoundException() {
        when(storedFoodRepo.findById(999L)).thenReturn(Optional.empty());

        ConsumeRequest req = new ConsumeRequest();
        req.setRemainingMlG(500d);

        assertThatThrownBy(() -> service.consume(999L, req))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void consume_unknownStorageTypeOverride_throwsEntityNotFoundException() {
        StoredFood pack = new StoredFood(10L, home, milkBase, fridge, 2);
        when(storedFoodRepo.findById(10L)).thenReturn(Optional.of(pack));
        when(storedFoodRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        stubFoodOriginalSave(104L);
        when(storageTypeRepo.findById(888L)).thenReturn(Optional.empty());

        ConsumeRequest req = new ConsumeRequest();
        req.setRemainingMlG(500d);
        req.setStorageTypeId(888L);

        assertThatThrownBy(() -> service.consume(10L, req))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("888");
    }
}