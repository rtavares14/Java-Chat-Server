package shared.messages.RPSGame.enter_game;

public record GameMsg(String player1) {

    public String getPlayer1() {
        return player1;
    }
}
