package online.shashki.rus.client.application.profile.settings;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.rest.delegates.client.ResourceDelegate;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.presenter.slots.NestedSlot;
import online.shashki.rus.client.application.widget.dialog.ErrorDialogBox;
import online.shashki.rus.client.application.widget.growl.Growl;
import online.shashki.rus.client.event.UpdateAllPlayerListEvent;
import online.shashki.rus.client.util.SHLog;
import online.shashki.rus.client.websocket.PlaySession;
import online.shashki.rus.shared.locale.DraughtsMessages;
import online.shashki.rus.shared.model.Player;
import online.shashki.rus.shared.rest.PlayersResource;


public class SettingsPresenter extends PresenterWidget<SettingsPresenter.MyView> implements SettingsUiHandlers {
  public static final NestedSlot SLOT_SETTINGS = new NestedSlot();
  private final DraughtsMessages messages;
  private final EventBus eventBus;
  private final ResourceDelegate<PlayersResource> playersDelegate;
  private final PlaySession playSession;
  private Player player;

  SettingsPresenter(
      EventBus eventBus,
      MyView view,
      DraughtsMessages messages,
      ResourceDelegate<PlayersResource> playersDelegate,
      PlaySession playSession,
      Player player) {
    super(eventBus, view);

    this.eventBus = eventBus;
    this.playersDelegate = playersDelegate;
    this.playSession = playSession;
    this.player = player;
    this.messages = messages;

    getView().setUiHandlers(this);
    getView().setPlayerName(player.getPublicName());
  }

  @Override
  public void submitNewPlayerName(String playerName) {
    player.setPlayerName(playerName);
    SHLog.debug("UPDATE PLAYER: " + player);
    playersDelegate.withCallback(new AsyncCallback<Player>() {
      @Override
      public void onFailure(Throwable caught) {
        ErrorDialogBox.setMessage(caught).show();
      }

      @Override
      public void onSuccess(Player result) {
        SettingsPresenter.this.player.setPlayerName(result.getPlayerName());
        Growl.growlNotif(messages.profileUpdated());
        if (playSession.isConnected()) {
          fireEvent(new UpdateAllPlayerListEvent());
        }
      }
    }).saveOrCreate(player);
  }

  public interface ViewFactory {
    MyView create();
  }

  public interface Factory {
    SettingsPresenter create(Player player);
  }

  public static class FactoryImpl implements Factory {

    private final EventBus eventBus;
    private final ViewFactory viewFactory;
    private final DraughtsMessages messages;
    private final ResourceDelegate<PlayersResource> playersDelegate;
    private final PlaySession playSession;

    @Inject
    FactoryImpl(EventBus eventBus,
                ViewFactory viewFactory,
                DraughtsMessages messages,
                ResourceDelegate<PlayersResource> playersDelegate,
                PlaySession playSession) {
      this.eventBus = eventBus;
      this.viewFactory = viewFactory;
      this.messages = messages;
      this.playersDelegate = playersDelegate;
      this.playSession = playSession;
    }

    public SettingsPresenter create(Player player) {
      return new SettingsPresenter(eventBus, viewFactory.create(), messages, playersDelegate, playSession, player);
    }
  }

  public interface MyView extends View, HasUiHandlers<SettingsUiHandlers> {
    void setPlayerName(String playerName);
  }
}