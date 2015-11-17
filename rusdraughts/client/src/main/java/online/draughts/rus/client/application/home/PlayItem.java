
package online.draughts.rus.client.application.home;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import online.draughts.rus.client.application.widget.popup.DraughtsPlayerPresenter;
import online.draughts.rus.client.resources.AppResources;
import online.draughts.rus.client.util.TrUtils;
import online.draughts.rus.shared.locale.DraughtsMessages;
import online.draughts.rus.shared.model.Game;
import online.draughts.rus.shared.model.Player;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Image;

import java.util.Date;

public class PlayItem extends Composite {

  private final DraughtsMessages messages = GWT.create(DraughtsMessages.class);
  private final AppResources resources = GWT.create(AppResources.class);
  private final static String PLAYER_COLOR_DELIMITER = ": ";
  private final int gamesInRow;
  private DraughtsPlayerPresenter draughtsPlayer;

  @UiField
  HTMLPanel panel;
  @UiField
  HTML whitePlayerName;
  @UiField
  HTML blackPlayerName;
  @UiField
  HTML whoDidWin;
  @UiField
  Image endGameScreenshot;
  @UiField
  HTML playEndDate;
  @UiField
  Button playGame;
  @UiField
  HTMLPanel whoAndWhenDidWin;

  @Inject
  PlayItem(Binder binder,
           final DraughtsPlayerPresenter.Factory draughtsPlayerFactory,
           final HomePresenter homePresenter,
           @Assisted int gamesInRow,
           @Assisted Player player,
           @Assisted final Game game) {
    initWidget(binder.createAndBindUi(this));

    this.gamesInRow = gamesInRow;
    panel.addStyleName(resources.style().playItem());
    setGame(player, game);

    playGame.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        draughtsPlayer = draughtsPlayerFactory.create(game);
        homePresenter.addToPopupSlot(draughtsPlayer);
      }
    });
  }

  public void setGame(Player player, Game game) {
    // todo идентификатор игры в качестве анчора
    if (game.getPlayEndStatus() != null) {
      whoDidWin.setHTML(TrUtils.translateEndGame(game.getPlayEndStatus()));
    }
    final Date playFinishDate = game.getPlayFinishDate();
    if (playFinishDate != null) {
      final DateTimeFormat dateTimeFromat;
      switch (gamesInRow) {
        case 2:
          dateTimeFromat = DateTimeFormat.getFormat("EEEE, dd MMMM yyyy, HH:mm:ss");
          break;
        case 4:
          dateTimeFromat = DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_TIME_MEDIUM);
          break;
        case 6:
          dateTimeFromat = DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_TIME_SHORT);
          break;
        default:
          dateTimeFromat = DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_TIME_MEDIUM);
          break;
      }
      String date = DateTimeFormat.getFormat(dateTimeFromat.getPattern())
          .format(playFinishDate);
      playEndDate.setHTML(date);
    }
    if (game.getEndGameScreenshot() != null) {
      endGameScreenshot.setUrl(game.getEndGameScreenshot());
      endGameScreenshot.setResponsive(true);
    }

    if (player != null
        && (player.getId().equals(game.getPlayerBlack().getId())
        || player.getId().equals(game.getPlayerWhite().getId()))) {
      whitePlayerName.setHTML(messages.white() + PLAYER_COLOR_DELIMITER + game.getPlayerWhite().getPublicName());
      whitePlayerName.getElement().addClassName(resources.style().playItemPlayerName());
      blackPlayerName.setHTML(messages.black() + PLAYER_COLOR_DELIMITER + game.getPlayerBlack().getPublicName());
      blackPlayerName.getElement().addClassName(resources.style().playItemPlayerName());
    }
  }

  interface Binder extends UiBinder<HTMLPanel, PlayItem> {
  }
}
