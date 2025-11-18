package ch.bzz.controller;

import ch.bzz.AccountRepository;
import ch.bzz.Project;
import ch.bzz.ProjectRepository;
import ch.bzz.generated.api.AccountApi;
import ch.bzz.generated.model.Account;
import ch.bzz.generated.model.UpdateAccountsRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/")
public class AccountApiController implements AccountApi {

    @Autowired
    private HttpServletRequest request;

    private final AccountRepository accountRepository;
    private final ProjectRepository projectRepository;
    private final JwtUtil jwtUtil;

    @Autowired
    public AccountApiController(AccountRepository accountRepository,
                                ProjectRepository projectRepository,
                                JwtUtil jwtUtil) {
        this.accountRepository = accountRepository;
        this.projectRepository = projectRepository;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public ResponseEntity<List<Account>> getAccounts() {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer "))
            return ResponseEntity.status(401).build();
        String token = authHeader.substring(7);
        String projectName = jwtUtil.extractSubject(token);
        Project project = projectRepository.findById(projectName).orElse(null);
        if (project == null)
            return ResponseEntity.status(401).build();
        List<ch.bzz.Account> entities = accountRepository.findByProject(project);
        if (entities.isEmpty()) {
            Account dummy = new Account();
            dummy.setName("Dummy-Konto");
            dummy.setNumber(1000);
            return ResponseEntity.ok(List.of(dummy));
        }
        List<Account> apiList = entities.stream()
                .map(e -> {
                    Account a = new Account();
                    a.setNumber(e.getAccountNumber());
                    a.setName(e.getName());
                    return a;
                })
                .toList();
        return ResponseEntity.ok(apiList);
    }

    @Override
    @Transactional
    public ResponseEntity<Void> updateAccounts(UpdateAccountsRequest body) {
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer "))
            return ResponseEntity.status(401).build();
        String token = auth.substring(7);
        String projectName = jwtUtil.extractSubject(token);
        Project project = projectRepository.findById(projectName).orElse(null);
        if (project == null)
            return ResponseEntity.status(401).build();
        Map<Integer, ch.bzz.Account> existing =
                accountRepository.findByProject(project).stream()
                        .collect(Collectors.toMap(ch.bzz.Account::getAccountNumber, a -> a));
        for (var api : body.getAccounts()) {
            Integer number = api.getNumber();
            String name = unwrap(api.getName());
            if (number == null)
                continue;
            ch.bzz.Account entity = existing.get(number);
            if (entity == null) {
                entity = new ch.bzz.Account();
                entity.setProject(project);
                entity.setAccountNumber(number);
            }
            if (name != null && !name.isBlank())
                entity.setName(name);
            accountRepository.save(entity);
        }
        return ResponseEntity.noContent().build();
    }

    private <T> T unwrap(JsonNullable<T> value) {
        return value != null && value.isPresent() ? value.get() : null;
    }
}
