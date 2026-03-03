package be.intecbrussel.shoppinglist.service;

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
    public Home saveHome(Home home) {
        return userHomeRepository.save(home);
    }

    @Override
    public List<Home> findAllHomes() {
        return userHomeRepository.findAll();
    }

    @Override
    public Home findHome(long id) {
        return userHomeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Home not found with id: " + id));
    }

    @Override
    public Home updateHome(Home home, long id) {
        Home existing = findHome(id);
        existing.setName(home.getName());
        return userHomeRepository.save(existing);
    }

    @Override
    public void deleteHome(long id) {
        findHome(id); // throws 404 if absent
        userHomeRepository.deleteById(id);
    }
}
