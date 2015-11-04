package online.draughts.rus.shared.config;

import com.google.gwt.i18n.client.ConstantsWithLookup;
import com.google.gwt.i18n.client.LocalizableResource;

/**
 * Created with IntelliJ IDEA.
 * User: alekspo
 * Date: 15.03.15
 * Time: 16:19
 */
public interface ClientConfiguration extends ConstantsWithLookup {
  @LocalizableResource.Key("player.websocket.url")
  String playerWebsocketUrl();

  String debug();

  String level();

  String site_url();

  String production();

  String initShowGamesPageSize();

  String incrementPlayShowSize();

  String strokeCommentLength();

  String escapeChars();
}
