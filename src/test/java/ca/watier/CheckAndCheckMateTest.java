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
import ca.watier.echechess.common.impl.WebSocketServiceTestImpl;
import ca.watier.echechess.common.interfaces.WebSocketService;
import ca.watier.echechess.engine.contexts.StandardGameHandlerContext;
import ca.watier.echechess.engine.game.GameConstraints;
import org.junit.Test;

import static ca.watier.echechess.common.enums.CasePosition.*;
import static ca.watier.echechess.common.enums.KingStatus.*;
import static ca.watier.echechess.common.enums.Side.BLACK;
import static ca.watier.echechess.common.enums.SpecialGameRules.NO_PLAYER_TURN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;


/**
 * Created by yannick on 5/9/2017.
 */
public class CheckAndCheckMateTest {

    private static final Side WHITE = Side.WHITE;
    private static final GameConstraints CONSTRAINT_SERVICE = new GameConstraints();
    private static final WebSocketService WEB_SOCKET_SERVICE = new WebSocketServiceTestImpl();

    /**
     * In this test, the king should be movable only to E5, F5 & F3
     */
    @Test
    public void checkFromMixShortAndLongRangeWithPawn_multipleExitTest() {
        String positionPieces = "H8:B_KING;E4:W_KING;B5:B_QUEEN;D5:B_PAWN;D4:B_PAWN;D3:B_PAWN;E5:B_PAWN;E3:B_PAWN;F3:B_PAWN;F4:B_PAWN;F5:B_PAWN";
        StandardGameHandlerContext gameHandler = new StandardGameHandlerContext(CONSTRAINT_SERVICE, WEB_SOCKET_SERVICE, positionPieces);
        gameHandler.addSpecialRule(NO_PLAYER_TURN);

        assertEquals(CHECK, gameHandler.getKingStatus(WHITE, true));
        assertThat(gameHandler.getPositionKingCanMove(WHITE)).containsOnly(E5, F3, F5);
    }


    /**
     * In this test, the king should be movable only to E5
     */
    @Test
    public void checkFromMixShortAndLongRangeWithPawn_oneExitTest() {
        String positionPieces = "H8:B_KING;E4:W_KING;B5:B_QUEEN;H5:B_QUEEN;E3:B_PAWN;E5:B_PAWN;D4:B_PAWN;F4:B_PAWN;D5:B_PAWN;F5:B_PAWN;D3:B_PAWN;F3:B_PAWN";
        StandardGameHandlerContext gameHandler = new StandardGameHandlerContext(CONSTRAINT_SERVICE, WEB_SOCKET_SERVICE, positionPieces);
        gameHandler.addSpecialRule(NO_PLAYER_TURN);
        assertEquals(CHECK, gameHandler.getKingStatus(WHITE, true));
        assertThat(gameHandler.getPositionKingCanMove(WHITE)).containsOnly(E5);
    }


    /**
     * In this test, the king is checkmate <br>
     * The king is blocked by it's own pawn, and a rook can hit the king <br>
     * This test make sure that the king is not blocking their field of view
     */
    @Test
    public void checkmateFromLongRange_horizontal_Test() {
        String positionPieces = "H8:B_KING;E1:W_KING;H1:B_ROOK;D2:W_PAWN;E2:W_PAWN;F2:W_PAWN";

        StandardGameHandlerContext context = new StandardGameHandlerContext(CONSTRAINT_SERVICE, WEB_SOCKET_SERVICE, positionPieces);
        context.addSpecialRule(NO_PLAYER_TURN);

        assertEquals(CHECKMATE, context.getKingStatus(WHITE, true));
        context.movePieceTo(H1, A1);
        assertEquals(CHECKMATE, context.getKingStatus(WHITE, true));
    }


    /**
     * In this test, the king is checkmate <br>
     * The king is blocked by it's own pawn, and a rook can hit the king <br>
     * This test make sure that the king is not blocking their field of view
     */
    @Test
    public void checkmateFromLongRange_vertical_Test() {
        String positionPieces = "H8:B_KING;A4:W_KING;A8:B_ROOK;B3:W_PAWN;B4:W_PAWN;B5:W_PAWN";

        StandardGameHandlerContext context = new StandardGameHandlerContext(CONSTRAINT_SERVICE, WEB_SOCKET_SERVICE, positionPieces);
        context.addSpecialRule(NO_PLAYER_TURN);

        assertEquals(CHECKMATE, context.getKingStatus(WHITE, true));
        context.movePieceTo(A8, A1); //Move the rook
        assertEquals(CHECKMATE, context.getKingStatus(WHITE, true));
    }

    @Test
    public void longRangeBlocked_Test() {
        String positionPieces = "G8:B_KING;E4:W_KING;D5:W_PAWN;E5:W_PAWN;F5:W_PAWN;D4:B_PAWN;F4:W_PAWN;D3:B_PAWN;E3:B_PAWN;F3:B_PAWN;E1:B_ROOK;A4:B_ROOK;H4:B_ROOK;A8:B_BISHOP;A1:B_BISHOP;H1:B_BISHOP;H7:B_BISHOP";
        StandardGameHandlerContext context = new StandardGameHandlerContext(CONSTRAINT_SERVICE, WEB_SOCKET_SERVICE, positionPieces);
        context.addSpecialRule(NO_PLAYER_TURN);

        assertEquals(OK, context.getKingStatus(WHITE, true));
    }

    @Test
    public void checkBlackKingPattern_check_Test() {

        StandardGameHandlerContext context = new StandardGameHandlerContext(CONSTRAINT_SERVICE, WEB_SOCKET_SERVICE);
        context.addSpecialRule(NO_PLAYER_TURN);

        String[] patterns = new String[]{
                "D5:B_KING;C4:W_PAWN;E4:W_PAWN",
                "D5:B_KING;B6:W_KNIGHT",
                "D5:B_KING;B7:W_BISHOP",
                "D5:B_KING;D7:W_ROOK",
                "D5:B_KING;D7:W_QUEEN",
                "D5:B_KING;C4:W_KING"
        };

        assertPattern(context, patterns, CHECK, BLACK);
    }

    private void assertPattern(StandardGameHandlerContext context, String[] patterns, KingStatus status, Side side) {
        for (String pattern : patterns) {
            context.setPieces(pattern);
            assertEquals(String.format("The pattern '%s' has failed !", pattern), status, context.getKingStatus(side, true));
        }
    }

    @Test
    public void checkmateBlackKingPattern_checkmate_Test() {

        StandardGameHandlerContext context = new StandardGameHandlerContext(CONSTRAINT_SERVICE, WEB_SOCKET_SERVICE);
        context.addSpecialRule(NO_PLAYER_TURN);

        //Thanks to www.serverchess.com/checkmate.htm and https://en.wikipedia.org/wiki/Checkmate_pattern for the patterns !
        String[] patterns = new String[]{
                "H8:B_KING;G7:B_PAWN;E7:W_KNIGHT;H4:W_ROOK;G2:W_PAWN;G1:W_KING", //Anastasia's Mate
                "H8:W_ROOK;G8:B_KING;G7:W_PAWN;F6:W_PAWN;F7:B_PAWN;B6:B_PAWN;G1:W_KING", //Anderssen's Mate
                "H8:B_KING;H7:W_ROOK;F6:W_KNIGHT;G1:W_KING;A7:B_PAWN", //Arabian Mate
                "B8:W_ROOK;A7:B_ROOK;A5:B_PAWN;F8:B_KING;F7:B_PAWN;F6:W_PAWN;G6:B_PAWN;H7:B_PAWN;G3:W_PAWN;H2:W_PAWN;G1:W_KING", //Back Rank Mate
                "F8:B_QUEEN;G8:B_KING;H7:W_BISHOP;G5:W_KNIGHT;E5:W_BISHOP;G1:W_KING", //Blackburne's Mate
                "B8:B_BISHOP;C8:B_KING;D8:B_ROOK;D7:B_KNIGHT;C6:B_PAWN;A6:W_BISHOP;A3:W_PAWN;B2:W_PAWN;C3:W_PAWN;D4:W_PAWN;D1:W_ROOK;G1:W_KNIGHT;H1:W_KING;H2:W_BISHOP;H5:B_QUEEN;H7:B_ROOK;G8:B_KNIGHT", //Boden's Mate
                "F8:B_ROOK;G8:B_KING;G7:B_PAWN;G6:W_PAWN;G1:W_KING;H7:W_QUEEN", //Damiano's Mate
                "H8:B_KING;H7:W_QUEEN;G6:W_BISHOP;G1:W_KING", //Damiano's bishop Mate
                "A7:B_PAWN;D7:W_ROOK;B6:B_PAWN;C6:B_KING;B5:W_PAWN;D5:B_PAWN;A4:W_PAWN;D4:W_PAWN;G4:W_BISHOP;G5:B_PAWN;G1:W_KING", //David & Goliath Mate
                "E8:W_QUEEN;C7:W_KNIGHT;F7:B_KING;G7:B_BISHOP;F6:B_QUEEN;G1:W_KING", //Dovetail Mate
                "G8:B_KING;F8:B_ROOK;H8:B_ROOK;G6:W_QUEEN;G2:W_PAWN;G1:W_KING", //Epaulette Mate
                "A8:B_ROOK;B8:B_KNIGHT;C8:B_BISHOP;D8:B_QUEEN;E8:B_KING;F8:B_BISHOP;G8:B_KNIGHT;H8:B_ROOK;A1:W_ROOK;B1:W_KNIGHT;C1:W_BISHOP;E1:W_KING;F1:W_BISHOP;G1:W_KNIGHT;H1:W_ROOK;H2:W_PAWN;G2:W_PAWN;F2:W_PAWN;C2:W_PAWN;B2:W_PAWN;A2:W_PAWN;D4:W_PAWN;E4:W_PAWN;G5:B_PAWN;F6:B_PAWN;E7:B_PAWN;H7:B_PAWN;D7:B_PAWN;C7:B_PAWN;B7:B_PAWN;A7:B_PAWN;H5:W_QUEEN", //Fool's Mate
                "H8:B_KING;G7:B_PAWN;D5:W_BISHOP;H3:W_ROOK;G2:W_PAWN;G1:W_KING", //Greco's Mate
                "D7:B_BISHOP;F7:B_PAWN;E6:B_KING;E5:W_QUEEN;B2:W_BISHOP;G2:W_PAWN;G1:W_KING", //Gueridon Mate
                "F8:W_ROOK;F7:B_KING;G7:B_PAWN;G6:W_KNIGHT;F5:W_PAWN;H1:W_KING", //Hook Mate
                "A8:B_ROOK;B8:B_KNIGHT;A7:B_PAWN;B7:B_PAWN;C7:B_PAWN;D8:B_QUEEN;E7:B_KING;F7:W_BISHOP;F8:B_BISHOP;G8:B_KNIGHT;H8:B_ROOK;H7:B_PAWN;G6:B_PAWN;D6:B_PAWN;D5:W_KNIGHT;E5:W_KNIGHT;H2:W_PAWN;G2:W_PAWN;F2:W_PAWN;D2:W_PAWN;C2:W_PAWN;B2:W_PAWN;A2:W_PAWN;A1:W_ROOK;C1:W_BISHOP;D1:B_BISHOP;E1:W_KING;H1:W_ROOK", //Legal's Mate
                "G8:B_KING;G7:W_QUEEN;F7:B_PAWN;G6:B_PAWN;F6:W_PAWN;G2:W_PAWN;G1:W_KING", //Lolli's Mate
                "G8:W_QUEEN;G7:B_PAWN;H7:B_KING;H6:B_PAWN;F7:W_BISHOP;G1:W_KING", //Max Lange Mate
                "F8:B_KING;E8:B_ROOK;F7:B_PAWN;F6:W_BISHOP;G6:B_PAWN;H7:W_KNIGHT;H6:W_KNIGHT;G1:W_KING", //Minor Piece Mate
                "H8:B_KING;H7:B_PAWN;D4:W_BISHOP;G1:W_ROOK;H2:W_KING", //Morphy's Mate
                "D8:W_ROOK;E8:B_KING;F7:B_PAWN;G5:W_BISHOP;G1:W_KING", //Opera Mate
                "F8:B_ROOK;G8:B_KING;F7:B_PAWN;H7:B_PAWN;G1:W_ROOK;H1:W_KING;C3:W_BISHOP", //Pillsbury's Mate
                "B8:B_ROOK;C8:B_BISHOP;D8:W_BISHOP;B7:B_PAWN;C6:B_PAWN;C7:B_KING;A2:W_PAWN;B2:W_PAWN;C1:W_KING;D1:W_ROOK", //Reti's Mate
                "A8:B_ROOK;A7:B_PAWN;B7:B_PAWN;C7:B_PAWN;D7:B_PAWN;C8:B_BISHOP;D8:B_QUEEN;E8:B_KING;F8:B_BISHOP;F7:W_QUEEN;G7:B_PAWN;H7:B_PAWN;H8:B_ROOK;F6:B_KNIGHT;C6:B_KNIGHT;E5:B_PAWN;E4:W_PAWN;C4:W_BISHOP;H1:W_ROOK;H2:W_PAWN;G2:W_PAWN;F2:W_PAWN;G1:W_KNIGHT;E1:W_KING;C1:W_BISHOP;B1:W_KNIGHT;A1:W_ROOK;A2:W_PAWN;B2:W_PAWN;C2:W_PAWN;D2:W_PAWN", //Scholar's Mate
                "G8:B_ROOK;H8:B_KING;H7:B_PAWN;G7:B_PAWN;F7:W_KNIGHT;G1:W_KING", //Smothered Mate
                "H8:B_KING;H6:W_KNIGHT;G6:W_KING;F6:W_BISHOP", //Bishop and knight mate
                "G8:B_KING;F8:B_ROOK;G7:W_ROOK;H7:W_ROOK", //Blind swine mate
                "A8:W_ROOK;D8:B_KING;D6:W_KING", //Box mate
                "H8:B_KING;H7:B_PAWN;F7:W_KNIGHT;G1:W_ROOK", //Corner mate
                "H2:W_QUEEN;G1:W_KING;G3:B_KING;F3:B_QUEEN;G4:B_PAWN", //Cozio's mate
                "H8:W_ROOK;G8:B_KING;F7:B_PAWN;B2:W_BISHOP", //h-file mate
                "E8:B_ROOK;G7:W_PAWN;G6:W_KING;G5:W_PAWN;A5:B_KING;A6:W_ROOK;C4:W_QUEEN;D2:B_QUEEN;G1:B_QUEEN", //Kill Box mate
                "H8:B_KING;H6:W_KING;E6:W_BISHOP;F6:W_BISHOP", //King and two bishops mate
                "H8:B_KING;H6:W_KING;G6:W_KNIGHT;F6:W_KNIGHT", //King and two knights mate
                "D8:B_KING;D7:W_QUEEN;D6:W_KING", //Queen mate
                "F8:B_ROOK;G8:B_KING;H7:B_PAWN;F7:B_PAWN;E7:W_KNIGHT;C3:W_BISHOP", // Suffocation mate
                "D4:B_KING;C4:B_PAWN;C5:B_PAWN;D5:B_PAWN;E5:B_PAWN;E4:B_PAWN;C3:W_PAWN;D3:W_PAWN;E3:W_PAWN;B2:W_PAWN;F2:W_PAWN;C2:W_PAWN;D2:W_PAWN;E2:W_PAWN", //Custom: Pawn
                "D4:B_KING;C4:B_PAWN;C5:B_PAWN;D5:B_PAWN;E5:B_PAWN;E4:B_PAWN;E2:W_KNIGHT;A2:W_KNIGHT;G2:W_KNIGHT;E1:W_KNIGHT", //Custom: Knight
                "D4:B_KING;C4:B_PAWN;C5:B_PAWN;D5:B_PAWN;E5:B_PAWN;E4:B_PAWN;A1:W_BISHOP;B1:W_BISHOP;G1:W_BISHOP", //Custom: Bishop
                "D4:B_KING;C4:B_PAWN;C5:B_PAWN;D5:B_PAWN;E5:B_PAWN;E4:B_PAWN;D1:W_ROOK;A3:W_ROOK", //Custom: Rook
                "D4:B_KING;C4:B_PAWN;C5:B_PAWN;D5:B_PAWN;E5:B_PAWN;E4:B_PAWN;D2:W_QUEEN", //Custom: Queen
                "D4:B_KING;C4:B_PAWN;C5:B_PAWN;D5:B_PAWN;E5:B_PAWN;E4:B_PAWN;D2:W_KING;C3:W_PAWN" //Custom: Pawn & King
        };

        assertPattern(context, patterns, CHECKMATE, BLACK);
    }
}
