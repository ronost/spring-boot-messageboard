package se.ronost.dev.messageboard.messages;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import se.ronost.dev.messageboard.users.User;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findAll();
    List<Message> findByUser(User user);
}