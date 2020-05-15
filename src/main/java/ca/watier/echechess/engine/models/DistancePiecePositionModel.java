package ca.watier.echechess.engine.models;

import ca.watier.echechess.common.enums.CasePosition;
import ca.watier.echechess.common.enums.Pieces;

public class DistancePiecePositionModel extends PiecePositionModel implements Comparable {

    private final int distance;

    public DistancePiecePositionModel(int distance, Pieces pieces, CasePosition position) {
        super(pieces, position);
        this.distance = distance;
    }

    public int getDistance() {
        return distance;
    }

    @Override
    public int compareTo(Object o) {
        if (!(o instanceof DistancePiecePositionModel)) {
            throw new UnsupportedOperationException();
        }
        DistancePiecePositionModel toCompare = (DistancePiecePositionModel) o;

        return Integer.compare(getDistance(), toCompare.getDistance());
    }
}
