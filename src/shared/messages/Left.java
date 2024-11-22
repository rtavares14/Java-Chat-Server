package shared.messages;

public record Left(String username) {

    public String getUsername() {
        return username;
    }
}
