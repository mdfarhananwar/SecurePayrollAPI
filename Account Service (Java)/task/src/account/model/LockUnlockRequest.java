package account.model;

public class LockUnlockRequest {

    String user;
    LockUnlockEnum operation;

    public LockUnlockRequest() {
    }

    public LockUnlockRequest(String user, LockUnlockEnum operation) {
        this.user = user;
        this.operation = operation;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public LockUnlockEnum getOperation() {
        return operation;
    }

    public void setOperation(LockUnlockEnum operation) {
        this.operation = operation;
    }
}
