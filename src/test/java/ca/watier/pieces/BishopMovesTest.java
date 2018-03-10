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
import ca.watier.utils.EngineGameTest;
import org.junit.Test;

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
        String pattern = "E8:B_BISHOP;G8:B_BISHOP;F5:W_BISHOP;E2:W_BISHOP;D7:W_PAWN;G6:W_PAWN;E4:W_PAWN;G4:W_PAWN;";
        StandardGameHandlerContext gameHandler = new StandardGameHandlerContext(CONSTRAINT_SERVICE, WEB_SOCKET_SERVICE, pattern);
        gameHandler.addSpecialRule(NO_PLAYER_TURN, NO_CHECK_OR_CHECKMATE);

        assertThat(gameHandler.getAllAvailableMoves(F5, WHITE)).containsOnly(E6);
        assertThat(gameHandler.getAllAvailableMoves(E2, WHITE)).containsOnly(F1, D1, F3, D3, C4, B5, A6);
        assertThat(gameHandler.getAllAvailableMoves(E8, BLACK)).containsOnly(D7, F7, G6);
        assertThat(gameHandler.getAllAvailableMoves(G8, BLACK)).containsOnly(H7, F7, E6, D5, C4, B3, A2);
    }
}
