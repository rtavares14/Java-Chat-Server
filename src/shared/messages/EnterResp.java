package shared.messages;

public record EnterResp(String status, Integer code) {
    public String getStatus() {
        return status;
    }
}
