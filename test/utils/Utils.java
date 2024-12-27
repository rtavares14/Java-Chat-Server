package utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import shared.messages.*;
import shared.messages.RPSGame.enter_game.GameMsg;
import shared.messages.RPSGame.enter_game.GameStartReq;
import shared.messages.RPSGame.enter_game.GameStartResp;
import shared.messages.RPSGame.play_game.GameChoiceReq;
import shared.messages.RPSGame.play_game.GameChoiceResp;
import shared.messages.RPSGame.play_game.GameEnd;
import shared.messages.broadcast.Broadcast;
import shared.messages.broadcast.BroadcastReq;
import shared.messages.broadcast.BroadcastResp;
import shared.messages.enter.Enter;
import shared.messages.enter.EnterReq;
import shared.messages.enter.EnterResp;
import shared.messages.errors.ParseError;
import shared.messages.list.List;
import shared.messages.list.ListReq;
import shared.messages.list.ListResp;
import shared.messages.login_logout.ByeResp;
import shared.messages.login_logout.Joined;
import shared.messages.login_logout.Left;
import shared.messages.ping_pong.Hangup;
import shared.messages.ping_pong.Ping;
import shared.messages.ping_pong.Pong;
import shared.messages.ping_pong.PongError;
import shared.messages.private_message.SendTo;
import shared.messages.private_message.SendToReq;
import shared.messages.private_message.SendToResp;

import java.util.HashMap;
import java.util.Map;

public class Utils {

    private final static ObjectMapper mapper = new ObjectMapper();
    private final static Map<Class<?>, String> objToNameMapping = new HashMap<>();
    static {
        objToNameMapping.put(Enter.class, "ENTER");
        objToNameMapping.put(EnterResp.class, "ENTER_RESP");
        objToNameMapping.put(EnterReq.class, "ENTER_REQ");
        objToNameMapping.put(BroadcastReq.class, "BROADCAST_REQ");
        objToNameMapping.put(BroadcastResp.class, "BROADCAST_RESP");
        objToNameMapping.put(Broadcast.class, "BROADCAST");
        objToNameMapping.put(Joined.class, "JOINED");
        objToNameMapping.put(ParseError.class, "PARSE_ERROR");
        objToNameMapping.put(Pong.class, "PONG");
        objToNameMapping.put(PongError.class, "PONG_ERROR");
        objToNameMapping.put(Ready.class, "READY");
        objToNameMapping.put(Ping.class, "PING");
        objToNameMapping.put(ListReq.class, "LIST_REQ");
        objToNameMapping.put(ListResp.class, "LIST_RESP");
        objToNameMapping.put(List.class, "LIST");
        objToNameMapping.put(Left.class, "LEFT");
        objToNameMapping.put(SendTo.class, "SENDTO");
        objToNameMapping.put(SendToReq.class, "SENDTO_REQ");
        objToNameMapping.put(SendToResp.class, "SENDTO_RESP");
        objToNameMapping.put(Hangup.class, "HANGUP");
        objToNameMapping.put(UnknownError.class, "UNKNOWN_COMMAND");
        objToNameMapping.put(ByeResp.class, "BYE_RESP");
        objToNameMapping.put(GameMsg.class , "RPS_MSG");
        objToNameMapping.put(GameStartReq.class , "RPS_START_REQ");
        objToNameMapping.put(GameStartResp.class , "RPS_START_RESP");
        objToNameMapping.put(GameChoiceReq.class , "RPS_CHOICE_REQ");
        objToNameMapping.put(GameChoiceResp.class , "RPS_CHOICE_RESP");
        objToNameMapping.put(GameEnd.class , "RPS_END");
    }

    public static String objectToMessage(Object object) throws JsonProcessingException {
        Class<?> clazz = object.getClass();
        String header = objToNameMapping.get(clazz);
        if (header == null) {
            throw new RuntimeException("Cannot convert this class to a message");
        }
        String body = mapper.writeValueAsString(object);
        return header+ " " + body;
    }

    public static <T> T messageToObject(String message) throws JsonProcessingException {
        String[] parts = message.split(" ", 2);
        if (parts.length > 2 || parts.length == 0) {
            throw new RuntimeException("Invalid message");
        }
        String header = parts[0];
        String body = "{}";
        if (parts.length == 2) {
            body = parts[1];
        }
        Class<?> clazz = getClass(header);
        Object obj = mapper.readValue(body, clazz);
        return (T) clazz.cast(obj);
    }

    private static Class<?> getClass(String header) {
        return objToNameMapping.entrySet().stream()
                .filter(e -> e.getValue().equals(header))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Cannot find class belonging to header " + header));
    }
}
