package ch.bzz;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter                 // generiert Getter
@Setter                 // generiert Setter
@NoArgsConstructor      // generiert Standard-Konstruktor
@AllArgsConstructor     // generiert Konstruktor mit allen Feldern
@Entity
@Table(name = "project")
public class Project {
    @Id
    @Column(name = "project_name", nullable = false, unique = true, length = 100)
    private String projectName;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;
}

