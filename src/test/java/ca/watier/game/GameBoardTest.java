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
import ca.watier.echechess.engine.abstracts.GameBoardData;
import ca.watier.echechess.engine.delegates.PieceMoveConstraintDelegate;
import ca.watier.echechess.engine.engines.GenericGameHandler;
import ca.watier.echechess.engine.handlers.PlayerHandlerImpl;
import ca.watier.echechess.engine.interfaces.GameEventEvaluatorHandler;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import static ca.watier.echechess.common.enums.CasePosition.*;
import static ca.watier.echechess.common.enums.Side.BLACK;
import static ca.watier.echechess.common.enums.Side.WHITE;
import static org.assertj.core.api.Assertions.assertThat;
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
    private PieceMoveConstraintDelegate pieceMoveConstraintDelegate;
    @Spy
    private GameEventEvaluatorHandler gameEventEvaluatorHandler;


    @InjectMocks
    private GenericGameHandler genericHandler;

    @Before
    public void setUp() {
        genericHandler = new GenericGameHandler(pieceMoveConstraintDelegate, playerHandler, gameEventEvaluatorHandler);
        when(gameEventEvaluatorHandler.isPlayerTurn(any(Side.class), any(GameBoardData.class))).thenReturn(true);
    }

    @Test
    public void getBlackTurnNumber() {
        assertThat(genericHandler.getCloneOfCurrentDataState().getBlackTurnNumber()).isZero();
        genericHandler.movePiece(H7, H6, BLACK);
        assertThat(genericHandler.getCloneOfCurrentDataState().getBlackTurnNumber()).isEqualTo(1);
        genericHandler.movePiece(H6, H5, BLACK);
        genericHandler.movePiece(H5, H4, BLACK);
        assertThat(genericHandler.getCloneOfCurrentDataState().getBlackTurnNumber()).isEqualTo(3);
    }

    @Test
    public void getWhiteTurnNumber() {
        assertThat(genericHandler.getCloneOfCurrentDataState().getWhiteTurnNumber()).isZero();
        genericHandler.movePiece(H2, H3, WHITE);
        assertThat(genericHandler.getCloneOfCurrentDataState().getWhiteTurnNumber()).isEqualTo(1);
        genericHandler.movePiece(H3, H4, WHITE);
        genericHandler.movePiece(H4, H5, WHITE);
        assertThat(genericHandler.getCloneOfCurrentDataState().getWhiteTurnNumber()).isEqualTo(3);
    }

    @Test
    public void getTurnNumberPiece() {
        assertThat(genericHandler.getCloneOfCurrentDataState().getPieceTurn(G2)).isZero();
        genericHandler.movePiece(G2, G3, WHITE);
        assertThat(genericHandler.getCloneOfCurrentDataState().getPieceTurn(G3)).isZero();
        genericHandler.movePiece(G3, G4, WHITE);
        assertThat(genericHandler.getCloneOfCurrentDataState().getPieceTurn(G3)).isNull();
        assertThat(genericHandler.getCloneOfCurrentDataState().getPieceTurn(G4)).isEqualTo(1);
    }
}