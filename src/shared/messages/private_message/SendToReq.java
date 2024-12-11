package shared.messages.private_message;

public record SendToReq (String username, String message) {

    public String getUsername() {
        return username;
    }

    public String getMessage() {
        return message;
    }
}
