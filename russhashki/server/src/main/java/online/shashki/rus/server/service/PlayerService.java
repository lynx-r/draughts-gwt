package online.shashki.rus.server.service;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import online.shashki.rus.server.dao.PlayerDao;
import online.shashki.rus.shared.model.Friend;
import online.shashki.rus.shared.model.Player;

import javax.servlet.http.HttpSession;
import java.util.*;

@Singleton
public class PlayerService {
  private Provider<PlayerDao> playerDaoProvider;

  @Inject
  PlayerService(
      Provider<PlayerDao> playerDaoProvider) {
    this.playerDaoProvider = playerDaoProvider;
  }

  public Player saveOrCreate(Player player) {
    return saveOrCreate(player, false);
  }

  public Player saveOrCreateOnServer(Player player) {
    return saveOrCreate(player, true);
  }

  private Player saveOrCreate(Player player, boolean serverSide) {
    if (player != null && player.getId() == null) {
      playerDaoProvider.get().create(player);
      return player;
    }

    if (player == null) {
      return null;
    }

    Player playerById = playerDaoProvider.get().findById(player.getId());

    if (playerById != null) {
      playerById.updateSerializable(player);
      playerDaoProvider.get().edit(playerById);
      return playerById;
    }
    return null;
  }

  public Player getLoggedInUser(HttpSession session) {
    if (session != null) {
      return playerDaoProvider.get().findBySessionId(session.getId());
    }
    return null;
  }

  public Player findByVkId(String vkId) {
    return playerDaoProvider.get().findByVkId(vkId);
  }

  public Player findBySessionId(String sessionId) {
    return playerDaoProvider.get().findBySessionId(sessionId);
  }

  public Player find(Long playerId) {
    return playerDaoProvider.get().find(playerId);
  }

  public List<Player> findAll() {
    return playerDaoProvider.get().findAll();
  }

  public Player findByFbId(String user_id) {
    return playerDaoProvider.get().findByFbId(user_id);
  }

  public Player findByGoogleSub(String sub) {
    return playerDaoProvider.get().findByGoogleSub(sub);
  }

  public List<Player> findLoggedIn() {
    return playerDaoProvider.get().findLoggedIn();
  }

  public List<Player> findFriends(Long playerId) {
    List<Friend> friends = new ArrayList<>(playerDaoProvider.get().findFriends(playerId));
    Collections.sort(friends, new Comparator<Friend>() {
      @Override
      public int compare(Friend o1, Friend o2) {
        return -Boolean.valueOf(o1.isFavorite()).compareTo(o2.isFavorite());
      }
    });
    List<Player> playerList = new ArrayList<>(friends.size());
    for (Friend friend : friends) {
      playerList.add(friend.getPk().getFriend());
    }
    Collections.sort(playerList, new Comparator<Player>() {
      @Override
      public int compare(Player o1, Player o2) {
        return -Integer.valueOf(o1.getRating()).compareTo(o2.getRating());
      }
    });
    return playerList;
  }
}
