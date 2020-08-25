package ca.watier.echechess.engine.models;

import ca.watier.echechess.common.enums.CasePosition;
import ca.watier.echechess.common.enums.Pieces;

public class PiecePositionModel {
    private final CasePosition position;
    private final Pieces pieces;

    public PiecePositionModel(Pieces pieces, CasePosition position) {
        this.position = position;
        this.pieces = pieces;
    }

    public CasePosition getPosition() {
        return position;
    }

    public Pieces getPieces() {
        return pieces;
    }
}
