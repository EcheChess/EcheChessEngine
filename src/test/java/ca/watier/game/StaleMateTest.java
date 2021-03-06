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

import ca.watier.echechess.common.enums.KingStatus;
import ca.watier.echechess.common.enums.Pieces;
import ca.watier.echechess.engine.delegates.PieceMoveConstraintDelegate;
import ca.watier.echechess.engine.engines.GenericGameHandler;
import ca.watier.echechess.engine.exceptions.FenParserException;
import ca.watier.echechess.engine.handlers.PlayerHandlerImpl;
import ca.watier.echechess.engine.interfaces.GameEventEvaluatorHandler;
import ca.watier.echechess.engine.utils.FenGameParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static ca.watier.echechess.common.enums.CasePosition.*;
import static ca.watier.echechess.common.enums.MoveType.MOVE_NOT_ALLOWED;
import static ca.watier.echechess.common.enums.Side.BLACK;
import static ca.watier.echechess.common.enums.Side.WHITE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by yannick on 6/20/2017.
 */
@ExtendWith(MockitoExtension.class)
public class StaleMateTest {

    private GenericGameHandler genericGameHandler;

    @Spy
    private PlayerHandlerImpl playerHandler;
    @Spy
    private PieceMoveConstraintDelegate pieceMoveConstraintDelegate;
    @Spy
    private GameEventEvaluatorHandler gameEventEvaluatorHandler;


    @Test
    public void movePieceRevertWhenCheck() throws FenParserException {
        genericGameHandler = FenGameParser.parse("4k3/2P1r3/8/8/8/8/2p1R3/4K3 w", pieceMoveConstraintDelegate, playerHandler, gameEventEvaluatorHandler);

        //Cannot move the rook, the king is check
        assertEquals(MOVE_NOT_ALLOWED, genericGameHandler.movePiece(E2, C2, WHITE));
        assertEquals(MOVE_NOT_ALLOWED, genericGameHandler.movePiece(E7, C7, WHITE));

        //Make sure that the attacked pawn was reverted (not deleted from the map)
        assertEquals(Pieces.B_PAWN, genericGameHandler.getPiece(C2));
        assertEquals(Pieces.W_PAWN, genericGameHandler.getPiece(C7));
    }


    @Test
    public void isCheckMateStaleMate() throws FenParserException {
        /*
            STALEMATE
        */
        genericGameHandler = FenGameParser.parse("8/2R1R3/1R6/3k4/1R6/8/8/7K b", pieceMoveConstraintDelegate, playerHandler, gameEventEvaluatorHandler);
        assertThat(genericGameHandler.isKing(KingStatus.STALEMATE, BLACK)).isTrue();
        assertThat(genericGameHandler.isKing(KingStatus.STALEMATE, WHITE)).isFalse();

        genericGameHandler = FenGameParser.parse("3k4/3P4/3K4/8/8/8/8/8 b", pieceMoveConstraintDelegate, playerHandler, gameEventEvaluatorHandler);
        assertThat(genericGameHandler.isKing(KingStatus.STALEMATE, BLACK)).isTrue();
        assertThat(genericGameHandler.isKing(KingStatus.STALEMATE, WHITE)).isFalse();

        genericGameHandler = FenGameParser.parse("R1pk4/3P4/3K4/8/8/8/8/8 b", pieceMoveConstraintDelegate, playerHandler, gameEventEvaluatorHandler);
        assertThat(genericGameHandler.isKing(KingStatus.STALEMATE, BLACK)).isTrue();
        assertThat(genericGameHandler.isKing(KingStatus.STALEMATE, WHITE)).isFalse();

        genericGameHandler = FenGameParser.parse("2PKP3/2PPP3/8/8/8/8/2ppp3/2pkp3 w", pieceMoveConstraintDelegate, playerHandler, gameEventEvaluatorHandler);
        assertThat(genericGameHandler.isKing(KingStatus.STALEMATE, BLACK)).isTrue();
        assertThat(genericGameHandler.isKing(KingStatus.STALEMATE, WHITE)).isTrue();

        genericGameHandler = FenGameParser.parse("7k/8/7K/6R1/8/8/8/8 b", pieceMoveConstraintDelegate, playerHandler, gameEventEvaluatorHandler);
        assertThat(genericGameHandler.isKing(KingStatus.STALEMATE, BLACK)).isTrue();

        /*
            Not STALEMATE
         */
        genericGameHandler = FenGameParser.parse("8/2R5/1R6/3k4/1R6/8/8/7K b", pieceMoveConstraintDelegate, playerHandler, gameEventEvaluatorHandler);
        assertThat(genericGameHandler.isKing(KingStatus.OK, BLACK)).isTrue();

        genericGameHandler = FenGameParser.parse("2PK4/2PPP3/8/8/8/8/2ppp3/2pk4 w", pieceMoveConstraintDelegate, playerHandler, gameEventEvaluatorHandler);
        assertThat(genericGameHandler.isKing(KingStatus.OK, WHITE)).isTrue();
        assertThat(genericGameHandler.isKing(KingStatus.OK, BLACK)).isTrue();

    }
}