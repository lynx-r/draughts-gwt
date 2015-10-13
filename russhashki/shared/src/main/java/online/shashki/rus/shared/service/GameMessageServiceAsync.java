package online.shashki.rus.shared.service;

import com.google.gwt.user.client.rpc.AsyncCallback;
import online.shashki.rus.shared.model.GameMessage;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: alekspo
 * Date: 30.12.14
 * Time: 9:31
 */
public interface GameMessageServiceAsync {

  void getLastPlayerMessages(int countLast, Long playerId, Long opponentId, AsyncCallback<List<GameMessage>> async);
}