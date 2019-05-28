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

import ca.watier.echechess.common.enums.CasePosition;
import ca.watier.echechess.common.enums.MoveType;
import ca.watier.echechess.common.enums.Pieces;
import ca.watier.echechess.common.enums.Side;
import ca.watier.echechess.common.utils.Pair;
import ca.watier.echechess.engine.abstracts.GameBoardData;
import ca.watier.echechess.engine.delegates.PieceMoveConstraintDelegate;
import ca.watier.echechess.engine.engines.GenericGameHandler;
import ca.watier.echechess.engine.exceptions.FenParserException;
import ca.watier.echechess.engine.game.FenPositionGameHandler;
import ca.watier.echechess.engine.handlers.StandardKingHandlerImpl;
import ca.watier.echechess.engine.handlers.PlayerHandlerImpl;
import ca.watier.echechess.engine.interfaces.GameEventEvaluatorHandler;
import ca.watier.echechess.engine.utils.FenGameParser;
import org.assertj.core.api.ListAssert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static ca.watier.echechess.common.enums.CasePosition.*;
import static ca.watier.echechess.common.enums.Side.BLACK;
import static ca.watier.echechess.common.enums.Side.WHITE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AllPiecesTest {

    @Spy
    private PlayerHandlerImpl playerHandler;
    @Spy
    private PieceMoveConstraintDelegate pieceMoveConstraintDelegate;
    @Spy
    private GameEventEvaluatorHandler gameEventEvaluatorHandler;

    @Test
    public void whiteTest() {
        GenericGameHandler gameHandler = new GenericGameHandler(pieceMoveConstraintDelegate, playerHandler, gameEventEvaluatorHandler);
        List<Pair<CasePosition, Pieces>> allPiecesThatCanMoveTo = gameHandler.getAllPiecesThatCanMoveTo(CasePosition.F3, Side.WHITE);

        ListAssert<Pair<CasePosition, Pieces>> pairListAssert = assertThat(allPiecesThatCanMoveTo);

        pairListAssert.hasSize(2);
        pairListAssert.containsExactlyInAnyOrder(new Pair<>(CasePosition.F2, Pieces.W_PAWN), new Pair<>(CasePosition.G1, Pieces.W_KNIGHT));
    }

    @Test
    public void blackTest() {
        GenericGameHandler gameHandler = new GenericGameHandler(pieceMoveConstraintDelegate, playerHandler, gameEventEvaluatorHandler);
        List<Pair<CasePosition, Pieces>> allPiecesThatCanMoveTo = gameHandler.getAllPiecesThatCanMoveTo(C6, BLACK);

        ListAssert<Pair<CasePosition, Pieces>> pairListAssert = assertThat(allPiecesThatCanMoveTo);

        pairListAssert.hasSize(2);
        pairListAssert.containsExactlyInAnyOrder(new Pair<>(CasePosition.B8, Pieces.B_KNIGHT), new Pair<>(CasePosition.C7, Pieces.B_PAWN));
    }

    @Test
    public void cannotKillKingTest() throws FenParserException {
        when(gameEventEvaluatorHandler.isPlayerTurn(any(Side.class), any(GameBoardData.class))).thenReturn(true);

        FenPositionGameHandler gameHandler = FenGameParser.parse("1nq4k/3p4/2K5/8/b1r5/8/8/8 w", pieceMoveConstraintDelegate, playerHandler, gameEventEvaluatorHandler);

        List<Pair<CasePosition, Pieces>> allPiecesThatCanMoveTo = gameHandler.getAllPiecesThatCanMoveTo(C6, BLACK);
        assertThat(allPiecesThatCanMoveTo).isEmpty();

        assertThat(gameHandler.movePiece(A4, C6, BLACK)).isEqualByComparingTo(MoveType.MOVE_NOT_ALLOWED);
        assertThat(gameHandler.movePiece(C4, C6, BLACK)).isEqualByComparingTo(MoveType.MOVE_NOT_ALLOWED);
        assertThat(gameHandler.movePiece(D7, C6, BLACK)).isEqualByComparingTo(MoveType.MOVE_NOT_ALLOWED);
        assertThat(gameHandler.movePiece(C8, C6, BLACK)).isEqualByComparingTo(MoveType.MOVE_NOT_ALLOWED);
        assertThat(gameHandler.movePiece(B8, C6, BLACK)).isEqualByComparingTo(MoveType.MOVE_NOT_ALLOWED);
    }


    @Test
    public void cantKillKingButCanKillOtherCheckMateTest() throws FenParserException {
        FenPositionGameHandler gameHandler = FenGameParser.parse("8/3B4/3R4/1k1R1q2/3R4/3B4/8/8 w", pieceMoveConstraintDelegate, playerHandler, gameEventEvaluatorHandler);

        assertThat(gameHandler.getAllAvailableMoves(D7, WHITE)).containsExactlyInAnyOrder(C6, C8, E8, E6, F5);
        assertThat(gameHandler.getAllAvailableMoves(D6, WHITE)).containsExactlyInAnyOrder(A6, B6, C6, E6, F6, G6, H6);
        assertThat(gameHandler.getAllAvailableMoves(D5, WHITE)).containsExactlyInAnyOrder(C5, E5, F5);
        assertThat(gameHandler.getAllAvailableMoves(D4, WHITE)).containsExactlyInAnyOrder(A4, B4, C4, E4, F4, G4, H4);
        assertThat(gameHandler.getAllAvailableMoves(D3, WHITE)).containsExactlyInAnyOrder(C4, C2, B1, E4, E2, F1, F5);

        assertThat(gameHandler.isCheck(BLACK)).isFalse();
        assertThat(gameHandler.isCheckMate(BLACK)).isTrue();
    }
}
