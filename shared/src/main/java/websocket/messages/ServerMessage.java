package websocket.messages;

import chess.ChessGame;
import java.util.Objects;

public class ServerMessage {
    private ServerMessageType serverMessageType;
    private String message;
    private String errorMessage;
    private ChessGame game;

    public ServerMessage(ServerMessageType type) {
        this.serverMessageType = type;
    }

    public enum ServerMessageType {
        LOAD_GAME,
        ERROR,
        NOTIFICATION
    }

    public ServerMessageType getServerMessageType() {
        return this.serverMessageType;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setGame(ChessGame game) {
        this.game = game;
    }

    public ChessGame getGame() {
        return game;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ServerMessage that = (ServerMessage) o;
        return getServerMessageType() == that.getServerMessageType() &&
                Objects.equals(message, that.message) &&
                Objects.equals(errorMessage, that.errorMessage) &&
                Objects.equals(game, that.game);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getServerMessageType(), message, errorMessage, game);
    }
}