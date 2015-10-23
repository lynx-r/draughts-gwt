package online.shashki.rus.client.application.component.play;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.rest.delegates.client.ResourceDelegate;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;
import online.shashki.rus.client.application.widget.NotationPanel;
import online.shashki.rus.client.application.widget.dialog.ErrorDialogBox;
import online.shashki.rus.client.application.widget.dialog.InfoDialogBox;
import online.shashki.rus.client.event.*;
import online.shashki.rus.client.util.SHLog;
import online.shashki.rus.client.websocket.GameWebsocket;
import online.shashki.rus.shared.locale.ShashkiMessages;
import online.shashki.rus.shared.model.Game;
import online.shashki.rus.shared.model.GameMessage;
import online.shashki.rus.shared.model.Move;
import online.shashki.rus.shared.model.Player;
import online.shashki.rus.shared.rest.GamesResource;
import online.shashki.rus.shared.rest.PlayersResource;
import online.shashki.rus.shashki.MoveFactory;
import online.shashki.rus.shashki.Stroke;

import java.util.Date;
import java.util.List;


public class PlayComponentPresenter extends PresenterWidget<PlayComponentPresenter.MyView>
    implements PlayComponentUiHandlers {

  private int CHECKERS_ON_DESK_INIT = 12;

  private final GameWebsocket gameWebsocket;
  private final ShashkiMessages messages;
  private final ResourceDelegate<GamesResource> gamesDelegate;
  private final ResourceDelegate<PlayersResource> playersDelegate;
  private final EventBus eventBus;

  @Inject
  PlayComponentPresenter(
      EventBus eventBus,
      MyView view,
      ShashkiMessages messages,
      ResourceDelegate<GamesResource> gamesDelegate,
      ResourceDelegate<PlayersResource> playersDelegate,
      GameWebsocket gameWebsocket) {
    super(eventBus, view);

    this.eventBus = eventBus;
    this.messages = messages;
    this.gameWebsocket = gameWebsocket;
    this.gamesDelegate = gamesDelegate;
    this.playersDelegate = playersDelegate;

    getView().setUiHandlers(this);
    getView().initNotationPanel(eventBus);

    addRegisteredHandler(ReceivedPlayerListEvent.TYPE, new ReceivedPlayerListEventHandler() {
          @Override
          public void onReceivedPlayerList(ReceivedPlayerListEvent event) {
            getView().setPlayerList(event.getPlayerList());
          }
        }
    );
  }

  @Override
  protected void onBind() {
    super.onBind();

    bindEvents();
  }

  @Override
  public void startPlayWith(final Player opponent) {
    if (opponent == null) {
      InfoDialogBox.setMessage(messages.selectPlayer()).show();
      return;
    }
    if (opponent.getId().equals(gameWebsocket.getPlayer().getId())) {
      InfoDialogBox.setMessage(messages.selectAnotherPlayerItsYou()).show();
      return;
    }
    getView().setOpponent(opponent);

    getView().showInviteDialog(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        GameMessage gameMessage = createGameMessage();
        gameMessage.setMessageType(GameMessage.MessageType.PLAY_INVITE);
        gameMessage.setReceiver(opponent);

        final boolean white = getView().opponentColor();
        SHLog.debug(white + " OPPONENT COLOR");
        gameMessage.setMessage(messages.inviteMessage(gameWebsocket.getPlayer().getPublicName(),
            String.valueOf(white ? messages.white() : messages.black())));
        gameMessage.setData(String.valueOf(white));

        eventBus.fireEvent(new GameMessageEvent(gameMessage));
      }
    });
  }

  @Override
  public void refreshConnectionToServer() {
    if (gameWebsocket.isConnected()) {
      gameWebsocket.reconnect();
    } else {
      gameWebsocket.connect();
    }
    getView().setPlayer(gameWebsocket.getPlayer());
    SHLog.debug("Set player for Play View: " + gameWebsocket.getPlayer());
  }

  @Override
  public boolean isMyTurn() {
    return gameWebsocket.getGame().getPlayerWhite().getId()
        .equals(gameWebsocket.getPlayer().getId());
  }

  @Override
  public EventBus getPlayEventBus() {
    return eventBus;
  }

  @Override
  public void proposeDraw() {
    GameMessage gameMessage = createGameMessage();
    gameMessage.setMessageType(GameMessage.MessageType.PLAY_PROPOSE_DRAW);
    eventBus.fireEvent(new GameMessageEvent(gameMessage));
  }

  @Override
  public void playerSurrendered() {
    GameMessage gameMessage = createGameMessage();
    gameMessage.setMessageType(GameMessage.MessageType.PLAY_SURRENDER);
    eventBus.fireEvent(new GameMessageEvent(gameMessage));
    eventBus.fireEvent(new ClearPlayComponentEvent());
  }

  @Override
  public void proposeCancelMove(Stroke stroke) {
    GameMessage gameMessage = createGameMessage();
    gameMessage.setMessageType(GameMessage.MessageType.PLAY_CANCEL_MOVE);
    if (stroke.isContinueBeat()) {
      stroke.mirror();
    }
    stroke.setOnCancelMove();
    Move move = MoveFactory.createMoveFromStroke(stroke);
    gameMessage.setMove(move);

    eventBus.fireEvent(new GameMessageEvent(gameMessage));
  }

  private GameMessage createGameMessage() {
    GameMessage gameMessage = GWT.create(GameMessage.class);
    gameMessage.setSender(gameWebsocket.getPlayer());
    gameMessage.setReceiver(gameWebsocket.getOpponent());
    gameMessage.setGame(gameWebsocket.getGame());
    return gameMessage;
  }

  public void bindEvents() {
    eventBus.addHandler(ReceivedPlayerListEvent.TYPE, new ReceivedPlayerListEventHandler() {
      @Override
      public void onReceivedPlayerList(ReceivedPlayerListEvent event) {
        if (!event.getPlayerList().contains(gameWebsocket.getOpponent()) && gameWebsocket.getGame() != null) {
          Game game = gameWebsocket.getGame();
          final Game.GameEnds gameEnd = gameWebsocket.isPlayerHasWhiteColor() ? Game.GameEnds.BLACK_LEFT : Game.GameEnds.WHITE_LEFT;
          eventBus.fireEvent(new GameOverEvent(game, gameEnd, new AsyncCallback<Game>() {
            @Override
            public void onFailure(Throwable throwable) {
              ErrorDialogBox.setMessage(messages.errorWhileSavingGame(), throwable).show();
            }

            @Override
            public void onSuccess(Game aVoid) {
              InfoDialogBox.setMessage(messages.opponentLeftGame()).show();
            }
          }));
        }
        getView().setPlayerList(event.getPlayerList());
      }
    });

    eventBus.addHandler(ConnectedToPlayEvent.TYPE, new ConnectedToPlayEventHandler() {
      @Override
      public void onConnectedToPlay(ConnectedToPlayEvent event) {
        getView().toggleInPlayButton();
      }
    });

    eventBus.addHandler(DisconnectFromPlayEvent.TYPE, new DisconnectFromPlayEventHandler() {
      @Override
      public void onDisconnectFromPlay(DisconnectFromPlayEvent event) {
        getView().setUpViewOnDisconnectFromServer();
      }
    });

    eventBus.addHandler(StartPlayEvent.TYPE, new StartPlayEventHandler() {
      @Override
      public void onStartPlay(StartPlayEvent event) {
        getView().hideInviteDialog();

        SHLog.debug(event.isWhite() + " START PLAY AT INVITER");
        SHLog.debug("PLAYER " + gameWebsocket.getPlayer());
        SHLog.debug("OPPONENT " + gameWebsocket.getOpponent());

        getView().startPlay(event.isWhite());
      }
    });

    eventBus.addHandler(RejectPlayEvent.TYPE, new RejectPlayEventHandler() {
      @Override
      public void onRejectPlay(RejectPlayEvent event) {
        getView().hideInviteDialog();
      }
    });

    eventBus.addHandler(TurnChangeEvent.TYPE, new TurnChangeEventHandler() {
      @Override
      public void onTurnChange(TurnChangeEvent event) {
        getView().updateTurn(event.isMyTurn());
      }
    });

    eventBus.addHandler(CheckWinnerEvent.TYPE, new CheckWinnerEventHandler() {
      @Override
      public void onCheckWinner(CheckWinnerEvent event) {
        getView().setBeatenMy(CHECKERS_ON_DESK_INIT - getView().getMyDraughtsSize());
        getView().setBeatenOpponent(CHECKERS_ON_DESK_INIT - getView().getOpponentDraughtsSize());
        final Game endGame = gameWebsocket.getGame();
        Game.GameEnds gameEnd = null;
        if (0 == getView().getMyDraughtsSize()) {
          InfoDialogBox.setMessage(messages.youLose()).show();
          if ((getView().isWhite())) {
            gameEnd = Game.GameEnds.BLACK_WIN;
          } else {
            gameEnd = Game.GameEnds.WHITE_WIN;
          }
        }
        if (0 == getView().getOpponentDraughtsSize()) {
          InfoDialogBox.setMessage(messages.youWon()).show();
          if (getView().isWhite()) {
            gameEnd = Game.GameEnds.WHITE_WIN;
          } else {
            gameEnd = Game.GameEnds.BLACK_WIN;
          }
        }
        if (gameEnd == null) {
          return;
        }
        eventBus.fireEvent(new GameOverEvent(endGame, gameEnd, new AsyncCallback<Game>() {
          @Override
          public void onFailure(Throwable caught) {
            ErrorDialogBox.setMessage(messages.errorWhileSavingGame(), caught).show();
          }

          @Override
          public void onSuccess(Game result) {
          }
        }));
      }
    });

    eventBus.addHandler(ClearPlayComponentEvent.TYPE, new ClearPlayComponentEventHandler() {
      @Override
      public void onClearPlayComponent(ClearPlayComponentEvent event) {
        eventBus.fireEvent(new ClearNotationEvent());
        eventBus.fireEvent(new UpdatePlayerListEvent());
        eventBus.fireEvent(new RemovePlayMoveOpponentHandlerEvent());

        gameWebsocket.setOpponent(null);
        getView().setOpponent(null);
        gameWebsocket.setGame(null);

        getView().clearPlayComponent();
        getView().hidePlayingButtonsAndShowPlayButton();
      }
    });

    eventBus.addHandler(HideInviteDialogBoxEvent.TYPE, new HideInviteDialogBoxEventHandler() {
      @Override
      public void onHideInviteDialogBox(HideInviteDialogBoxEvent event) {
        getView().hideInviteDialog();
      }
    });

    eventBus.addHandler(GameOverEvent.TYPE, new GameOverEventHandler() {
      @Override
      public void onGameOver(GameOverEvent event) {
        Game game = event.getGame();
        game.setPlayEndStatus(event.getGameEnd());
        game.setPlayFinishDate(new Date());
        game.setPartyNotation(NotationPanel.getNotation());
        game.setEndGameScreenshot(getView().takeScreenshot());
        gamesDelegate.withCallback(event.getAsyncCallback()).saveOrCreate(game);
        eventBus.fireEvent(new ClearPlayComponentEvent());
      }
    });
  }

  interface MyView extends View, HasUiHandlers<PlayComponentUiHandlers> {
    void initNotationPanel(EventBus eventBus);

    void setPlayerList(List<Player> playerList);

    void setPlayer(Player player);

    void toggleInPlayButton();

    void setUpViewOnDisconnectFromServer();

    void hidePlayButtonAndShowPlayingButtons();

    void clearPlayComponent();

    void hidePlayingButtonsAndShowPlayButton();

    void setBeatenMy(int num);

    void setBeatenOpponent(int num);

    void startPlay(boolean white);

    void updateTurn(boolean myTurn);

    int getMyDraughtsSize();

    int getOpponentDraughtsSize();

    boolean isWhite();

    void hideInviteDialog();

    void showInviteDialog(ClickHandler inviteClickHandler);

    boolean opponentColor();

    String takeScreenshot();

    void setOpponent(Player opponent);
  }
}
