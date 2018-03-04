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

import ca.watier.echechessengine.contexts.StandardGameHandlerContext;
import ca.watier.echesscommon.enums.CasePosition;
import ca.watier.echesscommon.enums.Pieces;
import ca.watier.utils.EngineGameTest;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ca.watier.echesscommon.enums.CasePosition.*;
import static ca.watier.echesscommon.enums.MoveType.CAPTURE;
import static ca.watier.echesscommon.enums.MoveType.MOVE_NOT_ALLOWED;
import static ca.watier.echesscommon.enums.Pieces.*;
import static ca.watier.echesscommon.enums.SpecialGameRules.NO_CHECK_OR_CHECKMATE;
import static ca.watier.echesscommon.enums.SpecialGameRules.NO_PLAYER_TURN;

/**
 * Created by yannick on 5/8/2017.
 */
public class QueenMovesTest extends EngineGameTest {

    @Test
    public void moveTest() {
        List<CasePosition> allowedMoves = Arrays.asList(A8, C8, A6, C6, B8, B6, C7, A7);
        Map<CasePosition, Pieces> pieces = new HashMap<>();

        //Cannot move (blocked in all ways)
        pieces.put(E4, W_QUEEN);
        pieces.put(E5, W_PAWN);
        pieces.put(E3, W_PAWN);
        pieces.put(D5, W_PAWN);
        pieces.put(D3, W_PAWN);
        pieces.put(D4, W_PAWN);
        pieces.put(F4, W_PAWN);
        pieces.put(F5, W_PAWN);
        pieces.put(F3, W_PAWN);


        StandardGameHandlerContext gameHandler = new StandardGameHandlerContext(CONSTRAINT_SERVICE, WEB_SOCKET_SERVICE, pieces);
        gameHandler.addSpecialRule(NO_PLAYER_TURN, NO_CHECK_OR_CHECKMATE);

        //Cannot move (blocked in all ways)
        Assert.assertEquals(MOVE_NOT_ALLOWED, gameHandler.movePiece(E4, E2, WHITE));
        Assert.assertEquals(MOVE_NOT_ALLOWED, gameHandler.movePiece(E4, E6, WHITE));
        Assert.assertEquals(MOVE_NOT_ALLOWED, gameHandler.movePiece(E4, C4, WHITE));
        Assert.assertEquals(MOVE_NOT_ALLOWED, gameHandler.movePiece(E4, G2, WHITE));
        Assert.assertEquals(MOVE_NOT_ALLOWED, gameHandler.movePiece(E4, G2, WHITE));
        Assert.assertEquals(MOVE_NOT_ALLOWED, gameHandler.movePiece(E4, G6, WHITE));
        Assert.assertEquals(MOVE_NOT_ALLOWED, gameHandler.movePiece(E4, C6, WHITE));
        Assert.assertEquals(MOVE_NOT_ALLOWED, gameHandler.movePiece(E4, C2, WHITE));


        //Kill in all direction
        for (CasePosition position : allowedMoves) {
            pieces.clear();
            pieces.put(B7, W_QUEEN);
            pieces.put(A8, B_ROOK);
            pieces.put(C8, B_ROOK);
            pieces.put(A6, B_ROOK);
            pieces.put(C6, B_ROOK);
            pieces.put(B8, B_ROOK);
            pieces.put(B6, B_ROOK);
            pieces.put(C7, B_ROOK);
            pieces.put(A7, B_ROOK);

            Assert.assertEquals(CAPTURE, gameHandler.movePiece(B7, position, WHITE));
        }
    }
}
