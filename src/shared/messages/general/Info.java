package shared.messages.general;

public record Info(String message) {

    public String getMessage() {
        return message;
    }
}
