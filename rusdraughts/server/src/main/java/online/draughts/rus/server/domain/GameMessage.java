package online.draughts.rus.server.domain;


import com.google.gwt.user.client.rpc.IsSerializable;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Ignore;
import com.googlecode.objectify.annotation.Index;

import java.util.Date;
import java.util.List;

import static online.draughts.rus.shared.util.ObjectifyUtil.getObject;
import static online.draughts.rus.shared.util.ObjectifyUtil.setObject;

/**
 * Created with IntelliJ IDEA.
 * User: alekspo
 * Date: 15.11.14
 * Time: 15:49
 */
@Entity
public class GameMessage extends PersistableObjectImpl {

  @Index
  private Ref<Player> sender;

  @Index
  private Ref<Player> receiver;

  private String message;

  @Index
  private MessageType messageType;

  private String data;

  @Index
  private Date sentDate;

  private Ref<Move> move;

  @Index
  private Ref<Game> game;

  @Ignore
  private List<Player> playerList;

  public Player getSender() {
    return getObject(sender);
  }

  public void setSender(Player sender) {
    if (sender == null) {
      return;
    }
    this.sender = Ref.create(sender);
  }

  public Player getReceiver() {
    return getObject(receiver);
  }

  public void setReceiver(Player receiver) {
    if (receiver == null) {
      return;
    }
    this.receiver = Ref.create(receiver);
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getData() {
    return data;
  }

  public void setData(String data) {
    this.data = data;
  }

  public Date getSentDate() {
    return sentDate;
  }

  public void setSentDate(Date sentDate) {
    this.sentDate = sentDate;
  }

  public MessageType getMessageType() {
    return messageType;
  }

  public void setMessageType(MessageType messageType) {
    this.messageType = messageType;
  }

  public Move getMove() {
    return getObject(move);
  }

  public void setMove(Move move) {
    if (move == null) {
      return;
    }
    this.move = Ref.create(move);
  }

  public Game getGame() {
    return getObject(game);
  }

  public void setGame(Game game) {
    setObject(this.game, game);
//    if (game == null) {
//      return;
//    }
//    this.game = Ref.create(game);
  }

  public void setPlayerList(List<Player> playerList) {
    this.playerList = playerList;
  }

  public List<Player> getPlayerList() {
    return playerList;
  }

  @Override
  public String toString() {
    return "GameMessage{" +
        "sender=" + sender +
        ", receiver=" + receiver +
        ", message='" + message + '\'' +
        ", messageType=" + messageType +
        ", data='" + data + '\'' +
        ", sentDate=" + sentDate +
        ", move=" + move +
        ", game=" + game +
        ", playerList=" + playerList +
        '}';
  }

  public enum MessageType implements IsSerializable {

    CHAT_MESSAGE,
    CHAT_PRIVATE_MESSAGE,
    USER_LIST_UPDATE,
    PLAY_INVITE,
    PLAY_REJECT_INVITE,
    PLAY_ALREADY_PLAYING,
    PLAY_START,
    PLAY_OPPONENT_MOVE,
    PLAY_CANCEL_MOVE,
    PLAY_CANCEL_MOVE_RESPONSE,
    PLAY_PROPOSE_DRAW,
    PLAY_ACCEPT_DRAW,
    PLAY_END,
    PLAY_SURRENDER,
    NOTIFICATION_ADDED_TO_FAVORITE,
    CHANNEL_CLOSE,
    PLAYER_REGISTER;
  }
}
