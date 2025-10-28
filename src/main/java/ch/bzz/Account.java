package ch.bzz;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter                 // generiert Getter
@Setter                 // generiert Setter
@NoArgsConstructor      // generiert Standard-Konstruktor
@AllArgsConstructor     // generiert Konstruktor mit allen Feldern
@Entity
@Table(name = "account")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "account_number", nullable = false)
    private Integer accountNumber;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @ManyToOne(optional = false)
    @JoinColumn(name = "project_name", nullable = false)
    private Project project;
}

