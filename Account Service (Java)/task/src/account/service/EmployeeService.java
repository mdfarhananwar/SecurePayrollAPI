package account.service;

import account.model.*;
import account.repository.*;
import account.util.DateUtils;
import account.util.EmployeeUtils;
import account.util.ErrorResponseFactory;
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

import java.util.*;
import java.util.stream.Collectors;

@Service
public class EmployeeService {
    public static final int MAX_FAILED_ATTEMPTS = 5;
    private final EmployeeRepository employeeRepository;
    RoleRepository roleRepository;
    private final PayrollRepository payrollRepository;
    private final GroupRepository groupRepository;
    private final EventRepository eventRepository;


    @Autowired
    public EmployeeService(EmployeeRepository employeeRepository, RoleRepository roleRepository, PayrollRepository payrollRepository, GroupRepository groupRepository, EventRepository eventRepository) {
        this.employeeRepository = employeeRepository;
        this.roleRepository = roleRepository;
        this.payrollRepository = payrollRepository;
        this.groupRepository = groupRepository;
        this.eventRepository = eventRepository;
    }



    public ResponseEntity<?> registerEmployee(EmployeeRequest employee) {
        System.out.println(employee.toString());
        System.out.println(employee.getPassword());
        System.out.println("Enter");
        if (EmployeeUtils.isNotValidEmployee(employee)) {
            System.out.println("EnterISNOTVALID");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponseFactory.createErrorResponse());
        }
        if (isEmailRegistered(employee.getEmail())) {
            System.out.println("EnterISEmailRegistered");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponseFactory.createErrorForRegisteredUser(HttpStatus.BAD_REQUEST,
                            "User exist!", "/api/auth/signup"));
        }
        if (EmployeeUtils.isLengthNotMin(employee.getPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponseFactory.createErrorForRegisteredUser(HttpStatus.BAD_REQUEST,
                            "The password length" +
                            " must be at least 12 chars!", "/api/auth/signup"));
        }
        if (EmployeeUtils.isPasswordBreached(employee.getPassword())) {
            System.out.println("PasswordBreach");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponseFactory.createErrorForRegisteredUser(HttpStatus.BAD_REQUEST,
                            "The password is in the hacker's database!",
                            "/api/auth/signup"));
        }
        System.out.println("Before Password ENcoder");
        System.out.println(employee.getPassword());
        //Optional<Employee> byEmailIgnoreCase = employeeRepository.findByEmailIgnoreCase(employee.getEmail());

        Employee saveEmployee = new Employee();
        saveEmployee.setEmail(employee.getEmail().toLowerCase());
        saveEmployee.setPassword(passwordEncoder().encode(employee.getPassword()));
        saveEmployee.setName(employee.getName());
        saveEmployee.setLastname(employee.getLastname());
        saveEmployee.setAccountNonLocked(true);
        saveEmployee = assignRoles(saveEmployee);
        System.out.println(saveEmployee.getPassword());
        System.out.println("getting password from Employee After password Encode");
        Event event = new Event( Calendar.getInstance().getTime(),"CREATE_USER",
                "Anonymous" ,employee.getEmail().toLowerCase() ,"/api/auth/signup");
        eventRepository.save(event);

        EmployeeDTO employeeDTO = new EmployeeDTO(saveEmployee);
        return ResponseEntity.ok(employeeDTO);
        //return ResponseEntity.ok(employeeDTO);
    }

    public ResponseEntity<?> getEmployee(@AuthenticationPrincipal UserDetails userDetails) {
        System.out.println("Enter");
        System.out.println(userDetails.getUsername());
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

    public ResponseEntity<?> changePassword(PasswordRequest passwordRequest,
                                            UserDetails userDetails) {
        String newPassword = passwordRequest.getNew_password();
        System.out.println("newPassword");
        System.out.println(newPassword);
        System.out.println("newPassword");
        String email = userDetails.getUsername();

        if (EmployeeUtils.isLengthNotMin(newPassword)) {
            System.out.println("NewPassword problem");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponseFactory.createErrorForRegisteredUser(HttpStatus.BAD_REQUEST,
                            "Password length must be 12 chars minimum!",
                            "/api/auth/changepass"));
        }
        Employee employee =  findByEmail(email);
        if (employee == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponseFactory.createUnauthorizedResponse());
        }

        if (passwordEncoder().matches(newPassword, employee.getPassword())) {
            System.out.println("Both Passwords are same");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponseFactory.createErrorForRegisteredUser(HttpStatus.BAD_REQUEST,
                            "The passwords must be different!",
                            "/api/auth/changepass"));
        }

        if (EmployeeUtils.isPasswordBreached(newPassword)) {
            System.out.println("PasswordBreach");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponseFactory.createErrorForRegisteredUser(HttpStatus.BAD_REQUEST,
                            "The password is in the hacker's database!",
                            "/api/auth/changepass"));
        }
        System.out.println("Successfull");
        System.out.println("Before Password Change : - " + employee.getPassword());
        employee.setPassword(passwordEncoder().encode(newPassword));
        System.out.println("After Password Change : - " + employee.getPassword());
        employeeRepository.save(employee);
        Event event = new Event( Calendar.getInstance().getTime(),"CHANGE_PASSWORD",
                employee.getEmail().toLowerCase(), employee.getEmail().toLowerCase(),"/api/auth/changepass");
        eventRepository.save(event);

        PasswordSuccessResponse passwordSuccessResponse = new PasswordSuccessResponse(email,
                "The password has been updated successfully");
        return ResponseEntity.ok(passwordSuccessResponse);

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

        if (!EmployeeUtils.isUserAdmin(employeeUser)) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("timestamp", new Date());
            errorResponse.put("status", HttpStatus.FORBIDDEN.value());
            errorResponse.put("error", HttpStatus.FORBIDDEN.getReasonPhrase());
            errorResponse.put("message", "Access Denied!");
            errorResponse.put("path", "/api/admin/user/role");

            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        }
        if (roleRequest != null) {
            System.out.println(roleRequest);
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
                    .body(ErrorResponseFactory.createErrorForRegisteredUser(HttpStatus.NOT_FOUND,
                            "User not found!", "/api/admin/user/role"));
        }
        if (!roleRepository.existsByName(role)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponseFactory.createErrorForRegisteredUser(HttpStatus.NOT_FOUND,
                            "Role not found!", "/api/admin/user/role"));
        }

//        Employee employee = findByEmail(email);
        Optional<Employee> employeeOptional = employeeRepository.findByEmailIgnoreCase(email);
        Employee employee = employeeOptional.orElse(new Employee());
        if (EmployeeUtils.isUserAdmin(employee)) {
            if (operation == Operation.GRANT) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ErrorResponseFactory.createErrorForRegisteredUser(HttpStatus.BAD_REQUEST,
                                "The user cannot combine administrative and business roles!",
                                "/api/admin/user/role"));
            } else {
              //"Can't remove ADMINISTRATOR role!"
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ErrorResponseFactory.createErrorForRegisteredUser(HttpStatus.BAD_REQUEST,
                                "Can't remove ADMINISTRATOR role!" ,
                                "/api/admin/user/role"));
            }

        } else {
          if (EmployeeUtils.isRoleAdmin(role))
              return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponseFactory.createErrorForRegisteredUser(HttpStatus.BAD_REQUEST,
                            "The user cannot combine administrative and business roles!",
                            "/api/admin/user/role"));
        }
        Role updateRole = roleRepository.findByName(role);
        System.out.println("updateROle");
        System.out.println(updateRole);
        System.out.println("updateROle");
        System.out.println(employee.getRoles());
        if (operation == Operation.GRANT) {
            employee.getRoles().add(updateRole);
            employeeRepository.save(employee);
            String object = "Grant role " + updateRole.getName() + " to " + employee.getEmail().toLowerCase();
            Event event = new Event( Calendar.getInstance().getTime(),"GRANT_ROLE",
                    userEmail, object,"/api/admin/user/role");
            eventRepository.save(event);

        }
        if (operation == Operation.REMOVE) {
            if (!employee.getRoles().contains(updateRole)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ErrorResponseFactory.createErrorForRegisteredUser(HttpStatus.BAD_REQUEST,
                                "The user does not have a role!",
                                "/api/admin/user/role"));
            }
            if (employee.getRoles().size() == 1) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(ErrorResponseFactory.createErrorForRegisteredUser(HttpStatus.BAD_REQUEST,
                                    "The user must have at least one role!",
                                    "/api/admin/user/role"));
            }

            employee.getRoles().remove(updateRole);
            employeeRepository.save(employee);
            System.out.println(employee);
            //Remove role ACCOUNTANT from petrpetrov@acme.com
            String object = "Remove role " + updateRole.getName() + " from " + employee.getEmail().toLowerCase();
            Event event = new Event( Calendar.getInstance().getTime(),"REMOVE_ROLE",
                    userEmail, object,"/api/admin/user/role");
            eventRepository.save(event);
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

    private boolean isEmailRegistered(String email) {
        return employeeRepository.existsByEmailIgnoreCase(email);
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    public ResponseEntity<?> deleteUser(String email, UserDetails userDetails) {
        String userEmail = userDetails.getUsername();
        System.out.println("YES I am inside DELETE USER METHOD for request /api/admin/user/petrpetrov@acme.com");
        if (!employeeRepository.existsByEmailIgnoreCase(email)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponseFactory.createErrorForRegisteredUser(HttpStatus.NOT_FOUND,
                            "User not found!", "/api/admin/user/" + email ));
        }
        Optional<Employee> employeeOptional = employeeRepository.findByEmailIgnoreCase(email);
        Employee employee = employeeOptional.orElse(new Employee());
        if (EmployeeUtils.isUserAdmin(employee)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponseFactory.createErrorForRegisteredUser(HttpStatus.BAD_REQUEST,
                            "Can't remove ADMINISTRATOR role!", "/api/admin/user/" + email));

        }
        Event event = new Event( Calendar.getInstance().getTime(),"DELETE_USER",
                userEmail, employee.getEmail().toLowerCase(),"/api/admin/user/role");
        eventRepository.save(event);
        employeeRepository.delete(employee);
        return ResponseEntity.ok(new DeleteResponse(email, "Deleted successfully!"));
    }

    public ResponseEntity<?> getUser(UserDetails userDetails) {
        System.out.println("Inside Employee Service");
        String email = userDetails.getUsername();
        System.out.println(email);
        Optional<Employee> employeeOptional = employeeRepository.findByEmailIgnoreCase(email);
        Employee employee = employeeOptional.orElse(new Employee());

        if (!EmployeeUtils.isUserAdmin(employee)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponseFactory.createErrorForRegisteredUser(HttpStatus.FORBIDDEN,
                            "Access Denied!", "api/admin/user/"));

        }
        List<Employee> employees = employeeRepository.findAllByOrderByIdAsc();
        List<EmployeeDTO> employeeDTOs = employees.stream()
                .map(EmployeeDTO::new) // Assuming EmployeeDTO constructor takes Employee as parameter
                .collect(Collectors.toList());
        return ResponseEntity.ok(employeeDTOs);
    }

    public ResponseEntity<?> lockUnlockUser(LockUnlockRequest lockUnlockRequest, UserDetails userDetails) {
        String userEmail = userDetails.getUsername();
        LockUnlockEnum operation = lockUnlockRequest.getOperation();
        String email = lockUnlockRequest.getUser();
        if (!employeeRepository.existsByEmailIgnoreCase(email)) {
            //
            System.out.println("The user doesnt exist");
        }
        Optional<Employee> byEmailIgnoreCase = employeeRepository.findByEmailIgnoreCase(email);
        Employee employee = byEmailIgnoreCase.orElse(new Employee());
        if (EmployeeUtils.isUserAdmin(employee)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponseFactory.createErrorForRegisteredUser(HttpStatus.BAD_REQUEST,
                            "Can't lock the ADMINISTRATOR!",
                            "/api/admin/user/access"));
        }
        String status;
        if (operation == LockUnlockEnum.LOCK) {
            status = "User " + email.toLowerCase() + " locked!";
            employee.setAccountNonLocked(false);
            employeeRepository.save(employee);
            String object = "Lock user " + employee.getEmail().toLowerCase();
            Event event = new Event( Calendar.getInstance().getTime(),"LOCK_USER",
                    userEmail,object,"/api/admin/user/role");
            eventRepository.save(event);
        } else {
            status = "User " + email.toLowerCase() + " unlocked!";
            String object = "Unlock user " + employee.getEmail().toLowerCase();
            employee.setAccountNonLocked(true);
            employee.setFailedAttempt(0);
            employeeRepository.save(employee);
            Event event = new Event( Calendar.getInstance().getTime(),"UNLOCK_USER",
                    userEmail,object,"/api/admin/user/role");
            eventRepository.save(event);

        }
        return ResponseEntity.ok(new StatusResponse(status));

    }
    @Transactional
    public void increaseFailedAttempts(Employee user) {
        int newFailAttempts = user.getFailedAttempt() + 1;
        employeeRepository.updateFailedAttempts(newFailAttempts, user.getEmail());
    }
    @Transactional
    public void resetFailedAttempts(String email) {
        employeeRepository.updateFailedAttempts(0, email);
    }
    @Transactional
    public void lock(Employee user) {
        user.setAccountNonLocked(false);
        employeeRepository.save(user);
    }

    public ResponseEntity<?> getEvents() {

        System.out.println("Inside getEvents method :- api/security/events");
        List<Event> eventList = eventRepository.findAllByOrderByIdAsc();
        if (eventList.isEmpty()) {
            return ResponseEntity.ok(Collections.EMPTY_LIST);
        }
        return ResponseEntity.ok(eventList);
    }
    public void saveEvent(Event event) {
        eventRepository.save(event);
    }

}
