package online.shashki.rus.client.service;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

/**
 * Created with IntelliJ IDEA.
 * User: alekspo
 * Date: 27.09.15
 * Time: 10:48
 */
public class RpcServiceModule extends AbstractPresenterModule {
  @Override
  protected void configure() {
    bind(PlayerServiceAsync.class).asEagerSingleton();
    bind(GameRpcServiceAsync.class).asEagerSingleton();
  }
}
