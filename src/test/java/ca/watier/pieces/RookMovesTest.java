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

import ca.watier.echechess.common.enums.Side;
import ca.watier.echechess.engine.abstracts.GameBoardData;
import ca.watier.echechess.engine.delegates.PieceMoveConstraintDelegate;
import ca.watier.echechess.engine.exceptions.FenParserException;
import ca.watier.echechess.engine.game.FenPositionGameHandler;
import ca.watier.echechess.engine.handlers.StandardKingHandlerImpl;
import ca.watier.echechess.engine.handlers.PlayerHandlerImpl;
import ca.watier.echechess.engine.interfaces.GameEventEvaluatorHandler;
import ca.watier.echechess.engine.interfaces.KingHandler;
import ca.watier.echechess.engine.utils.FenGameParser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import static ca.watier.echechess.common.enums.CasePosition.*;
import static ca.watier.echechess.common.enums.KingStatus.OK;
import static ca.watier.echechess.common.enums.MoveType.CAPTURE;
import static ca.watier.echechess.common.enums.MoveType.MOVE_NOT_ALLOWED;
import static ca.watier.echechess.common.enums.Side.WHITE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Created by yannick on 5/8/2017.
 */
@RunWith(MockitoJUnitRunner.class)
public class RookMovesTest {

    @Spy
    private PlayerHandlerImpl playerHandler;
    @Spy
    private PieceMoveConstraintDelegate pieceMoveConstraintDelegate;
    @Spy
    private GameEventEvaluatorHandler gameEventEvaluatorHandler;

    @Before
    public void setUp() {
        when(gameEventEvaluatorHandler.isPlayerTurn(any(Side.class), any(GameBoardData.class))).thenReturn(true);
    }

    @Test
    public void moveTest() throws FenParserException {
        FenPositionGameHandler gameHandler = FenGameParser.parse("r6r/8/8/4p3/3pRp2/4p3/8/r5rR w KQkq", pieceMoveConstraintDelegate, playerHandler, gameEventEvaluatorHandler);

        //Cannot move (blocked in all ways)
        Assert.assertEquals(MOVE_NOT_ALLOWED, gameHandler.movePiece(E4, E8, WHITE));
        Assert.assertEquals(MOVE_NOT_ALLOWED, gameHandler.movePiece(E4, E1, WHITE));
        Assert.assertEquals(MOVE_NOT_ALLOWED, gameHandler.movePiece(E4, A4, WHITE));
        Assert.assertEquals(MOVE_NOT_ALLOWED, gameHandler.movePiece(E4, H4, WHITE));

        //Kill in all direction
        Assert.assertEquals(CAPTURE, gameHandler.movePiece(H1, H8, WHITE));
        Assert.assertEquals(CAPTURE, gameHandler.movePiece(H8, A8, WHITE));
        Assert.assertEquals(CAPTURE, gameHandler.movePiece(A8, A1, WHITE));
        Assert.assertEquals(CAPTURE, gameHandler.movePiece(A1, G1, WHITE));

        //cannot move diagonally
        Assert.assertEquals(MOVE_NOT_ALLOWED, gameHandler.movePiece(E4, D5, WHITE));
        Assert.assertEquals(MOVE_NOT_ALLOWED, gameHandler.movePiece(E4, D3, WHITE));
        Assert.assertEquals(MOVE_NOT_ALLOWED, gameHandler.movePiece(E4, F5, WHITE));
        Assert.assertEquals(MOVE_NOT_ALLOWED, gameHandler.movePiece(E4, F3, WHITE));

    }

    //FIXME: TO MOCK
//    @Test
//    public void rook_Test() throws FenParserException {
//        FenPositionGameHandler gameHandler = FenGameParser.parse("r7/p7/8/K7/8/8/8/8 w KQkq");
//        KingHandler kingHandler = gameHandler.getKingHandler();
//
//        assertThat(kingHandler.getPositionKingCanMove(WHITE, )).containsOnly(A4, A6, B5, B4);
//        assertThat(gameHandler.isKing(OK, WHITE)).isTrue();
//
//        gameHandler.removePieceFromBoard(A7); //remove the pawn blocking
//
//        assertThat(kingHandler.getPositionKingCanMove(WHITE, )).containsOnly(B6, B5, B4);
//        assertThat(gameHandler.isCheck(WHITE)).isTrue();
//    }

}
