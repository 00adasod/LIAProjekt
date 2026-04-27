package se.liaprojekt.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "answered_questions")
public class AnsweredQuestion {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private TestQuestion question;

    private boolean isCorrect;
}
