package account.repository;

import account.config.UserNotFoundException;
import account.model.Employee;
import account.model.Event;
import account.model.Privilege;
import account.model.Role;
import account.service.EmployeeService;
import account.service.LoginAttemptService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

@Service("userDetailsService")

public class UserDetailsServiceImp implements UserDetailsService {
    private final Logger logger = LoggerFactory.getLogger(UserDetailsServiceImp.class);

    private final EmployeeService employeeService;
    @Autowired
    public UserDetailsServiceImp(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }
    @Autowired
    private LoginAttemptService loginAttemptService;

    @Autowired
    private HttpServletRequest request;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//        if (loginAttemptService.isBlocked()) {
//            throw new RuntimeException("blocked");
//        }
        System.out.println(username);
        System.out.println("Enter");
        logger.debug("Entering loadUserByUsername method");
        logger.debug("Username: {}", username);
        Employee employee = employeeService.findByEmail(username.toLowerCase());
        System.out.println("password from employee");
        String path = request.getRequestURI();


        if (employee == null) {
            throw new UserNotFoundException("User not found: " + username, username, path);
        }
        if (!employee.isAccountNonLocked()) {
            throw new BadCredentialsException("User account is locked");
        }

        Set<Role> roles = employee.getRoles();
        List<String> roleNamesWithPrefix = roles.stream()
                .map(role -> "ROLE_" + role.getName())
                .collect(Collectors.toList());

        List<SimpleGrantedAuthority> grantedAuthorities = roleNamesWithPrefix.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        System.out.println(grantedAuthorities);
        System.out.println("GRANTED AUTHORITIES");

        System.out.println("roles original");
        System.out.println("roles original");

        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                employee.getEmail().toLowerCase(),
                employee.getPassword(),
                grantedAuthorities);


        logger.debug("UserDetails created - Username: {}", userDetails.getUsername());
        logger.trace("UserDetails password: {}", userDetails.getPassword());

        System.out.println(userDetails.getUsername());
        System.out.println(userDetails.getPassword());
        System.out.println(userDetails.getAuthorities());
        System.out.println("userdetails");
        return userDetails;
    }
    private Collection<? extends GrantedAuthority> getAuthorities(final Collection<Role> roles) {
        return getGrantedAuthorities(getPrivileges(roles));
    }


    private List<String> getPrivileges(final Collection<Role> roles) {
        final List<String> privileges = new ArrayList<>();
        final List<Privilege> collection = new ArrayList<>();
        for (final Role role : roles) {
            privileges.add(role.getName());
            collection.addAll(role.getPrivileges());
        }
        for (final Privilege item : collection) {
            privileges.add(item.getName());
        }

        return privileges;
    }

    private List<GrantedAuthority> getGrantedAuthorities(final List<String> privileges) {
        final List<GrantedAuthority> authorities = new ArrayList<>();
        for (final String privilege : privileges) {
            authorities.add(new SimpleGrantedAuthority(privilege));
        }
        return authorities;
    }

}
