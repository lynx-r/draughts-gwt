/*
 * Copyright 2011 ArcBees Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package online.shashki.rus.client.application.home;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import online.shashki.rus.client.application.component.playshowpanel.PlayShowPanel;
import online.shashki.rus.client.application.security.CurrentSession;
import online.shashki.rus.client.util.SHCookies;
import online.shashki.rus.client.util.SHLog;
import online.shashki.rus.shared.locale.ShashkiMessages;
import online.shashki.rus.shared.model.Game;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.ButtonGroup;
import org.gwtbootstrap3.client.ui.CheckBoxButton;
import org.gwtbootstrap3.client.ui.html.Strong;

import java.util.List;

public class HomeView extends ViewWithUiHandlers<HomeUiHandlers> implements HomePresenter.MyView {

  private static Binder binder = GWT.create(Binder.class);
  private final ShashkiMessages messages;
  private final CurrentSession currentSession;

  @UiField
  SimplePanel play;
  @UiField(provided = true)
  PlayShowPanel playShowPanel;
  @UiField
  Button newGameButton;
  @UiField
  Button moreGameOnPage;
  @UiField
  Button lessGameOnPage;
  @UiField
  ButtonGroup showGameOnPageButtonGroup;
  @UiField
  CheckBoxButton myGameListCheckButton;
  @UiField
  Strong gameListLabel;

  private boolean newGameState = true;

  @Inject
  HomeView(CurrentSession currentSession,
      ShashkiMessages messages) {
    playShowPanel = new PlayShowPanel(this);
    initWidget(binder.createAndBindUi(this));

    this.currentSession = currentSession;
    this.messages = messages;
    bindSlot(HomePresenter.SLOT_PLAY, play);
  }

  @Override
  protected void onAttach() {
    super.onAttach();
    newGameState = SHCookies.getNewGameButtonState();
    SHLog.debug("NEW STATE " + newGameState);
    if (currentSession.isLoggedIn()) {
      showControlsNewGameButton();
    }
  }

  @UiHandler("newGameButton")
  public void onNewGame(ClickEvent event) {
    toggelNewGameButton();
    showControlsNewGameButton();
  }

  private void showControlsNewGameButton() {
    showGameOnPageButtonGroup.setVisible(!newGameState);
    myGameListCheckButton.setVisible(!newGameState);

    newGameButton.setText(newGameState ? messages.playListButtonText() : messages.newGameButtonText());

    play.setVisible(newGameState);
    playShowPanel.setVisible(!newGameState);
  }

  private void toggelNewGameButton() {
    newGameState = !newGameState;
    SHCookies.setNewGameButtonState(newGameState);
  }

  @UiHandler("moreGameOnPage")
  public void onLessGameOnPage(ClickEvent event) {
    playShowPanel.moreGameOnPanel();
  }

  @UiHandler("lessGameOnPage")
  public void onMoreGameOnPage(ClickEvent event) {
    playShowPanel.lessGameOnPanel();
  }

  @UiHandler("myGameListCheckButton")
  public void onMyGameList(ClickEvent event) {
    getUiHandlers().getMoreGames(myGameListCheckButton.getValue(), HomePresenter.INIT_SHOW_GAMES_PAGE_SIZE);
  }

  @Override
  public void setShowLoggedInControls(Boolean loggedIn) {
    SHLog.debug("LOGGED IN " + loggedIn);
    newGameButton.setVisible(loggedIn);
    myGameListCheckButton.setVisible(loggedIn);
    gameListLabel.setVisible(!loggedIn);
  }

  @Override
  public void setGames(List<Game> gameList) {
    playShowPanel.setGames(gameList);
  }

  public void setEnableMoreGameButton(boolean enable) {
    moreGameOnPage.setEnabled(enable);
  }

  public boolean isEnabledMoreGameButton() {
    return moreGameOnPage.isEnabled();
  }

  public void setEnableLessGameButton(boolean enableMoreGameButton) {
    lessGameOnPage.setEnabled(enableMoreGameButton);
  }

  public boolean isEnabledLessGameButton() {
    return lessGameOnPage.isEnabled();
  }

  public void getMoreGames(int newPageSize) {
    SHLog.debug("GET MORE GAMES " + newPageSize);
    getUiHandlers().getMoreGames(myGameListCheckButton.getValue(), newPageSize);
  }

  interface Binder extends UiBinder<Widget, HomeView> {
  }
}
