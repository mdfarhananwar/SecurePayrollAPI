package account.model;

public class EmployeeDTO {

    private String name;
    private String lastname;
    private String email;

    public EmployeeDTO() {
    }

    public EmployeeDTO(Employee employee) {
        this.name = employee.getName();
        this.lastname = employee.getLastname();
        this.email = employee.getEmail();
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
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
