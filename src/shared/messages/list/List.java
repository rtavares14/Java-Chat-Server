package shared.messages.list;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;

@JsonIgnoreProperties(ignoreUnknown = true)
public record List(ArrayList<String> users) {
    public ArrayList<String> getUsers() {
        return users;
    }

    public int getSize() {
        return users.size();
    }
}
