package shared.messages;

import java.util.ArrayList;

public record List(ArrayList<String> users) {
    public ArrayList<String> getUsers() {
        return users;
    }
}
