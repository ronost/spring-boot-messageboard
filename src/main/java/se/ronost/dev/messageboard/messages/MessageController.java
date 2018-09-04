package se.ronost.dev.messageboard.messages;

import java.net.URI;
import java.util.Collection;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import se.ronost.dev.messageboard.MessageBoardUtil;
import se.ronost.dev.messageboard.users.User;
import se.ronost.dev.messageboard.users.UserRepository;

@RestController
public class MessageController {
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;

    @Autowired
    MessageController(UserRepository userRepository, MessageRepository messageRepository) {
        this.userRepository = userRepository;
        this.messageRepository = messageRepository;
    }

    @RequestMapping(value = "/messages", method = RequestMethod.GET)
    public Collection<Message> getAllMessages() {
        return this.messageRepository.findAll();
    }

    private boolean isAuthenticated(String userName) {
        return userRepository.findByUsername(userName).isPresent();
    }

    private boolean isAuthorized(String userName1, String userName2) {
        return userName1.equals(userName2);
    }

    @RequestMapping(value = "/messages", method = RequestMethod.POST)
    ResponseEntity<?> addMessage(@RequestHeader(value = "X-AUTH-USER-HEADER") String userName, @RequestBody Message messageInput) {
        if(!MessageBoardUtil.validUserNameFormat(userName)) {
            return ResponseEntity.badRequest().build();
        }

        if(!this.isAuthenticated(userName)) {
            return ResponseEntity.status(401).build();
        }

        if(!this.isAuthorized(userName, messageInput.getUser().getUsername())) {
            return ResponseEntity.status(403).build();
        }

        if(messageInput.getId() != null) {
            Optional<Message> existingMessage = messageRepository.findById(messageInput.getId());

            if(existingMessage.isPresent()) {
                return ResponseEntity.status(409).body("ID already exists!");
            }
        }

        Message message = messageRepository.save(messageInput);

        URI location = ServletUriComponentsBuilder
                    .fromCurrentRequest().path("/{messageId}")
                    .buildAndExpand(message.getId()).toUri();
        return ResponseEntity.created(location).build();
    }

    @RequestMapping(value = "/messages/{messageId}", method = RequestMethod.PUT)
    ResponseEntity<?> editMessage(@RequestHeader(value = "X-AUTH-USER-HEADER") String userName, 
                                @PathVariable String messageId, @RequestBody Message messageInput) {
        if(!MessageBoardUtil.validUserNameFormat(userName)) {
            return ResponseEntity.badRequest().build();
        }
        
        if(!this.isAuthenticated(userName)) {
            return ResponseEntity.status(401).build();
        }

        Optional<Message> existingMessage = messageRepository.findById(Long.parseLong(messageId));

        if(existingMessage.isPresent()) {
            if(!this.isAuthorized(userName, existingMessage.get().getUser().getUsername())) {
                return ResponseEntity.status(403).build();
            }

            existingMessage.get().setMessage(messageInput.getMessage());
            Message message = messageRepository.save(existingMessage.get());

            URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{messageId}")
                .buildAndExpand(message.getId()).toUri();
            return ResponseEntity.ok(location);
        }
        else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @RequestMapping(value = "/messages/{messageId}", method = RequestMethod.DELETE)
    ResponseEntity<?> deleteMessage(@RequestHeader(value = "X-AUTH-USER-HEADER") String userName, @PathVariable String messageId) {
        if(!MessageBoardUtil.validUserNameFormat(userName)) {
            return ResponseEntity.badRequest().build();
        }

        if(!this.isAuthenticated(userName)) {
            return ResponseEntity.status(401).build();
        }

        Long messId = Long.parseLong(messageId);
        Optional<Message> existingMessage = messageRepository.findById(messId);
        if(existingMessage.isPresent()) {
            // Authorization check
            if(!this.isAuthorized(userName, existingMessage.get().getUser().getUsername())) {
                return ResponseEntity.status(403).build();
            }

            messageRepository.deleteById(messId);
            return ResponseEntity.ok().build();
        }
        else {
            return ResponseEntity.notFound().build();
        }
    }

    @RequestMapping(value = "/messages/user/{userId}", method = RequestMethod.GET)
    public Collection<Message> getByUserId(@PathVariable String userId) {
        Optional<User> user = this.userRepository.findById(new Long(userId));
        return this.messageRepository.findByUser(user.get());
    }
}
