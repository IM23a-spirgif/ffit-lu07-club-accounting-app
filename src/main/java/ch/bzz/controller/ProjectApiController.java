package ch.bzz.controller;

import ch.bzz.Project;
import ch.bzz.ProjectRepository;
import ch.bzz.generated.api.ProjectApi;
import ch.bzz.generated.model.LoginProject200Response;
import ch.bzz.generated.model.LoginRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.util.StringUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@RestController
@RequestMapping("/api")
public class ProjectApiController implements ProjectApi {

    private final ProjectRepository projectRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Autowired
    public ProjectApiController(ProjectRepository projectRepository, JwtUtil jwtUtil) {
        this.projectRepository = projectRepository;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public ResponseEntity<Void> createProject(LoginRequest loginRequest) {
        // Basic validation
        if (loginRequest == null || !StringUtils.hasText(loginRequest.getProjectName()) || !StringUtils.hasText(loginRequest.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "projectName and password are required");
        }

        String projectName = loginRequest.getProjectName().trim();
        String rawPassword = loginRequest.getPassword();

        if (projectRepository.existsById(projectName)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Project already exists");
        }

        String hash = passwordEncoder.encode(rawPassword);
        Project project = new Project(projectName, hash);
        projectRepository.save(project);

        return ResponseEntity.created(URI.create("/api/projects/" + projectName)).build();
    }

    @Override
    public ResponseEntity<LoginProject200Response> loginProject(LoginRequest loginRequest) {
        if (loginRequest == null || !StringUtils.hasText(loginRequest.getProjectName()) || !StringUtils.hasText(loginRequest.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        String projectName = loginRequest.getProjectName().trim();
        String rawPassword = loginRequest.getPassword();

        Optional<Project> projectOpt = projectRepository.findById(projectName);
        if (projectOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        Project project = projectOpt.get();
        if (!passwordEncoder.matches(rawPassword, project.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        String token = jwtUtil.generateToken(projectName);
        LoginProject200Response body = new LoginProject200Response();
        body.setAccessToken(token);
        return ResponseEntity.ok(body);
    }
}
