package ca.watier.echechess.engine.pojos;

import ca.watier.echechess.common.enums.KingStatus;
import ca.watier.echechess.common.enums.Side;

import java.io.Serializable;

public class KingStatusHolderPojo implements Serializable {
    private static final long serialVersionUID = -4106042665849861588L;

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
