package shared.messages.enter;

public record EnterReq(String username) {
    public String getUsername() {
        return username;
    }
}
