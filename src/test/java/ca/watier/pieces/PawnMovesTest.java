/*
 *    Copyright 2014 - 2017 Yannick Watier
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package ca.watier.pieces;

import ca.watier.echechess.common.enums.KingStatus;
import ca.watier.echechess.common.enums.Side;
import ca.watier.echechess.common.responses.GameScoreResponse;
import ca.watier.echechess.engine.abstracts.GameBoardData;
import ca.watier.echechess.engine.delegates.PieceMoveConstraintDelegate;
import ca.watier.echechess.engine.exceptions.FenParserException;
import ca.watier.echechess.engine.game.FenPositionGameHandler;
import ca.watier.echechess.engine.handlers.PlayerHandlerImpl;
import ca.watier.echechess.engine.interfaces.GameEventEvaluatorHandler;
import ca.watier.echechess.engine.utils.FenGameParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static ca.watier.echechess.common.enums.CasePosition.*;
import static ca.watier.echechess.common.enums.MoveType.*;
import static ca.watier.echechess.common.enums.Pieces.B_PAWN;
import static ca.watier.echechess.common.enums.Pieces.W_PAWN;
import static ca.watier.echechess.common.enums.Side.BLACK;
import static ca.watier.echechess.common.enums.Side.WHITE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Created by yannick on 5/8/2017.
 */
@ExtendWith(MockitoExtension.class)
public class PawnMovesTest {

    @Spy
    private PlayerHandlerImpl playerHandler;
    @Spy
    private PieceMoveConstraintDelegate pieceMoveConstraintDelegate;
    @Spy
    private GameEventEvaluatorHandler gameEventEvaluatorHandler;

    /**
     * In this test, the white king should not be checked by the pawn
     */
    @Test
    public void check_with_pawns_front_move_two_position_Test() throws FenParserException {
        FenPositionGameHandler gameHandler = FenGameParser.parse("1k6/1p6/8/1K6/8/8/8/8 w KQkq", pieceMoveConstraintDelegate, playerHandler, gameEventEvaluatorHandler);

        assertThat(gameHandler.isKing(KingStatus.OK, WHITE)).isTrue();
    }

    /**
     * In this test, the white king should not be checked by the pawn
     */
    @Test
    public void check_with_pawns_front_move_one_position_Test() throws FenParserException {
        FenPositionGameHandler gameHandler = FenGameParser.parse("1k6/1p6/1K6/8/8/8/8/8 w KQkq", pieceMoveConstraintDelegate, playerHandler, gameEventEvaluatorHandler);

        assertThat(gameHandler.isKing(KingStatus.OK, WHITE)).isTrue();
    }

    //FIXME: TO MOCK
//    @Test
//    public void pawn_front_Test() throws FenParserException {
//        FenPositionGameHandler gameHandler = FenGameParser.parse("8/p7/p7/K7/8/8/8/8 w KQkq");
//        KingHandler kingHandler = gameHandler.getKingHandler();
//
//        assertThat(kingHandler.getPositionKingCanMove(WHITE, )).containsOnly(A4, A6, B4);
//        assertThat(gameHandler.isKing(OK, WHITE)).isTrue();
//
//        gameHandler.removePieceFromBoard(A6); //remove the pawn blocking
//
//        assertThat(kingHandler.getPositionKingCanMove(WHITE, )).containsOnly(A4, A6, B4, B5);
//        assertThat(gameHandler.isKing(OK, WHITE)).isTrue();
//    }
//
    //FIXME: TO MOCK
//    @Test
//    public void pawn_diagonal_Test() throws FenParserException {
//        FenPositionGameHandler gameHandler = FenGameParser.parse("8/1p6/2K5/8/8/8/8/8 w KQkq");
//        KingHandler kingHandler = gameHandler.getKingHandler();
//
//        assertThat(kingHandler.getPositionKingCanMove(WHITE, )).containsOnly(B7, C5, C7, D7, B6, D6, B5, D5);
//        assertThat(gameHandler.isCheck(WHITE)).isTrue();
//    }


    @Test
    public void moveTest() throws FenParserException {
        FenPositionGameHandler gameHandler = FenGameParser.parse("8/pp3p1p/7r/8/8/7R/PP3P1P/8 w KQkq", pieceMoveConstraintDelegate, playerHandler, gameEventEvaluatorHandler);
        when(gameEventEvaluatorHandler.isPlayerTurn(any(Side.class), any(GameBoardData.class))).thenReturn(true);

        //Cannot move (blocked in front)
        assertEquals(MOVE_NOT_ALLOWED, gameHandler.movePiece(H2, H4, WHITE)); // 2 cases
        assertEquals(MOVE_NOT_ALLOWED, gameHandler.movePiece(H2, H3, WHITE));
        assertEquals(MOVE_NOT_ALLOWED, gameHandler.movePiece(H7, H5, BLACK)); // 2 cases
        assertEquals(MOVE_NOT_ALLOWED, gameHandler.movePiece(H7, H6, BLACK));

        //Can move
        assertEquals(PAWN_HOP, gameHandler.movePiece(A2, A4, WHITE)); // 2 cases
        assertEquals(NORMAL_MOVE, gameHandler.movePiece(B2, B3, WHITE));
        assertEquals(PAWN_HOP, gameHandler.movePiece(A7, A5, BLACK)); // 2 cases
        assertEquals(NORMAL_MOVE, gameHandler.movePiece(B7, B6, BLACK));

        //Cannot move by 2 position (not on the starting position)
        assertEquals(MOVE_NOT_ALLOWED, gameHandler.movePiece(B3, B5, WHITE));
        assertEquals(MOVE_NOT_ALLOWED, gameHandler.movePiece(B6, B4, WHITE));

        //Can move by one position
        assertEquals(NORMAL_MOVE, gameHandler.movePiece(B3, B4, WHITE));
        assertEquals(NORMAL_MOVE, gameHandler.movePiece(B6, B5, BLACK));

        //cannot move diagonally (without attack)
        assertEquals(MOVE_NOT_ALLOWED, gameHandler.movePiece(F2, E3, WHITE));
        assertEquals(MOVE_NOT_ALLOWED, gameHandler.movePiece(F2, G3, WHITE));
        assertEquals(MOVE_NOT_ALLOWED, gameHandler.movePiece(F2, D4, WHITE)); // 2 cases
        assertEquals(MOVE_NOT_ALLOWED, gameHandler.movePiece(F2, H4, WHITE)); // 2 cases
        assertEquals(MOVE_NOT_ALLOWED, gameHandler.movePiece(F7, E6, BLACK));
        assertEquals(MOVE_NOT_ALLOWED, gameHandler.movePiece(F7, G6, BLACK));
        assertEquals(MOVE_NOT_ALLOWED, gameHandler.movePiece(F7, D5, WHITE)); // 2 cases
        assertEquals(MOVE_NOT_ALLOWED, gameHandler.movePiece(F7, H5, WHITE)); // 2 cases

        //Kill in all direction
        gameHandler = FenGameParser.parse("8/8/2p3p1/3P1P2/8/3p1p2/2P3P1/8 w KQkq", pieceMoveConstraintDelegate, playerHandler, gameEventEvaluatorHandler);
        assertEquals(CAPTURE, gameHandler.movePiece(D5, C6, WHITE));
        assertEquals(CAPTURE, gameHandler.movePiece(D3, C2, BLACK));
        assertEquals(CAPTURE, gameHandler.movePiece(F5, G6, WHITE));
        assertEquals(CAPTURE, gameHandler.movePiece(F3, G2, BLACK));
    }

    @Test
    public void pawnHopBlocked() throws FenParserException {
        FenPositionGameHandler gameHandler = FenGameParser.parse("k7/6p1/6n1/8/8/6N1/6P1/K7 w", pieceMoveConstraintDelegate, playerHandler, gameEventEvaluatorHandler);

        assertThat(gameHandler.getAllAvailableMoves(G2, WHITE)).isEmpty();
        assertThat(gameHandler.getAllAvailableMoves(G7, BLACK)).isEmpty();
    }

    @Test
    public void pawnHopAndNormalBlackSide() throws FenParserException {
        FenPositionGameHandler gameHandler = FenGameParser.parse("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq", pieceMoveConstraintDelegate, playerHandler, gameEventEvaluatorHandler);

        assertThat(gameHandler.getAllAvailableMoves(H2, WHITE)).isNotEmpty().containsOnly(H4, H3);
        assertThat(gameHandler.getAllAvailableMoves(D2, WHITE)).isNotEmpty().containsOnly(D4, D3);
    }

    @Test
    public void pawnHopAndNormalWhiteSide() throws FenParserException {
        FenPositionGameHandler gameHandler = FenGameParser.parse("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq", pieceMoveConstraintDelegate, playerHandler, gameEventEvaluatorHandler);

        assertThat(gameHandler.getAllAvailableMoves(H7, BLACK)).isNotEmpty().containsOnly(H5, H6);
        assertThat(gameHandler.getAllAvailableMoves(D7, BLACK)).isNotEmpty().containsOnly(D5, D6);
    }

    @Test
    public void pawnHopAndNormalWhiteSideBlackKingCheckMate1() throws FenParserException {

        // given
        FenPositionGameHandler gameHandler = FenGameParser.parse("8/8/1ppp4/1pkp4/1p6/1P2P3/3P4/4K3 w KQkq", pieceMoveConstraintDelegate, playerHandler, gameEventEvaluatorHandler);

        // when
        when(gameEventEvaluatorHandler.isPlayerTurn(any(Side.class), any(GameBoardData.class))).thenReturn(true);
        assertThat(gameHandler.isKing(KingStatus.OK, BLACK)).isTrue();
        assertThat(gameHandler.movePiece(D2, D4, WHITE)).isEqualByComparingTo(PAWN_HOP);

        // then
        assertThat(gameHandler.isCheckMate(BLACK)).isTrue();
    }

    @Test
    public void pawnHopAndCheck_enPassantAndOk() throws FenParserException {

        // given
        FenPositionGameHandler gameHandler = FenGameParser.parse("8/8/4R3/kp6/p1p4R/8/1P6/RR5K w KQkq", pieceMoveConstraintDelegate, playerHandler, gameEventEvaluatorHandler);

        // when
        when(gameEventEvaluatorHandler.isPlayerTurn(any(), any())).thenReturn(true);

        assertThat(gameHandler.isKing(KingStatus.OK, BLACK)).isTrue();
        assertThat(gameHandler.movePiece(B2, B4, WHITE)).isEqualByComparingTo(PAWN_HOP);

        // then
        assertThat(gameHandler.isCheck(BLACK)).isTrue(); //Checked because of the pawn, can be killed with the en passant move (A4 & C4)
    }


    @Test
    public void enPassantBlackSide() throws FenParserException {
        FenPositionGameHandler gameHandler = FenGameParser.parse("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq", pieceMoveConstraintDelegate, playerHandler, gameEventEvaluatorHandler);
        when(gameEventEvaluatorHandler.isPlayerTurn(any(Side.class), any(GameBoardData.class))).thenReturn(true);
        gameHandler.movePiece(H2, H4, WHITE);
        gameHandler.movePiece(H4, H5, WHITE);
        gameHandler.movePiece(G7, G5, BLACK); //Move by 2

        assertEquals(EN_PASSANT, gameHandler.movePiece(H5, G6, WHITE)); // En passant on the black pawn
        assertEquals(W_PAWN, gameHandler.getPiece(G6));
        assertEquals(new GameScoreResponse((short) 1, (short) 0), gameHandler.getGameScore());
    }

    @Test
    public void enPassantWhiteSide() throws FenParserException {
        FenPositionGameHandler gameHandler = FenGameParser.parse("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq", pieceMoveConstraintDelegate, playerHandler, gameEventEvaluatorHandler);
        when(gameEventEvaluatorHandler.isPlayerTurn(any(Side.class), any(GameBoardData.class))).thenReturn(true);
        gameHandler.movePiece(G7, G5, BLACK);
        gameHandler.movePiece(G5, G4, BLACK);
        gameHandler.movePiece(H2, H4, WHITE);

        assertEquals(EN_PASSANT, gameHandler.movePiece(G4, H3, BLACK)); // En passant on the white pawn
        assertEquals(B_PAWN, gameHandler.getPiece(H3));
        assertEquals(new GameScoreResponse((short) 0, (short) 1), gameHandler.getGameScore());
    }
}
