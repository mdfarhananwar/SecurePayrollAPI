package account.repository;

import account.model.Employee;
import account.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service("userDetailsService")
public class UserDetailsServiceImp implements UserDetailsService {

    private final EmployeeService employeeService;
    @Autowired
    public UserDetailsServiceImp(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println(username);
        System.out.println("Enter");
        Employee employee = employeeService.findByEmail(username.toLowerCase());
        System.out.println(employee.toString());
        if (employee == null) {
            throw new UsernameNotFoundException("Not found: " + username);
        }
        String roleWithPrefix = "ROLE_" + employee.getRole().getName();
        List<SimpleGrantedAuthority> grantedAuthorities = Collections.singletonList(new SimpleGrantedAuthority(roleWithPrefix));
        UserDetails userDetails =  new org.springframework.security.core.userdetails.User(employee.getEmail().toLowerCase(), employee.getPassword(), grantedAuthorities);
        System.out.println(userDetails.getUsername());
        System.out.println(userDetails.getPassword());
        System.out.println("userdetails");
        return userDetails;
    }
}
