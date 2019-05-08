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
import ca.watier.echechess.engine.engines.GenericGameHandler;
import ca.watier.echechess.engine.exceptions.FenParserException;
import ca.watier.echechess.engine.game.FenPositionGameHandler;
import ca.watier.echechess.engine.handlers.GamePropertiesHandlerImpl;
import ca.watier.echechess.engine.handlers.KingHandlerImpl;
import ca.watier.echechess.engine.handlers.PlayerHandlerImpl;
import ca.watier.echechess.engine.utils.FenGameParser;
import org.assertj.core.api.ListAssert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static ca.watier.echechess.common.enums.CasePosition.*;
import static ca.watier.echechess.common.enums.Side.BLACK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AllPiecesTest {

    @Spy
    private PlayerHandlerImpl playerHandler;
    @Spy
    private KingHandlerImpl kingHandler;
    @Spy
    private GamePropertiesHandlerImpl gamePropertiesHandler;

    @Test
    public void whiteTest() {
        GenericGameHandler gameHandler = new GenericGameHandler(kingHandler, playerHandler, gamePropertiesHandler);
        List<Pair<CasePosition, Pieces>> allPiecesThatCanMoveTo = gameHandler.getAllPiecesThatCanMoveTo(CasePosition.F3, Side.WHITE);

        ListAssert<Pair<CasePosition, Pieces>> pairListAssert = assertThat(allPiecesThatCanMoveTo);

        pairListAssert.hasSize(2);
        pairListAssert.containsExactlyInAnyOrder(new Pair<>(CasePosition.F2, Pieces.W_PAWN), new Pair<>(CasePosition.G1, Pieces.W_KNIGHT));
    }

    @Test
    public void blackTest() {
        GenericGameHandler gameHandler = new GenericGameHandler(kingHandler, playerHandler, gamePropertiesHandler);
        List<Pair<CasePosition, Pieces>> allPiecesThatCanMoveTo = gameHandler.getAllPiecesThatCanMoveTo(C6, BLACK);

        ListAssert<Pair<CasePosition, Pieces>> pairListAssert = assertThat(allPiecesThatCanMoveTo);

        pairListAssert.hasSize(2);
        pairListAssert.containsExactlyInAnyOrder(new Pair<>(CasePosition.B8, Pieces.B_KNIGHT), new Pair<>(CasePosition.C7, Pieces.B_PAWN));
    }

    @Test
    public void cannotKillKingTest() throws FenParserException {
        when(playerHandler.isPlayerTurn(any(Side.class))).thenReturn(true);

        FenPositionGameHandler gameHandler = FenGameParser.parse("1nq4k/3p4/2K5/8/b1r5/8/8/8 w", kingHandler, playerHandler, gamePropertiesHandler);

        List<Pair<CasePosition, Pieces>> allPiecesThatCanMoveTo = gameHandler.getAllPiecesThatCanMoveTo(C6, BLACK);
        assertThat(allPiecesThatCanMoveTo).isEmpty();

        assertThat(gameHandler.movePiece(A4, C6, BLACK)).isEqualByComparingTo(MoveType.MOVE_NOT_ALLOWED);
        assertThat(gameHandler.movePiece(C4, C6, BLACK)).isEqualByComparingTo(MoveType.MOVE_NOT_ALLOWED);
        assertThat(gameHandler.movePiece(D7, C6, BLACK)).isEqualByComparingTo(MoveType.MOVE_NOT_ALLOWED);
        assertThat(gameHandler.movePiece(C8, C6, BLACK)).isEqualByComparingTo(MoveType.MOVE_NOT_ALLOWED);
        assertThat(gameHandler.movePiece(B8, C6, BLACK)).isEqualByComparingTo(MoveType.MOVE_NOT_ALLOWED);
    }
}
