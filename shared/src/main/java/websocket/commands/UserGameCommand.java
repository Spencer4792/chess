package websocket.commands;

import chess.ChessMove;
import com.google.gson.Gson;

public class UserGameCommand {
    private final CommandType commandType;
    private final String authToken;
    private final Integer gameID;
    private String move;

    public UserGameCommand(CommandType commandType, String authToken, Integer gameID) {
        this.commandType = commandType;
        this.authToken = authToken;
        this.gameID = gameID;
    }

    public enum CommandType {
        CONNECT,
        MAKE_MOVE,
        LEAVE,
        RESIGN
    }

    public CommandType getCommandType() {
        return commandType;
    }

    public String getAuthToken() {
        return authToken;
    }

    public Integer getGameID() {
        return gameID;
    }

    public String getMove() {
        return move;
    }

    public void setMove(String move) {
        this.move = move;
    }

    public ChessMove getChessMove() {
        if (move == null) {
            return null;
        }
        return new Gson().fromJson(move, ChessMove.class);
    }
}