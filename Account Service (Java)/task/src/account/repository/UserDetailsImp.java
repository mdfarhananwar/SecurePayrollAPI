package account.repository;

import account.model.Employee;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class UserDetailsImp implements UserDetails {

    private final String username;
    private final String password;
//    private final Set<SimpleGrantedAuthority> rolesAndAuthorities;
private final Set<SimpleGrantedAuthority> rolesAndAuthorities;
    public UserDetailsImp(Employee employee, List<GrantedAuthority> rolesAndAuthorities) {
        this.username = employee.getEmail().toLowerCase();
        this.password = employee.getPassword();
//        this.rolesAndAuthorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));;

        this.rolesAndAuthorities = employee.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
                .collect(Collectors.toSet());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        System.out.println("INSIDE USERDETAILS : getAuthorities");
        System.out.println(rolesAndAuthorities);
        return rolesAndAuthorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username.toLowerCase();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

}
