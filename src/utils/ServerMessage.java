package utils;

public class ServerMessage {
    private ServerCommands type;
    private int code;
    private String body;

    public ServerMessage(ServerCommands type,int code, String body) {
        this.type = type;
        this.code = code;
        this.body = body;
    }

    public ServerCommands getType() {
        return type;
    }

    public int getCode() {
        return code;
    }

    public String getBody() {
        return body;
    }

    @Override
    public String toString() {
        return "ServerMessage{" +
                "type=" + type +
                ", code=" + code +
                ", body='" + body + '\'' +
                '}';
    }
}