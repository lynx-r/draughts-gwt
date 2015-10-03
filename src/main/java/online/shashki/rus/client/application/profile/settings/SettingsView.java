package online.shashki.rus.client.application.profile.settings;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import org.gwtbootstrap3.client.ui.TextBox;

public class SettingsView extends ViewWithUiHandlers<SettingsUiHandlers> implements SettingsPresenter.MyView {
  @UiField
  SimplePanel main;
  @UiField
  TextBox playerNameTextBox;

  @Inject
  SettingsView(Binder uiBinder) {
    initWidget(uiBinder.createAndBindUi(this));

    bindSlot(SettingsPresenter.SLOT_SETTINGS, main);
  }

  @Override
  public void setPlayerName(String playerName) {
    playerNameTextBox.setText(playerName);
  }

  @UiHandler("submitPlayerNameButton")
  public void onSubmitPlayerName(ClickEvent event) {
    getUiHandlers().submitNewPlayerName(playerNameTextBox.getText());
  }

  @UiHandler("playerNameTextBox")
  public void onSubmit(KeyPressEvent event) {
    if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
      getUiHandlers().submitNewPlayerName(playerNameTextBox.getText());
    }
  }

  public static class ViewFactoryImpl implements SettingsPresenter.ViewFactory {

    private final Binder binder;

    @Inject
    public ViewFactoryImpl(Binder binder) {
      this.binder = binder;
    }

    @Override
    public SettingsPresenter.MyView create() {
      return new SettingsView(binder);
    }
  }

  interface Binder extends UiBinder<Widget, SettingsView> {
  }
}
