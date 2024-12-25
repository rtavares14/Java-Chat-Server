package shared.messages.list;

public record ListReq(String username) {
    public String getUsername() {
        return username;
    }
}
