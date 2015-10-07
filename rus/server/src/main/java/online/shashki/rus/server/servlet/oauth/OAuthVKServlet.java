package online.shashki.rus.server.servlet.oauth;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.extensions.servlet.auth.oauth2.AbstractAuthorizationCodeServlet;
import com.google.api.client.http.GenericUrl;
import com.google.inject.Inject;
import online.shashki.rus.server.config.ServerConfiguration;
import online.shashki.rus.server.utils.Utils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: alekspo
 * Date: 16.11.14
 * Time: 12:37
 */
@WebServlet(name = "OAuthVKServlet", urlPatterns = {"/OAuthVKServlet"})
public class OAuthVKServlet extends AbstractAuthorizationCodeServlet {

  private List<String> scope = Collections.singletonList("email");

  private final ServerConfiguration serverConfiguration;

  @Inject
  public OAuthVKServlet(ServerConfiguration serverConfiguration) {
    this.serverConfiguration = serverConfiguration;
  }

  @Override
  protected AuthorizationCodeFlow initializeFlow() throws ServletException, IOException {
    ClientSecrets clientSecrets = new ClientSecrets(serverConfiguration, ClientSecrets.SocialType.VK);
    return Utils.getFlow(clientSecrets, scope);
  }

  @Override
  protected String getRedirectUri(HttpServletRequest httpServletRequest) throws ServletException, IOException {
    GenericUrl url = new GenericUrl(httpServletRequest.getRequestURL().toString());
    url.setRawPath(serverConfiguration.getVkRedirectUri());
    return url.build();
  }

  @Override
  protected String getUserId(HttpServletRequest httpServletRequest) throws ServletException, IOException {
    return httpServletRequest.getSession(true).getId();
  }
}