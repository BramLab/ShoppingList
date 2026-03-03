package be.intecbrussel.shoppinglist.service;

import be.intecbrussel.shoppinglist.dto.HomeMapper;
import be.intecbrussel.shoppinglist.dto.HomeRequest;
import be.intecbrussel.shoppinglist.dto.HomeResponse;
import be.intecbrussel.shoppinglist.exception.ResourceNotFoundException;
import be.intecbrussel.shoppinglist.model.Home;
import be.intecbrussel.shoppinglist.repository.UserHomeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class HomeServiceImpl implements HomeService {

    private final UserHomeRepository userHomeRepository;

    @Override
    public HomeResponse saveHome(HomeRequest request) {
        Home saved = userHomeRepository.save(HomeMapper.mapToHome(request));
        return HomeMapper.mapToHomeResponse(saved);
    }

    @Override
    public List<HomeResponse> findAllHomes() {
        return userHomeRepository.findAll()
                .stream()
                .map(HomeMapper::mapToHomeResponse)
                .toList();
    }

    @Override
    public HomeResponse findHomeById(long id) {
        return HomeMapper.mapToHomeResponse(findEntity(id));
    }

    @Override
    public HomeResponse updateHome(long id, HomeRequest request) {
        Home existing = findEntity(id);
        existing.setName(request.name());
        return HomeMapper.mapToHomeResponse(userHomeRepository.save(existing));
    }

    @Override
    public void deleteHome(long id) {
        findEntity(id); // throws 404 if absent
        userHomeRepository.deleteById(id);
    }

    // Package-private: lets StoredFoodServiceImpl resolve a Home entity
    // without exposing a raw-entity method on the public interface.
    Home findEntity(long id) {
        return userHomeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Home not found with id: " + id));
    }
}
