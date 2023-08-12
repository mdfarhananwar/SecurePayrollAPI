package account.controller;

import account.model.*;
import account.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
        System.out.println("@PostMapping(\"api/auth/signup\")");
        return employeeService.registerEmployee(employee);
    }

    @PostMapping("/api/auth/changepass")
    public ResponseEntity<?> changePassword(@AuthenticationPrincipal UserDetails userDetails,
                                            @RequestBody PasswordRequest passwordRequest) {
        System.out.println(userDetails.getUsername());
        System.out.println("POST MAPPING CHECK PASSWORD");
        return employeeService.changePassword(passwordRequest, userDetails);
    }

    @PostMapping("/api/acct/payments")
    public ResponseEntity<?> payment(@RequestBody List<Payrolls> payrolls) {
        System.out.println("@PostMapping(\"/api/acct/payments\")");
        return employeeService.payment(payrolls);
    }

    @PutMapping("/api/acct/payments")
    public ResponseEntity<?> updatePayment(@RequestBody Payrolls payroll) {
        System.out.println("@PutMapping(\"/api/acct/payments\")");
        System.out.println(payroll.toString());
        return employeeService.updatePayment(payroll);
    }

    @PutMapping("api/admin/user/role")
    public ResponseEntity<?> setRole(@RequestBody RoleRequest roleRequest,
                                     @AuthenticationPrincipal UserDetails userDetails) {
        return employeeService.updateRole(roleRequest, userDetails);
    }



    @GetMapping("api/admin/user/")
    public ResponseEntity<?> getUser(@AuthenticationPrincipal UserDetails userDetails) {
        System.out.println("ENTER ADMIN USER");
        return employeeService.getUser(userDetails);
    }

    @DeleteMapping("api/admin/user/{email}")
    public ResponseEntity<?> deleteUser(@PathVariable String email,
                                        @AuthenticationPrincipal UserDetails userDetails) {
        System.out.println("@DeleteMapping( \"api/admin/user/\")");
        return employeeService.deleteUser(email, userDetails);
    }

    //value = {"api/admin/user/{email}",

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
    public ResponseEntity<?> getEmployeeWithOptionalPeriod(
            @RequestParam(name = "period", required = false) String period,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (period != null) {
            System.out.println("Enter @GetMapping(\"/api/empl/payment/period\")");

            return employeeService.getEmployeeWithPeriod(userDetails, period);
        } else {
            System.out.println("Enter @GetMapping(\"/api/empl/payment\") without RequestParam");
            System.out.println(userDetails.getUsername());
            System.out.println(userDetails.getAuthorities());
            return employeeService.getEmployee(userDetails);
        }
    }

    @PutMapping("api/admin/user/access")
    public ResponseEntity<?> lockUnlockUser(@RequestBody LockUnlockRequest lockUnlockRequest) {
        return employeeService.lockUnlockUser(lockUnlockRequest);
    }


}
