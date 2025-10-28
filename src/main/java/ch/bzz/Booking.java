package ch.bzz;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Date;


@Getter                 // generiert Getter
@Setter                 // generiert Setter
@NoArgsConstructor      // generiert Standard-Konstruktor
@AllArgsConstructor     // generiert Konstruktor mit allen Feldern
public class Booking {
    private Integer id;
    private Date date;
    private String text;
    private Integer debitAccount;
    private Integer creditAccount;
    private double amount;
    private String project;
}
