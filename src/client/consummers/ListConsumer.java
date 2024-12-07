package client.consummers;

import shared.messages.UserList;
import shared.utils.JsonUtils;
import shared.utils.MessageHelper;

import java.util.function.Consumer;

import static shared.enumerations.CmdColors.*;

public class ListConsumer implements Consumer<String> {

    @Override
    public void accept(String json) {
        try {
            UserList userList = JsonUtils.fromJson(json, UserList.class);
            StringBuilder usersList = new StringBuilder();
            for (String user : userList.getUsers()) {
                usersList.append(user).append("\n");
            }
            MessageHelper.printColoredMessage(CYAN, usersList.toString());        } catch (Exception e) {
            System.err.println("Failed to process BROADCAST message: " + e.getMessage());
        }
    }
}
