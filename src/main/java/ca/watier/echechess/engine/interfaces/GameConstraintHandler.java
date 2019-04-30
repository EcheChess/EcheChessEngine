package ca.watier.echechess.engine.interfaces;

import ca.watier.echechess.common.enums.CasePosition;
import ca.watier.echechess.common.enums.MoveMode;
import ca.watier.echechess.common.enums.MoveType;
import ca.watier.echechess.common.enums.Side;
import ca.watier.echechess.engine.engines.GenericGameHandler;

public interface GameConstraintHandler {

    /**
     * Get the move type of the current move, based on coordinates
     *
     * @param from
     * @param to
     * @param gameHandler
     * @return
     */
    MoveType getMoveType(CasePosition from, CasePosition to, GenericGameHandler gameHandler);

    /**
     * Checks if the piece is movable to the specified location
     *
     * @param from
     * @param to
     * @param playerSide
     * @param gameHandler
     * @param moveMode    - Gives the full move of the piece, ignoring the other pieces
     * @return
     */
    boolean isPieceMovableTo(CasePosition from, CasePosition to, Side playerSide, GenericGameHandler gameHandler, MoveMode moveMode);
}
