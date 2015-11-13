package online.draughts.rus.client.websocket;

import com.google.gwt.inject.client.AbstractGinModule;

/**
 * Created with IntelliJ IDEA.
 * User: alekspo
 * Date: 27.09.15
 * Time: 10:46
 */
public class WebsocketModule extends AbstractGinModule {

  @Override
  protected void configure() {
    bind(ClientWebsocket.class).asEagerSingleton();
  }
}
