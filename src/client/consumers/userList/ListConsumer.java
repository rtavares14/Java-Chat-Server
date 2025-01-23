package client.consumers.userList;

import shared.messages.list.List;
import shared.utils.JsonUtils;
import shared.utils.messages.MessageHelper;

import java.util.function.Consumer;

import static shared.enumerations.CmdColors.CYAN;

public class ListConsumer implements Consumer<String> {

    @Override
    public void accept(String json) {
        try {
            List list = JsonUtils.fromJson(json, List.class);
            StringBuilder usersList = new StringBuilder();
            int index = 1;
            int lastIndex = list.getSize();
            for (String user : list.getUsers()) {
                if (index != lastIndex) {
                    usersList.append(index++).append("- ").append(user).append("\n");
                } else {
                    usersList.append(index++).append("- ").append(user);
                }
            }
            MessageHelper.printColoredMessage(CYAN, usersList.toString());
        } catch (Exception e) {
            System.err.println("Failed to process LIST message: " + e.getMessage());
        }
    }
}
