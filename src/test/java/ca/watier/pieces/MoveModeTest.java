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
import ca.watier.echechess.engine.interfaces.KingHandler;
import ca.watier.echechess.engine.utils.FenGameParser;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static ca.watier.echechess.common.enums.CasePosition.*;
import static ca.watier.echechess.common.enums.KingStatus.OK;
import static ca.watier.echechess.common.enums.Side.WHITE;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by yannick on 5/30/2017.
 */
@RunWith(MockitoJUnitRunner.class)
public class MoveModeTest {


    @Test
    public void rook_Test() throws FenParserException {
        FenPositionGameHandler gameHandler = FenGameParser.parse("r7/p7/8/K7/8/8/8/8 w KQkq");
        KingHandler kingHandler = gameHandler.getKingHandler();

        assertThat(kingHandler.getPositionKingCanMove(WHITE)).containsOnly(A4, A6, B5, B4);
        assertThat(gameHandler.isKing(OK, WHITE)).isTrue();

        gameHandler.removePieceFromBoard(A7); //remove the pawn blocking

        assertThat(kingHandler.getPositionKingCanMove(WHITE)).containsOnly(B6, B5, B4);
        assertThat(gameHandler.isCheck(WHITE)).isTrue();
    }


    @Test
    public void knight_Test() throws FenParserException {
        FenPositionGameHandler gameHandler = FenGameParser.parse("8/8/5n2/3K4/8/5n2/8/8 w KQkq");
        KingHandler kingHandler = gameHandler.getKingHandler();

        assertThat(kingHandler.getPositionKingCanMove(WHITE)).containsOnly(C6, D6, E6, C5, C4);
        assertThat(gameHandler.isCheck(WHITE)).isTrue();
    }


    @Test
    public void bishop_Test() throws FenParserException {
        FenPositionGameHandler gameHandler = FenGameParser.parse("8/1b6/2r5/3K4/8/8/8/8 w KQkq");
        KingHandler kingHandler = gameHandler.getKingHandler();

        assertThat(kingHandler.getPositionKingCanMove(WHITE)).containsOnly(D4, E4, E5);
        assertThat(gameHandler.isKing(OK, WHITE)).isTrue();

        gameHandler.removePieceFromBoard(C6); //remove the rook blocking

        assertThat(kingHandler.getPositionKingCanMove(WHITE)).containsOnly(D6, E6, E5, D4, C4, C5);
        assertThat(gameHandler.isCheck(WHITE)).isTrue();
    }


    @Test
    public void king_Test() throws FenParserException {
        FenPositionGameHandler gameHandler = FenGameParser.parse("8/8/2k5/3K4/8/8/8/8 w KQkq");
        KingHandler kingHandler = gameHandler.getKingHandler();

        assertThat(kingHandler.getPositionKingCanMove(WHITE)).containsOnly(C4, D4, E4, E5, E6);
        assertThat(gameHandler.isCheck(WHITE)).isTrue();
    }


    @Test
    public void queen_Test() throws FenParserException {
        FenPositionGameHandler gameHandler = FenGameParser.parse("8/1q6/2P5/3K4/8/8/8/8 w KQkq");
        KingHandler kingHandler = gameHandler.getKingHandler();

        assertThat(kingHandler.getPositionKingCanMove(WHITE)).containsOnly(D6, E6, C5, C4, D4, E4, E5);
        assertThat(gameHandler.isKing(OK, WHITE)).isTrue();

        gameHandler.removePieceFromBoard(C6); //remove the pawn blocking

        assertThat(kingHandler.getPositionKingCanMove(WHITE)).containsOnly(D6, E6, C5, C4, D4, E5);
        assertThat(gameHandler.isCheck(WHITE)).isTrue();
    }

    @Test
    public void pawn_front_Test() throws FenParserException {
        FenPositionGameHandler gameHandler = FenGameParser.parse("8/p7/p7/K7/8/8/8/8 w KQkq");
        KingHandler kingHandler = gameHandler.getKingHandler();

        assertThat(kingHandler.getPositionKingCanMove(WHITE)).containsOnly(A4, A6, B4);
        assertThat(gameHandler.isKing(OK, WHITE)).isTrue();

        gameHandler.removePieceFromBoard(A6); //remove the pawn blocking

        assertThat(kingHandler.getPositionKingCanMove(WHITE)).containsOnly(A4, A6, B4, B5);
        assertThat(gameHandler.isKing(OK, WHITE)).isTrue();
    }

    @Test
    public void pawn_diagonal_Test() throws FenParserException {
        FenPositionGameHandler gameHandler = FenGameParser.parse("8/1p6/2K5/8/8/8/8/8 w KQkq");
        KingHandler kingHandler = gameHandler.getKingHandler();

        assertThat(kingHandler.getPositionKingCanMove(WHITE)).containsOnly(B7, C5, C7, D7, B6, D6, B5, D5);
        assertThat(gameHandler.isCheck(WHITE)).isTrue();
    }
}
