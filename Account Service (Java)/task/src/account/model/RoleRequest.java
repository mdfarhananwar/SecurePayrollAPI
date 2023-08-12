package account.model;

public class RoleRequest {

    private String user;
    private String role;
    private Operation operation;

    public RoleRequest(String user, String role, Operation operation) {
        this.user = user;
        this.role = role;
        this.operation = operation;
    }

    public RoleRequest() {
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    @Override
    public String toString() {
        return "RoleRequest{" +
                "user='" + user + '\'' +
                ", role=" + role +
                ", operation=" + operation +
                '}';
    }
}
