package account.service;

import account.model.Employee;
import account.model.EmployeeDTO;
import account.model.Role;
import account.repository.EmployeeRepository;
import account.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    RoleRepository roleRepository;

    @Autowired
    public EmployeeService(EmployeeRepository employeeRepository,  RoleRepository roleRepository) {
        this.employeeRepository = employeeRepository;
        this.roleRepository = roleRepository;
    }

    public ResponseEntity<?> registerEmployee(Employee employee) {
        System.out.println("Enter");
        if (isNotValidEmployee(employee)) {
            System.out.println("EnterISNOTVALID");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(HttpStatus.BAD_REQUEST));
        }
        if (isEmailRegistered(employee.getEmail())) {
            System.out.println("EnterISEmailRegistered");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorForRegisteredUser(HttpStatus.BAD_REQUEST));
        }
        System.out.println("Before Password ENcoder");
        System.out.println(employee.getPassword());
        employee.setPassword(passwordEncoder().encode(employee.getPassword()));
//        Role role = new Role();
//        role = roleRepository.save(role);
//        employee.setRole(role);
        //employee.setPassword(passwordEncoder().encode(employee.getPassword()));
        employee = employeeRepository.save(employee);
        System.out.println(employee.getPassword());
        System.out.println("getting password from Employee After password Encode");
        EmployeeDTO employeeDTO = new EmployeeDTO(employee);
        return ResponseEntity.ok(employeeDTO);
    }

    public ResponseEntity<?> getEmployee(@AuthenticationPrincipal UserDetails userDetails) {
        System.out.println("Enter");
        System.out.println(userDetails.getUsername());
        String email = userDetails.getUsername();
        Employee employee =  findByEmail(email);
        if (employee == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createUnauthorizedResponse());
        }
        return ResponseEntity.ok(employee);
    }



    public boolean isNotValidEmployee(Employee employee) {
        return employee == null ||
                employee.getEmail() == null ||
                employee.getEmail().isEmpty() ||
                !employee.getEmail().endsWith("@acme.com") ||
                employee.getName() == null ||
                employee.getName().isEmpty() ||
                employee.getLastname() == null ||
                employee.getLastname().isEmpty() ||
                employee.getPassword() == null ||
                employee.getPassword().isEmpty();

    }
    public Employee findByEmail(String email) {
        Optional<Employee> optionalEmployee = employeeRepository.findByEmailIgnoreCase(email);
        return optionalEmployee.orElse(null);
    }

    private boolean isEmailRegistered(String email) {
        return employeeRepository.existsByEmailIgnoreCase(email);
    }

    private Map<String, Object> createErrorResponse(HttpStatus status) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", new Date());
        errorResponse.put("status", status.value());
        errorResponse.put("error", status.getReasonPhrase());
        errorResponse.put("path", "/api/auth/signup");
        return errorResponse;
    }

    private Map<String, Object> createErrorForRegisteredUser(HttpStatus status) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", new Date());
        errorResponse.put("status", status.value());
        errorResponse.put("error", status.getReasonPhrase());
        errorResponse.put("message", "User exist!");
        errorResponse.put("path", "/api/auth/signup");
        return errorResponse;
    }

    private Map<String, Object> createUnauthorizedResponse() {
        Map<String, Object> unauthorizedResponse = new HashMap<>();
        unauthorizedResponse.put("timestamp", new Date());
        unauthorizedResponse.put("status", HttpStatus.UNAUTHORIZED.value());
        unauthorizedResponse.put("error", HttpStatus.UNAUTHORIZED.getReasonPhrase());
        unauthorizedResponse.put("message", "");
        unauthorizedResponse.put("path", "/api/empl/payment");
        return unauthorizedResponse;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


}
