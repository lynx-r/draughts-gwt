package online.draughts.rus.client.application.analysis;

import com.ait.lienzo.client.core.shape.Layer;
import com.ait.lienzo.client.core.shape.Rectangle;
import com.ait.lienzo.client.widget.LienzoPanel;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import online.draughts.rus.client.resources.AppResources;
import online.draughts.rus.draughts.Board;
import online.draughts.rus.draughts.BoardBackgroundLayer;
import online.draughts.rus.draughts.PlayComponent;
import online.draughts.rus.draughts.Stroke;
import online.draughts.rus.shared.dto.MoveDto;
import online.draughts.rus.shared.dto.PlayerDto;
import online.draughts.rus.shared.locale.DraughtsMessages;
import org.gwtbootstrap3.client.ui.*;

import javax.inject.Inject;


public class AnalysisView extends ViewWithUiHandlers<AnalysisUiHandlers>
    implements AnalysisPresenter.MyView, PlayComponent {

  @UiField
  HTMLPanel main;
  @UiField
  HTMLPanel draughtsDesk;
  @UiField
  Column whiteDeskColumn;
  @UiField
  Button placeDraughts;
  @UiField
  Button clearDesk;
  @UiField
  HTML turnLabel;
  @UiField
  RadioButton whiteDraughtButton;
  @UiField
  RadioButton blackDraughtButton;
  @UiField
  CheckBox addDraughtCheckbox;
  @UiField
  Button myPlaceDraughts;
  @UiField
  CheckBox addQueenCheckbox;
  @UiField
  Image blackDraughtImage;
  @UiField
  Image whiteDraughtImage;
  @UiField
  RadioButton removeDraughtButton;
  private final DraughtsMessages messages;
  private final AppResources resources;
  private Desk desk;

  @Inject
  AnalysisView(Binder uiBinder,
               DraughtsMessages messages,
               AppResources resources) {
    this.messages = messages;
    this.resources = resources;
    initWidget(uiBinder.createAndBindUi(this));
    handlers();
  }

  private void handlers() {
    addQueenCheckbox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
      @Override
      public void onValueChange(ValueChangeEvent<Boolean> event) {
        onAddQueenCheckboxClicked(event.getValue());
      }
    });
  }

  @Override
  protected void onAttach() {
    if (null == desk) {
      desk = initEmptyDesk(draughtsDesk, whiteDeskColumn.getOffsetWidth(), true);
    }
    addDraughtChangeState();
  }

  @SuppressWarnings("unused")
  @UiHandler("placeDraughts")
  public void onPlaceDraughtsClicked(ClickEvent clickEvent) {
    desk = placeDraughtsOnDesk(desk, true, false);
    addDraughtCheckbox.setEnabled(false);
  }

  public void onAddQueenCheckboxClicked(Boolean value) {
    if (value) {
      blackDraughtImage.setResource(resources.images().blackQueen());
      whiteDraughtImage.setResource(resources.images().whiteQueen());
    } else {
      blackDraughtImage.setResource(resources.images().blackDraught());
      whiteDraughtImage.setResource(resources.images().whiteDraught());
    }
  }

  @SuppressWarnings("unused")
  @UiHandler("addDraughtCheckbox")
  public void onAddDraughtCheckboxClicked(ClickEvent clickEvent) {
    addDraughtChangeState();
  }

  @SuppressWarnings("unused")
  @UiHandler("myPlaceDraughts")
  public void onMyPlaceDraughtsClicked(ClickEvent clickEvent) {
    desk = placeDraughtsOnDesk(desk, true, true);
    addDraughtCheckbox.setEnabled(true);
    addDraughtCheckbox.setValue(true);
    addDraughtChangeState();
  }

  private void addDraughtChangeState() {
    whiteDraughtButton.setEnabled(addDraughtCheckbox.getValue());
    blackDraughtButton.setEnabled(addDraughtCheckbox.getValue());
    addQueenCheckbox.setEnabled(addDraughtCheckbox.getValue());
    removeDraughtButton.setEnabled(addDraughtCheckbox.getValue());
    whiteDraughtButton.setValue(addDraughtCheckbox.getValue());
    blackDraughtButton.setValue(false);
  }

  private Desk placeDraughtsOnDesk(Desk desk, boolean white, boolean clear) {
    Board board = new Board(desk.backgroundLayer, 8, 8, white);
    board.setSelfPlay(true);
    if (clear) {
      board.clearDesk();
    }
    board.setView(this);
    desk.lienzoPanel.add(board);
    desk.lienzoPanel.getElement().getStyle().setCursor(Style.Cursor.POINTER);
    desk.board = board;
    return desk;
  }

  @SuppressWarnings("unused")
  @UiHandler("clearDesk")
  public void onClearPlayComponent(ClickEvent clickEvent) {
    desk.lienzoPanel.removeAll();
    desk.board.clearDesk();
    draughtsDesk.remove(desk.lienzoPanel);
    desk = initEmptyDesk(draughtsDesk, draughtsDesk.getOffsetWidth(), true);
    addDraughtCheckbox.setValue(false);
    addDraughtChangeState();
  }

  private Desk initEmptyDesk(HTMLPanel draughtsDesk, int draughtsSide, boolean white) {
    final LienzoPanel lienzoPanel = new LienzoPanel(draughtsSide, draughtsSide);
    int lienzoSide = lienzoPanel.getHeight() - 30;
    final Layer initDeskRect = new Layer();
    Rectangle contour = new Rectangle(lienzoSide, lienzoSide);
    contour.setX(1);
    contour.setY(1);
    initDeskRect.add(contour);
    lienzoPanel.setBackgroundLayer(initDeskRect);
    draughtsDesk.add(lienzoPanel);

    lienzoSide = lienzoSide - 30;
    BoardBackgroundLayer boardBackgroundLayer = new BoardBackgroundLayer(
        lienzoSide, lienzoSide - 30,
        8, 8);
    boardBackgroundLayer.drawCoordinates(white);
    lienzoPanel.setBackgroundLayer(boardBackgroundLayer);

    final Desk desk = new Desk();
    desk.lienzoPanel = lienzoPanel;
    desk.backgroundLayer = boardBackgroundLayer;

    lienzoPanel.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        if (null == desk.board) {
          return;
        }
        if (removeDraughtButton.getValue()) {
          desk.board.removeDraughtFrom(event.getX(), event.getY());
        } else {
          if (whiteDraughtButton.getValue()) {
            desk.board.addDraught(event.getX(), event.getY(), true, addQueenCheckbox.getValue());
          } else if (blackDraughtButton.getValue()) {
            desk.board.addDraught(event.getX(), event.getY(), false, addQueenCheckbox.getValue());
          }
        }
      }
    });

    return desk;
  }

  @Override
  public void checkWinner() {

  }

  @Override
  public void addNotationStroke(Stroke strokeForNotation) {

  }

  @Override
  public void toggleTurn(boolean turn) {
    if (turn) {
      turnLabel.setHTML(messages.whitesTurn());
    } else {
      turnLabel.setHTML(messages.blacksTurn());
    }
  }

  @Override
  public void doPlayerMove(MoveDto move) {

  }

  @Override
  public PlayerDto getPlayer() {
    return null;
  }

  @Override
  public PlayerDto getOpponent() {
    return null;
  }

  @Override
  public String takeScreenshot() {
    return null;
  }

  private static class Desk {
    LienzoPanel lienzoPanel;
    Board board;
    BoardBackgroundLayer backgroundLayer;
  }

  interface Binder extends UiBinder<Widget, AnalysisView> {
  }
}
