package antifraud.service;

import antifraud.repository.UserRepository;
import antifraud.dto.EditUserRoleRequest;
import antifraud.dto.UserResponse;
import antifraud.exceptions.BadRequestException;
import antifraud.exceptions.HttpConflictException;
import antifraud.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public void save(User user) {
        userRepository.save(user);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User findByName(String name) {
        return userRepository.findByUsername(name);
    }


    public Optional<UserResponse> editUserRole(EditUserRoleRequest editUserRoleRequest) {
        String requestRole = editUserRoleRequest.getRole();

        if (!"SUPPORT".equalsIgnoreCase(requestRole) &&
                !"MERCHANT".equalsIgnoreCase(requestRole)) {
            throw new BadRequestException("Unknown or not allowed role");
        }

        Optional<User> userByUsername = userRepository.findUserByUsernameIgnoreCase(editUserRoleRequest.getUsername());
        if (userByUsername.isPresent()) {
            User user = userByUsername.get();

            if (user.getRole().equals(requestRole)) {
                throw new HttpConflictException("This role is already assigned");
            }

            user.setRole(requestRole);
            userRepository.save(user);
            return Optional.of(new UserResponse(user));
        }
        return Optional.empty();
    }

}
