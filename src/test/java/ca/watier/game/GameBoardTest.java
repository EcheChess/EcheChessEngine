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

package ca.watier.game;

import ca.watier.echechess.common.enums.Side;
import ca.watier.echechess.engine.game.SimpleCustomPositionGameHandler;
import ca.watier.echechess.engine.handlers.KingHandlerImpl;
import ca.watier.echechess.engine.handlers.PlayerHandlerImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import static ca.watier.echechess.common.enums.CasePosition.*;
import static ca.watier.echechess.common.enums.Side.BLACK;
import static ca.watier.echechess.common.enums.Side.WHITE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Created by yannick on 7/1/2017.
 */
@RunWith(MockitoJUnitRunner.class)
public class GameBoardTest {

    @Spy
    private PlayerHandlerImpl playerHandler;
    @Spy
    private KingHandlerImpl kingHandler;

    private SimpleCustomPositionGameHandler simpleCustomPositionGameHandler;

    @Before
    public void setUp() {
        when(playerHandler.isPlayerTurn(any(Side.class))).thenReturn(true);
        simpleCustomPositionGameHandler = new SimpleCustomPositionGameHandler(kingHandler, playerHandler);
    }

    @Test
    public void getBlackTurnNumber() {
        assertThat(simpleCustomPositionGameHandler.getBlackTurnNumber()).isZero();
        simpleCustomPositionGameHandler.movePiece(H7, H6, BLACK);
        assertThat(simpleCustomPositionGameHandler.getBlackTurnNumber()).isEqualTo(1);
        simpleCustomPositionGameHandler.movePiece(H6, H5, BLACK);
        simpleCustomPositionGameHandler.movePiece(H5, H4, BLACK);
        assertThat(simpleCustomPositionGameHandler.getBlackTurnNumber()).isEqualTo(3);
    }

    @Test
    public void getWhiteTurnNumber() {
        assertThat(simpleCustomPositionGameHandler.getWhiteTurnNumber()).isZero();
        simpleCustomPositionGameHandler.movePiece(H2, H3, WHITE);
        assertThat(simpleCustomPositionGameHandler.getWhiteTurnNumber()).isEqualTo(1);
        simpleCustomPositionGameHandler.movePiece(H3, H4, WHITE);
        simpleCustomPositionGameHandler.movePiece(H4, H5, WHITE);
        assertThat(simpleCustomPositionGameHandler.getWhiteTurnNumber()).isEqualTo(3);
    }

    @Test
    public void isPieceMoved() {
        assertFalse(simpleCustomPositionGameHandler.isPieceMoved(G1));

        simpleCustomPositionGameHandler.movePiece(G1, F3, WHITE);

        assertTrue(simpleCustomPositionGameHandler.isPieceMoved(F3));
        assertNull(simpleCustomPositionGameHandler.isPieceMoved(G1));
    }

    @Test
    public void isPawnUsedSpecialMove() {
        assertFalse(simpleCustomPositionGameHandler.isPawnUsedSpecialMove(H2));
        simpleCustomPositionGameHandler.movePiece(H2, H4, WHITE);
        assertTrue(simpleCustomPositionGameHandler.isPawnUsedSpecialMove(H4));

        assertFalse(simpleCustomPositionGameHandler.isPawnUsedSpecialMove(G2));
        simpleCustomPositionGameHandler.movePiece(G2, G3, WHITE);
        assertFalse(simpleCustomPositionGameHandler.isPawnUsedSpecialMove(G3));

    }

    @Test
    public void getDefaultPositions() {
        assertThat(simpleCustomPositionGameHandler.getDefaultPositions()).isEqualTo(simpleCustomPositionGameHandler.getPiecesLocation());
        simpleCustomPositionGameHandler.movePiece(G2, G3, WHITE);
        assertThat(simpleCustomPositionGameHandler.getDefaultPositions()).isNotEqualTo(simpleCustomPositionGameHandler.getPiecesLocation());
    }


    @Test
    public void getTurnNumberPiece() {
        assertThat(simpleCustomPositionGameHandler.getPieceTurn(G2)).isZero();
        simpleCustomPositionGameHandler.movePiece(G2, G3, WHITE);
        assertThat(simpleCustomPositionGameHandler.getPieceTurn(G3)).isZero();
        simpleCustomPositionGameHandler.movePiece(G3, G4, WHITE);
        assertThat(simpleCustomPositionGameHandler.getPieceTurn(G3)).isNull();
        assertThat(simpleCustomPositionGameHandler.getPieceTurn(G4)).isEqualTo(1);
    }


}