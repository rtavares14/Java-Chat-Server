package shared.messages;

public record Enter(String username) {

    public String getUsername() {
        return username;
    }
}
