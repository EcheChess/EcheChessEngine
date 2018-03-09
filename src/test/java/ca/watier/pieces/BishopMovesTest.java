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
import ca.watier.utils.EngineGameTest;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

import static ca.watier.echesscommon.enums.CasePosition.*;
import static ca.watier.echesscommon.enums.SpecialGameRules.NO_CHECK_OR_CHECKMATE;
import static ca.watier.echesscommon.enums.SpecialGameRules.NO_PLAYER_TURN;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by yannick on 5/8/2017.
 */
public class BishopMovesTest extends EngineGameTest {

    @Test
    public void moveTest() {
        CasePosition wBishopF5 = CasePosition.F5;
        CasePosition wBishopE2 = CasePosition.E2;
        CasePosition bBishopE8 = CasePosition.E8;
        CasePosition bBishopG8 = CasePosition.G8;

        String pattern = "E8:B_BISHOP;G8:B_BISHOP;F5:W_BISHOP;E2:W_BISHOP;D7:W_PAWN;G6:W_PAWN;E4:W_PAWN;G4:W_PAWN;";
        StandardGameHandlerContext gameHandler = new StandardGameHandlerContext(CONSTRAINT_SERVICE, WEB_SOCKET_SERVICE, pattern);
        gameHandler.addSpecialRule(NO_PLAYER_TURN, NO_CHECK_OR_CHECKMATE);

        List<CasePosition> allAvailableMovesWBishopF5 = gameHandler.getAllAvailableMoves(wBishopF5, WHITE);
        List<CasePosition> allAvailableMovesWBishopE2 = gameHandler.getAllAvailableMoves(wBishopE2, WHITE);
        List<CasePosition> allAvailableMovesBBishopE8 = gameHandler.getAllAvailableMoves(bBishopE8, BLACK);
        List<CasePosition> allAvailableMovesBBishopG8 = gameHandler.getAllAvailableMoves(bBishopG8, BLACK);


        assertThat(allAvailableMovesWBishopF5).containsOnly(E6);
        assertThat(allAvailableMovesWBishopE2).containsOnly(F1, D1, F3, D3, C4, B5, A6);
        assertThat(allAvailableMovesBBishopE8).containsOnly(D7, F7, G6);
        assertThat(allAvailableMovesBBishopG8).containsOnly(H7, F7, E6, D5, C4, B3, A2);

        //gameHandler.movePiece(E4, C6, WHITE);
        Assert.fail("Add the revertLastMove method");


//        List<CasePosition> allowedMoves = Arrays.asList(A8, C8, A6, C6);
//        Map<CasePosition, Pieces> pieces = new HashMap<>();
//
//        //Cannot move (blocked in all ways)
//        pieces.put(E4, W_BISHOP);
//        pieces.put(D5, W_PAWN);
//        pieces.put(D3, W_PAWN);
//        pieces.put(F5, W_PAWN);
//        pieces.put(F3, W_PAWN);
//
//        StandardGameHandlerContext gameHandler = new StandardGameHandlerContext(CONSTRAINT_SERVICE, WEB_SOCKET_SERVICE, pieces);
//        gameHandler.addSpecialRule(NO_PLAYER_TURN, NO_CHECK_OR_CHECKMATE);
//
//        //Cannot move (blocked in all ways)
//        Assert.assertEquals(MoveType.MOVE_NOT_ALLOWED, gameHandler.movePiece(E4, C6, WHITE));
//        Assert.assertEquals(MoveType.MOVE_NOT_ALLOWED, gameHandler.movePiece(E4, G6, WHITE));
//        Assert.assertEquals(MoveType.MOVE_NOT_ALLOWED, gameHandler.movePiece(E4, C2, WHITE));
//        Assert.assertEquals(MoveType.MOVE_NOT_ALLOWED, gameHandler.movePiece(E4, G2, WHITE));
//
//
//        //Kill in all direction
//        for (CasePosition position : allowedMoves) {
//            pieces.clear();
//            pieces.put(B7, W_BISHOP);
//            pieces.put(A8, B_ROOK);
//            pieces.put(C8, B_ROOK);
//            pieces.put(A6, B_ROOK);
//            pieces.put(C6, B_ROOK);
//
//            Assert.assertEquals(MoveType.CAPTURE, gameHandler.movePiece(B7, position, WHITE));
//        }
    }
}
