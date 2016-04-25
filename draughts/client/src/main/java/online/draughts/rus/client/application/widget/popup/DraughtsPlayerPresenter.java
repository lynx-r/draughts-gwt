package online.draughts.rus.client.application.widget.popup;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.PresenterWidget;
import online.draughts.rus.client.event.CheckWinnerEvent;
import online.draughts.rus.client.event.GameStuckEvent;
import online.draughts.rus.client.event.TurnChangeEvent;
import online.draughts.rus.shared.dto.GameDto;


public class DraughtsPlayerPresenter extends PresenterWidget<DraughtsPlayerPresenter.MyView> implements DraughtsPlayerUiHandlers {

  DraughtsPlayerPresenter(
      EventBus eventBus,
      MyView view) {
    super(eventBus, view);

    getView().setUiHandlers(this);
  }

  public interface ViewFactory {
    MyView create(GameDto game);
  }

  public interface Factory {
    DraughtsPlayerPresenter create(GameDto game);
  }

  public static class FactoryImpl implements Factory {

    private final EventBus eventBus;
    private final ViewFactory viewFactory;

    @Inject
    FactoryImpl(EventBus eventBus,
                ViewFactory viewFactory) {
      this.eventBus = eventBus;
      this.viewFactory = viewFactory;
    }

    public DraughtsPlayerPresenter create(GameDto game) {
      return new DraughtsPlayerPresenter(eventBus, viewFactory.create(game));
    }
  }

  @Override
  public void checkWinner() {
    fireEvent(new CheckWinnerEvent());
  }

  @Override
  public void gameStuck(boolean isWhite) {
    fireEvent(new GameStuckEvent(isWhite));
  }

  @Override
  public void toggleTurn(boolean turn) {
    fireEvent(new TurnChangeEvent(turn));
  }

  public interface MyView extends PopupView, HasUiHandlers<DraughtsPlayerUiHandlers> {
  }
}
