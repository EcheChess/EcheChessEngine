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

import ca.watier.echechess.common.enums.MoveType;
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
import static ca.watier.echechess.common.enums.Side.BLACK;
import static ca.watier.echechess.common.enums.Side.WHITE;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by yannick on 5/8/2017.
 */
@ExtendWith(MockitoExtension.class)
public class BishopMovesTest {

    @Spy
    private PlayerHandlerImpl playerHandler;
    @Spy
    private PieceMoveConstraintDelegate pieceMoveConstraintDelegate;
    @Spy
    private GameEventEvaluatorHandler gameEventEvaluatorHandler;


    @Test
    public void moveTest() throws FenParserException {
        FenPositionGameHandler gameHandler = FenGameParser.parse("4b1b1/3P4/6P1/5B2/4P1P1/8/4B3/8 w", pieceMoveConstraintDelegate, playerHandler, gameEventEvaluatorHandler);

        assertThat(gameHandler.getAllAvailableMoves(F5, WHITE)).containsOnly(E6);
        assertThat(gameHandler.getAllAvailableMoves(E2, WHITE)).containsOnly(F1, D1, F3, D3, C4, B5, A6);
        assertThat(gameHandler.getAllAvailableMoves(E8, BLACK)).containsOnly(D7, F7, G6);
        assertThat(gameHandler.getAllAvailableMoves(G8, BLACK)).containsOnly(H7, F7, E6, D5, C4, B3, A2);
    }


    @Test
    public void cantMoveBlockedPiecesSameSide() throws FenParserException {
        FenPositionGameHandler gameHandler = FenGameParser.parse("8/8/8/3P1P2/4B3/3P1P2/8/8 w", pieceMoveConstraintDelegate, playerHandler, gameEventEvaluatorHandler);

        assertThat(gameHandler.getAllAvailableMoves(E4, WHITE)).isEmpty();

        //Move over other ally pieces
        assertThat(gameHandler.movePiece(E4, C6, WHITE)).isEqualByComparingTo(MoveType.MOVE_NOT_ALLOWED);
        assertThat(gameHandler.movePiece(E4, C2, WHITE)).isEqualByComparingTo(MoveType.MOVE_NOT_ALLOWED);
        assertThat(gameHandler.movePiece(E4, G6, WHITE)).isEqualByComparingTo(MoveType.MOVE_NOT_ALLOWED);
        assertThat(gameHandler.movePiece(E4, G2, WHITE)).isEqualByComparingTo(MoveType.MOVE_NOT_ALLOWED);


        //Try to kill ally
        assertThat(gameHandler.movePiece(E4, D5, WHITE)).isEqualByComparingTo(MoveType.MOVE_NOT_ALLOWED);
        assertThat(gameHandler.movePiece(E4, D3, WHITE)).isEqualByComparingTo(MoveType.MOVE_NOT_ALLOWED);
        assertThat(gameHandler.movePiece(E4, F3, WHITE)).isEqualByComparingTo(MoveType.MOVE_NOT_ALLOWED);
        assertThat(gameHandler.movePiece(E4, F5, WHITE)).isEqualByComparingTo(MoveType.MOVE_NOT_ALLOWED);
    }

    //FIXME: TO MOCK
//    @Test
//    public void bishop_Test() throws FenParserException {
//        FenPositionGameHandler gameHandler = FenGameParser.parse("8/1b6/2r5/3K4/8/8/8/8 w KQkq");
//
//        assertThat(kingHandler.getPositionKingCanMove(WHITE, )).containsOnly(D4, E4, E5);
//        assertThat(gameHandler.isKing(OK, WHITE)).isTrue();
//
//        gameHandler.removePieceFromBoard(C6); //remove the rook blocking
//
//        assertThat(kingHandler.getPositionKingCanMove(WHITE, )).containsOnly(D6, E6, E5, D4, C4, C5);
//        assertThat(gameHandler.isCheck(WHITE)).isTrue();
//    }

    @Test
    public void cantKillKingButCheckTest() throws FenParserException {
        FenPositionGameHandler gameHandler = FenGameParser.parse("8/8/8/8/8/5k2/8/7B w", pieceMoveConstraintDelegate, playerHandler, gameEventEvaluatorHandler);

        assertThat(gameHandler.getAllAvailableMoves(H1, WHITE)).containsOnly(G2, F3);
        assertThat(gameHandler.isCheck(BLACK)).isTrue();
        assertThat(gameHandler.isCheckMate(BLACK)).isFalse();
    }

    @Test
    public void cantKillKingButCheckMateTest() throws FenParserException {
        FenPositionGameHandler gameHandler = FenGameParser.parse("k7/8/3B4/3B4/3B4/8/8/8 w", pieceMoveConstraintDelegate, playerHandler, gameEventEvaluatorHandler);

        assertThat(gameHandler.getAllAvailableMoves(D4, WHITE)).containsExactlyInAnyOrder(C5, A7, B6, E3, F2, G1, C3, B2, A1, E5, F6, G7, H8);
        assertThat(gameHandler.getAllAvailableMoves(D5, WHITE)).containsExactlyInAnyOrder(C6, B7, E4, F3, G2, H1, C4, B3, A2, E6, F7, G8, A8); //King: A8
        assertThat(gameHandler.getAllAvailableMoves(D6, WHITE)).containsExactlyInAnyOrder(C7, B8, E5, F4, G3, H2, C5, B4, A3, E7, F8);

        assertThat(gameHandler.isCheck(BLACK)).isFalse();
        assertThat(gameHandler.isCheckMate(BLACK)).isTrue();
    }

    @Test
    public void cantMoveBishopOrCheckMateTest() throws FenParserException {
        FenPositionGameHandler gameHandler = FenGameParser.parse("kb6/b7/2b5/8/4B3/8/8/8 w", pieceMoveConstraintDelegate, playerHandler, gameEventEvaluatorHandler);

        assertThat(gameHandler.getAllAvailableMoves(C6, BLACK)).containsExactlyInAnyOrder(B7, D5, E4);

        assertThat(gameHandler.isCheck(BLACK)).isFalse();
        assertThat(gameHandler.isCheckMate(BLACK)).isFalse();
    }
}
