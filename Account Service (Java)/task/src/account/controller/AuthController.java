package account.controller;

import account.model.Employee;
import account.model.EmployeeDTO;
import account.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class AuthController {

    private final AuthService authService;
    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("api/auth/signup")
    public ResponseEntity<EmployeeDTO> register(@Valid @RequestBody Employee employee) {
        return authService.registerEmployee(employee);
    }

    @GetMapping("/api/auth/signup")
    public ResponseEntity<Void> handleGetRequest() {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
    }

    @PutMapping("/api/auth/signup")
    public ResponseEntity<Void> handlePutRequest() {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
    }

    @DeleteMapping("/api/auth/signup")
    public ResponseEntity<Void> handleDeleteRequest() {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
    }

}
