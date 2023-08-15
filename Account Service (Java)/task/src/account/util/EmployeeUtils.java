package account.util;

import account.model.Employee;
import account.model.EmployeeRequest;
import account.model.Role;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class EmployeeUtils {

    public static boolean isNotValidEmployee(EmployeeRequest employee) {
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

    public static boolean isNotValidUserDetails(UserDetails userDetails) {
        return userDetails == null ||
                userDetails.getUsername() == null ||
                userDetails.getUsername().isEmpty() ||
                !userDetails.getUsername().endsWith("@acme.com") ||
                userDetails.getPassword() == null ||
                userDetails.getPassword().isEmpty();
    }

    public static boolean isPasswordBreached(String password) {
        List<String> breachedPassword = new ArrayList<>(List.of(
                "PasswordForJanuary", "PasswordForFebruary", "PasswordForMarch",
                "PasswordForApril", "PasswordForMay", "PasswordForJune",
                "PasswordForJuly", "PasswordForAugust", "PasswordForSeptember",
                "PasswordForOctober", "PasswordForNovember", "PasswordForDecember"
        ));
        return breachedPassword.contains(password);
    }

    public static boolean isLengthNotMin(String password) {
        return password == null || password.length() < 12;
    }

    public static String convertToFormattedString(long salary) {
        long dollars = salary / 100;
        long cents = salary % 100;

        String dollarsText = dollars + " dollar(s)";
        String centsText = cents + " cent(s)";

        return dollarsText + " " + centsText;
    }

    public static boolean isRoleAdmin(String role) {
        return role.equals("ADMINISTRATOR");
    }

    public static boolean isUserAdmin(Employee employee) {
        Set<Role> roles = employee.getRoles();
        for (Role role : roles) {
            if (role.getName().equals("ADMINISTRATOR")) {
                return true;
            }
        }
        return false;
    }
    public static boolean isSalaryNotValid(Long salary) {
        return salary < 0;
    }


    // Other methods from the original EmployeeUtils class
}
