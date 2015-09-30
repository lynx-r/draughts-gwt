
package online.shashki.rus.client.application.component.playshowpanel;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.presenter.slots.PermanentSlot;
import com.gwtplatform.mvp.client.proxy.Proxy;
import online.shashki.rus.client.application.component.playrow.PlayRowPresenter;
import online.shashki.rus.shared.model.Game;

import java.util.ArrayList;
import java.util.List;

public class PlayShowPanelPresenter extends Presenter<PlayShowPanelPresenter.MyView, PlayShowPanelPresenter.MyProxy>
    implements PlayShowPanelUiHandlers {

//  private final ArrayList<PlayRowPresenter> rowPresenterList;
//  public static final OrderedSlot<PlayRowPresenter> SLOT_PLAY_ROW = new OrderedSlot<>();
  public static final PermanentSlot<PlayRowPresenter> SLOT_PLAY_2 = new PermanentSlot<>();
  private final PlayRowPresenter rowPresenter;

  @Inject
  PlayShowPanelPresenter(EventBus eventBus,
                         MyView view,
                         MyProxy proxy,
                         PlayRowPresenter.Factory playRowFactory,
                         List<Game> gameList) {
    super(eventBus, view, proxy);

    this.rowPresenter = playRowFactory.create(0, new ArrayList<Game>());
//    rowPresenterList = new ArrayList<>();
//    List<Game> rowGames = new ArrayList<>();
//    int order = 0;
//    for (int i = 0; i < gameList.size(); i++) {
//      if ((i + 1) % 4 == 0) {
//        rowGames.add(gameList.get(i));
//        PlayRowPresenter row = playRowFactory.create(order, rowGames);
//        order++;
//        rowPresenterList.add(row);
//        rowGames.clear();
//      } else {
//        rowGames.add(gameList.get(i));
//      }
//    }
//    if (!rowGames.isEmpty()) {
//      PlayRowPresenter row = playRowFactory.create(order, rowGames);
//      rowPresenterList.add(row);
//    }
  }

  @Override
  protected void onBind() {
    super.onBind();

//    for (PlayRowPresenter playRowPresenter : rowPresenterList) {
//      addToSlot(SLOT_PLAY_ROW, playRowPresenter);
//    }
    setInSlot(SLOT_PLAY_2, rowPresenter);
  }

  public interface ViewFactory {
    MyView create();
  }

  public interface Factory {
    PlayShowPanelPresenter create(List<Game> gameList);
  }

  public static class FactoryImpl implements Factory {
    private final EventBus eventBus;
    private final ViewFactory viewFactory;
    private final PlayRowPresenter.Factory playRowFactory;
    private final MyProxy proxy;

    @Inject
    public FactoryImpl(EventBus eventBus,
                       MyProxy proxy,
                       PlayRowPresenter.Factory playRowFactory,
                       ViewFactory viewFactory) {
      this.eventBus = eventBus;
      this.proxy = proxy;
      this.playRowFactory = playRowFactory;
      this.viewFactory = viewFactory;
    }

    @Override
    public PlayShowPanelPresenter create(List<Game> gameList) {
      return new PlayShowPanelPresenter(eventBus, viewFactory.create(), proxy, playRowFactory, gameList);
    }
  }

  @ProxyCodeSplit
  public interface MyProxy extends Proxy<PlayShowPanelPresenter> {
  }

  public interface MyView extends View, HasUiHandlers<PlayShowPanelUiHandlers> {
  }
}
