package ca.watier.echechess.engine.game;

import ca.watier.echechess.common.enums.CasePosition;
import ca.watier.echechess.common.enums.PgnPieceFound;
import ca.watier.echechess.common.enums.Pieces;
import ca.watier.echechess.common.enums.Side;
import ca.watier.echechess.common.utils.PositionUtils;
import ca.watier.echechess.engine.engines.GenericGameHandler;
import ca.watier.echechess.engine.exceptions.FenParserException;
import ca.watier.echechess.engine.interfaces.GameConstraint;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.CharUtils;

import java.util.*;

public class FenPositionGameHandler extends GenericGameHandler {

    private static final String NO_PIECES_ON_ROW = "8";

    public FenPositionGameHandler(GameConstraint defaultGameConstraint) {
        super(defaultGameConstraint);
    }

    public void init(char sideToPlay,
                     boolean isWhiteQueenMoveAvail,
                     boolean isWhiteKingMoveAvail,
                     boolean isBlackQueenMoveAvail,
                     boolean isBlackKingMoveAvail) throws FenParserException {

        sideToPlay = Character.toUpperCase(sideToPlay);

        setWhiteKingCastlingAvailable(isWhiteKingMoveAvail);
        setWhiteQueenCastlingAvailable(isWhiteQueenMoveAvail);
        setBlackKingCastlingAvailable(isBlackKingMoveAvail);
        setBlackQueenCastlingAvailable(isBlackQueenMoveAvail);

        setCurrentAllowedMoveSide(getSide(sideToPlay));
    }

    private Side getSide(char sideToPlay) throws FenParserException {
        Side side;

        switch (sideToPlay) {
            case 'W':
                side = Side.WHITE;
                break;
            case 'B':
                side = Side.BLACK;
                break;
            default:
                throw new FenParserException();
        }
        return side;
    }

    public void setPieces(String[] rows) throws FenParserException {
        if (ArrayUtils.isEmpty(rows)) {
            return;
        }

        setPositionPiecesMap(calculatePositionFromRows(rows));
    }

    private Map<CasePosition, Pieces> calculatePositionFromRows(String[] rows) throws FenParserException {
        Map<CasePosition, Pieces> positionPiecesMap = new HashMap<>();

        List<EnumSet<CasePosition>> board = PositionUtils.getBoard();
        for (int i = 0, boardSize = board.size(); i < boardSize; i++) {
            EnumSet<CasePosition> enumSet = board.get(i);
            String row = rows[i];

            if (NO_PIECES_ON_ROW.equals(row)) {
                continue;
            }

            char[] piecesToPutInGame = row.toCharArray();
            char currentCol = 0;
            for (Iterator<CasePosition> iterator = enumSet.iterator(); iterator.hasNext(); ) {
                CasePosition position = iterator.next();
                char current = piecesToPutInGame[currentCol];

                if (Character.isLetter(current)) {
                    positionPiecesMap.put(position, getPieceFromFen(current));
                } else if (Character.isDigit(current)) {

                    int numberOfEmptySpace = Character.getNumericValue(current);
                    for (char j = 0; j < (numberOfEmptySpace - 1); j++) {
                        if (iterator.hasNext()) {
                            iterator.next();
                        } else {
                            throw new FenParserException();
                        }
                    }
                    currentCol++;
                    continue;
                }

                currentCol++;
            }
        }

        return positionPiecesMap;
    }

    private Pieces getPieceFromFen(char current) {
        if (!Character.isLetter(current)) {
            return null;
        }

        Pieces pieces = null;
        Side side;
        char currentCharUpperCased = Character.toUpperCase(current);
        for (PgnPieceFound pieceFound : PgnPieceFound.values()) {
            if (pieceFound.getLetter() == currentCharUpperCased) {
                side = CharUtils.isAsciiAlphaUpper(current) ? Side.WHITE : Side.BLACK;
                pieces = pieceFound.getPieceBySide(side);
                break;
            }
        }

        return pieces;
    }
}
