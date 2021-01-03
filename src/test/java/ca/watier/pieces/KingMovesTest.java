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

import ca.watier.echechess.common.enums.Pieces;
import ca.watier.echechess.common.enums.Side;
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
import static ca.watier.echechess.common.enums.MoveType.CASTLING;
import static ca.watier.echechess.common.enums.MoveType.MOVE_NOT_ALLOWED;
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
public class KingMovesTest {

    @Spy
    private PlayerHandlerImpl playerHandler;
    @Spy
    private PieceMoveConstraintDelegate pieceMoveConstraintDelegate;
    @Spy
    private GameEventEvaluatorHandler gameEventEvaluatorHandler;

    @Test
    public void validPathCastlingTest() throws FenParserException {
        FenPositionGameHandler gameHandler = FenGameParser.parse("r3k2r/8/8/8/8/8/8/R3K2R w KQkq", pieceMoveConstraintDelegate, playerHandler, gameEventEvaluatorHandler);

        when(gameEventEvaluatorHandler.isPlayerTurn(any(Side.class), any(GameBoardData.class))).thenReturn(true);

        assertEquals(CASTLING, gameHandler.movePiece(E1, A1, WHITE)); //Queen side
        assertEquals(CASTLING, gameHandler.movePiece(E8, H8, BLACK)); //Normal castling

        assertThat(gameHandler.getPiecesLocation()).isNotNull().containsKeys(A8, F8, G8, C1, D1, H1);
    }


    @Test
    public void attackedBothPathCastlingTest() throws FenParserException {
        FenPositionGameHandler gameHandler = FenGameParser.parse("r3k2r/2R3R1/8/8/8/8/1r3r2/R3K2R w KQkq");

        assertEquals(CASTLING, gameHandler.movePiece(E1, A1, WHITE)); //Queen side
        assertEquals(MOVE_NOT_ALLOWED, gameHandler.movePiece(E1, H1, WHITE)); //Normal castling, blocked
        assertEquals(MOVE_NOT_ALLOWED, gameHandler.movePiece(E8, H8, BLACK)); //Normal castling, blocked
        assertEquals(MOVE_NOT_ALLOWED, gameHandler.movePiece(E8, A8, BLACK)); //Queen side, blocked by C7
    }


    @Test
    public void attackedQueenSidePathCastlingTest() throws FenParserException {
        FenPositionGameHandler gameHandler = FenGameParser.parse("r3k2r/1r2pppp/8/8/8/8/1r2PPPP/R3K2R w KQkq", pieceMoveConstraintDelegate, playerHandler, gameEventEvaluatorHandler);


        when(gameEventEvaluatorHandler.isPlayerTurn(any(Side.class), any(GameBoardData.class))).thenReturn(true);

        assertEquals(CASTLING, gameHandler.movePiece(E1, A1, WHITE)); //Queen side
        assertEquals(MOVE_NOT_ALLOWED, gameHandler.movePiece(E8, A8, BLACK)); //Queen side, blocked the previous castling by the rook on D1
        assertEquals(CASTLING, gameHandler.movePiece(E8, H8, BLACK)); //Normal castling

        assertEquals(Pieces.W_KING, gameHandler.getPiece(C1));
        assertEquals(Pieces.W_ROOK, gameHandler.getPiece(D1));
        assertEquals(Pieces.B_KING, gameHandler.getPiece(G8));
        assertEquals(Pieces.B_ROOK, gameHandler.getPiece(F8));
    }


    @Test
    public void checkAtBeginningPositionQueenSidePathCastlingTest() throws FenParserException {
        FenPositionGameHandler gameHandler = FenGameParser.parse("r3k2r/1r3ppp/8/4R3/4r3/8/1r3PPP/R3K2R w KQkq");

        assertEquals(MOVE_NOT_ALLOWED, gameHandler.movePiece(E1, A1, WHITE)); //Queen side
        assertEquals(MOVE_NOT_ALLOWED, gameHandler.movePiece(E8, A8, BLACK)); //Queen side
        assertEquals(MOVE_NOT_ALLOWED, gameHandler.movePiece(E1, H1, WHITE)); //Normal castling
        assertEquals(MOVE_NOT_ALLOWED, gameHandler.movePiece(E8, H8, BLACK)); //Normal castling
    }


    @Test
    public void checkAtEndingPositionQueenSidePathCastlingTest() throws FenParserException {
        FenPositionGameHandler gameHandler = FenGameParser.parse("r3k2r/2R1pppp/8/8/8/8/2r1PPPP/R3K2R w KQkq");

        assertEquals(MOVE_NOT_ALLOWED, gameHandler.movePiece(E1, A1, WHITE)); //Queen side
        assertEquals(MOVE_NOT_ALLOWED, gameHandler.movePiece(E8, A8, BLACK)); //Queen side
        assertEquals(CASTLING, gameHandler.movePiece(E1, H1, WHITE)); //Normal castling
        assertEquals(CASTLING, gameHandler.movePiece(E8, H8, BLACK)); //Normal castling

        assertEquals(Pieces.W_KING, gameHandler.getPiece(G1));
        assertEquals(Pieces.W_ROOK, gameHandler.getPiece(F1));
        assertEquals(Pieces.B_KING, gameHandler.getPiece(G8));
        assertEquals(Pieces.B_ROOK, gameHandler.getPiece(F8));
    }

    @Test
    public void blockingPieceQueenSidePathCastlingTest() throws FenParserException {
        FenPositionGameHandler gameHandler = FenGameParser.parse("r2pk2r/4pppp/8/8/8/8/4PPPP/R2PK2R w KQkq", pieceMoveConstraintDelegate, playerHandler, gameEventEvaluatorHandler);

        when(gameEventEvaluatorHandler.isPlayerTurn(any(Side.class), any(GameBoardData.class))).thenReturn(true);

        assertEquals(MOVE_NOT_ALLOWED, gameHandler.movePiece(E1, A1, WHITE)); //Queen side
        assertEquals(MOVE_NOT_ALLOWED, gameHandler.movePiece(E8, A8, BLACK)); //Queen side
        assertEquals(CASTLING, gameHandler.movePiece(E1, H1, WHITE)); //Normal castling
        assertEquals(CASTLING, gameHandler.movePiece(E8, H8, BLACK)); //Normal castling

        assertEquals(Pieces.W_KING, gameHandler.getPiece(G1));
        assertEquals(Pieces.W_ROOK, gameHandler.getPiece(F1));
        assertEquals(Pieces.B_KING, gameHandler.getPiece(G8));
        assertEquals(Pieces.B_ROOK, gameHandler.getPiece(F8));
    }

    //FIXME: TO MOCK
//    @Test
//    public void king_Test() throws FenParserException {
//        FenPositionGameHandler gameHandler = FenGameParser.parse("8/8/2k5/3K4/8/8/8/8 w KQkq");
//        KingHandler kingHandler = gameHandler.getKingHandler();
//
//        assertThat(kingHandler.getPositionKingCanMove(WHITE, )).containsOnly(C4, D4, E4, E5, E6);
//        assertThat(gameHandler.isCheck(WHITE)).isTrue();
//    }

    @Test
    public void movedPiecesQueenSidePathCastlingTest() throws FenParserException {
        FenPositionGameHandler gameHandler = FenGameParser.parse("r3k2r/4pppp/8/8/8/8/4PPPP/R3K2R w KQkq", pieceMoveConstraintDelegate, playerHandler, gameEventEvaluatorHandler);

        //Move the white king
        gameHandler.movePiece(E1, D2, WHITE);
        //Move the white king to the original position
        gameHandler.movePiece(D2, E1, WHITE);

        //Move the black rook
        gameHandler.movePiece(A8, A7, BLACK);
        //Move the black rook to the original position
        gameHandler.movePiece(A7, A8, BLACK);

        assertEquals(MOVE_NOT_ALLOWED, gameHandler.movePiece(E1, A1, WHITE)); //Queen side
        assertEquals(MOVE_NOT_ALLOWED, gameHandler.movePiece(E8, A8, BLACK)); //Queen side
    }
}
