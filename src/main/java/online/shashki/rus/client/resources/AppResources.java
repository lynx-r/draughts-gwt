package online.shashki.rus.client.resources;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import online.shashki.rus.client.resources.images.Images;
import online.shashki.rus.client.resources.sounds.Sounds;

public interface AppResources extends ClientBundle {
  Images images();

  Sounds sounds();

  @Source("css/normalize.gss")
  Normalize normalize();

  @Source("css/style.gss")
  Style style();

  interface Normalize extends CssResource {
  }

  interface Style extends CssResource {
    @ClassName("player-search")
    String playerSearch();

    @ClassName("shashki-column")
    String shashkiColumn();

    @ClassName("notation-column")
    String notationColumn();

    @ClassName("player-list")
    String playerList();
  }
}
