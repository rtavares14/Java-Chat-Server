package shared.messages;

public record Info(String message) {

    public String getMessage() {
        return message;
    }
}
