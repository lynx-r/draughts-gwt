package online.shashki.rus.shared.model;

/**
 * Created with IntelliJ IDEA.
 * User: alekspo
 * Date: 12.12.14
 * Time: 10:20
 */
public interface PersistableObject extends BasePersistableObject {

  Long getId();

  void setId(Long id);
}