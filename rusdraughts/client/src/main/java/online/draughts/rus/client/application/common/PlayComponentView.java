package online.draughts.rus.client.application.common;

import com.ait.lienzo.client.core.shape.Layer;
import com.ait.lienzo.client.core.shape.Rectangle;
import com.ait.lienzo.client.core.shape.Text;
import com.ait.lienzo.client.widget.LienzoPanel;
import com.ait.lienzo.shared.core.types.DataURLType;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import online.draughts.rus.client.application.widget.NotationPanel;
import online.draughts.rus.client.application.widget.dialog.ConfirmeDialogBox;
import online.draughts.rus.client.application.widget.dialog.InviteDialogBox;
import online.draughts.rus.client.application.widget.growl.Growl;
import online.draughts.rus.client.channel.PlaySession;
import online.draughts.rus.client.gin.NotationPanelFactory;
import online.draughts.rus.client.resources.AppResources;
import online.draughts.rus.draughts.Board;
import online.draughts.rus.draughts.BoardBackgroundLayer;
import online.draughts.rus.draughts.PlayComponent;
import online.draughts.rus.draughts.Stroke;
import online.draughts.rus.shared.dto.FriendDto;
import online.draughts.rus.shared.dto.MoveDto;
import online.draughts.rus.shared.dto.PlayerDto;
import online.draughts.rus.shared.locale.DraughtsMessages;
import online.draughts.rus.shared.util.StringUtils;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.gwt.ButtonCell;
import org.gwtbootstrap3.client.ui.gwt.CellTable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PlayComponentView extends ViewWithUiHandlers<PlayComponentUiHandlers>
    implements PlayComponentPresenter.MyView, PlayComponent {

  private final DraughtsMessages messages;
  private final AppResources resources;
  private final NotationPanelFactory notationPanelFactory;
  private final PlaySession playSession;
  @UiField
  HTMLPanel main;
  @UiField
  HTMLPanel draughtsDesk;
  @UiField
  org.gwtbootstrap3.client.ui.Column draughtsColumn;
  @UiField
  HTMLPanel notationColumn;
  @UiField
  HTMLPanel notationList;
  @UiField
  Button playButton;
  @UiField
  Button drawButton;
  @UiField
  Button surrenderButton;
  @UiField
  HTMLPanel playerListColumn;
  @UiField
  HTML turnLabel;
  @UiField
  Label beatenOpponentDraughtsLabel;
  @UiField
  Label beatenMineDraughtsLabel;
  @UiField
  Button cancelMove;
  private Board board;
  private LienzoPanel lienzoPanel;
  private PlayerDto player;
  @UiField
  CellTable<FriendDto> friendCellTable;
  @UiField
  CellTable<PlayerDto> playerCellTable;
  @UiField
  HTMLPanel infoHTMLPanel;
  @UiField
  HTMLPanel notationHTMLPanel;
  @UiField
  ScrollPanel playerCellTablePanel;
  @UiField
  ScrollPanel friendCellTablePanel;
  @UiField
  TextBox playerSearch;
  @UiField
  Label timeLabel;
  @UiField
  Label fisherTimeLabel;
  SingleSelectionModel<FriendDto> playerFriendSelectionModel;
  SingleSelectionModel<PlayerDto> playerSelectionModel;
  private boolean prevSelected = false;
  private NotationPanel notationPanel;
  private InviteDialogBox inviteDialogBox;
  private boolean opponentColor;
  private PlayerDto opponent;
  private Map<Long, Integer> unreadMessagesMap;
  private List<PlayerDto> playerList;
  private List<PlayerDto> playerOrigList;
  private List<FriendDto> friendList;
  private String timeOnPlay;
  private String fisherTime;

  @Inject
  PlayComponentView(Binder binder,
                    DraughtsMessages messages,
                    AppResources resources,
                    PlaySession playSession,
                    NotationPanelFactory notationPanelFactory) {
    this.messages = messages;
    this.resources = resources;
    this.notationPanelFactory = notationPanelFactory;
    this.playSession = playSession;

    initWidget(binder.createAndBindUi(this));
    initPlayerFriendsCellList();
    initPlayersCellList();
  }

  @Override
  protected void onAttach() {
    initEmptyDeskPanel();
    playerListColumn.setHeight(draughtsColumn.getOffsetHeight() + "px");
    int axillaryWidgets = 80;
    playerCellTablePanel.setHeight(playerListColumn.getOffsetHeight() / 2 - axillaryWidgets + "px");
    friendCellTablePanel.setHeight(playerListColumn.getOffsetHeight() / 2 - axillaryWidgets + "px");
  }

  @SuppressWarnings(value = "unused")
  @UiHandler("playButton")
  public void onConnectToServer(ClickEvent event) {
    if (IconType.REFRESH.equals(playButton.getIcon())) {
      getUiHandlers().refreshConnectionToServer();
      player = playSession.getPlayer();
      playerSearch.setEnabled(true);
    } else {
      PlayerDto selectedPlayer = null;
      if (playerSelectionModel.getSelectedObject() != null) {
        selectedPlayer = playerSelectionModel.getSelectedObject();
      } else if (playerFriendSelectionModel.getSelectedObject() != null) {
        selectedPlayer = playerFriendSelectionModel.getSelectedObject().getFriendOf();
      }
      getUiHandlers().startPlayWith(selectedPlayer);
    }
  }

  @SuppressWarnings(value = "unused")
  @UiHandler("playerSearch")
  public void onPlayerSearchKeyDown(KeyPressEvent event) {
    final String searchText = playerSearch.getText().toUpperCase();
    if (StringUtils.isEmpty(searchText)) {
      setPlayerList(playerOrigList);
      return;
    }
    List<PlayerDto> filtered = new ArrayList<>(playerList.size());
    for (PlayerDto playerDto : playerOrigList) {
      if (playerDto.getPublicName().toUpperCase().contains(searchText)) {
        filtered.add(playerDto);
      }
    }
    this.playerList = filtered;
    playerCellTable.setRowCount(0);
    playerCellTable.setRowData(filtered);
  }

  @SuppressWarnings(value = "unused")
  @UiHandler("drawButton")
  public void onDrawButton(ClickEvent event) {
    new ConfirmeDialogBox(messages.doYouWantToProposeDraw()) {
      @Override
      public void procConfirm() {
        if (isConfirmed()) {
          getUiHandlers().proposeDraw();
        }
      }
    };
  }

  @SuppressWarnings(value = "unused")
  @UiHandler("surrenderButton")
  public void onSurrenderButton(ClickEvent event) {
    new ConfirmeDialogBox(messages.areYouSureYouWantSurrender()) {
      @Override
      public void procConfirm() {
        if (isConfirmed()) {
          getUiHandlers().playerSurrendered();
        }
      }
    };
  }

  @UiHandler("cancelMove")
  @SuppressWarnings(value = "unused")
  public void onCancelMove(ClickEvent event) {
    if (board == null) {
      return;
    }
    final Stroke lastStroke = board.getLastMove();
    final Stroke lastOpponentMove = board.getLastOpponentMove();
    if (lastOpponentMove != null && lastOpponentMove.isContinueBeat()) {
      return;
    }
    if (board.isMyTurn() && !(lastStroke == null || lastStroke.isContinueBeat()) || lastStroke == null) {
      Growl.growlNotif(messages.youDontMove());
      return;
    }
    new ConfirmeDialogBox(messages.doYouWantToCancelMove()) {
      @Override
      public void procConfirm() {
        if (isConfirmed()) {
          getUiHandlers().proposeCancelMove(lastStroke);
        }
      }
    };
  }

  private void initEmptyDeskPanel() {
    if (null != lienzoPanel) {
      return;
    }
    int draughtsSide = draughtsColumn.getOffsetWidth();

    lienzoPanel = new LienzoPanel(draughtsSide, draughtsSide);
    int lienzoSide = lienzoPanel.getHeight() - 20;
    final Layer initDeskRect = new Layer();
    Rectangle contour = new Rectangle(lienzoSide, lienzoSide);
    contour.setX(1);
    contour.setY(1);
    initDeskRect.add(contour);
    String[] descriptions = messages.playStartDescription().split("\n");
    int y = 0;
    for (String description : descriptions) {
      Text greeting = new Text(description, "Times New Roman", 14);
      greeting.setFillColor("blue");
      greeting.setStrokeColor("blue");
      greeting.setY(lienzoSide / 2 - 20 + y);
      greeting.setX(lienzoSide / 2 - 180);
      initDeskRect.add(greeting);
      y += 20;
    }
    lienzoPanel.setBackgroundLayer(initDeskRect);
    draughtsDesk.add(lienzoPanel);
    cancelMove.setEnabled(false);
  }

  @Override
  public void initNotationPanel(Long gameId) {
    if (notationPanel != null) {
      notationList.remove(notationPanel);
    }
    notationPanel = notationPanelFactory.createNotationPanel(gameId);
    notationList.add(notationPanel);
    notationHTMLPanel.setHeight((notationColumn.getOffsetHeight() - infoHTMLPanel.getOffsetHeight()) + "px");
  }

  @Override
  public void setUnreadMessagesMapForPlayers(Map<Long, Integer> result) {
    this.unreadMessagesMap = result;
    setPlayerList(playerList);
  }

  @Override
  public String getTimeOnPlay() {
    return timeOnPlay;
  }

  @Override
  public String getFisherTime() {
    return fisherTime;
  }

  @Override
  public void setTime(String time) {
    timeLabel.setText(time);
  }

  @Override
  public void setFisherTime(String fisherTime) {
    fisherTimeLabel.setText(fisherTime);
  }

  @Override
  public void setUnreadMessagesMapForFriends(Map<Long, Integer> result) {
    this.unreadMessagesMap = result;
    setFriendList(friendList);
  }

  private void initPlayerFriendsCellList() {
    playerFriendSelectionModel = new SingleSelectionModel<>();
    friendCellTable.setSelectionModel(playerFriendSelectionModel);
    final HTML connectToServer = new HTML();
    connectToServer.setHTML(messages.connectToServer());
    connectToServer.getElement().getStyle().setFontSize(12, Style.Unit.PX);
    friendCellTable.setLoadingIndicator(connectToServer);
    friendCellTable.getElement().getStyle().setCursor(Style.Cursor.POINTER);
    playerFriendSelectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
      @Override
      public void onSelectionChange(SelectionChangeEvent event) {
        resetPlayerSelection(playerSelectionModel);
      }
    });

    Column<FriendDto, SafeHtml> statusColumn = new Column<FriendDto, SafeHtml>(new SafeHtmlCell()) {
      @Override
      public SafeHtml getValue(FriendDto friend) {
        return getStatusSafeHtml(friend.getFriendOf());
      }
    };
    statusColumn.setCellStyleNames(resources.style().cellWithButton());
    friendCellTable.addColumn(statusColumn);

    TextColumn<FriendDto> publicNameColumn = new TextColumn<FriendDto>() {
      @Override
      public String getValue(FriendDto friend) {
        return friend.getFriendOf().getPublicName();
      }
    };
    publicNameColumn.setCellStyleNames(resources.style().cellWithButton());
    friendCellTable.addColumn(publicNameColumn);

    final ButtonCell favoriteButtonCell = new ButtonCell();
    final Column<FriendDto, String> favoriteColumn = new Column<FriendDto, String>(favoriteButtonCell) {
      @Override
      public String getValue(FriendDto object) {
        return "";
      }

      @Override
      public void render(Cell.Context context, FriendDto object, SafeHtmlBuilder sb) {
        sb.appendHtmlConstant("<button type=\"button\" class=\"btn btn-link btn-large pull-right\" tabindex=\"-1\">");
        if (object != null) {
          Icon icon = new Icon(object.isFavorite() ? IconType.STAR : IconType.STAR_O);
          sb.appendHtmlConstant(icon.getElement().getString());
        }
        sb.appendHtmlConstant("</button>");
      }
    };
    favoriteColumn.setFieldUpdater(new FieldUpdater<FriendDto, String>() {
      @Override
      public void update(int index, FriendDto object, String value) {
        if (object != null) {
          object.setFavorite(!object.isFavorite());
          getUiHandlers().saveFriend(object);
          friendList.get(friendList.indexOf(object)).setFavorite(object.isFavorite());
          friendCellTable.setRowCount(0);
          friendCellTable.setRowData(friendList);
        }
      }
    });
    friendCellTable.addColumn(favoriteColumn);

    final ButtonCell writeButtonCell = new ButtonCell(ButtonType.LINK, IconType.PENCIL);
    final Column<FriendDto, String> writeColumn = new Column<FriendDto, String>(writeButtonCell) {
      @Override
      public String getValue(FriendDto object) {
        return "";
      }

      @Override
      public void render(Cell.Context context, FriendDto object, SafeHtmlBuilder sb) {
        if (object.getFriendOf().getId() != player.getId()) {
          super.render(context, object, sb);
        }
      }
    };
    writeColumn.setFieldUpdater(new FieldUpdater<FriendDto, String>() {
      @Override
      public void update(int index, FriendDto object, String value) {
        unreadMessagesMap.remove(object.getMe().getId());
        getUiHandlers().writeToFriend(object.getFriendOf());
      }
    });
    friendCellTable.addColumn(writeColumn);

    TextColumn<FriendDto> newMessagesColumn = new TextColumn<FriendDto>() {
      @Override
      public String getValue(FriendDto friend) {
        if (unreadMessagesMap != null && unreadMessagesMap.containsKey(friend.getFriendOf().getId())) {
          return String.valueOf(unreadMessagesMap.get(friend.getFriendOf().getId()));
        }
        return "";
      }

      @Override
      public void render(Cell.Context context, FriendDto object, SafeHtmlBuilder sb) {
        if (unreadMessagesMap == null) {
          return;
        }
        final Integer unreadMsg = unreadMessagesMap.get(object.getFriendOf().getId());
        sb.appendHtmlConstant("<span class=\""
            + (unreadMsg != null ? resources.style().newMessageCircle() : "") + "\" " +
            ">");
        if (unreadMsg != null) {
          sb.append(unreadMsg);
        }
        sb.appendHtmlConstant("</span>");
      }
    };
    newMessagesColumn.setCellStyleNames(resources.style().cellWithButton());
    friendCellTable.addColumn(newMessagesColumn);
  }

  private void initPlayersCellList() {
    playerSelectionModel = new SingleSelectionModel<>();
    playerCellTable.setSelectionModel(playerSelectionModel);
    final HTML connectToServer = new HTML();
    connectToServer.setHTML(messages.connectToServer());
    connectToServer.getElement().getStyle().setFontSize(12, Style.Unit.PX);
    playerCellTable.setLoadingIndicator(connectToServer);
    playerCellTable.getElement().getStyle().setCursor(Style.Cursor.POINTER);
    playerSelectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
      @Override
      public void onSelectionChange(SelectionChangeEvent event) {
        resetPlayerSelection(playerFriendSelectionModel);
      }
    });

    Column<PlayerDto, SafeHtml> statusColumn = new Column<PlayerDto, SafeHtml>(new SafeHtmlCell()) {
      @Override
      public SafeHtml getValue(PlayerDto player) {
        return getStatusSafeHtml(player);
      }
    };
    statusColumn.setCellStyleNames(resources.style().cellWithButton());
    playerCellTable.addColumn(statusColumn);

    TextColumn<PlayerDto> publicNameColumn = new TextColumn<PlayerDto>() {
      @Override
      public String getValue(PlayerDto player) {
        return player.getPublicName();
      }
    };
    publicNameColumn.setCellStyleNames(resources.style().cellWithButton());
    playerCellTable.addColumn(publicNameColumn);

    final ButtonCell writeButtonCell = new ButtonCell(ButtonType.LINK, IconType.PENCIL);
    final Column<PlayerDto, String> writeColumn = new Column<PlayerDto, String>(writeButtonCell) {
      @Override
      public String getValue(PlayerDto object) {
        return "";
      }

      @Override
      public void render(Cell.Context context, PlayerDto object, SafeHtmlBuilder sb) {
        if (object.getId() != player.getId()) {
          super.render(context, object, sb);
        }
      }
    };
    writeColumn.setFieldUpdater(new FieldUpdater<PlayerDto, String>() {
      @Override
      public void update(int index, PlayerDto object, String value) {
        unreadMessagesMap.remove(object.getId());
        getUiHandlers().writeToFriend(object);
      }
    });
    playerCellTable.addColumn(writeColumn);

    TextColumn<PlayerDto> newMessagesColumn = new TextColumn<PlayerDto>() {
      @Override
      public String getValue(PlayerDto player) {
        if (unreadMessagesMap != null && unreadMessagesMap.containsKey(player.getId())) {
          return String.valueOf(unreadMessagesMap.get(player.getId()));
        }
        return "";
      }

      @Override
      public void render(Cell.Context context, PlayerDto object, SafeHtmlBuilder sb) {
        if (unreadMessagesMap == null) {
          return;
        }
        final Integer unreadMsg = unreadMessagesMap.get(object.getId());
        sb.appendHtmlConstant("<span class=\""
            + (unreadMsg != null ? resources.style().newMessageCircle() : "") + "\" " +
            ">");
        if (unreadMsg != null) {
          sb.append(unreadMsg);
        }
        sb.appendHtmlConstant("</span>");
      }
    };
    newMessagesColumn.setCellStyleNames(resources.style().cellWithButton());
    playerCellTable.addColumn(newMessagesColumn);
  }

  private SafeHtml getStatusSafeHtml(PlayerDto player) {
    if (player.getId() == this.player.getId()) {
      Icon html = getIconString(IconType.USER);
      return new SafeHtmlBuilder().appendHtmlConstant(html.getElement().getString()).toSafeHtml();
    }
    Image img;
    String playerPublicName = player.getPublicName();
    if (player.isPlaying()) {
      img = new Image(resources.images().playingIconImage().getSafeUri());
      img.setTitle(playerPublicName + " " + messages.playingTitle());
    } else {
      if (player.isOnline()) {
        img = new Image(resources.images().onlineIconImage().getSafeUri());
        img.setTitle(playerPublicName + " " + messages.onlineTitle());
      } else {
        img = new Image(resources.images().offlineIconImage().getSafeUri());
        img.setTitle(playerPublicName + " " + messages.offlineTitle());
      }
    }
    return new SafeHtmlBuilder().appendHtmlConstant(img.getElement().getString()).toSafeHtml();
  }

  private Icon getIconString(IconType iconType) {
    final Icon userIcon = new Icon();
    userIcon.getElement().addClassName("fa btn-link");
    userIcon.getElement().addClassName(iconType.getCssName());
    return userIcon;
  }

  @SuppressWarnings(value = "unchecked")
  private void resetPlayerSelection(SingleSelectionModel<?> prevSelectionModel) {
    final Object selectedObject = prevSelectionModel.getSelectedObject();
    if (selectedObject == null) {
      return;
    }
    if (prevSelected) {
      prevSelected = false;
      return;
    }
    prevSelected = true;
    ((SingleSelectionModel<Object>) prevSelectionModel).setSelected(selectedObject, false);
  }

  @Override
  public void setFriendList(List<FriendDto> friendList) {
    if (null == friendList) {
      return;
    }
    if (!unreadMessagesMap.isEmpty()) {
      this.friendList = getUiHandlers().getSortedFriendList(unreadMessagesMap.keySet(), friendList);
    } else {
      this.friendList = friendList;
    }
    friendCellTable.setRowCount(0);
    friendCellTable.setRowData(this.friendList);
  }

  @Override
  public void setPlayerList(List<PlayerDto> playerList) {
    if (playerList == null) {
      return;
    }
    if (!unreadMessagesMap.isEmpty()) {
      this.playerList = getUiHandlers().getSortedPlayerList(unreadMessagesMap.keySet(), playerList);
    } else {
      this.playerList = playerList;
    }
    this.playerOrigList = this.playerList;
    this.playerList.remove(player);
    this.playerList.add(0, player);
    playerCellTable.setRowCount(0);
    playerCellTable.setRowData(this.playerList);
  }

  @Override
  public void toggleInPlayButton() {
    playButton.setType(ButtonType.DEFAULT);
    playButton.setIcon(IconType.PLAY);
    playButton.setText(messages.play());
  }

  @Override
  public void setUpViewOnDisconnectFromServer() {
    playButton.setActive(true);
    playButton.setBlock(true);
    playButton.setType(ButtonType.DANGER);
    playButton.setIcon(IconType.REFRESH);
    playButton.setText(messages.reconnect());
    cancelMove.setEnabled(false);
    playerSearch.setEnabled(false);

    playerCellTable.setRowData(new ArrayList<PlayerDto>());
    friendCellTable.setRowData(new ArrayList<FriendDto>());
    turnLabel.setHTML(messages.youDisconnected());

    timeLabel.setText("");
    fisherTimeLabel.setText("");

    hidePlayingButtonsAndShowPlayButton();
  }

  @Override
  public void hidePlayingButtonsAndShowPlayButton() {
    playButton.setVisible(true);
    drawButton.setVisible(false);
    surrenderButton.setVisible(false);
    beatenMineDraughtsLabel.setText("");
    beatenOpponentDraughtsLabel.setText("");
  }

  @Override
  public void hidePlayButtonAndShowPlayingButtons() {
    playButton.setVisible(false);
    drawButton.setVisible(true);
    surrenderButton.setVisible(true);
    setBeatenMy(0);
    setBeatenOpponent(0);
  }

  @Override
  public void clearPlayComponent() {
    lienzoPanel.removeAll();
    board.clearDesk();
    draughtsDesk.remove(lienzoPanel);
    lienzoPanel = null;
    initEmptyDeskPanel();

    getUiHandlers().stopTimers();
    turnLabel.setHTML(messages.playDidNotStart());
    timeLabel.setText("");
    fisherTimeLabel.setText("");
  }

  public void setBeatenMy(int count) {
    beatenMineDraughtsLabel.setText(count + " - " + (board.isWhite() ? messages.whites()
        : messages.blacks()));
  }

  public void setBeatenOpponent(int count) {
    beatenOpponentDraughtsLabel.setText(count + " - " + (board.isWhite() ? messages.blacks()
        : messages.whites()));
  }

  @Override
  public void startPlay(boolean white) {
    BoardBackgroundLayer backgroundLayer = initDeskPanel(white);
    board = new Board(backgroundLayer, 8, 8, white);

    board.setView(this);
    lienzoPanel.add(board);
    lienzoPanel.getElement().getStyle().setCursor(Style.Cursor.POINTER);
    cancelMove.setEnabled(true);
    hidePlayButtonAndShowPlayingButtons();

//    lienzoPanel.addClickHandler(new ClickHandler() {
//      @Override
//      public void onClick(ClickEvent event) {
//        Circle circle = new Circle(2)
//            .setX(event.getX())
//            .setY(event.getY())
//            .setFillColor(ColorName.RED);
//        board.add(circle);
//        lienzoPanel.draw();
//      }
//    });
  }

  @Override
  public void updateTurn(boolean myTurn) {
    if (myTurn) {
      setTurnMessage(messages.yourTurn());
    } else {
      setTurnMessage(messages.opponentTurn());
    }
  }

  @Override
  public void setTurnMessage(String message) {
    turnLabel.setHTML(message);
  }

  @Override
  public int getMyDraughtsSize() {
    return board.getMyDraughts().size();
  }

  @Override
  public int getOpponentDraughtsSize() {
    return board.getOpponentDraughts().size();
  }

  @Override
  public boolean isWhite() {
    return board.isWhite();
  }

  @Override
  public void hideInviteDialog() {
    if (inviteDialogBox != null && inviteDialogBox.isVisible()) {
      inviteDialogBox.hide();
    }
  }

  @Override
  public void showInviteDialog(ClickHandler inviteClickHandler) {
    inviteDialogBox = new InviteDialogBox(inviteClickHandler) {
      @Override
      public void didNotResponse() {
        getUiHandlers().didNotResponse(opponent);
      }

      @Override
      public void submitted(boolean white, String timeOnPlay, String fisherTime) {
        PlayComponentView.this.opponentColor = !white;
        PlayComponentView.this.timeOnPlay = timeOnPlay;
        PlayComponentView.this.fisherTime = fisherTime;
      }
    };
    inviteDialogBox.show(messages.inviteToPlay(opponent.getPublicName(),
        messages.draughts()));
  }

  @Override
  public boolean getOpponentColor() {
    return opponentColor;
  }

  @Override
  public String takeScreenshot() {
    return takeScreenshot(DataURLType.PNG, true);
  }

  public String takeScreenshot(DataURLType dataType, boolean includeBackgroundLayer) {
    return lienzoPanel.toDataURL(dataType, includeBackgroundLayer);
  }

  private BoardBackgroundLayer initDeskPanel(boolean white) {
    int lienzoSide = lienzoPanel.getHeight() - 50;
//    if (lienzoSide > 800) {
//      lienzoSide = 800;
//    } else if (lienzoSide < 200) {
//      lienzoSide = 200;
//    }
    BoardBackgroundLayer boardBackgroundLayer = new BoardBackgroundLayer(
        lienzoSide, lienzoSide - 30,
        8, 8);
    boardBackgroundLayer.drawCoordinates(white);
    lienzoPanel.setBackgroundLayer(boardBackgroundLayer);
    return boardBackgroundLayer;
  }

  @Override
  public void setOpponent(PlayerDto opponent) {
    this.opponent = opponent;
  }

  @Override
  public Board getBoard() {
    return board;
  }


  @Override
  public void checkWinner() {
    getUiHandlers().checkWinner();
  }

  @Override
  public void addNotationStroke(Stroke strokeForNotation) {
    getUiHandlers().addNotationStroke(strokeForNotation);
  }

  @Override
  public void toggleTurn(boolean turn) {
    getUiHandlers().toggleTurn(turn);
  }

  @Override
  public void doPlayerMove(MoveDto move) {
    getUiHandlers().doPlayerMove(move);
  }

  @Override
  public PlayerDto getPlayer() {
    return playSession.getPlayer();
  }

  @Override
  public PlayerDto getOpponent() {
    return playSession.getOpponent();
  }

  interface Binder extends UiBinder<Widget, PlayComponentView> {
  }
}
