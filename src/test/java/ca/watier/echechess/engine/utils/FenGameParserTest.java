package ca.watier.echechess.engine.utils;

import ca.watier.echechess.common.enums.CasePosition;
import ca.watier.echechess.common.enums.Pieces;
import ca.watier.echechess.common.enums.Side;
import ca.watier.echechess.engine.abstracts.GameBoardData;
import ca.watier.echechess.engine.exceptions.FenParserException;
import ca.watier.echechess.engine.game.FenPositionGameHandler;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.Map;

import static ca.watier.echechess.common.enums.CasePosition.*;
import static ca.watier.echechess.common.enums.Pieces.*;
import static org.junit.jupiter.api.Assertions.*;

public class FenGameParserTest {

    @Test
    public void parseStartingPosition() throws FenParserException {
        // given
        Map<CasePosition, Pieces> blackPositions = new EnumMap<>(CasePosition.class);
        blackPositions.put(A8, B_ROOK);
        blackPositions.put(B8, B_KNIGHT);
        blackPositions.put(C8, B_BISHOP);
        blackPositions.put(D8, B_QUEEN);
        blackPositions.put(E8, B_KING);
        blackPositions.put(F8, B_BISHOP);
        blackPositions.put(G8, B_KNIGHT);
        blackPositions.put(H8, B_ROOK);
        blackPositions.put(A7, B_PAWN);
        blackPositions.put(B7, B_PAWN);
        blackPositions.put(C7, B_PAWN);
        blackPositions.put(D7, B_PAWN);
        blackPositions.put(E7, B_PAWN);
        blackPositions.put(F7, B_PAWN);
        blackPositions.put(G7, B_PAWN);
        blackPositions.put(H7, B_PAWN);

        Map<CasePosition, Pieces> whitePositions = new EnumMap<>(CasePosition.class);
        whitePositions.put(A2, W_PAWN);
        whitePositions.put(B2, W_PAWN);
        whitePositions.put(C2, W_PAWN);
        whitePositions.put(D2, W_PAWN);
        whitePositions.put(E2, W_PAWN);
        whitePositions.put(F2, W_PAWN);
        whitePositions.put(G2, W_PAWN);
        whitePositions.put(H2, W_PAWN);
        whitePositions.put(A1, W_ROOK);
        whitePositions.put(B1, W_KNIGHT);
        whitePositions.put(C1, W_BISHOP);
        whitePositions.put(D1, W_QUEEN);
        whitePositions.put(E1, W_KING);
        whitePositions.put(F1, W_BISHOP);
        whitePositions.put(G1, W_KNIGHT);
        whitePositions.put(H1, W_ROOK);

        // when
        FenPositionGameHandler game = FenGameParser.parse("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq");

        // then
        Map<CasePosition, Pieces> piecesLocationBlack = game.getPiecesLocation(Side.BLACK);
        Map<CasePosition, Pieces> piecesLocationWhite = game.getPiecesLocation(Side.WHITE);

        GameBoardData cloneOfCurrentDataState = game.getCloneOfCurrentDataState();

        Side currentAllowedMoveSide = cloneOfCurrentDataState.getCurrentAllowedMoveSide();

        assertTrue(cloneOfCurrentDataState.isWhiteKingCastlingAvailable());
        assertTrue(cloneOfCurrentDataState.isWhiteQueenCastlingAvailable());
        assertTrue(cloneOfCurrentDataState.isBlackKingCastlingAvailable());
        assertTrue(cloneOfCurrentDataState.isBlackQueenCastlingAvailable());

        assertEquals(blackPositions, piecesLocationBlack);
        assertEquals(whitePositions, piecesLocationWhite);
        assertEquals(Side.WHITE, currentAllowedMoveSide);
    }

    @Test
    public void parseWithMovedPieces_one() throws FenParserException {
        // given
        Map<CasePosition, Pieces> blackPositions = new EnumMap<>(CasePosition.class);
        blackPositions.put(A8, B_ROOK);
        blackPositions.put(B8, B_KNIGHT);
        blackPositions.put(C8, B_BISHOP);
        blackPositions.put(D8, B_QUEEN);
        blackPositions.put(E8, B_KING);
        blackPositions.put(F8, B_BISHOP);
        blackPositions.put(G8, B_KNIGHT);
        blackPositions.put(H8, B_ROOK);
        blackPositions.put(A7, B_PAWN);
        blackPositions.put(B7, B_PAWN);
        blackPositions.put(C5, B_PAWN);
        blackPositions.put(D7, B_PAWN);
        blackPositions.put(E7, B_PAWN);
        blackPositions.put(F7, B_PAWN);
        blackPositions.put(G7, B_PAWN);
        blackPositions.put(H7, B_PAWN);

        Map<CasePosition, Pieces> whitePositions = new EnumMap<>(CasePosition.class);
        whitePositions.put(A2, W_PAWN);
        whitePositions.put(B2, W_PAWN);
        whitePositions.put(C2, W_PAWN);
        whitePositions.put(D2, W_PAWN);
        whitePositions.put(E4, W_PAWN);
        whitePositions.put(F2, W_PAWN);
        whitePositions.put(G2, W_PAWN);
        whitePositions.put(H2, W_PAWN);
        whitePositions.put(A1, W_ROOK);
        whitePositions.put(B1, W_KNIGHT);
        whitePositions.put(C1, W_BISHOP);
        whitePositions.put(D1, W_QUEEN);
        whitePositions.put(E1, W_KING);
        whitePositions.put(F1, W_BISHOP);
        whitePositions.put(G1, W_KNIGHT);
        whitePositions.put(H1, W_ROOK);

        // when
        FenPositionGameHandler game = FenGameParser.parse("rnbqkbnr/pp1ppppp/8/2p5/4P3/8/PPPP1PPP/RNBQKBNR b Qq");
        Map<CasePosition, Pieces> piecesLocationBlack = game.getPiecesLocation(Side.BLACK);
        Map<CasePosition, Pieces> piecesLocationWhite = game.getPiecesLocation(Side.WHITE);

        GameBoardData cloneOfCurrentDataState = game.getCloneOfCurrentDataState();


        Side currentAllowedMoveSide = cloneOfCurrentDataState.getCurrentAllowedMoveSide();

        // then
        assertFalse(cloneOfCurrentDataState.isWhiteKingCastlingAvailable());
        assertTrue(cloneOfCurrentDataState.isWhiteQueenCastlingAvailable());
        assertFalse(cloneOfCurrentDataState.isBlackKingCastlingAvailable());
        assertTrue(cloneOfCurrentDataState.isBlackQueenCastlingAvailable());

        assertEquals(blackPositions, piecesLocationBlack);
        assertEquals(whitePositions, piecesLocationWhite);
        assertEquals(Side.BLACK, currentAllowedMoveSide);
    }


    @Test
    public void parseWithMovedPieces_two() throws FenParserException {
        // given
        Map<CasePosition, Pieces> blackPositions = new EnumMap<>(CasePosition.class);
        blackPositions.put(A8, B_ROOK);
        blackPositions.put(C8, B_BISHOP);
        blackPositions.put(D8, B_QUEEN);
        blackPositions.put(E8, B_KING);
        blackPositions.put(F8, B_BISHOP);
        blackPositions.put(H8, B_ROOK);

        blackPositions.put(A7, B_PAWN);
        blackPositions.put(B7, B_PAWN);
        blackPositions.put(C7, B_PAWN);
        blackPositions.put(D7, B_PAWN);
        blackPositions.put(E7, B_PAWN);
        blackPositions.put(G7, B_PAWN);
        blackPositions.put(H7, B_PAWN);

        blackPositions.put(C6, B_KNIGHT);
        blackPositions.put(F6, B_KNIGHT);
        blackPositions.put(F5, B_PAWN);

        Map<CasePosition, Pieces> whitePositions = new EnumMap<>(CasePosition.class);
        whitePositions.put(A2, W_PAWN);
        whitePositions.put(B2, W_PAWN);
        whitePositions.put(C2, W_PAWN);
        whitePositions.put(E2, W_PAWN);
        whitePositions.put(F2, W_PAWN);
        whitePositions.put(G2, W_PAWN);
        whitePositions.put(H2, W_PAWN);

        whitePositions.put(A1, W_ROOK);
        whitePositions.put(C1, W_BISHOP);
        whitePositions.put(E1, W_KING);
        whitePositions.put(F1, W_BISHOP);
        whitePositions.put(H1, W_ROOK);

        whitePositions.put(D4, W_PAWN);
        whitePositions.put(A3, W_KNIGHT);
        whitePositions.put(D3, W_QUEEN);
        whitePositions.put(H3, W_KNIGHT);

        // when
        FenPositionGameHandler game = FenGameParser.parse("r1bqkb1r/ppppp1pp/2n2n2/5p2/3P4/N2Q3N/PPP1PPPP/R1B1KB1R W");
        Map<CasePosition, Pieces> piecesLocationBlack = game.getPiecesLocation(Side.BLACK);
        Map<CasePosition, Pieces> piecesLocationWhite = game.getPiecesLocation(Side.WHITE);

        GameBoardData cloneOfCurrentDataState = game.getCloneOfCurrentDataState();

        Side currentAllowedMoveSide = cloneOfCurrentDataState.getCurrentAllowedMoveSide();

        // then
        assertFalse(cloneOfCurrentDataState.isWhiteKingCastlingAvailable());
        assertFalse(cloneOfCurrentDataState.isWhiteQueenCastlingAvailable());
        assertFalse(cloneOfCurrentDataState.isBlackKingCastlingAvailable());
        assertFalse(cloneOfCurrentDataState.isBlackQueenCastlingAvailable());

        assertEquals(blackPositions, piecesLocationBlack);
        assertEquals(whitePositions, piecesLocationWhite);
        assertEquals(Side.WHITE, currentAllowedMoveSide);
    }
}