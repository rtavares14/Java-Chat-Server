package shared.messages;

public record ListReq(String username){
    public String getUsername() {
        return username;
    }
}
