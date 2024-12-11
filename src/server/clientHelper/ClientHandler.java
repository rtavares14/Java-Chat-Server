package server.clientHelper;

import com.fasterxml.jackson.core.JsonProcessingException;
import server.logger.ClientLogger;
import server.validator.UsernameValidator;
import shared.messages.enter.Enter;
import shared.messages.enter.EnterResp;

public class ClientHandler {

    private ClientInstance clientInstance;

    /**
     * Constructor for the ClientHandler class
     *
     * @param clientInstance the client instance
     */
    public ClientHandler(ClientInstance clientInstance) {
        this.clientInstance = clientInstance;
    }

    /**
     * Handle the login message
     * This method is used to handle the login message
     * It checks if the username is valid and available and then logs the user in
     *
     * @param loginMessage the login message
     * @return the response to the login message
     * @throws JsonProcessingException if an exception occurs
     */
    public EnterResp handleLogin(Enter loginMessage) {
        String username = loginMessage.getUsername();

        if (UsernameValidator.isUsernameValid(username) && UsernameValidator.isUsernameAvailable(username) &&
                clientInstance.getUsername().equals("")) {
            ClientLogger.getInstance().logInUser(username, clientInstance);
            clientInstance.setUsername(username);
            return new EnterResp("OK", null);
        } else {
            int errorCode = UsernameValidator.checkUsername(username);
            return new EnterResp("ERROR", errorCode);
        }
    }
}
