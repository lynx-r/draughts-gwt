package online.shashki.rus.client.event;

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.client.rpc.AsyncCallback;
import online.shashki.rus.shared.model.Game;
import online.shashki.rus.shared.model.GameEnds;

/**
 * Created with IntelliJ IDEA.
 * User: alekspo
 * Date: 03.10.15
 * Time: 13:53
 */
public class GameOverEvent extends GwtEvent<GameOverEventHandler> {
  public static Type<GameOverEventHandler> TYPE = new Type<GameOverEventHandler>();
  private final Game game;
  private final GameEnds gameEnd;
  private final AsyncCallback<Void> asyncCallback;

  public GameOverEvent(Game game, GameEnds gameEnd, AsyncCallback<Void> asyncCallback) {
    this.game = game;
    this.gameEnd = gameEnd;
    this.asyncCallback = asyncCallback;
  }

  public Game getGame() {
    return game;
  }

  public GameEnds getGameEnd() {
    return gameEnd;
  }

  public AsyncCallback<Void> getAsyncCallback() {
    return asyncCallback;
  }

  public Type<GameOverEventHandler> getAssociatedType() {
    return TYPE;
  }

  protected void dispatch(GameOverEventHandler handler) {
    handler.onGameOver(this);
  }
}