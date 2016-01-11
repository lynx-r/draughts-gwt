package online.draughts.rus.client.application.widget.popup;

import com.gwtplatform.mvp.client.UiHandlers;
import online.draughts.rus.draughts.Stroke;
import online.draughts.rus.shared.dto.MoveDto;

interface DraughtsPlayerUiHandlers extends UiHandlers {

  void checkWinner();

  void addNotationStroke(Stroke strokeForNotation);

  void toggleTurn(boolean turn);

  void doPlayerMove(MoveDto move);
}