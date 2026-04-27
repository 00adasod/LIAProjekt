package se.liaprojekt.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "email_notifications")
public class EmailNotification {

    @Id
    @GeneratedValue
    private Long id;

    private String type;
    private String subject;
    private LocalDateTime sentAt;
    private String status;

    @ManyToOne
    private User user;
}