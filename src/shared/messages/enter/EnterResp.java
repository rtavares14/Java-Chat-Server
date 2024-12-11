package shared.messages.enter;

public record EnterResp(String status, Integer code) {
    public String getStatus() {
        return status;
    }
}
