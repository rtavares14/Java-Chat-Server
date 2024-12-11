package shared.messages.enter;

public record Enter(String username) {

    public String getUsername() {
        return username;
    }
}
