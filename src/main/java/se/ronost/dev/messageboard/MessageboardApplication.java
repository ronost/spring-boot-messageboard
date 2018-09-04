package se.ronost.dev.messageboard;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import se.ronost.dev.messageboard.users.User;
import se.ronost.dev.messageboard.messages.Message;
import se.ronost.dev.messageboard.messages.MessageRepository;
import se.ronost.dev.messageboard.users.UserRepository;

@SpringBootApplication
public class MessageboardApplication {

    private static final Logger log = LoggerFactory.getLogger(MessageboardApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(MessageboardApplication.class, args);
    }

    @Bean
    CommandLineRunner init(UserRepository userRepository, MessageRepository messageRepository) {
        return (args) -> {
            User mrPink = new User("Mr.Pink");
            User niceGuyEddie = new User("Nice Guy Eddie");
            userRepository.save(mrPink);
            userRepository.save(niceGuyEddie);

            messageRepository.save(new Message("C'mon, throw in a buck!", niceGuyEddie));
            messageRepository.save(new Message("Uh-uh, I don't tip.", mrPink));
            messageRepository.save(new Message("You don't tip?", niceGuyEddie));
            messageRepository.save(new Message("Nah, I don't believe in it.", mrPink));
            messageRepository.save(new Message("You don't believe in tipping?", niceGuyEddie));

            // fetch all users
            log.info("Users found with findAll():");
            log.info("-------------------------------");
            log.info("ID -> UserName");
            for (User user : userRepository.findAll()) {
                log.info(user.getId() + " -> " + user.getUsername());
            }
            log.info("");

            // fetch all messages
            log.info("Messages found with findAll():");
            log.info("-------------------------------");
            log.info("ID -> Created -> User.username -> Message");
            for(Message message: messageRepository.findAll()) {
                log.info(message.getId() + " -> " + message.getCreated().toString() + " -> " + message.getUser().getUsername() + " -> " + message.getMessage());
            }
            log.info("");
        };
    }
}
