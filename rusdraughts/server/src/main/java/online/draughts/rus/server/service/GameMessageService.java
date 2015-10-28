package online.draughts.rus.server.service;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import online.draughts.rus.server.dao.GameMessageDao;
import online.draughts.rus.shared.model.GameMessage;

import java.util.logging.Logger;

@Singleton
public class GameMessageService {
  private Logger logger;
  private Provider<GameMessageDao> gameMessageDaoProvider;

  @Inject
  GameMessageService(
      Logger logger,
      Provider<GameMessageDao> gameDaoProvider) {
    this.logger = logger;
    this.gameMessageDaoProvider = gameDaoProvider;
  }

  public void saveOrCreate(GameMessage gameMessage) {
    if (gameMessage == null) {
      return;
    }

    logger.info("New message: " + gameMessage.toString());
    if (gameMessage.getId() == null) {
      gameMessageDaoProvider.get().create(gameMessage);
    } else {
      gameMessageDaoProvider.get().edit(gameMessage);
    }
  }
}
