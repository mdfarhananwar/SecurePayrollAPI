package account.repository;

import account.model.Employee;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class UserDetailsImp implements UserDetails {

    private final String username;
    private final String password;
    private final List<GrantedAuthority> rolesAndAuthorities;
    public UserDetailsImp(Employee employee) {
        this.username = employee.getEmail().toLowerCase();
        this.password = employee.getPassword();
        String roleWithPrefix = "ROLE_" + "USER";
        rolesAndAuthorities = List.of(new SimpleGrantedAuthority(roleWithPrefix));

    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
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
