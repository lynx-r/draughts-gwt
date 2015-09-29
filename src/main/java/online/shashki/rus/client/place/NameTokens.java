package online.shashki.rus.client.place;

import com.google.inject.Inject;
import online.shashki.rus.client.utils.Utils;
import online.shashki.rus.shared.locale.ShashkiMessages;

public class NameTokens {
  // токены - адреса в навигации
  public static final String homePage = "!home";
  public static final String loginPage = "!login";
  public static final String profilePage = "!profile";

  public static final String errorPage = "!error";
  public static final String myGamesPage = "!myGames";
  public static final String settingsPage = "!settings";
  public static final String logoutPage = "/rus/logout";

  // ссылки - название и токен
  private final Link homeLink;
  private final Link loginLink;
  private final Link logoutLink;
  private final Link profileLink;
  private final Link settingsLink;
  private final Link myGamesLink;

  @Inject
  public NameTokens(ShashkiMessages messages) {
    homeLink = new Link(homePage, messages.home());
    loginLink = new Link(loginPage, messages.login());
    logoutLink = new Link(logoutPage, messages.logout());
    profileLink = new Link(profilePage, messages.profile());
    settingsLink = new Link(settingsPage, messages.settings());
    myGamesLink = new Link(myGamesPage, messages.myGames());
  }

  public Link[] getLeftLinks() {
    return new Link[]{homeLink};
  }

  public Link[] getRightLinks() {
    return new Link[]{loginLink};
  }

  public Link[] getRightAuthLinks() {
    return new Link[]{profileLink, logoutLink};
  }

  public Link[] getProfileLinks() {
    return new Link[]{myGamesLink, settingsLink};
  }

  public Link[] getAllLinks() {
    return Utils.concatLinks(Utils.concatLinks(getLeftLinks(), getRightAuthLinks()), getRightAuthLinks());
  }

  public static class Link {
    public String token;
    public String name;

    public Link(String token, String name) {
      this.token = token;
      this.name = name;
    }
  }
}
