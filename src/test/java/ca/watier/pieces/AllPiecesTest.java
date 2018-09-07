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
import ca.watier.echechess.common.enums.Pieces;
import ca.watier.echechess.common.enums.Side;
import ca.watier.echechess.common.utils.Pair;
import ca.watier.echechess.engine.game.SimpleCustomPositionGameHandler;
import ca.watier.utils.EngineGameTest;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class AllPiecesTest extends EngineGameTest {


    private SimpleCustomPositionGameHandler simpleCustomPositionGameHandler;

    @Before
    public void setUp() {
        simpleCustomPositionGameHandler = new SimpleCustomPositionGameHandler(CONSTRAINT_SERVICE);
    }

    @Test
    public void whiteTest() {
        List<Pair<CasePosition, Pieces>> allPiecesThatCanMoveTo = simpleCustomPositionGameHandler.getAllPiecesThatCanMoveTo(CasePosition.F3, Side.WHITE);
        Assertions.assertThat(allPiecesThatCanMoveTo).hasSize(2);

        Pair<CasePosition, Pieces> whitePawn = allPiecesThatCanMoveTo.get(0); //Pawn
        Assertions.assertThat(whitePawn.getFirstValue()).isEqualByComparingTo(CasePosition.F2);
        Assertions.assertThat(whitePawn.getSecondValue()).isEqualByComparingTo(Pieces.W_PAWN);

        Pair<CasePosition, Pieces> whiteKnight = allPiecesThatCanMoveTo.get(1); //Knight
        Assertions.assertThat(whiteKnight.getFirstValue()).isEqualByComparingTo(CasePosition.G1);
        Assertions.assertThat(whiteKnight.getSecondValue()).isEqualByComparingTo(Pieces.W_KNIGHT);
    }

    @Test
    public void blackTest() {
        List<Pair<CasePosition, Pieces>> allPiecesThatCanMoveTo = simpleCustomPositionGameHandler.getAllPiecesThatCanMoveTo(CasePosition.C6, Side.BLACK);
        Assertions.assertThat(allPiecesThatCanMoveTo).hasSize(2);

        Pair<CasePosition, Pieces> blackKnight = allPiecesThatCanMoveTo.get(0); //Knight
        Assertions.assertThat(blackKnight.getFirstValue()).isEqualByComparingTo(CasePosition.B8);
        Assertions.assertThat(blackKnight.getSecondValue()).isEqualByComparingTo(Pieces.B_KNIGHT);

        Pair<CasePosition, Pieces> blackPawn = allPiecesThatCanMoveTo.get(1); //Pawn
        Assertions.assertThat(blackPawn.getFirstValue()).isEqualByComparingTo(CasePosition.C7);
        Assertions.assertThat(blackPawn.getSecondValue()).isEqualByComparingTo(Pieces.B_PAWN);
    }
}
