package shared.messages.broadcast;

public record BroadcastReq(String message) {
    public String getMessage() {
        return message;
    }
}
