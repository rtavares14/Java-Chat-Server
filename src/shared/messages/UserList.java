package shared.messages;

import java.util.ArrayList;

public record UserList(ArrayList<String> users) {
    public ArrayList<String> getUsers() {
        return users;
    }
}
