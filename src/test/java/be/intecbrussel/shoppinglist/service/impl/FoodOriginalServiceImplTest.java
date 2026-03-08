package be.intecbrussel.shoppinglist.service.impl;

import be.intecbrussel.shoppinglist.dto.AddToStorageRequest;
import be.intecbrussel.shoppinglist.dto.AddToStorageResult;
import be.intecbrussel.shoppinglist.model.*;
import be.intecbrussel.shoppinglist.repository.FoodOriginalRepository;
import be.intecbrussel.shoppinglist.repository.StorageTypeRepository;
import be.intecbrussel.shoppinglist.repository.StoredFoodRepository;
import be.intecbrussel.shoppinglist.repository.UserHomeRepository;
import be.intecbrussel.shoppinglist.service.FoodOriginalServiceImpl;
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
class FoodOriginalServiceImplTest {

    @Mock FoodOriginalRepository foodOriginalRepo;
    @Mock StoredFoodRepository   storedFoodRepo;
    @Mock UserHomeRepository     homeRepo;
    @Mock StorageTypeRepository  storageTypeRepo;

    @InjectMocks
    FoodOriginalServiceImpl service;

    private Home        home;
    private StorageType fridge;

    @BeforeEach
    void setUp() {
        home   = new Home(1L, "Test home");
        fridge = new StorageType(2L, "Fridge", null);
        // No stubs here – each test (or stubHappyPath) registers only what it needs,
        // so Mockito strict mode never sees an unused stub.
    }

    /**
     * Registers the three stubs that every happy-path test requires.
     * Error-path tests do NOT call this because they throw before reaching
     * foodOriginalRepo or storedFoodRepo, and one of them also overrides the
     * homeId / storageTypeId lookup – so those stubs would be unused there.
     */
    private void stubHappyPath() {
        when(homeRepo.findById(1L)).thenReturn(Optional.of(home));
        when(storageTypeRepo.findById(2L)).thenReturn(Optional.of(fridge));
        when(foodOriginalRepo.save(any())).thenAnswer(inv -> {
            FoodOriginal fo = inv.getArgument(0);
            fo.setId(10L);
            return fo;
        });
        when(storedFoodRepo.save(any())).thenAnswer(inv -> {
            StoredFood sf = inv.getArgument(0);
            sf.setId(20L);
            return sf;
        });
    }

    /** Minimal valid request; individual tests override what they need. */
    private AddToStorageRequest baseRequest() {
        AddToStorageRequest req = new AddToStorageRequest();
        req.setName("Milk");
        req.setOriginalMlG(1000d);
        req.setHomeId(1L);
        req.setStorageTypeId(2L);
        req.setQuantity(1);
        return req;
    }

    // ── Happy paths ────────────────────────────────────────────────────────────

    @Test
    void addToStorage_sealedUnit_remainingDefaultsToOriginal() {
        stubHappyPath();
        AddToStorageRequest req = baseRequest();
        // remainingMlG intentionally omitted → must default to originalMlG

        AddToStorageResult result = service.addToStorage(req);

        assertThat(result.getFoodOriginalId()).isEqualTo(10L);
        assertThat(result.getStoredFoodId()).isEqualTo(20L);
        assertThat(result.getRemainingMlG()).isEqualTo(1000d);
        assertThat(result.getQuantity()).isEqualTo(1);

        ArgumentCaptor<FoodOriginal> foCaptor = ArgumentCaptor.forClass(FoodOriginal.class);
        verify(foodOriginalRepo).save(foCaptor.capture());
        FoodOriginal saved = foCaptor.getValue();
        assertThat(saved.getOriginal_ml_g()).isEqualTo(1000d);
        assertThat(saved.getRemaining_ml_g()).isEqualTo(1000d);
        assertThat(saved.getName()).isEqualTo("Milk");
    }

    @Test
    void addToStorage_openedUnit_remainingOverrideIsRespected() {
        stubHappyPath();
        AddToStorageRequest req = baseRequest();
        req.setRemainingMlG(600d);
        req.setUseBy(LocalDate.of(2026, 3, 15));

        AddToStorageResult result = service.addToStorage(req);

        assertThat(result.getRemainingMlG()).isEqualTo(600d);

        ArgumentCaptor<FoodOriginal> foCaptor = ArgumentCaptor.forClass(FoodOriginal.class);
        verify(foodOriginalRepo).save(foCaptor.capture());
        FoodOriginal saved = foCaptor.getValue();
        assertThat(saved.getRemaining_ml_g()).isEqualTo(600d);
        assertThat(saved.getOriginal_ml_g()).isEqualTo(1000d);
        assertThat(saved.getUseBy()).isEqualTo(LocalDate.of(2026, 3, 15));
    }

    @Test
    void addToStorage_multipleUnits_quantityStoredCorrectly() {
        stubHappyPath();
        AddToStorageRequest req = baseRequest();
        req.setName("Yoghurt");
        req.setOriginalMlG(150d);
        req.setQuantity(6);

        AddToStorageResult result = service.addToStorage(req);

        assertThat(result.getQuantity()).isEqualTo(6);

        ArgumentCaptor<StoredFood> sfCaptor = ArgumentCaptor.forClass(StoredFood.class);
        verify(storedFoodRepo).save(sfCaptor.capture());
        assertThat(sfCaptor.getValue().getQuantity()).isEqualTo(6);
    }

    @Test
    void addToStorage_correctHomeAndStorageTypeLinked() {
        stubHappyPath();
        service.addToStorage(baseRequest());

        ArgumentCaptor<StoredFood> sfCaptor = ArgumentCaptor.forClass(StoredFood.class);
        verify(storedFoodRepo).save(sfCaptor.capture());
        assertThat(sfCaptor.getValue().getHome()).isEqualTo(home);
        assertThat(sfCaptor.getValue().getStorageType()).isEqualTo(fridge);
    }

    @Test
    void addToStorage_allOptionalFieldsPersisted() {
        stubHappyPath();
        AddToStorageRequest req = baseRequest();
        req.setRemarks("Whole milk 3.5%");
        req.setBestBeforeEnd(LocalDate.of(2026, 4, 1));
        req.setUseBy(LocalDate.of(2026, 3, 20));
        req.setRemainingMlG(800d);

        service.addToStorage(req);

        ArgumentCaptor<FoodOriginal> foCaptor = ArgumentCaptor.forClass(FoodOriginal.class);
        verify(foodOriginalRepo).save(foCaptor.capture());
        FoodOriginal saved = foCaptor.getValue();
        assertThat(saved.getRemarks()).isEqualTo("Whole milk 3.5%");
        assertThat(saved.getBestBeforeEnd()).isEqualTo(LocalDate.of(2026, 4, 1));
        assertThat(saved.getUseBy()).isEqualTo(LocalDate.of(2026, 3, 20));
        assertThat(saved.getRemaining_ml_g()).isEqualTo(800d);
    }

    // ── Error paths ────────────────────────────────────────────────────────────

    @Test
    void addToStorage_unknownHomeId_throwsEntityNotFoundException() {
        // Only stub the lookup that is actually called before the exception is thrown.
        // The storageType / save stubs are NOT registered here – the method throws
        // on the home lookup so they would never be invoked.
        when(homeRepo.findById(99L)).thenReturn(Optional.empty());

        AddToStorageRequest req = baseRequest();
        req.setHomeId(99L);

        assertThatThrownBy(() -> service.addToStorage(req))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void addToStorage_unknownStorageTypeId_throwsEntityNotFoundException() {
        // Home lookup succeeds, storageType lookup fails → only these two stubs needed.
        when(homeRepo.findById(1L)).thenReturn(Optional.of(home));
        when(storageTypeRepo.findById(99L)).thenReturn(Optional.empty());

        AddToStorageRequest req = baseRequest();
        req.setStorageTypeId(99L);

        assertThatThrownBy(() -> service.addToStorage(req))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }
}