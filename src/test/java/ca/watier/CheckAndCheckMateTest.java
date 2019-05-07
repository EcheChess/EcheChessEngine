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

package ca.watier;


import ca.watier.echechess.common.enums.KingStatus;
import ca.watier.echechess.common.enums.Side;
import ca.watier.echechess.engine.exceptions.FenParserException;
import ca.watier.echechess.engine.game.FenPositionGameHandler;
import ca.watier.echechess.engine.handlers.GamePropertiesHandlerImpl;
import ca.watier.echechess.engine.handlers.KingHandlerImpl;
import ca.watier.echechess.engine.handlers.PlayerHandlerImpl;
import ca.watier.echechess.engine.interfaces.KingHandler;
import ca.watier.echechess.engine.utils.FenGameParser;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import static ca.watier.echechess.common.enums.CasePosition.*;
import static ca.watier.echechess.common.enums.KingStatus.*;
import static ca.watier.echechess.common.enums.Side.BLACK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;


/**
 * Created by yannick on 5/9/2017.
 */
@RunWith(MockitoJUnitRunner.class)
public class CheckAndCheckMateTest {

    private static final Side WHITE = Side.WHITE;

    @Spy
    private PlayerHandlerImpl playerHandler;
    @Spy
    private KingHandlerImpl kingHandler;
    @Spy
    private GamePropertiesHandlerImpl gamePropertiesHandler;


    /**
     * In this test, the king should be movable only to E5, F5 & F3
     */
    @Test
    public void checkFromMixShortAndLongRangeWithPawn_multipleExitTest() throws FenParserException {
        FenPositionGameHandler gameHandler = FenGameParser.parse("8/8/8/1q1ppp2/3pKp2/3ppp2/8/8 w", kingHandler, playerHandler, gamePropertiesHandler);
        KingHandler kingHandler = gameHandler.getKingHandler();

        Assertions.assertThat(gameHandler.isCheck(WHITE)).isTrue();
        assertThat(kingHandler.getPositionKingCanMove(WHITE)).containsOnly(E5, F3, F5);
    }


    /**
     * In this test, the king should be movable only to E5
     */
    @Test
    public void checkFromMixShortAndLongRangeWithPawn_oneExitTest() throws FenParserException {
        FenPositionGameHandler gameHandler = FenGameParser.parse("7k/8/8/1q1ppp1q/3pKp2/3ppp2/8/8 w", kingHandler, playerHandler,gamePropertiesHandler);
        KingHandler kingHandler = gameHandler.getKingHandler();

        Assertions.assertThat(gameHandler.isCheck(WHITE)).isTrue();
        assertThat(kingHandler.getPositionKingCanMove(WHITE)).containsOnly(E5);
    }


    /**
     * In this test, the king is checkmate <br>
     * The king is blocked by it's own pawn, and a rook can hit the king <br>
     * This test make sure that the king is not blocking their field of view
     */
    @Test
    public void checkmateFromLongRange_horizontal_Test() throws FenParserException {
        FenPositionGameHandler gameHandler = FenGameParser.parse("7k/8/8/8/8/8/3PPP2/4K2r w", kingHandler, playerHandler,gamePropertiesHandler);
        Assertions.assertThat(gameHandler.isCheckMate(WHITE)).isTrue();

        FenPositionGameHandler gameHandler2 = FenGameParser.parse("7k/8/8/8/8/8/3PPP2/r3K3 w", kingHandler, playerHandler,gamePropertiesHandler);
        Assertions.assertThat(gameHandler2.isCheckMate(WHITE)).isTrue();
    }


    /**
     * In this test, the king is checkmate <br>
     * The king is blocked by it's own pawn, and a rook can hit the king <br>
     * This test make sure that the king is not blocking their field of view
     */
    @Test
    public void checkmateFromLongRange_vertical_Test() throws FenParserException {
        FenPositionGameHandler gameHandler = FenGameParser.parse("r6k/8/8/1P6/KP6/1P6/8/8 w", kingHandler, playerHandler,gamePropertiesHandler);
        Assertions.assertThat(gameHandler.isCheckMate(WHITE)).isTrue();

        FenPositionGameHandler gameHandler2 = FenGameParser.parse("7k/8/8/1P6/KP6/1P6/8/r7 w", kingHandler, playerHandler,gamePropertiesHandler);
        Assertions.assertThat(gameHandler2.isCheckMate(WHITE)).isTrue();
    }

    @Test
    public void longRangeBlocked_Test() throws FenParserException {
        FenPositionGameHandler gameHandler = FenGameParser.parse("b5k1/7b/8/3PPP2/r2pKP1r/3ppp2/8/b3r2b w", kingHandler, playerHandler,gamePropertiesHandler);
        Assertions.assertThat(gameHandler.isKing(OK, WHITE)).isTrue();
    }

    @Test
    public void checkBlackKingPattern_check_Test() {
        String[] patterns = {
                "8/8/8/3k4/2P1P3/8/8/8 w",
                "8/8/1N6/3k4/8/8/8/8 w",
                "8/1B6/8/3k4/8/8/8/8 w",
                "8/3R4/8/3k4/8/8/8/8 w",
                "8/3Q4/8/3k4/8/8/8/8 w",
                "8/8/8/3k4/2K5/8/8/8 w"
        };

        try {
            assertPattern(patterns, CHECK, BLACK);
        } catch (FenParserException e) {
            fail(e.getMessage());
        }
    }

    private void assertPattern(String[] patterns, KingStatus status, Side side) throws FenParserException {
        for (String pattern : patterns) {
            FenPositionGameHandler gameHandler = FenGameParser.parse(pattern);

            assertThat(gameHandler.isKing(status, side)).isTrue().withFailMessage("The pattern '%s' has failed !", pattern);
        }
    }

    @Test
    public void checkmateBlackKingPattern_checkmate_Test() throws FenParserException {
        //Thanks to https://en.wikipedia.org/wiki/Checkmate_pattern for the patterns !
        String[] patterns = {
                "8/4N1pk/8/7R/8/8/8/8 w", //Anastasia's Mate
                "6kR/6P1/5K2/8/8/8/8/8 w", //Anderssen's Mate
                "7k/7R/5N2/8/8/8/8/8 w", //Arabian Mate
                "3R2k1/5ppp/8/8/8/8/8/8 w", //Back Rank Mate
                "7k/5BKN/8/8/8/8/8/8 w", //Bishop and knight mate
                "5rk1/7B/8/6N1/8/8/1B6/8 w", //Blackburne's mate
                "5rk1/6RR/8/8/8/8/8/8 w", //Blind swine mate
                "2kr4/3p4/B7/8/5B2/8/8/8 w", //Boden's mate
                "R2k4/8/3K4/8/8/8/8/8 w", //Box mate (Rook mate)
                "7k/5N1p/8/8/8/8/8/6R1 w", //Corner mate
                "8/8/8/8/6p1/5qk1/7Q/6K1 w", //Cozio's mate
                "5k2/5Q2/6B1/8/8/8/8/8 w", //Damiano's bishop mate
                "5rk1/6pQ/6P1/8/8/8/8/8 w", //Damiano's mate
                "8/8/8/7R/pkp5/Pn6/1P6/7K w", //David and Goliath mate
                "7k/7p/8/3B4/8/2B5/8/8 w", //Double bishop mate
                "3rkr2/1p6/2p1Q3/8/8/1P6/P1P5/1K6 w", //Epaulette mate
                "7k/6p1/8/7Q/2B5/8/8/8 w", //Greco's mate
                "6kR/5p2/8/8/8/8/1B6/8 w", //h-file mate
                "4R3/4kp2/5N2/4P3/8/8/8/8 w", //Hook mate
                "4r3/6P1/R5K1/k5P1/2Q5/8/3q4/6q1 w", //Kill Box mate
                "7k/8/4BB1K/8/8/8/8/8 w", //King and two bishops mate
                "7k/8/5NNK/8/8/8/8/8 w", //King and two knights mate
                "3q1b2/4kB2/3p4/3NN3/8/8/8/8 w", //Légal mate
                "6k1/5pQ1/5Pp1/8/8/8/8/8 w", //Lolli's mate
                "6Q1/5Bpk/7p/8/8/8/8/8 w", //Max Lange's mate
                "7k/5p1p/5B2/8/8/8/8/6R1 w", //Morphy's mate
                "3Rk3/5p2/8/6B1/8/8/8/8 w", //Opera mate
                "3k4/3Q4/3K4/8/8/8/8/8 w", //Queen mate
                "1nbB4/1pk5/2p5/8/8/8/8/3R3K w Q", //Réti's mate
                "6rk/5Npp/8/8/8/8/8/1K6 w", //Smothered mate
                "5rk1/4Np1p/8/8/8/2B5/8/8 w", //Suffocation mate
                "3r1r2/4k3/R3Q3/8/8/8/8/8 w" //Swallow's tail mate
        };

        assertPattern(patterns, CHECKMATE, BLACK);
    }
}
