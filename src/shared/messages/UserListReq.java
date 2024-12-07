package shared.messages;

public record UserListReq(String username){
    public String getUsername() {
        return username;
    }
}
