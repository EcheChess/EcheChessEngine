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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static ca.watier.echechess.common.enums.CasePosition.*;
import static ca.watier.echechess.common.enums.Side.BLACK;
import static ca.watier.echechess.common.enums.Side.WHITE;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by yannick on 5/8/2017.
 */
@ExtendWith(MockitoExtension.class)
public class KnightMovesTest {

    @Test
    public void moveTest() throws FenParserException {
        FenPositionGameHandler gameHandler = FenGameParser.parse("K7/8/8/8/4N3/8/8/8 w KQkq");

        assertThat(gameHandler.getAllAvailableMoves(E4, Side.WHITE)).containsOnly(C3, C5, D6, F6, G5, G3, D2, F2);
    }

    @Test
    public void attackTest() throws FenParserException {
        FenPositionGameHandler gameHandler = FenGameParser.parse("8/8/3p1k2/2p3p1/4N3/2K3p1/3p1p2/8 w KQkq");

        assertThat(gameHandler.getAllAvailableMoves(E4, Side.WHITE)).containsOnly(C5, D6, G5, G3, D2, F2, F6);
    }

    //FIXME: TO MOCK
//    @Test
//    public void knight_Test() throws FenParserException {
//        FenPositionGameHandler gameHandler = FenGameParser.parse("8/8/5n2/3K4/8/5n2/8/8 w KQkq");
//        KingHandler kingHandler = gameHandler.getKingHandler();
//
//        assertThat(kingHandler.getPositionKingCanMove(WHITE, )).containsOnly(C6, D6, E6, C5, C4);
//        assertThat(gameHandler.isCheck(WHITE)).isTrue();
//    }


    @Test
    public void canJumpOverOtherPieces() throws FenParserException {
        FenPositionGameHandler gameHandler = FenGameParser.parse("8/8/8/3ppp2/3pNP2/3PPP2/8/8 w");

        assertThat(gameHandler.getAllAvailableMoves(E4, Side.WHITE)).containsOnly(F6, G5, G3, F2, D2, C3, C5, D6);
    }


    @Test
    public void cantKillKingButCheckTest() throws FenParserException {
        FenPositionGameHandler gameHandler = FenGameParser.parse("8/8/8/3k4/5N2/8/8/8 w");

        assertThat(gameHandler.getAllAvailableMoves(F4, WHITE)).containsExactlyInAnyOrder(E6, D3, E2, G2, H3, H5, G6, D5);
        assertThat(gameHandler.isCheck(BLACK)).isTrue();
        assertThat(gameHandler.isCheckMate(BLACK)).isFalse();
    }

    @Test
    public void cantKillKingButCheckMateTest() throws FenParserException {
        FenPositionGameHandler gameHandler = FenGameParser.parse("8/8/2pp4/2pkr3/2pppN2/8/8/8 w");

        assertThat(gameHandler.getAllAvailableMoves(F4, WHITE)).containsExactlyInAnyOrder(E6, D3, E2, G2, H3, H5, G6, D5);

        assertThat(gameHandler.isCheck(BLACK)).isFalse();
        assertThat(gameHandler.isCheckMate(BLACK)).isTrue();
    }

}
