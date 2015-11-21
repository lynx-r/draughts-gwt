package online.draughts.rus.server.channel;

import com.google.appengine.api.channel.ChannelMessage;
import com.google.appengine.api.channel.ChannelServiceFactory;
import com.google.common.base.Splitter;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import no.eirikb.gwtchannelapi.server.ChannelServer;
import online.draughts.rus.server.service.GameMessageService;
import online.draughts.rus.server.service.GameService;
import online.draughts.rus.server.service.PlayerService;
import online.draughts.rus.server.util.AuthUtils;
import online.draughts.rus.server.util.Utils;
import online.draughts.rus.shared.channel.Chunk;
import online.draughts.rus.shared.model.Game;
import online.draughts.rus.shared.model.GameMessage;
import online.draughts.rus.shared.model.Move;
import online.draughts.rus.shared.model.Player;
import online.draughts.rus.shared.util.StringUtils;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: alekspo
 * Date: 07.12.14
 * Time: 12:05
 */
@Singleton
public class ServerChannel extends ChannelServer {

  private static Map<String, String> channelTokenPeers = Collections.synchronizedMap(new HashMap<String, String>());
  private final PlayerService playerService;
  private final GameMessageService gameMessageService;
  private final GameService gameService;
  private final Provider<Boolean> authProvider;
  private final Logger logger;

  @Inject
  ServerChannel(PlayerService playerService,
                GameService gameService,
                GameMessageService gameMessageService,
                @Named(AuthUtils.AUTHENTICATED) Provider<Boolean> authProvider,
                Logger logger) {
    this.playerService = playerService;
    this.gameService = gameService;
    this.gameMessageService = gameMessageService;
    this.authProvider = authProvider;
    this.logger = logger;
  }

  @Override
  public void onJoin(String token, String channelName) {
    channelTokenPeers.put(channelName, token);
  }

  @Override
  public void onMessage(String token, String channelName, String message) {
    if (StringUtils.isEmpty(message)) {
      return;
    }
    GameMessage gameMessage;
    try {
      gameMessage = Utils.deserializeFromJson(message, GameMessage.class);
    } catch (IOException e) {
      logger.severe(e.getLocalizedMessage());
      return;
    }
    if (gameMessage == null) {
      return;
    }

    // проверка что пользователь аутентифицирован на сайте
//    if (authProvider.get()) {
//      logger.info("Unauthorized access: " + gameMessage);
//      return;
//    }

    // разбор сообщения
    switch (gameMessage.getMessageType()) {
      case PLAYER_REGISTER:
        handleNewPlayer(gameMessage, token);
        break;
//      case CHAT_MESSAGE:
//        handleChatMessage(session, gameMessage);
//        break;
      case USER_LIST_UPDATE:
        updatePlayerList();
        break;
      case PLAY_INVITE:
      case PLAY_REJECT_INVITE:
      case PLAY_ALREADY_PLAYING:
      case PLAY_START:
      case PLAY_OPPONENT_MOVE:
      case PLAY_PROPOSE_DRAW:
      case PLAY_CANCEL_MOVE:
      case PLAY_CANCEL_MOVE_RESPONSE:
      case CHAT_PRIVATE_MESSAGE:
      case NOTIFICATION_ADDED_TO_FAVORITE:
        handleChatPrivateMessage(gameMessage);
        break;
      case PLAY_END:
      case PLAY_SURRENDER:
      case PLAY_ACCEPT_DRAW:
        handleGameOver(gameMessage);
        break;
      case CHANNEL_CLOSE:
        onClose(channelName);
        break;
    }
  }

  private void handleGameOver(GameMessage gameMessage) {
    Game game = gameMessage.getGame();
    if (game == null) {
      return;
    }

    Player black = playerService.find(game.getPlayerBlack().getId());
    Player white = playerService.find(game.getPlayerWhite().getId());

    black.setPlaying(false);
    white.setPlaying(false);

    playerService.saveOrCreateOnServer(black);
    playerService.saveOrCreateOnServer(white);

    handleChatPrivateMessage(gameMessage);
  }

//  private void handleChatMessage(Session session, GameMessage message) {
//    for (Session s : session.getOpenSessions()) {
//      if (s.isOpen()) {
//        sendMessage(s, message);
//      }
//    }
//  }

  private void handleNewPlayer(GameMessage message, String token) {
    Player player = message.getSender();
    final Long playerId = player.getId();
    player = playerService.find(playerId);

    player.setOnline(true);
    playerService.saveOrCreateOnServer(player);

    channelTokenPeers.put(String.valueOf(playerId), token);
    updatePlayerList();
  }

  public void onClose(String chanelName) {
    String token = channelTokenPeers.get(chanelName);
    if (token == null) {
      return;
    }
    Player player = playerService.find(Long.valueOf(chanelName));

    if (player.isPlaying()) {
      Game game = gameService.findUserGames(player.getId(), 0, 1).get(0);
      final boolean isPlayerHasWhiteColor = game.getPlayerWhite().getId().equals(player.getId());
      GameMessage gameMessage = new GameMessage();
      final Player secondPlayer = isPlayerHasWhiteColor ? game.getPlayerBlack() : game.getPlayerWhite();

      secondPlayer.setPlaying(false);
      playerService.saveOrCreateOnServer(secondPlayer);

      gameMessage.setReceiver(secondPlayer);
      gameMessage.setMessageType(GameMessage.MessageType.PLAY_END);
      sendMessage(String.valueOf(secondPlayer.getId()), gameMessage);
    }

    player.setOnline(false);
    player.setPlaying(false);
    playerService.saveOrCreateOnServer(player);

    channelTokenPeers.remove(chanelName);
    updatePlayerList();
  }

  public void onError(Throwable throwable) {
    throwable.printStackTrace();
  }

  private void handleChatPrivateMessage(GameMessage message) {
    Player receiver = message.getReceiver();
    if (receiver == null) {
      return;
    }
    final String receiverChannel = String.valueOf(receiver.getId());
    String token = channelTokenPeers.get(receiverChannel);
    if (token == null) {
      return;
    }

    saveGameMessage(message);
    sendMessage(receiverChannel, message);
  }

  private void saveGameMessage(GameMessage message) {
    Player playerReceiver = playerService.find(message.getReceiver().getId());
    Player playerSender = playerService.find(message.getSender().getId());
    Game game = message.getGame() != null ? gameService.find(message.getGame().getId()) : null;

    GameMessage gameMessage = new GameMessage();

    gameMessage.setMessageType(message.getMessageType());
    gameMessage.setData(message.getData());
    gameMessage.setMessage(message.getMessage());

    gameMessage.setGame(game);

    if (message.getMove() != null) {
      gameMessage.setMove(new Move(message.getMove()));
      gameMessage.getMove().setGameMessage(gameMessage);
    }

    gameMessage.setReceiver(playerReceiver);
    gameMessage.setSender(playerSender);

    gameMessage.setSentDate(new Date());

    gameMessageService.saveOrCreate(gameMessage);
  }

  private void updatePlayerList() {
    GameMessage gameMessage = new GameMessage();
    gameMessage.setMessageType(GameMessage.MessageType.USER_LIST_UPDATE);
    List<Player> playerList = playerService.findLoggedIn();
    gameMessage.setPlayerList(playerList);
    gameMessageService.saveOrCreate(gameMessage);
    for (String channelName : channelTokenPeers.keySet()) {
      sendMessage(channelName, gameMessage);
    }
  }

  private void sendMessage(String channel, GameMessage message) {
    final String serialized = Utils.serializeToJson(message);
    List<String> chunks = Splitter.fixedLength(1024 * 31).splitToList(serialized);
    for (int i = 0; i < chunks.size(); i++) {
      send(channel, Utils.serializeToJson(new Chunk(chunks.size(), i + 1, chunks.get(i))));
    }
  }

  private void send(String channel, String chunk) {
    ChannelServiceFactory.getChannelService().sendMessage(new ChannelMessage(channel, chunk));
  }
}