package account.model;

public class PasswordRequest {
    private String new_password;

    public PasswordRequest() {
    }

    public PasswordRequest(String new_password) {
        this.new_password = new_password;
    }

    public String getNew_password() {
        return new_password;
    }

    public void setNew_password(String new_password) {
        this.new_password = new_password;
    }
}
