package se.ronost.dev.messageboard.users;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.HashSet;
import java.util.Set;
import se.ronost.dev.messageboard.messages.Message;

@Entity
public class User {

    @Id
    @GeneratedValue
    private Long id;
    private String username;
    @OneToMany(mappedBy = "user")
    private Set<Message> messages = new HashSet<>();

    protected User() {}

    public User(String username) {
        this.username = username;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }
}