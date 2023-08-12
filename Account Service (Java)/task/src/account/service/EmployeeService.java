package account.service;

import account.model.*;
import account.repository.EmployeeRepository;
import account.repository.GroupRepository;
import account.repository.PayrollRepository;
import account.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    RoleRepository roleRepository;
    private final PayrollRepository payrollRepository;
    private GroupRepository groupRepository;



    @Autowired
    public EmployeeService(EmployeeRepository employeeRepository, RoleRepository roleRepository, PayrollRepository payrollRepository, GroupRepository groupRepository) {
        this.employeeRepository = employeeRepository;
        this.roleRepository = roleRepository;
        this.payrollRepository = payrollRepository;
        this.groupRepository = groupRepository;
    }



    public ResponseEntity<?> registerEmployee(EmployeeRequest employee) {
        System.out.println(employee.toString());
        System.out.println(employee.getPassword());
        System.out.println("Enter");
        if (isNotValidEmployee(employee)) {
            System.out.println("EnterISNOTVALID");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(HttpStatus.BAD_REQUEST,
                            "/api/auth/signup"));
        }
        if (isEmailRegistered(employee.getEmail())) {
            System.out.println("EnterISEmailRegistered");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorForRegisteredUser(HttpStatus.BAD_REQUEST,
                            "User exist!", "/api/auth/signup"));
        }
        if (isLengthNotMin(employee.getPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorForRegisteredUser(HttpStatus.BAD_REQUEST,
                            "The password length" +
                            " must be at least 12 chars!", "/api/auth/signup"));
        }
        if (isPasswordBreached(employee.getPassword())) {
            System.out.println("PasswordBreach");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorForRegisteredUser(HttpStatus.BAD_REQUEST,
                            "The password is in the hacker's database!",
                            "/api/auth/signup"));
        }
        System.out.println("Before Password ENcoder");
        System.out.println(employee.getPassword());
        //Optional<Employee> byEmailIgnoreCase = employeeRepository.findByEmailIgnoreCase(employee.getEmail());

        Employee saveEmployee = new Employee();
        saveEmployee.setEmail(employee.getEmail());
        saveEmployee.setPassword(passwordEncoder().encode(employee.getPassword()));
        saveEmployee.setName(employee.getName());
        saveEmployee.setLastname(employee.getLastname());
//        Role role = new Role();
//        role = roleRepository.save(role);
//        employee.setRole(role);
        //employee.setPassword(passwordEncoder().encode(employee.getPassword()));
        saveEmployee = assignRoles(saveEmployee);
        System.out.println(saveEmployee.getPassword());
        System.out.println("getting password from Employee After password Encode");
        EmployeeDTO employeeDTO = new EmployeeDTO(saveEmployee);
        return ResponseEntity.ok(employeeDTO);
        //return ResponseEntity.ok(employeeDTO);
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
        List<Payrolls> payrolls = payrollRepository.findByEmployee(email);
        List<EmployeeResponse> employeeResponses = new ArrayList<>();
        for (Payrolls payroll : payrolls) {
            String formattedPeriod = convertToNameOfMonthFormat(payroll.getPeriod());
            String formattedSalary = convertToFormattedString(payroll.getSalary());
            String name = employee.getName();
            String lastname = employee.getLastname();
            EmployeeResponse employeeResponse = new EmployeeResponse(name, lastname, formattedPeriod, formattedSalary);
            employeeResponses.add(employeeResponse);
        }
        Collections.reverse(employeeResponses);

        return ResponseEntity.ok(employeeResponses);
    }
    public ResponseEntity<?> changePassword(PasswordRequest passwordRequest,
                                            UserDetails userDetails) {
        String newPassword = passwordRequest.getNew_password();
        System.out.println("newPassword");
        System.out.println(newPassword);
        System.out.println("newPassword");
        String email = userDetails.getUsername();
//        if (isNotValidUSerDetails(userDetails)) {
//            System.out.println("EnterISNOTVALID");
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                    .body(createErrorForRegisteredUser(HttpStatus.BAD_REQUEST,
//                            "Not Valid User",
//                            "/api/auth/changepass"));
//        }
//        if (isLengthNotMin(userDetailsPassword)) {
//            System.out.println("userDetailsPassword problem");
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                    .body(createErrorForRegisteredUser(HttpStatus.BAD_REQUEST,
//                            "Password length must be 12 chars minimum!",
//                            "/api/auth/changepass"));
//        }

        if (isLengthNotMin(newPassword)) {
            System.out.println("NewPassword problem");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorForRegisteredUser(HttpStatus.BAD_REQUEST,
                            "Password length must be 12 chars minimum!",
                            "/api/auth/changepass"));
        }
        Employee employee =  findByEmail(email);
        if (employee == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createUnauthorizedResponse());
        }

        if (passwordEncoder().matches(newPassword, employee.getPassword())) {
            System.out.println("Both Passwords are same");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorForRegisteredUser(HttpStatus.BAD_REQUEST,
                            "The passwords must be different!",
                            "/api/auth/changepass"));
        }

        if (isPasswordBreached(newPassword)) {
            System.out.println("PasswordBreach");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorForRegisteredUser(HttpStatus.BAD_REQUEST,
                            "The password is in the hacker's database!",
                            "/api/auth/changepass"));
        }
        System.out.println("Successfull");
        System.out.println("Before Password Change : - " + employee.getPassword());
        employee.setPassword(passwordEncoder().encode(newPassword));
        System.out.println("After Password Change : - " + employee.getPassword());
        employeeRepository.save(employee);
        PasswordSuccessResponse passwordSuccessResponse = new PasswordSuccessResponse(email,
                "The password has been updated successfully");
        return ResponseEntity.ok(passwordSuccessResponse);

    }
    @Transactional
    public ResponseEntity<?> payment(List<Payrolls> payrolls) {
        if (isEmailNotValid(payrolls)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorForRegisteredUser(HttpStatus.BAD_REQUEST,
                            "Error!", "/api/acct/payments"));
        }
        String errorMessage = getErrorMessage(payrolls);
        if (!errorMessage.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorForRegisteredUser(HttpStatus.BAD_REQUEST,
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
                    .body(createErrorForRegisteredUser(HttpStatus.BAD_REQUEST,
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
                    .body(createErrorForRegisteredUser(HttpStatus.BAD_REQUEST,
                            "Error!", "/api/empl/payment"));
        }
        Optional<Employee> employee = employeeRepository.findByEmailIgnoreCase(email);

        Payrolls checkPayroll = payrollRepository.findByEmployeeAndPeriod(email, period);



        String formattedPeriod = convertToNameOfMonthFormat(period);
        String formattedSalary = convertToFormattedString(checkPayroll.getSalary());
        String name = employee.get().getName();
        String lastname = employee.get().getLastname();
        EmployeeResponse employeeResponse = new EmployeeResponse(name,lastname,formattedPeriod,formattedSalary);
        return ResponseEntity.ok(employeeResponse);
    }

    private Employee assignRoles(Employee employee) {
        System.out.println(groupRepository.findByName("Administrative"));
        System.out.println(groupRepository.findByName("Administrative"));
        System.out.println(roleRepository.findByName("USER"));
        System.out.println(roleRepository.findByName("ADMINISTRATOR"));
        System.out.println(roleRepository.findByName("ACCOUNTANT"));
        Group administrativeGroup = groupRepository.findByName("Administrative");
        Group businessGroup =  groupRepository.findByName("Business");
        Role userRole = roleRepository.findByName("USER");
        Role adminRole = roleRepository.findByName("ADMINISTRATOR");
        Role accountantRole = roleRepository.findByName("ACCOUNTANT");


        if (employeeRepository.count() == 0) {
            employee.setGroup(administrativeGroup);
            employee.getRoles().add(adminRole);
        } else {
            employee.setGroup(businessGroup);
            employee.getRoles().add(userRole);
        }

        return employeeRepository.save(employee);
    }


    public ResponseEntity<?> updateRole(RoleRequest roleRequest, UserDetails userDetails) {
        String userEmail = userDetails.getUsername();
        Optional<Employee> employeeOptionalUser = employeeRepository.findByEmailIgnoreCase(userEmail);
        Employee employeeUser = employeeOptionalUser.orElse(new Employee());

        if (!isUserAdmin(employeeUser)) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("timestamp", new Date());
            errorResponse.put("status", HttpStatus.FORBIDDEN.value());
            errorResponse.put("error", HttpStatus.FORBIDDEN.getReasonPhrase());
            errorResponse.put("message", "Access Denied!");
            errorResponse.put("path", "/api/admin/user/role");

            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        }
        if (roleRequest != null) {
            System.out.println(roleRequest.toString());
        } else {
            System.out.println("ROle Request is null");
        }
        assert roleRequest != null;
        String email = roleRequest.getUser();
        Operation operation = roleRequest.getOperation();
        String role = roleRequest.getRole();
        System.out.println("ROLE TO CHECK");
        System.out.println(role);
        System.out.println("ROLE TO CHECK");

        if (!employeeRepository.existsByEmailIgnoreCase(email)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorForRegisteredUser(HttpStatus.NOT_FOUND,
                            "User not found!", "/api/admin/user/role"));
        }
        if (!roleRepository.existsByName(role)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorForRegisteredUser(HttpStatus.NOT_FOUND,
                            "Role not found!", "/api/admin/user/role"));
        }

//        Employee employee = findByEmail(email);
        Optional<Employee> employeeOptional = employeeRepository.findByEmailIgnoreCase(email);
        Employee employee = employeeOptional.orElse(new Employee());
        if (isUserAdmin(employee)) {
            if (operation == Operation.GRANT) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createErrorForRegisteredUser(HttpStatus.BAD_REQUEST,
                                "The user cannot combine administrative and business roles!",
                                "/api/admin/user/role"));
            } else {
              //"Can't remove ADMINISTRATOR role!"
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createErrorForRegisteredUser(HttpStatus.BAD_REQUEST,
                                "Can't remove ADMINISTRATOR role!" ,
                                "/api/admin/user/role"));
            }

        } else {
          if (isRoleAdmin(role))
              return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorForRegisteredUser(HttpStatus.BAD_REQUEST,
                            "The user cannot combine administrative and business roles!",
                            "/api/admin/user/role"));
        }
        Role updateRole = roleRepository.findByName(role);
        Set<Role> roles = employee.getRoles();
        System.out.println("updateROle");
        System.out.println(updateRole);
        System.out.println("updateROle");
        System.out.println(employee.getRoles());
        if (operation == Operation.GRANT) {
            employee.getRoles().add(updateRole);
            employeeRepository.save(employee);
        }
        if (operation == Operation.REMOVE) {
            if (!employee.getRoles().contains(updateRole)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createErrorForRegisteredUser(HttpStatus.BAD_REQUEST,
                                "The user does not have a role!",
                                "/api/admin/user/role"));
            }
            if (employee.getRoles().size() == 1) {

                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(createErrorForRegisteredUser(HttpStatus.BAD_REQUEST,
                                    "The user must have at least one role!",
                                    "/api/admin/user/role"));

            }

            employee.getRoles().remove(updateRole);
            employeeRepository.save(employee);
        }
        EmployeeDTO employeeDTO = new EmployeeDTO(employee);
        return ResponseEntity.ok(employeeDTO);
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
            String email = payrolls.get(i).getEmployee();
            String period = payrolls.get(i).getPeriod();
            Long salary = payrolls.get(i).getSalary();
            if (!isValidMMYYYYFormat(period)) {
                message = "payments[" + i + "].period: Wrong date!";
                errorMessageList.add(message);
            }
            // if period contains in the list, not unique
            if (isSalaryNotValid(salary)) {
                message = "payments[" + i + "].salary: Salary must be non negative!";
            }
        }
        if (errorMessageList.isEmpty()) return message;
        return errorMessageList
                .stream()
                .collect(Collectors.joining(", "));
    }

    public boolean isValidMMYYYYFormat(String inputDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-yyyy");
        try {
            YearMonth yearMonth = YearMonth.parse(inputDate, formatter);

            // Check if the month value is valid
            if (yearMonth.getMonthValue() >= 1 && yearMonth.getMonthValue() <= 12) {
                return true; // Input date is in valid format and month value is valid
            } else {
                return false; // Input date is in valid format but month value is invalid
            }
        } catch (DateTimeException e) {
            return false; // Input date is not in valid format
        }
    }

    public boolean isSalaryNotValid(Long salary) {
        return salary < 0;
    }


    public boolean isNotValidEmployee(EmployeeRequest employee) {
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
    public boolean isNotValidUSerDetails(UserDetails userDetails) {
        return userDetails == null ||
                userDetails.getUsername() == null ||
                userDetails.getUsername().isEmpty() ||
                !userDetails.getUsername().endsWith("@acme.com") ||

                userDetails.getPassword() == null ||
                userDetails.getPassword().isEmpty();

    }
    public Employee findByEmail(String email) {
        Optional<Employee> optionalEmployee = employeeRepository.findByEmailIgnoreCase(email);
        return optionalEmployee.orElse(null);
    }

    private boolean isEmailRegistered(String email) {
        return employeeRepository.existsByEmailIgnoreCase(email);
    }

    private Map<String, Object> createErrorResponse(HttpStatus status, String path) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", new Date());
        errorResponse.put("status", status.value());
        errorResponse.put("error", status.getReasonPhrase());
        errorResponse.put("path", path);
        return errorResponse;
    }


    private Map<String, Object> createErrorForRegisteredUser(HttpStatus status, String message, String path) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", new Date());
        errorResponse.put("status", status.value());
        errorResponse.put("error", status.getReasonPhrase());
        errorResponse.put("message", message);
        errorResponse.put("path", path);
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
    public boolean isPasswordBreached(String password) {
        List<String> breachedPassword = new ArrayList<>(List.of("PasswordForJanuary", "PasswordForFebruary",
                "PasswordForMarch", "PasswordForApril",
                "PasswordForMay", "PasswordForJune", "PasswordForJuly", "PasswordForAugust",
                "PasswordForSeptember", "PasswordForOctober", "PasswordForNovember", "PasswordForDecember"));
        return breachedPassword.contains(password);
    }
    public boolean isLengthNotMin(String password) {
        return password == null ||
                password.length() < 12;
    }

    public String convertToNameOfMonthFormat(String inputDate) {
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("MM-yyyy");
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("MMMM-yyyy");
        YearMonth yearMonth = YearMonth.parse(inputDate, inputFormatter);
        LocalDate localDate = yearMonth.atDay(1); // Get the first day of the month
        return localDate.format(outputFormatter);
    }

    public String convertToFormattedString(long salary) {
        long dollars = salary / 100; // Extract dollars portion
        long cents = salary % 100;   // Extract cents portion

        String dollarsText = dollars + " dollar(s)";
        String centsText = cents + " cent(s)";

        // Combine dollars and cents portions
        String formattedSalary = dollarsText + " " + centsText;
        return formattedSalary;
    }



    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    public ResponseEntity<?> deleteUser(String email, UserDetails userDetails) {
        System.out.println("YES I am inside DELETE USER METHOD for request /api/admin/user/petrpetrov@acme.com");
        if (!employeeRepository.existsByEmailIgnoreCase(email)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorForRegisteredUser(HttpStatus.NOT_FOUND,
                            "User not found!", "/api/admin/user/" + email ));
        }
        Optional<Employee> employeeOptional = employeeRepository.findByEmailIgnoreCase(email);
        Employee employee = employeeOptional.orElse(new Employee());
        System.out.println(employee.toString());
        System.out.println(isUserAdmin(employee));
        if (isUserAdmin(employee)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorForRegisteredUser(HttpStatus.BAD_REQUEST,
                            "Can't remove ADMINISTRATOR role!", "/api/admin/user/" + email));

        }
        employeeRepository.delete(employee);
        return ResponseEntity.ok(new DeleteResponse(email, "Deleted successfully!"));
    }

    public ResponseEntity<?> getUser(UserDetails userDetails) {
        System.out.println("Inside Employee Service");
        String email = userDetails.getUsername();
        System.out.println(email);
        Optional<Employee> employeeOptional = employeeRepository.findByEmailIgnoreCase(email);
        Employee employee = employeeOptional.orElse(new Employee());
        System.out.println(isUserAdmin(employee));
        System.out.println("isUserAdmin(employee)");
        if (!isUserAdmin(employee)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(createErrorForRegisteredUser(HttpStatus.FORBIDDEN,
                            "Access Denied!", "api/admin/user/"));

        }
        List<Employee> employees = employeeRepository.findAllByOrderByIdAsc();
        List<EmployeeDTO> employeeDTOs = employees.stream()
                .map(EmployeeDTO::new) // Assuming EmployeeDTO constructor takes Employee as parameter
                .collect(Collectors.toList());
        return ResponseEntity.ok(employeeDTOs);
    }

    public boolean isRoleAdmin(String role) {
            return role.equals("ADMINISTRATOR");
    }

    public boolean isUserAdmin(Employee employee) {
        Set<Role> roles = employee.getRoles();
        System.out.println(roles);
        System.out.println("chceking roles ");
        for (Role role : roles) {
            System.out.println(role);
            System.out.println(role.getName());
            System.out.println("NAME OF ROLES");
            if (role.getName().equals("ADMINISTRATOR")) {
                return true;
            }
        }
        return false;
    }
}
