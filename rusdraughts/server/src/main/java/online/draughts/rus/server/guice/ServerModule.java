package online.draughts.rus.server.guice;

import com.google.inject.AbstractModule;
import org.dozer.DozerBeanMapper;
import org.dozer.Mapper;

/**
 * Created with IntelliJ IDEA.
 * User: alekspo
 * Date: 09.10.15
 * Time: 14:03
 */
public class ServerModule extends AbstractModule {
  @Override
  protected void configure() {
    install(new ResourceModule());
    install(new DispatchServletModule());
    install(new DbModule());
    install(new ServerHandlerModule());

//    bind(DatastoreService.class).in(Singleton.class);
    bind(Mapper.class).to(DozerBeanMapper.class);
  }
}
