
package online.draughts.rus.client.application.component.playshowpanel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import online.draughts.rus.client.application.home.HomeView;
import online.draughts.rus.client.resources.Variables;
import online.draughts.rus.client.util.SHCookies;
import online.draughts.rus.client.util.SHLog;
import online.draughts.rus.shared.model.Game;
import org.gwtbootstrap3.client.ui.Column;
import org.gwtbootstrap3.client.ui.Row;
import org.gwtbootstrap3.client.ui.html.Hr;

import java.util.ArrayList;
import java.util.List;

public class PlayShowPanel extends Composite {

  private static Binder binder = GWT.create(Binder.class);
  private final HomeView homeView;

  @UiField
  HTMLPanel playRowList;

  private final int[] gameOnPanelArr = {1, 2, 3, 4, 6};
  private int gamesOnPanelCounter = 1;
  private List<Game> gameList;
  private int panelHeight;

  private final int incrementSize = 1;

  private boolean updateFlag = true;

  private int lastMaxHeight = 0;
  private int lastScrollPos = 0;

  public PlayShowPanel(HomeView homeView) {
    this.homeView = homeView;
    initWidget(binder.createAndBindUi(this));

    final String marginStr = Variables.S_MAIN_CONTAINER_MARGIN_TOP.substring(0, Variables.S_MAIN_CONTAINER_MARGIN_TOP.length() - 2);
    panelHeight = Window.getClientHeight() - Integer.valueOf(marginStr) - 50;
//    mainScrollPanel.setHeight(panelHeight + "px");
    Window.addWindowScrollHandler(new Window.ScrollHandler() {
      @Override
      public void onWindowScroll(Window.ScrollEvent event) {
        int oldScrollPos = lastScrollPos;
        lastScrollPos = Window.getScrollTop();
        // если листаем вверх
        if (oldScrollPos >= lastScrollPos) {
          return;
        }
        SHLog.debug("SCROLL POS " + lastScrollPos);
        SHLog.debug("OLD POS " + oldScrollPos);
        int maxScrollTop = getOffsetHeight() - Window.getScrollTop();
        SHLog.debug("max scroll top" + maxScrollTop);
        int halfIncrementScrollSize = (maxScrollTop - lastMaxHeight) / 2;
        SHLog.debug("half inc " + halfIncrementScrollSize);
        if (lastScrollPos >= (maxScrollTop - halfIncrementScrollSize) && updateFlag) {
          SHLog.debug("load new games");
          final int newPageSize = gameList.size() + incrementSize;
          PlayShowPanel.this.homeView.getMoreGames(newPageSize);
          SHLog.debug("DISPLAYED GAMES " + gameList.size());
          lastMaxHeight = maxScrollTop;
          updateFlag = false;
        }
        if (maxScrollTop > lastMaxHeight) {
          updateFlag = true;
        }
      }
//      @Override
//      public void onScroll(Window.ScrollEvent event) {
//        int oldScrollPos = lastScrollPos;
//        lastScrollPos = mainScrollPanel.getVerticalScrollPosition();
//        if (oldScrollPos >= lastScrollPos) {
//          return;
//        }
//
//        int maxScrollTop = mainScrollPanel.getWidget().getOffsetHeight() - mainScrollPanel.getOffsetHeight();
//        int halfIncrementScrollSize = (maxScrollTop - lastMaxHeight) / 2;
//        if (lastScrollPos >= (maxScrollTop - halfIncrementScrollSize) && updateFlag) {
//          final int newPageSize = gameList.size() + incrementSize;
//          PlayShowPanel.this.homeView.getMoreGames(newPageSize);
//          SHLog.debug("DISPLAYED GAMES " + gameList.size());
//          lastMaxHeight = maxScrollTop;
//          updateFlag = false;
//        }
//        if (maxScrollTop > lastMaxHeight) {
//          updateFlag = true;
//        }
//      }
    });
  }

  public void setGames(List<Game> gameList) {
    this.gameList = gameList;
    updateGameListPanel();
  }

  private void updateGameListPanel() {
    if (gameList == null || gameList.isEmpty()) {
      SHLog.debug("EMPTY GAME LIST");
      return;
    }
    gamesOnPanelCounter = SHCookies.getGamesOnPageCounter();
    if (gamesOnPanelCounter <= 0) {
      homeView.setEnableLessGameButton(false);
    }
    if (gamesOnPanelCounter >= 4) {
      homeView.setEnableMoreGameButton(false);
    }
    playRowList.clear();
    List<Game> rowGameList = new ArrayList<>();
    final int gameInRow = gameOnPanelArr[gamesOnPanelCounter];
    SHLog.debug("GAME IN ROW " + gameInRow + " " + gamesOnPanelCounter);
    for (int i = 0; i < gameList.size(); i++) {
      if ((i + 1) % gameInRow == 0) {
        rowGameList.add(gameList.get(i));
        addGameRow(rowGameList, gameInRow);
        rowGameList.clear();
      } else {
        rowGameList.add(gameList.get(i));
      }
    }
    if (!rowGameList.isEmpty()) {
      addGameRow(rowGameList, gameInRow);
    }
  }

  private void addGameRow(List<Game> rowGameList, int gameInRow) {
    Row row = new Row();
    if (gameInRow == 1) {
      gameInRow = 2;
    }
    for (Game game : rowGameList) {
      Column column = new Column("MD_" + Variables.COLUMNS_IN_LAYOUT / gameInRow);
      column.add(new PlayItem(homeView.getPlayer(), game));
      row.add(column);
    }
    playRowList.add(row);
    playRowList.add(new Hr());
  }

  public void moreGameOnPanel() {
    gamesOnPanelCounter++;
    SHCookies.setGamesOnPageCounter(gamesOnPanelCounter);
    if (!homeView.isEnabledLessGameButton()) {
      homeView.setEnableLessGameButton(true);
    }
    updateGameListPanel();
    if (gamesOnPanelCounter == gameOnPanelArr.length - 1) {
      homeView.setEnableMoreGameButton(false);
    }
  }

  public void lessGameOnPanel() {
    gamesOnPanelCounter--;
    SHCookies.setGamesOnPageCounter(gamesOnPanelCounter);
    if (!homeView.isEnabledMoreGameButton()) {
      homeView.setEnableMoreGameButton(true);
    }
    updateGameListPanel();
    if (gamesOnPanelCounter == 0) {
      homeView.setEnableLessGameButton(false);
    }
  }

  interface Binder extends UiBinder<HTMLPanel, PlayShowPanel> {
  }
}
