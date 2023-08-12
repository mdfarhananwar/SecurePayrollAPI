package account.model;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

public class EmployeeDTO {

    private Long id;
    private String name;
    private String lastname;
    private String email;

    private Set<String> roles = new TreeSet<>();

    public EmployeeDTO() {
    }

    public EmployeeDTO(Employee employee) {
        this.id = employee.getId();
        this.name = employee.getName();
        this.lastname = employee.getLastname();
        this.email = employee.getEmail();
        for (Role role : employee.getRoles()) {
            this.roles.add("ROLE_" + role.getName());
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getEmail() {
        return email.toLowerCase();
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }
}
