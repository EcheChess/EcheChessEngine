package ca.watier.echechess.engine.pojos;

import ca.watier.echechess.common.enums.KingStatus;
import ca.watier.echechess.common.enums.Side;

public class KingStatusHolderPojo {
    private KingStatus whiteKingStatus;
    private KingStatus blackKingStatus;

    public void setKingStatusBySide(KingStatus kingStatus, Side side) {
        if (kingStatus == null || side == null) {
            return;
        }

        switch (side) {
            case BLACK:
                blackKingStatus = kingStatus;
                break;
            case WHITE:
                whiteKingStatus = kingStatus;
                break;
        }
    }


    public KingStatus getKingStatusBySide(Side side) {
        if (side == null) {
            return null;
        }

        switch (side) {
            case BLACK:
                return blackKingStatus;
            case WHITE:
                return whiteKingStatus;
            default:
                return null;
        }
    }

    public KingStatus getWhiteKingStatus() {
        return whiteKingStatus;
    }

    public KingStatus getBlackKingStatus() {
        return blackKingStatus;
    }
}
