package ch.bzz;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;


@Getter                 // generiert Getter
@Setter                 // generiert Setter
@NoArgsConstructor      // generiert Standard-Konstruktor
@AllArgsConstructor     // generiert Konstruktor mit allen Feldern
public class Project {
    private String projectName;
    private String passwordHash;
}

