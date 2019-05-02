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
import ca.watier.echechess.common.enums.SpecialGameRules;
import ca.watier.echechess.engine.game.SimpleCustomPositionGameHandler;
import ca.watier.utils.EngineGameTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static ca.watier.echechess.common.enums.CasePosition.*;
import static ca.watier.echechess.common.enums.MoveType.MOVE_NOT_ALLOWED;
import static ca.watier.echechess.engine.game.SimpleCustomPositionGameHandler.THE_NUMBER_OF_PARAMETER_IS_INCORRECT;
import static junit.framework.TestCase.fail;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * Created by yannick on 6/20/2017.
 */
public class SimpleCustomPositionGameHandlerTest extends EngineGameTest {

    private static final Class<UnsupportedOperationException> UNSUPPORTED_OPERATION_EXCEPTION_CLASS = UnsupportedOperationException.class;
    private SimpleCustomPositionGameHandler customPieceGameHandler;

    @Before
    public void setUp() {
        customPieceGameHandler = new SimpleCustomPositionGameHandler(CONSTRAINT_SERVICE);
    }

    @Test
    public void setPieces() {
        assertThatExceptionOfType(UNSUPPORTED_OPERATION_EXCEPTION_CLASS).isThrownBy(() ->
                customPieceGameHandler.setPieces("B1")).withMessage(THE_NUMBER_OF_PARAMETER_IS_INCORRECT);

        assertThatExceptionOfType(UNSUPPORTED_OPERATION_EXCEPTION_CLASS).isThrownBy(() ->
                customPieceGameHandler.setPieces("B1:W_KING;B2:")).withMessage(THE_NUMBER_OF_PARAMETER_IS_INCORRECT);

        try {
            customPieceGameHandler.setPieces("B1:W_KING;B8:B_KING");
        } catch (UnsupportedOperationException ex) {
            fail();
        }
    }

    @Test
    public void movePieceRevertWhenCheck() {
        customPieceGameHandler.setPieces("E1:W_KING;E8:B_KING;E7:B_ROOK;E2:W_ROOK;C2:B_PAWN;C7:W_PAWN");

        //Cannot move the rook, the king is check
        Assert.assertEquals(MOVE_NOT_ALLOWED, customPieceGameHandler.movePiece(E2, C2, WHITE));
        Assert.assertEquals(MOVE_NOT_ALLOWED, customPieceGameHandler.movePiece(E7, C7, WHITE));

        //Make sure that the attacked pawn was reverted (not deleted from the map)
        Assert.assertEquals(Pieces.B_PAWN, customPieceGameHandler.getPiece(C2));
        Assert.assertEquals(Pieces.W_PAWN, customPieceGameHandler.getPiece(C7));
    }


    @Test
    public void isCheckMateStaleMate() {
        customPieceGameHandler.addSpecialRule(SpecialGameRules.NO_PLAYER_TURN);

        assertThat(customPieceGameHandler.isKing(KingStatus.OK, WHITE)).isTrue();
        assertThat(customPieceGameHandler.isKing(KingStatus.OK, BLACK)).isTrue();

        /*
            STALEMATE
        */
        customPieceGameHandler.setPieces("H1:W_KING;D5:B_KING;C7:W_ROOK;E7:W_ROOK;B6:W_ROOK;B4:W_ROOK");
        assertThat(customPieceGameHandler.isKing(KingStatus.STALEMATE, BLACK)).isTrue();

        customPieceGameHandler.setPieces("D8:B_KING;D7:W_PAWN;D6:W_KING");
        assertThat(customPieceGameHandler.isKing(KingStatus.STALEMATE, BLACK)).isTrue();

        customPieceGameHandler.setPieces("D8:B_KING;D6:W_KING;D7:W_PAWN;C8:B_PAWN;A8:W_ROOK");
        assertThat(customPieceGameHandler.isKing(KingStatus.STALEMATE, BLACK)).isTrue();

        customPieceGameHandler.setPieces("D1:B_KING;C1:B_PAWN;C2:B_PAWN;D2:B_PAWN;E2:B_PAWN;E1:B_PAWN;D8:W_KING;C8:W_PAWN;C7:W_PAWN;D7:W_PAWN;E7:W_PAWN;E8:W_PAWN");
        assertThat(customPieceGameHandler.isKing(KingStatus.STALEMATE, BLACK)).isTrue();
        assertThat(customPieceGameHandler.isKing(KingStatus.STALEMATE, WHITE)).isTrue();


        /*
            Not STALEMATE
         */
        customPieceGameHandler.setPieces("D8:B_KING;D6:W_KING;D7:W_PAWN;C8:B_PAWN;A8:W_ROOK;E8:B_PAWN");
        assertThat(customPieceGameHandler.isKing(KingStatus.OK, BLACK)).isTrue();

        customPieceGameHandler.setPieces("H1:W_KING;D5:B_KING;C7:W_ROOK;B6:W_ROOK;B4:W_ROOK");
        assertThat(customPieceGameHandler.isKing(KingStatus.OK, BLACK)).isTrue();

        customPieceGameHandler.setPieces("D8:B_KING;D6:W_KING;D7:W_PAWN;C8:B_PAWN");
        assertThat(customPieceGameHandler.isKing(KingStatus.OK, BLACK)).isTrue();

        customPieceGameHandler.setPieces("D1:B_KING;C1:B_PAWN;C2:B_PAWN;D2:B_PAWN;E2:B_PAWN;D8:W_KING;C8:W_PAWN;C7:W_PAWN;D7:W_PAWN;E7:W_PAWN");
        assertThat(customPieceGameHandler.isKing(KingStatus.OK, WHITE)).isTrue();
        assertThat(customPieceGameHandler.isKing(KingStatus.OK, BLACK)).isTrue();

    }

    @Test
    public void isCheckMateCheckmateWithEnPassant() {
        customPieceGameHandler.addSpecialRule(SpecialGameRules.NO_PLAYER_TURN);

        //k7/5p2/4r3/4r1PP/4rPKP/4rPPP/4rrrr/8 w - -
        customPieceGameHandler.setPieces(
                "A8:B_KING;" +
                        "G4:W_KING;" +
                        "F3:W_PAWN;" +
                        "G3:W_PAWN;" +
                        "H3:W_PAWN;" +
                        "F4:W_PAWN;" +
                        "H4:W_PAWN;" +
                        "G5:W_PAWN;" +
                        "H5:W_PAWN;" +
                        "F7:B_PAWN;" +
                        "E6:B_ROOK;" +
                        "E5:B_ROOK;" +
                        "E4:B_ROOK;" +
                        "E3:B_ROOK;" +
                        "E2:B_ROOK;" +
                        "F2:B_ROOK;" +
                        "G2:B_ROOK;" +
                        "H2:B_ROOK;"
        );

        assertThat(customPieceGameHandler.isKing(KingStatus.OK, WHITE)).isTrue();
        customPieceGameHandler.movePiece(F7, F5, BLACK); //Pawn hop
        assertThat(customPieceGameHandler.isKing(KingStatus.CHECK, WHITE)).isTrue(); //Check by the pawn (can kill it by "en passant")
    }
}