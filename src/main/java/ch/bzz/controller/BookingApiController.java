package ch.bzz.controller;

import ch.bzz.Account;
import ch.bzz.AccountRepository;
import ch.bzz.BookingRepository;
import ch.bzz.ProjectRepository;
import ch.bzz.Project;
import ch.bzz.generated.api.BookingApi;
import ch.bzz.generated.model.Booking;
import ch.bzz.generated.model.UpdateBookingsRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
    private final AccountRepository accountRepository;

    @Autowired
    public BookingApiController(BookingRepository bookingRepository,
                                ProjectRepository projectRepository,
                                JwtUtil jwtUtil,
                                AccountRepository accountRepository) {
        this.bookingRepository = bookingRepository;
        this.projectRepository = projectRepository;
        this.jwtUtil = jwtUtil;
        this.accountRepository = accountRepository;
    }

    private LocalDate convert(Date date) {
        if (date == null) return null;
        if (date instanceof java.sql.Date sqlDate) {
            return sqlDate.toLocalDate();
        }
        return date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
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
            return ResponseEntity.ok(List.of());
        }
        List<Booking> api = entities.stream()
                .map(b -> {
                    Booking x = new Booking();
                    x.setNumber(b.getId());
                    x.setText(b.getText());
                    x.setAmount((float) b.getAmount());
                    x.setDate(convert(b.getDate()));
                    Account debit = b.getDebitAccount();
                    Account credit = b.getCreditAccount();
                    x.setDebit(debit != null ? debit.getAccountNumber() : null);
                    x.setCredit(credit != null ? credit.getAccountNumber() : null);
                    return x;
                })
                .toList();
        return ResponseEntity.ok(api);
    }

    @Override
    @Transactional
    public ResponseEntity<Void> updateBookings(UpdateBookingsRequest body) {
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            return ResponseEntity.status(401).build();
        }
        String projectName = jwtUtil.extractSubject(auth.substring(7));
        Project project = projectRepository.findById(projectName).orElse(null);
        if (project == null) {
            return ResponseEntity.status(401).build();
        }
        for (var entry : body.getEntries()) {
            Integer id = entry.getId();
            LocalDate date = unwrap(entry.getDate());
            String text = unwrap(entry.getText());
            Integer debitNumber = unwrap(entry.getDebit());
            Integer creditNumber = unwrap(entry.getCredit());
            Float amount = unwrap(entry.getAmount());
            boolean allNull = (date == null &&
                    text == null &&
                    debitNumber == null &&
                    creditNumber == null &&
                    amount == null);
            ch.bzz.Booking entity = null;
            if (id != null) {
                var opt = bookingRepository.findById(id);
                if (opt.isPresent()) {
                    entity = opt.get();
                    if (!entity.getProject().getProjectName().equals(project.getProjectName())) {
                        return ResponseEntity.status(403).build();
                    }
                }
            }
            if (entity == null) {
                if (allNull) {
                    continue;
                }
                if (date == null || text == null || debitNumber == null ||
                        creditNumber == null || amount == null) {
                    return ResponseEntity.badRequest().build();
                }
                Account debitAcc = resolveAccount(project, debitNumber);
                Account creditAcc = resolveAccount(project, creditNumber);
                if (debitAcc == null || creditAcc == null) {
                    return ResponseEntity.badRequest().build();
                }
                ch.bzz.Booking newEntity = new ch.bzz.Booking();
                newEntity.setProject(project);
                newEntity.setText(text);
                newEntity.setAmount(amount);
                newEntity.setDate(Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant()));
                newEntity.setDebitAccount(debitAcc);
                newEntity.setCreditAccount(creditAcc);
                bookingRepository.save(newEntity);
                continue;
            }
            if (allNull) {
                bookingRepository.delete(entity);
                continue;
            }
            if (date != null) {
                entity.setDate(Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant()));
            }
            if (text != null && !text.isBlank()) {
                entity.setText(text);
            }
            if (debitNumber != null) {
                Account debitAcc = resolveAccount(project, debitNumber);
                if (debitAcc == null) {
                    return ResponseEntity.badRequest().build();
                }
                entity.setDebitAccount(debitAcc);
            }
            if (creditNumber != null) {
                Account creditAcc = resolveAccount(project, creditNumber);
                if (creditAcc == null) {
                    return ResponseEntity.badRequest().build();
                }
                entity.setCreditAccount(creditAcc);
            }
            if (amount != null) {
                entity.setAmount(amount);
            }
            bookingRepository.save(entity);
        }
        return ResponseEntity.noContent().build();
    }

    private Account resolveAccount(Project project, Integer accountNumberOrId) {
        if (accountNumberOrId == null) return null;
        var accounts = accountRepository.findByProject(project);
        for (var acc : accounts) {
            if (accountNumberOrId.equals(acc.getAccountNumber())) {
                return acc;
            }
        }
        return accountRepository.findById(accountNumberOrId)
                .filter(a -> a.getProject().getProjectName().equals(project.getProjectName()))
                .orElse(null);
    }

    @DeleteMapping("/bookings/{id}")
    @Transactional
    public ResponseEntity<Void> deleteBooking(@PathVariable("id") Integer id) {
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer "))
            return ResponseEntity.status(401).build();
        String token = auth.substring(7);
        String projectName = jwtUtil.extractSubject(token);
        Project project = projectRepository.findById(projectName).orElse(null);
        if (project == null)
            return ResponseEntity.status(401).build();
        var optional = bookingRepository.findById(id);
        if (optional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        var booking = optional.get();
        if (!booking.getProject().getProjectName().equals(project.getProjectName())) {
            return ResponseEntity.status(403).build();
        }
        bookingRepository.delete(booking);
        return ResponseEntity.noContent().build();
    }

    private <T> T unwrap(JsonNullable<T> value) {
        return (value != null && value.isPresent()) ? value.get() : null;
    }
}
