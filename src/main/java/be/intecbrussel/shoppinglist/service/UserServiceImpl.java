//package be.intecbrussel.shoppinglist.service;
//
//import jakarta.transaction.Transactional;
//import lombok.RequiredArgsConstructor;
//import org.intecbrussel.onlinecoursespringproject.dto.UserChangeRoleRequest;
//import org.intecbrussel.onlinecoursespringproject.dto.UserMapper;
//import org.intecbrussel.onlinecoursespringproject.dto.UserResponse;
//import org.intecbrussel.onlinecoursespringproject.exception.ResourceNotFoundException;
//import org.intecbrussel.onlinecoursespringproject.exception.UnauthorizedActionException;
//import org.intecbrussel.onlinecoursespringproject.model.User;
//import org.intecbrussel.onlinecoursespringproject.repository.UserRepository;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//import java.util.Optional;
//
//@Service
//@Transactional
//@RequiredArgsConstructor
//public class UserServiceImpl implements UserService {
//
//    private final UserRepository userRepository;
//
//    @Override
//    public List<UserResponse> getAllUsers() {
//        return userRepository.findAll()
//                .stream()
//                .map(u -> UserMapper.mapToUserResponse(u))
//                //.map(UserMapper::mapToUserResponse) //method reference not intuitive yet
//                .toList();
//    }
//
//    @Override
//    public UserResponse updateUserChangeRole(long id, UserChangeRoleRequest userChangeRoleRequest) {
//        User user = userRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
//        user.setRole(userChangeRoleRequest.role());
//        User updatedUser = userRepository.save(user);
//        return UserMapper.mapToUserResponse(updatedUser);
//    }
//
//    @Override
//    public void deleteUser(long id) {
//        userRepository.deleteById(id);
//    }
//
//    @Override
//    public User getLoggedInUser() {
//        // Find currently logged-in user:
//        String currentUsernameFromSecurityContext;
//        try{
//            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//            currentUsernameFromSecurityContext = authentication.getName();
//        }catch(Exception e){
//            throw new UnauthorizedActionException("You are not authenticated. Please login (again).");
//        }
//
//        Optional<User> optionalUser = userRepository.findByUsername(currentUsernameFromSecurityContext);
//        if(optionalUser.isEmpty()){
//            throw new UnauthorizedActionException("User not found.");
//        }
//        return optionalUser.get();
//    }
//
//}
