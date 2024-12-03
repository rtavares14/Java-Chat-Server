package server;

import com.fasterxml.jackson.core.JsonProcessingException;
import shared.messages.*;
import shared.utils.JsonUtils;

public class ClientHandler {

    private ClientInstance clientInstance;

    public ClientHandler(ClientInstance clientInstance ) {
        this.clientInstance = clientInstance;
    }

    public EnterResp handleLogin(Enter loginMessage) throws JsonProcessingException {
        String username = loginMessage.getUsername();

        if (UsernameValidator.isUsernameValid(username) && UsernameValidator.isUsernameAvailable(username) &&
                clientInstance.getUsername() == "") {
            // Add username to a global list of logged-in users
            Server.logInUser(username, clientInstance);
            clientInstance.setUsername(username);
            return new EnterResp("OK", 0);
        } else {
            int errorCode = UsernameValidator.checkUsername(username);
            return new EnterResp("ERROR", errorCode);
        }
    }
}
