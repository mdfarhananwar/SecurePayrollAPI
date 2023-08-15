package account.controller;

import account.model.*;
import account.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class AuthController {

    private final EmployeeService employeeService;
    @Autowired
    public AuthController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @PostMapping("api/auth/signup")
    public ResponseEntity<?> register(@RequestBody EmployeeRequest employee) {
        
        return employeeService.registerEmployee(employee);
    }

    @PostMapping("/api/auth/changepass")
    public ResponseEntity<?> changePassword(@AuthenticationPrincipal UserDetails userDetails,
                                            @RequestBody PasswordRequest passwordRequest) {
        return employeeService.changePassword(passwordRequest, userDetails);
    }

    @PutMapping("api/admin/user/role")
    public ResponseEntity<?> setRole(@RequestBody RoleRequest roleRequest,
                                     @AuthenticationPrincipal UserDetails userDetails) {
        return employeeService.updateRole(roleRequest, userDetails);
    }

    @GetMapping("api/admin/user/")
    public ResponseEntity<?> getUser(@AuthenticationPrincipal UserDetails userDetails) {
        return employeeService.getUser(userDetails);
    }

    @DeleteMapping("api/admin/user/{email}")
    public ResponseEntity<?> deleteUser(@PathVariable String email,
                                        @AuthenticationPrincipal UserDetails userDetails) {
        return employeeService.deleteUser(email, userDetails);
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

    @PutMapping("api/admin/user/access")
    public ResponseEntity<?> lockUnlockUser(@RequestBody LockUnlockRequest lockUnlockRequest,
                                            @AuthenticationPrincipal UserDetails userDetails) {
        return employeeService.lockUnlockUser(lockUnlockRequest, userDetails);
    }

    @GetMapping("/api/security/events/")
    public ResponseEntity<?> getEvents() {
        return employeeService.getEvents();
    }

}
