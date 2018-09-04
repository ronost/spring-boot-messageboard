package se.ronost.dev.messageboard.users;

import java.net.URI;
import java.util.Collection;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import se.ronost.dev.messageboard.MessageBoardUtil;

@RestController
public class UserController {
    private final UserRepository userRepository;

	@Autowired
	UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @RequestMapping(value = "/users", method = RequestMethod.GET)
    public Collection<User> getAllUsers() {
        return this.userRepository.findAll();
    }

	@RequestMapping(value = "/users", method = RequestMethod.POST)
	ResponseEntity<?> addUser(@RequestParam String userName) {
        if(!MessageBoardUtil.validUserNameFormat(userName)) {
            return ResponseEntity.badRequest().build();
        }

        Optional<User> existingUser = userRepository.findByUsername(userName);

        if(existingUser.isPresent()) {
            return ResponseEntity.status(409).body("Username already exists!");
        }

        User user = userRepository.save(new User(userName));

        URI location = ServletUriComponentsBuilder
                    .fromCurrentRequest().path("/{messageId}")
                    .buildAndExpand(user.getId()).toUri();
        return ResponseEntity.created(location).build();
	}
}