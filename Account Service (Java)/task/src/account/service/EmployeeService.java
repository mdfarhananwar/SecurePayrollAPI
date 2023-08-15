package account.service;

import account.model.*;
import account.repository.EmployeeRepository;
import account.repository.EventRepository;
import account.repository.GroupRepository;
import account.repository.RoleRepository;
import account.util.EmployeeUtils;
import account.util.ErrorResponseFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EmployeeService {
    public static final int MAX_FAILED_ATTEMPTS = 5;
    private final EmployeeRepository employeeRepository;
    private final GroupRepository groupRepository;
    private final EventRepository eventRepository;
    RoleRepository roleRepository;

    @Autowired
    public EmployeeService(EmployeeRepository employeeRepository, RoleRepository roleRepository, GroupRepository groupRepository, EventRepository eventRepository) {
        this.employeeRepository = employeeRepository;
        this.roleRepository = roleRepository;
        this.groupRepository = groupRepository;
        this.eventRepository = eventRepository;
    }

    // Handlers
    // Access and User Management Handlers
    private ResponseEntity<?> handleAccessDenied(String path) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErrorResponseFactory.createErrorForRegisteredUser(HttpStatus.FORBIDDEN,
                        "Access Denied!", path));
    }

    private ResponseEntity<?> handleUnauthorizedEmployee() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponseFactory.createUnauthorizedResponse());
    }

    private ResponseEntity<?> handleUserOrRoleNotFoundError(String email, String role) {
        if (!isEmailRegistered(email)) {
            return handleUserNotFound("/api/admin/user/role");
        }
        String message = "The user does not have a role!";
        String path = "/api/admin/user/role";
        return handleRoleNotFound(HttpStatus.NOT_FOUND, message, path);
    }

    private ResponseEntity<?> handleUserNotFound(String path) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponseFactory.createErrorForRegisteredUser(HttpStatus.NOT_FOUND,
                        "User not found!", path));
    }

    private ResponseEntity<?> handleUserMinimumRolesViolation() {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponseFactory.createErrorForRegisteredUser(HttpStatus.BAD_REQUEST,
                        "The user must have at least one role!",
                        "/api/admin/user/role"));
    }

    private ResponseEntity<?> handleAdministratorLock() {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponseFactory.createErrorForRegisteredUser(HttpStatus.BAD_REQUEST,
                        "Can't lock the ADMINISTRATOR!",
                        "/api/admin/user/access"));
    }

    // Role Management Handlers
    private ResponseEntity<?> handleRoleNotFound(HttpStatus status, String errorMessage, String path) {
        return ResponseEntity.status(status)
                .body(ErrorResponseFactory.createErrorForRegisteredUser(status, errorMessage, path));
    }

    private ResponseEntity<?> handleInvalidRoleCombination() {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponseFactory.createErrorForRegisteredUser(HttpStatus.BAD_REQUEST,
                        "The user cannot combine administrative and business roles!",
                        "/api/admin/user/role"));
    }

    private ResponseEntity<?> handleAdministratorRoleRemoval(String path) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponseFactory.createErrorForRegisteredUser(HttpStatus.BAD_REQUEST,
                        "Can't remove ADMINISTRATOR role!",
                        path));
    }

    private ResponseEntity<?> handleRoleGrant(Employee employee, Role updateRole, String userEmail) {
        employee.getRoles().add(updateRole);
        employeeRepository.save(employee);
        String object = "Grant role " + updateRole.getName() + " to " + employee.getEmail().toLowerCase();
        saveEvent("GRANT_ROLE", userEmail, object, "/api/admin/user/role");
        return ResponseEntity.ok(new EmployeeDTO(employee));
    }

    private ResponseEntity<?> handleRoleRemove(Employee employee, Role updateRole, String userEmail) {
        if (!employee.getRoles().contains(updateRole)) {
            String message = "Role not found!";
            String path = "/api/admin/user/role";
            return handleRoleNotFound(HttpStatus.NOT_FOUND, message, path);
        }

        if (employee.getRoles().size() == 1) {
            return handleUserMinimumRolesViolation();
        }

        employee.getRoles().remove(updateRole);
        employeeRepository.save(employee);

        String object = "Remove role " + updateRole.getName() + " from " + employee.getEmail().toLowerCase();
        saveEvent("REMOVE_ROLE", userEmail, object, "/api/admin/user/role");

        return ResponseEntity.ok(new EmployeeDTO(employee));
    }


    // Password and Security Handlers
    private ResponseEntity<?> handleSamePasswords() {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponseFactory.createErrorForRegisteredUser(HttpStatus.BAD_REQUEST,
                        "The new password must be different from the current password!",
                        "/api/auth/changepass"));
    }

    private ResponseEntity<?> handleInvalidEmployee() {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponseFactory.createErrorResponse());
    }

    private ResponseEntity<?> handleEmailAlreadyRegistered() {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponseFactory.createErrorForRegisteredUser(HttpStatus.BAD_REQUEST,
                        "User already exists!", "/api/auth/signup"));
    }

    private ResponseEntity<?> handlePasswordTooShort(String message, String path) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponseFactory.createErrorForRegisteredUser(HttpStatus.BAD_REQUEST,
                        message, path));
    }

    private ResponseEntity<?> handleBreachedPassword(String message, String path) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponseFactory.createErrorForRegisteredUser(HttpStatus.BAD_REQUEST,
                        message, path));
    }

    //Rest Methods
    // Employee Registration and Authentication Methods
    // Allows the user to register on the service and assign roles;
    public ResponseEntity<?> registerEmployee(EmployeeRequest employee) {
        if (EmployeeUtils.isNotValidEmployee(employee)) {
            return handleInvalidEmployee();
        }
        if (isEmailRegistered(employee.getEmail())) {
            return handleEmailAlreadyRegistered();
        }
        if (EmployeeUtils.isLengthNotMin(employee.getPassword())) {
            String message = "The password length must be at least 12 characters!";
            String path = "/api/auth/signup";
            return handlePasswordTooShort(message, path);
        }
        if (EmployeeUtils.isPasswordBreached(employee.getPassword())) {
            String message = "The password is in the hacker's database!";
            String path = "/api/auth/signup";
            return handleBreachedPassword(message, path);
        }
        Employee saveEmployee = new Employee(employee.getName(), employee.getLastname(),
                employee.getEmail(),passwordEncoder().encode(employee.getPassword()) );

        // Set the user account as unlocked since it's their first-time registration
        saveEmployee.setAccountNonLocked(true);

        //Assigning roles and updating the user with the help of helper method
        saveEmployee = assignRoles(saveEmployee);

        String userEmail = employee.getEmail().toLowerCase();
        String path = "/api/auth/signup";

        //Capturing the event and saving it in the database
        saveEvent( "CREATE_USER", "Anonymous", userEmail, path);

        //Creating ResponseBody as per requirement
        EmployeeDTO employeeDTO = new EmployeeDTO(saveEmployee);
        return ResponseEntity.ok(employeeDTO);
    }

    // Helper Method of registerEmployee
    // Assigns roles to employees during registration based on the count of existing users.
    private Employee assignRoles(Employee employee) {
        Group administrativeGroup = groupRepository.findByName("Administrative");
        Group businessGroup = groupRepository.findByName("Business");
        Role userRole = roleRepository.findByName("USER");
        Role adminRole = roleRepository.findByName("ADMINISTRATOR");

        //First user should be Admin
        if (employeeRepository.count() == 0) {
            employee.setGroup(administrativeGroup);
            employee.getRoles().add(adminRole);
        } else {
            employee.setGroup(businessGroup);
            employee.getRoles().add(userRole);
        }
        return employeeRepository.save(employee);
    }

    // Allows user to change their password
    public ResponseEntity<?> changePassword(PasswordRequest passwordRequest,
                                            UserDetails userDetails) {
        String newPassword = passwordRequest.getNew_password();
        String email = userDetails.getUsername();

        // Checking characters, if it is less than minimum
        if (EmployeeUtils.isLengthNotMin(newPassword)) {
            String message = "Password length must be 12 chars minimum!";
            String path = "/api/auth/changepass";
            return handlePasswordTooShort(message, path);
        }
        Employee employee = findByEmail(email);
        if (employee == null) {
            return handleUnauthorizedEmployee();
        }

        // Check if the newly requested password matches the user's previous password
        if (passwordEncoder().matches(newPassword, employee.getPassword())) {
            return handleSamePasswords();
        }

        // Check if the new password is commonly used or found in the hacker's database
        if (EmployeeUtils.isPasswordBreached(newPassword)) {
            String message = "The password is in the hacker's database!";
            String path = "/api/auth/changepass";
            return handleBreachedPassword(message, path);
        }

        //Updating the password and storing in database
        employee.setPassword(passwordEncoder().encode(newPassword));
        employeeRepository.save(employee);
        String userEmail = employee.getEmail().toLowerCase();

        //capturing event of Password Change
        saveEvent("CHANGE_PASSWORD", userEmail, userEmail, "/api/auth/changepass");
        PasswordSuccessResponse passwordSuccessResponse = new PasswordSuccessResponse(email,
                "The password has been updated successfully");
        return ResponseEntity.ok(passwordSuccessResponse);
    }

    //Employee Operations Methods
    public ResponseEntity<?> deleteUser(String email, UserDetails userDetails) {
        String userEmail = userDetails.getUsername();

        // Checks if user is registered
        if (!employeeRepository.existsByEmailIgnoreCase(email)) {
            String path = "/api/admin/user/" + email;
            return handleUserNotFound(path);
        }

        Employee employee = findByEmail(email);

        //Checks if user has role of Admin
        if (EmployeeUtils.isUserAdmin(employee)) {
            String path = "/api/admin/user/" + email;
            return handleAdministratorRoleRemoval(path);
        }

        // Capturing Event of Deleting USer
        saveEvent("DELETE_USER", userEmail, employee.getEmail().toLowerCase(), "/api/admin/user/role");
        employeeRepository.delete(employee);
        return ResponseEntity.ok(new DeleteResponse(email, "Deleted successfully!"));
    }

    public ResponseEntity<?> getUser(UserDetails userDetails) {
        String email = userDetails.getUsername();
        Employee employee = findByEmail(email);
        if (!EmployeeUtils.isUserAdmin(employee)) {
            String path = "api/admin/user/";
            return handleAccessDenied(path);
        }
        List<Employee> employees = employeeRepository.findAllByOrderByIdAsc();
        List<EmployeeDTO> employeeDTOs = employees.stream()
                .map(EmployeeDTO::new) // EmployeeDTO constructor takes Employee as parameter
                .collect(Collectors.toList());
        return ResponseEntity.ok(employeeDTOs);
    }

    public ResponseEntity<?> lockUnlockUser(LockUnlockRequest lockUnlockRequest, UserDetails userDetails) {
        String userEmail = userDetails.getUsername();
        LockUnlockEnum operation = lockUnlockRequest.getOperation();
        String email = lockUnlockRequest.getUser();
        Employee employee = findByEmail(email);
        if (EmployeeUtils.isUserAdmin(employee)) {
            return handleAdministratorLock();
        }
        String status;
        if (operation == LockUnlockEnum.LOCK) {
            status = "User " + email.toLowerCase() + " locked!";

            // The User is set to Lock and updated
            employee.setAccountNonLocked(false);
            employeeRepository.save(employee);

            String object = "Lock user " + employee.getEmail().toLowerCase();

            //capturing Event of Locking a User
            saveEvent("LOCK_USER", userEmail, object, "/api/admin/user/role");
        } else {
            status = "User " + email.toLowerCase() + " unlocked!";
            String object = "Unlock user " + employee.getEmail().toLowerCase();
            // User is set To UnLock and updated
            employee.setAccountNonLocked(true);

            // User Login Attempts Count is reset to 0, as it is unlocked
            employee.setFailedAttempt(0);
            employeeRepository.save(employee);

            // Capturing Event of Unlocking a User
            saveEvent("UNLOCK_USER", userEmail, object, "/api/admin/user/role");
        }
        return ResponseEntity.ok(new StatusResponse(status));
    }

    // Allows Admin to change or update roles
    public ResponseEntity<?> updateRole(RoleRequest roleRequest, UserDetails userDetails) {
        String userEmail = userDetails.getUsername();
        Employee employeeUser = findByEmail(userEmail);

        // Only administrators have permission to change user roles
        if (!EmployeeUtils.isUserAdmin(employeeUser)) {
            String path = "/api/admin/user/role";
            return handleAccessDenied(path);
        }
        assert roleRequest != null;
        String email = roleRequest.getUser();
        Operation operation = roleRequest.getOperation();
        String role = roleRequest.getRole();

        // Check if the user is not registered or if the requested role is not valid
        if (!isEmailRegistered(email) || !isRoleValid(role)) {
            return handleUserOrRoleNotFoundError(email, role);
        }

        Employee employee = findByEmail(email);

        // Prevent administrators from changing their own role
        if (EmployeeUtils.isUserAdmin(employee)) {
            if (operation == Operation.GRANT) {
                return handleInvalidRoleCombination();
            } else {
                //Prevent administrators from removing their own ADMINISTRATOR role!
                return handleAdministratorRoleRemoval("/api/admin/user/role");
            }
        } else {
            // Prevent a general user from having Admin role
            if (EmployeeUtils.isRoleAdmin(role))
                return handleInvalidRoleCombination();
        }
        Role updateRole = roleRepository.findByName(role);

        // Update New Role to User, Handle if any error occurs
        // Capture the Event of Updating new role to user
        if (operation == Operation.GRANT) {
            return handleRoleGrant(employee, updateRole, userEmail);
        }

        // Remove the requested Role from the User, Handle if any error occurs
        //capture the event of Deleting role from the user
        if (operation == Operation.REMOVE) {
            return handleRoleRemove(employee, updateRole, userEmail);
        }

        // Create A Response Body as per requirements
        EmployeeDTO employeeDTO = new EmployeeDTO(employee);
        return ResponseEntity.ok(employeeDTO);
    }


    // Event Logging and Retrieval
    /**
     * Methods related to logging and retrieving events capturing user actions, such as
     * registration, deletion, locking, unlocking, etc. These methods allow for event
     * data retrieval and the saving of new events into the event log.
     */
    public ResponseEntity<?> getEvents() {
        List<Event> eventList = eventRepository.findAllByOrderByIdAsc();
        if (eventList.isEmpty()) {
            return ResponseEntity.ok(Collections.EMPTY_LIST);
        }
        return ResponseEntity.ok(eventList);
    }
    private void saveEvent(String action, String subject, String object, String path) {
        Event event = new Event(Calendar.getInstance().getTime(), action, subject, object, path);
        eventRepository.save(event);
    }

    public void saveEvent(Event event) {
        eventRepository.save(event);
    }

    // User Login Attempt Tracking
    /**
     * Methods for tracking and managing the number of login attempts made by a user.
     * These methods provide functionality to increase the count of failed attempts
     * and reset the count upon successful login.
     * and lock a user account if the number of failed login attempts exceeds a predefined limit.
     */
    @Transactional
    public void increaseFailedAttempts(Employee user) {
        int newFailAttempts = user.getFailedAttempt() + 1;
        employeeRepository.updateFailedAttempts(newFailAttempts, user.getEmail());
    }

    @Transactional
    public void resetFailedAttempts(String email) {
        employeeRepository.updateFailedAttempts(0, email);
    }

    /**
     * Locks a user account if the number of failed login attempts exceeds the limit = 5.
     * Once locked, the user cannot log in until the account is unlocked.
     *
     * @param user The user account to lock.
     */
    @Transactional
    public void lock(Employee user) {
        user.setAccountNonLocked(false);
        employeeRepository.save(user);
    }

    // User Account Management - Email Operations
    // Methods for finding users by email and checking registration status.
    public Employee findByEmail(String email) {
        Optional<Employee> optionalEmployee = employeeRepository.findByEmailIgnoreCase(email);
        return optionalEmployee.orElse(null);
    }

    // Methods for checking valid emails and role.
    private boolean isEmailRegistered(String email) {
        return employeeRepository.existsByEmailIgnoreCase(email);
    }

    private boolean isRoleValid(String role) {
        return roleRepository.existsByName(role);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
