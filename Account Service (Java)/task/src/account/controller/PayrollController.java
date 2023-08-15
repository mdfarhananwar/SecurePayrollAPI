package account.controller;

import account.model.Payrolls;
import account.service.EmployeeService;
import account.service.PayrollService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class PayrollController {


    private final PayrollService payrollService;

    @Autowired
    public PayrollController(PayrollService payrollService) {
        this.payrollService = payrollService;
    }

    @PostMapping("/api/acct/payments")
    public ResponseEntity<?> payment(@RequestBody List<Payrolls> payrolls) {
        return payrollService.payment(payrolls);
    }

    @PutMapping("/api/acct/payments")
    public ResponseEntity<?> updatePayment(@RequestBody Payrolls payroll) {
        return payrollService.updatePayment(payroll);
    }

    @GetMapping("/api/empl/payment")
    public ResponseEntity<?> getEmployeeWithOptionalPeriod(
            @RequestParam(name = "period", required = false) String period,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (period != null) {
            return payrollService.getEmployeeWithPeriod(userDetails, period);
        } else {
            return payrollService.getEmployee(userDetails);
        }
    }
}
