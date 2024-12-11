package shared.messages.list;

import java.util.ArrayList;

public record List(ArrayList<String> users) {
    public ArrayList<String> getUsers() {
        return users;
    }
}
