package shared.messages.RPSGame.enter_game;

public record GameStartReq(String player2) {

    public String getPlayer2() {
        return player2;
    }
}
