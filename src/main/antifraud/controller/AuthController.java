package antifraud.controller;

import antifraud.dto.UserDeleteResponse;
import antifraud.repository.UserRepository;
import antifraud.dto.UserResponse;
import antifraud.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


@RestController
@RequestMapping("/api/auth")
public class AuthController {
    UserRepository userRepo;
    PasswordEncoder encoder;

    public AuthController(UserRepository userRepo, PasswordEncoder encoder) {
        this.userRepo = userRepo;
        this.encoder = encoder;
    }

    @PostMapping("/user")
    public ResponseEntity<UserResponse> register(@RequestBody User user) {
        try {
            if (user.getUsername() == null || user.getName() == null || user.getPassword() == null) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            if (userRepo.findByUsername(user.getUsername()) != null) {
                return new ResponseEntity<>(null, HttpStatus.CONFLICT);
            }

            user.setPassword(encoder.encode(user.getPassword()));

            //admin if first
            if (userRepo.count() == 0) {
                user.setRole("ROLE_ADMINISTRATOR");
                user.setAccountNonLocked(true);
            } else {
                user.setRole("ROLE_MERCHANT");
            }
            user.setOperation("LOCK");
            userRepo.save(user);

            UserResponse userResponse = new UserResponse();
            userResponse.setId(user.getUserId());
            userResponse.setName(user.getName());
            userResponse.setUsername(user.getUsername());
            userResponse.setRole(user.getRole().substring(5));

            return new ResponseEntity<>(userResponse, HttpStatus.CREATED);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return new ResponseEntity<>(null,HttpStatus.BAD_REQUEST);
        }

    }


    @GetMapping("/list")
    public Iterable<UserResponse> list() {
        List<User> userList = userRepo.findAll();
        List<UserResponse> userResponseList = new ArrayList<>();
        userList.sort(new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {
                if (((User) o1).getUserId() > ((User) o2).getUserId()) {
                    return 1;
                }
                return -1;
            }
        });
        for (User user : userList) {
            userResponseList.add(new UserResponse(user.getUserId(), user.getName(), user.getUsername(), user.getRole().substring(5)));
        }
        //sort by id
        return userResponseList;
    }

    @DeleteMapping("user/{username}")
    public ResponseEntity<UserDeleteResponse> deleteUser(@PathVariable String username) {
        User user = userRepo.findByUsername(username);
        if (user == null) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
        userRepo.delete(user);
        return new ResponseEntity<>(new UserDeleteResponse(username, "Deleted successfully!"), HttpStatus.OK);
    }

    @PutMapping("/role")
    public ResponseEntity<UserResponse> updateRole(@RequestBody User user) {
        User u = userRepo.findByUsername(user.getUsername());
        if (u == null) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
        if (user.getRole().equals("ADMINISTRATOR")) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
        if(userRepo.findByRole(user.getRole())==null && !user.getRole().equals("SUPPORT") && !user.getRole().equals("MERCHANT")) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
        if (user.getRole().equals(u.getRole())) {
            return new ResponseEntity<>(null, HttpStatus.CONFLICT);
        }
        if (user.getRole().equals("SUPPORT") && userRepo.existsUserByRole("ROLE_SUPPORT")) {
            return new ResponseEntity<>(null, HttpStatus.CONFLICT);
        }

        u.setRole("ROLE_" + user.getRole());
        userRepo.save(u);
        UserResponse userResponse = new UserResponse(u.getUserId(), u.getName(), u.getUsername(), u.getRole().substring(5));

        return new ResponseEntity<>(userResponse, HttpStatus.OK);
    }

    @PutMapping("/access")
    public ResponseEntity<String> updateAccess(@RequestBody User user) {
        User u = userRepo.findByUsername(user.getUsername());

        u.setOperation(user.getOperation());
        String status = "";
        if (user.getOperation().equals("LOCK")) {
            status = "locked";
            u.setAccountNonLocked(false);
        } else if (user.getOperation().equals("UNLOCK")) {
            status = "unlocked";
            u.setAccountNonLocked(true);
        }
        userRepo.save(u);
        String result = ("{\n" +
                "   \"status\": \"User " + u.getUsername() + " " + status + "!\"\n" +
                "}");
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

}