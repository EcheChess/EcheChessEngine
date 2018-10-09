package ca.watier.echechess.engine.game;

import ca.watier.echechess.common.enums.CasePosition;
import ca.watier.echechess.common.enums.Pieces;
import ca.watier.echechess.engine.constraints.DefaultGameConstraint;
import ca.watier.echechess.engine.engines.GenericGameHandler;
import org.apache.commons.lang3.StringUtils;

import java.util.EnumMap;
import java.util.Map;

public class SimpleCustomPositionGameHandler extends GenericGameHandler {
    public static final String THE_NUMBER_OF_PARAMETER_IS_INCORRECT = "The number of parameter is incorrect !";
    private static final long serialVersionUID = 5207738868839259022L;

    public SimpleCustomPositionGameHandler(DefaultGameConstraint defaultGameConstraint) {
        super(defaultGameConstraint);
    }

    public void setPieces(String specialGamePieces) {
        if (StringUtils.isBlank(specialGamePieces)) {
            return;
        }

        Map<CasePosition, Pieces> positionPieces = new EnumMap<>(CasePosition.class);

        for (String section : specialGamePieces.split(";")) {
            String[] values = section.split(":");

            if (values.length != 2) {
                throw new UnsupportedOperationException(THE_NUMBER_OF_PARAMETER_IS_INCORRECT);
            }

            positionPieces.put(CasePosition.valueOf(values[0]), Pieces.valueOf(values[1]));
        }

        setPositionPiecesMap(positionPieces);
    }
}
