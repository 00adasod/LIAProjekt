package se.liaprojekt.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "materials")
public class Material {

    @Id
    @GeneratedValue
    private Long id;

    private String title;
    private String contentType;
    private String contentUrl;
}
