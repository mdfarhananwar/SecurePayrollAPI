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
        Employee employee = employeeService.findByEmail(username);

        if (employee == null) {
            throw new UsernameNotFoundException("Not found: " + username);
        }
        String roleWithPrefix = "ROLE_" + employee.getRole().getName();
        List<SimpleGrantedAuthority> grantedAuthorities = Collections.singletonList(new SimpleGrantedAuthority(roleWithPrefix));
        return new org.springframework.security.core.userdetails.User(employee.getEmail(), employee.getPassword(), grantedAuthorities);
    }
}
