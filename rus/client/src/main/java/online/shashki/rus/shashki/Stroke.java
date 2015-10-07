package online.shashki.rus.shashki;

import online.shashki.rus.client.utils.SHLog;
import online.shashki.rus.shared.model.Move;
import org.codehaus.jackson.annotate.JsonIgnore;

import java.util.HashSet;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: alekspo
 * Date: 07.10.15
 * Time: 15:13
 */
public class Stroke {

  private static final String SIMPLE_MOVE_SEP = "-";
  private static final String BEAT_MOVE_SEP = ":";

  private Square takenSquare;
  private Square startSquare;
  private Square endSquare;
  private Set<Move.MoveFlags> moveFlags = new HashSet<>();
  private boolean first;
  private int number;

  public Square getTakenSquare() {
    return takenSquare;
  }

  public Stroke setTakenSquare(Square takenSquare) {
    this.takenSquare = takenSquare;
    return this;
  }


    public Square getStartSquare() {
    return startSquare;
  }

  public Stroke setStartSquare(Square startSquare) {
    this.startSquare = startSquare;
    return this;
  }

    public Square getEndSquare() {
    return endSquare;
  }

  public Stroke setEndSquare(Square endSquare) {
    this.endSquare = endSquare;
    return this;
  }

    public String toNotation(boolean isWhite) {
    String notation;
    if (first && isWhite) {
      SHLog.debug("FIRST WHITE");
      notation = getNotation(true);
    } else if (!first && isWhite){
      SHLog.debug("SECOND WHITE");
      notation = getNotation(true);
    } else if (first) {
      SHLog.debug("FIRST BLACK");
      notation = getNotation(false);
    } else {
      SHLog.debug("SECOND BLACK");
      notation = getNotation(false);
    }
    return notation;
  }

  private String getNotation(boolean normal) {
    final String s = startSquare.toNotation(normal);
    final String e = endSquare.toNotation(normal);
    return isSimple() ? s + SIMPLE_MOVE_SEP + e
        : s + BEAT_MOVE_SEP + e;
  }

  public String toNotationLastMove() {
    return endSquare.toNotation(first);
  }
  @JsonIgnore
  public boolean isCancel() {
    return moveFlags.contains(Move.MoveFlags.CANCEL_MOVE);
  }

  @JsonIgnore
  public boolean isSimple() {
    return moveFlags.contains(Move.MoveFlags.SIMPLE_MOVE);
  }

  @JsonIgnore
  public boolean isContinueBeat() {
    return moveFlags.contains(Move.MoveFlags.CONTINUE_BEAT);
  }

  @JsonIgnore
  public boolean isStopBeat() {
    return moveFlags.contains(Move.MoveFlags.STOP_BEAT);
  }

  @JsonIgnore
  public boolean isStartBeat() {
    return moveFlags.contains(Move.MoveFlags.START_BEAT);
  }


  /**
   * Отражает ход на доске не изменяя сам объект
   *
   * @return moveDto отраженный объект
   */
  public Stroke mirror() {
    Stroke move = new Stroke();

    move.setStartSquare(startSquare.mirror());
    move.setEndSquare(endSquare.mirror());
    move.setTakenSquare(takenSquare.mirror());
    move.setMoveFlags(moveFlags);
    move.setFirst(first);
    move.setNumber(number);
    return move;
  }

  public void setMoveFlags(Set<Move.MoveFlags> moveFlags) {
    this.moveFlags = moveFlags;
  }

  public void setFirst(boolean first) {
    this.first = first;
  }

  public Stroke setNumber(int number) {
    this.number = number;
    return this;
  }

  public int getNumber() {
    return number;
  }
}
