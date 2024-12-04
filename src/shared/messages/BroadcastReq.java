package shared.messages;

public record BroadcastReq(String message) {
    public String getMessage() {
        return message;
    }
}
