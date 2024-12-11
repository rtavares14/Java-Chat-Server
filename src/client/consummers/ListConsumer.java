package client.consummers;

import shared.messages.list.List;
import shared.utils.JsonUtils;
import shared.utils.messages.MessageHelper;

import java.util.function.Consumer;

import static shared.enumerations.CmdColors.*;

public class ListConsumer implements Consumer<String> {

    @Override
    public void accept(String json) {
        try {
            List list = JsonUtils.fromJson(json, List.class);
            StringBuilder usersList = new StringBuilder();
            for (String user : list.getUsers()) {
                usersList.append(user).append("\n");
            }
            MessageHelper.printColoredMessage(CYAN, usersList.toString());        } catch (Exception e) {
            System.err.println("Failed to process BROADCAST message: " + e.getMessage());
        }
    }
}
