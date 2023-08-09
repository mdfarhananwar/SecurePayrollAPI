package account.controller;

import account.model.Employee;
import account.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
public class AuthController {

    private final EmployeeService employeeService;
    @Autowired
    public AuthController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @PostMapping("api/auth/signup")
    public ResponseEntity<?> register(@Valid @RequestBody Employee employee) {
        return employeeService.registerEmployee(employee);
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

    @GetMapping("/api/empl/payment")
    public ResponseEntity<?> getEmployee() {
        //System.out.println(authentication.getName());
        return ResponseEntity.ok("Farhan");
    }

}
