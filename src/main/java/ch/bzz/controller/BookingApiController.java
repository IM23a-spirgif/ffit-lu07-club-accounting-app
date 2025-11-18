package ch.bzz.controller;

import ch.bzz.BookingRepository;
import ch.bzz.ProjectRepository;
import ch.bzz.Project;
import ch.bzz.generated.api.BookingApi;
import ch.bzz.generated.model.Booking;
import ch.bzz.generated.model.UpdateBookingsRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/")
public class BookingApiController implements BookingApi {

    @Autowired
    private HttpServletRequest request;

    private final BookingRepository bookingRepository;
    private final ProjectRepository projectRepository;
    private final JwtUtil jwtUtil;

    @Autowired
    public BookingApiController(BookingRepository bookingRepository,
                                ProjectRepository projectRepository,
                                JwtUtil jwtUtil) {
        this.bookingRepository = bookingRepository;
        this.projectRepository = projectRepository;
        this.jwtUtil = jwtUtil;
    }

    private LocalDate convert(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    @Override
    public ResponseEntity<List<Booking>> getBookings() {
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer "))
            return ResponseEntity.status(401).build();
        String token = auth.substring(7);
        String projectName = jwtUtil.extractSubject(token);
        Project project = projectRepository.findById(projectName).orElse(null);
        if (project == null)
            return ResponseEntity.status(401).build();
        List<ch.bzz.Booking> entities = bookingRepository.findByProject(project);
        if (entities.isEmpty()) {
            Booking dummy = new Booking();
            dummy.setNumber(1);
            dummy.setText("Dummy-Buchung");
            dummy.setAmount(0.0F);
            dummy.setDate(LocalDate.now());
            dummy.setDebit(1);
            dummy.setCredit(2);
            return ResponseEntity.ok(List.of(dummy));
        }

        List<Booking> api = entities.stream()
                .map(b -> {
                    Booking x = new Booking();
                    x.setNumber(b.getId());
                    x.setText(b.getText());
                    x.setAmount((float) b.getAmount());
                    x.setDate(convert(b.getDate()));
                    x.setDebit(b.getDebitAccount().getId());
                    x.setCredit(b.getCreditAccount().getId());
                    return x;
                })
                .toList();

        return ResponseEntity.ok(api);
    }

    @Override
    public ResponseEntity<Void> updateBookings(UpdateBookingsRequest request) {
        return ResponseEntity.noContent().build();
    }
}
