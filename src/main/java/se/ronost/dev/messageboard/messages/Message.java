package se.ronost.dev.messageboard.messages;

import java.time.LocalDateTime;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import se.ronost.dev.messageboard.users.User;

@Entity
public class Message {
    @Id
    @GeneratedValue
    private Long id;
    private LocalDateTime created;
    @ManyToOne
    private User user;
    private String message;

    protected Message() {
        this.created = LocalDateTime.now();
    }

    public Message(String message, User user) {
        this.created = LocalDateTime.now();
        this.message = message;
        this.user = user;
     }

     public Long getId() {
        return this.id;
     }

     public LocalDateTime getCreated() {
         return this.created;
     }

     public User getUser() {
         return this.user;
     }

     public void setMessage(String message) {
         this.message = message;
     }

     public String getMessage() {
         return this.message;
     }
}
