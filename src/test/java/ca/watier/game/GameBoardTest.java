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

import ca.watier.echechess.common.enums.MoveType;
import ca.watier.echechess.common.enums.Side;
import ca.watier.echechess.engine.engines.GenericGameHandler;
import ca.watier.echechess.engine.handlers.GamePropertiesHandlerImpl;
import ca.watier.echechess.engine.handlers.KingHandlerImpl;
import ca.watier.echechess.engine.handlers.PlayerHandlerImpl;
import ca.watier.echechess.engine.interfaces.GameHandler;
import ca.watier.echechess.engine.interfaces.GenericHandler;
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
    @Spy
    private GamePropertiesHandlerImpl gamePropertiesHandler;

    private GenericGameHandler genericHandler;

    @Before
    public void setUp() {
        genericHandler = new GenericGameHandler(kingHandler, playerHandler, gamePropertiesHandler);
        when(playerHandler.isPlayerTurn(any(Side.class))).thenReturn(true);
    }

    @Test
    public void getBlackTurnNumber() {
        assertThat(genericHandler.getBlackTurnNumber()).isZero();
        genericHandler.movePiece(H7, H6, BLACK);
        assertThat(genericHandler.getBlackTurnNumber()).isEqualTo(1);
        genericHandler.movePiece(H6, H5, BLACK);
        genericHandler.movePiece(H5, H4, BLACK);
        assertThat(genericHandler.getBlackTurnNumber()).isEqualTo(3);
    }

    @Test
    public void getWhiteTurnNumber() {
        assertThat(genericHandler.getWhiteTurnNumber()).isZero();
        genericHandler.movePiece(H2, H3, WHITE);
        assertThat(genericHandler.getWhiteTurnNumber()).isEqualTo(1);
        genericHandler.movePiece(H3, H4, WHITE);
        genericHandler.movePiece(H4, H5, WHITE);
        assertThat(genericHandler.getWhiteTurnNumber()).isEqualTo(3);
    }

    @Test
    public void isPieceMoved() {
        assertFalse(genericHandler.isPieceMoved(G1));

        genericHandler.movePiece(G1, F3, WHITE);

        assertTrue(genericHandler.isPieceMoved(F3));
        assertNull(genericHandler.isPieceMoved(G1));
    }

    @Test
    public void isPawnUsedSpecialMove() {
        assertFalse(genericHandler.isPawnUsedSpecialMove(H2));
        genericHandler.movePiece(H2, H4, WHITE);
        assertTrue(genericHandler.isPawnUsedSpecialMove(H4));

        assertFalse(genericHandler.isPawnUsedSpecialMove(G2));
        genericHandler.movePiece(G2, G3, WHITE);
        assertFalse(genericHandler.isPawnUsedSpecialMove(G3));

    }

    @Test
    public void getDefaultPositions() {
        assertThat(genericHandler.getDefaultPositions()).isEqualTo(genericHandler.getPiecesLocation());
        genericHandler.movePiece(G2, G3, WHITE);
        assertThat(genericHandler.getDefaultPositions()).isNotEqualTo(genericHandler.getPiecesLocation());
    }


    @Test
    public void getTurnNumberPiece() {
        assertThat(genericHandler.getPieceTurn(G2)).isZero();
        genericHandler.movePiece(G2, G3, WHITE);
        assertThat(genericHandler.getPieceTurn(G3)).isZero();
        genericHandler.movePiece(G3, G4, WHITE);
        assertThat(genericHandler.getPieceTurn(G3)).isNull();
        assertThat(genericHandler.getPieceTurn(G4)).isEqualTo(1);
    }


}