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
import ca.watier.echechess.engine.exceptions.FenParserException;
import ca.watier.echechess.engine.game.FenPositionGameHandler;
import ca.watier.echechess.engine.utils.FenGameParser;
import ca.watier.utils.EngineGameTest;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import static ca.watier.echechess.common.enums.CasePosition.*;
import static ca.watier.echechess.common.enums.SpecialGameRules.NO_PLAYER_TURN;

/**
 * Created by yannick on 5/8/2017.
 */
public class KnightMovesTest extends EngineGameTest {

    @Test
    public void moveTest() throws FenParserException {
        FenPositionGameHandler gameHandler = FenGameParser.parse("8/8/8/8/4N3/8/8/8 w KQkq");
        gameHandler.addSpecialRule(NO_PLAYER_TURN);

        Assertions.assertThat(gameHandler.getAllAvailableMoves(E4, Side.WHITE)).containsOnly(C3, C5, D6, F6, G5, G3, D2, F2);
    }

    @Test
    public void attackTest() throws FenParserException {
        FenPositionGameHandler gameHandler = FenGameParser.parse("8/8/3p1k2/2p3p1/4N3/2K3p1/3p1p2/8 w KQkq");
        gameHandler.addSpecialRule(NO_PLAYER_TURN);

        Assertions.assertThat(gameHandler.getAllAvailableMoves(E4, Side.WHITE)).containsOnly(C5, D6, G5, G3, D2, F2);
    }

}
