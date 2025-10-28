package ch.bzz;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;


@Getter                 // generiert Getter
@Setter                 // generiert Setter
@NoArgsConstructor      // generiert Standard-Konstruktor
@AllArgsConstructor     // generiert Konstruktor mit allen Feldern
public class Account {
    private Integer id;
    private Integer accountNumber;
    private String name;
    private String project;
}

