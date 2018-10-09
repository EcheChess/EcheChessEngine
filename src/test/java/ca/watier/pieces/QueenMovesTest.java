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
import ca.watier.echechess.engine.exceptions.FenParserException;
import ca.watier.echechess.engine.game.FenPositionGameHandler;
import ca.watier.echechess.engine.utils.FenGameParser;
import ca.watier.utils.EngineGameTest;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static ca.watier.echechess.common.enums.CasePosition.*;
import static ca.watier.echechess.common.enums.MoveType.MOVE_NOT_ALLOWED;
import static ca.watier.echechess.common.enums.SpecialGameRules.NO_CHECK_OR_CHECKMATE;
import static ca.watier.echechess.common.enums.SpecialGameRules.NO_PLAYER_TURN;
import static org.junit.Assert.assertEquals;

/**
 * Created by yannick on 5/8/2017.
 */
public class QueenMovesTest extends EngineGameTest {

    @Test
    public void moveTest() throws FenParserException {
        List<CasePosition> allowedMoves = Arrays.asList(A8, C8, A6, C6, B8, B6, C7, A7);

        FenPositionGameHandler gameHandler = FenGameParser.parse("8/8/8/3PPP2/3PQP2/3PPP2/8/8 w KQkq");
        gameHandler.addSpecialRule(NO_PLAYER_TURN, NO_CHECK_OR_CHECKMATE);

        //Cannot move (blocked in all ways)
        assertEquals(MOVE_NOT_ALLOWED, gameHandler.movePiece(E4, E2, WHITE));
        assertEquals(MOVE_NOT_ALLOWED, gameHandler.movePiece(E4, E6, WHITE));
        assertEquals(MOVE_NOT_ALLOWED, gameHandler.movePiece(E4, C4, WHITE));
        assertEquals(MOVE_NOT_ALLOWED, gameHandler.movePiece(E4, G2, WHITE));
        assertEquals(MOVE_NOT_ALLOWED, gameHandler.movePiece(E4, G2, WHITE));
        assertEquals(MOVE_NOT_ALLOWED, gameHandler.movePiece(E4, G6, WHITE));
        assertEquals(MOVE_NOT_ALLOWED, gameHandler.movePiece(E4, C6, WHITE));
        assertEquals(MOVE_NOT_ALLOWED, gameHandler.movePiece(E4, C2, WHITE));
    }


    @Test
    public void attackTest() throws FenParserException {
        FenPositionGameHandler gameHandler = FenGameParser.parse("rrr5/rQr5/rrr5/8/8/8/8/8 w KQkq");
        gameHandler.addSpecialRule(NO_PLAYER_TURN, NO_CHECK_OR_CHECKMATE);

        Assertions.assertThat(gameHandler.getAllAvailableMoves(B7, WHITE)).containsOnly(A8, C8, A6, C6, B8, B6, C7, A7);
    }
}
