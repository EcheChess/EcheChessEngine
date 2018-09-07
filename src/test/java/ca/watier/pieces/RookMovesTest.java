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

import ca.watier.echechess.engine.exceptions.FenParserException;
import ca.watier.echechess.engine.game.FenPositionGameHandler;
import ca.watier.echechess.engine.utils.FenGameParser;
import ca.watier.utils.EngineGameTest;
import org.junit.Assert;
import org.junit.Test;

import static ca.watier.echechess.common.enums.CasePosition.*;
import static ca.watier.echechess.common.enums.MoveType.CAPTURE;
import static ca.watier.echechess.common.enums.MoveType.MOVE_NOT_ALLOWED;
import static ca.watier.echechess.common.enums.SpecialGameRules.NO_CHECK_OR_CHECKMATE;
import static ca.watier.echechess.common.enums.SpecialGameRules.NO_PLAYER_TURN;

/**
 * Created by yannick on 5/8/2017.
 */
public class RookMovesTest extends EngineGameTest {

    @Test
    public void moveTest() throws FenParserException {
        FenPositionGameHandler gameHandler = FenGameParser.parse("r6r/8/8/4p3/3pRp2/4p3/8/r5rR w KQkq");
        gameHandler.addSpecialRule(NO_PLAYER_TURN, NO_CHECK_OR_CHECKMATE);

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
}
