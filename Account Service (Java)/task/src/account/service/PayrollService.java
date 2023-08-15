package account.service;

import account.model.Employee;
import account.model.EmployeeResponse;
import account.model.Payrolls;
import account.model.StatusResponse;
import account.repository.EmployeeRepository;
import account.repository.PayrollRepository;
import account.util.DateUtils;
import account.util.EmployeeUtils;
import account.util.ErrorResponseFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class PayrollService {

    private final PayrollRepository payrollRepository;
    private final EmployeeRepository employeeRepository;

    @Autowired
    public PayrollService(PayrollRepository payrollRepository, EmployeeRepository employeeRepository) {
        this.payrollRepository = payrollRepository;
        this.employeeRepository = employeeRepository;
    }

    @Transactional
    public ResponseEntity<?> payment(List<Payrolls> payrolls) {
        if (isEmailNotValid(payrolls)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponseFactory.createErrorForRegisteredUser(HttpStatus.BAD_REQUEST,
                            "Error!", "/api/acct/payments"));
        }
        String errorMessage = getErrorMessage(payrolls);
        if (!errorMessage.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponseFactory.createErrorForRegisteredUser(HttpStatus.BAD_REQUEST,
                            errorMessage, "/api/acct/payments"));
        }
        StatusResponse statusResponse = new StatusResponse("Added successfully!");
        for (Payrolls payroll : payrolls) {
            payrollRepository.save(payroll); // Save each payroll
        }
        return ResponseEntity.ok(statusResponse);
    }

    @Transactional
    public ResponseEntity<?> updatePayment(Payrolls payroll) {
        String email = payroll.getEmployee();
        String period = payroll.getPeriod();
        Long salary = payroll.getSalary();
        if (!payrollRepository.existsByEmployeeAndPeriod(email,period)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponseFactory.createErrorForRegisteredUser(HttpStatus.BAD_REQUEST,
                            "Error!", "/api/acct/payments"));
        }
        Payrolls checkPayroll = payrollRepository.findByEmployeeAndPeriod(email, period);
        checkPayroll.setSalary(salary);
        payrollRepository.save(checkPayroll);
        return ResponseEntity.ok(new StatusResponse("Updated successfully!"));
    }

    public ResponseEntity<?> getEmployeeWithPeriod(UserDetails userDetails, String period) {
        String email = userDetails.getUsername();

        if (!payrollRepository.existsByEmployeeAndPeriod(email,period)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponseFactory.createErrorForRegisteredUser(HttpStatus.BAD_REQUEST,
                            "Error!", "/api/empl/payment"));
        }
        Employee employee = findByEmail(email);
        Payrolls checkPayroll = payrollRepository.findByEmployeeAndPeriod(email, period);
        String formattedPeriod = DateUtils.convertToNameOfMonthFormat(period);
        String formattedSalary =EmployeeUtils.convertToFormattedString(checkPayroll.getSalary());
        String name = employee.getName();
        String lastname = employee.getLastname();
        EmployeeResponse employeeResponse = new EmployeeResponse(name,lastname,formattedPeriod,formattedSalary);
        return ResponseEntity.ok(employeeResponse);
    }
    public ResponseEntity<?> getEmployee(@AuthenticationPrincipal UserDetails userDetails) {

        String email = userDetails.getUsername();
        Employee employee =  findByEmail(email);
        if (!employeeRepository.existsByEmailIgnoreCase(email)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponseFactory.createUnauthorizedResponse());
        }

        List<Payrolls> payrolls = payrollRepository.findByEmployee(email);
        List<EmployeeResponse> employeeResponses = new ArrayList<>();
        for (Payrolls payroll : payrolls) {
            String formattedPeriod = DateUtils.convertToNameOfMonthFormat(payroll.getPeriod());
            String formattedSalary = EmployeeUtils.convertToFormattedString(payroll.getSalary());
            String name = employee.getName();
            String lastname = employee.getLastname();
            EmployeeResponse employeeResponse = new EmployeeResponse(name, lastname, formattedPeriod, formattedSalary);
            employeeResponses.add(employeeResponse);
        }
        Collections.reverse(employeeResponses);
        return ResponseEntity.ok(employeeResponses);
    }

    public boolean isEmailNotValid(List<Payrolls> payrolls) {
        for (Payrolls payroll : payrolls) {
            String email = payroll.getEmployee();
            Employee employee =  findByEmail(email);
            if (employee == null) {
                return true;
            }
            if (payrollRepository.existsByEmployee(email)) {
                return  true;
            }
        }
        return false;
    }

    public String getErrorMessage(List<Payrolls> payrolls) {
        String message = "";
        List<String> errorMessageList = new ArrayList<>();
        for (int i = 0; i < payrolls.size(); i++) {
            String period = payrolls.get(i).getPeriod();
            Long salary = payrolls.get(i).getSalary();
            if (!DateUtils.isValidMMYYYYFormat(period)) {
                message = "payments[" + i + "].period: Wrong date!";
                errorMessageList.add(message);
            }
            // if period contains in the list, not unique
            if (EmployeeUtils.isSalaryNotValid(salary)) {
                message = "payments[" + i + "].salary: Salary must be non negative!";
            }
        }
        if (errorMessageList.isEmpty()) return message;
        return String.join(", ", errorMessageList);
    }

    public Employee findByEmail(String email) {
        Optional<Employee> optionalEmployee = employeeRepository.findByEmailIgnoreCase(email);
        return optionalEmployee.orElse(null);
    }
}
