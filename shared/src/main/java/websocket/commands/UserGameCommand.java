package websocket.commands;

import chess.ChessMove;
import com.google.gson.Gson;

public class UserGameCommand {
    private final CommandType commandType;
    private final String authToken;
    private final Integer gameID;
    private String move;
    private static final Gson GSON = new Gson();

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

    public void setMove(String move) {
        this.move = move;
    }

    public ChessMove getChessMove() {
        if (move == null) {
            return null;
        }
        return GSON.fromJson(move, ChessMove.class);
    }
}