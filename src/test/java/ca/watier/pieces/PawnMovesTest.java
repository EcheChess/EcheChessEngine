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

import ca.watier.echechess.common.enums.KingStatus;
import ca.watier.echechess.common.enums.Side;
import ca.watier.echechess.common.responses.GameScoreResponse;
import ca.watier.echechess.engine.exceptions.FenParserException;
import ca.watier.echechess.engine.game.FenPositionGameHandler;
import ca.watier.echechess.engine.handlers.KingHandlerImpl;
import ca.watier.echechess.engine.handlers.PlayerHandlerImpl;
import ca.watier.echechess.engine.utils.FenGameParser;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import static ca.watier.echechess.common.enums.CasePosition.*;
import static ca.watier.echechess.common.enums.MoveType.*;
import static ca.watier.echechess.common.enums.Pieces.B_PAWN;
import static ca.watier.echechess.common.enums.Pieces.W_PAWN;
import static ca.watier.echechess.common.enums.Side.BLACK;
import static ca.watier.echechess.common.enums.Side.WHITE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Created by yannick on 5/8/2017.
 */
@RunWith(MockitoJUnitRunner.class)
public class PawnMovesTest {

    @Spy
    private PlayerHandlerImpl playerHandler;
    @Spy
    private KingHandlerImpl kingHandler;


    /**
     * In this test, the white king should not be checked by the pawn
     */
    @Test
    public void check_with_pawns_front_move_two_position_Test() throws FenParserException {
        FenPositionGameHandler gameHandler = FenGameParser.parse("1k6/1p6/8/1K6/8/8/8/8 w KQkq", kingHandler, playerHandler);

        assertThat(gameHandler.isKing(KingStatus.OK, WHITE)).isTrue();
    }


    /**
     * In this test, the white king should not be checked by the pawn
     */
    @Test
    public void check_with_pawns_front_move_one_position_Test() throws FenParserException {
        FenPositionGameHandler gameHandler = FenGameParser.parse("1k6/1p6/1K6/8/8/8/8/8 w KQkq", kingHandler, playerHandler);

        assertThat(gameHandler.isKing(KingStatus.OK, WHITE)).isTrue();
    }


    @Test
    public void moveTest() throws FenParserException {
        FenPositionGameHandler gameHandler = FenGameParser.parse("8/pp3p1p/7r/8/8/7R/PP3P1P/8 w KQkq", kingHandler, playerHandler);
        when(playerHandler.isPlayerTurn(any(Side.class))).thenReturn(true);

        //Cannot move (blocked in front)
        Assert.assertEquals(MOVE_NOT_ALLOWED, gameHandler.movePiece(H2, H4, WHITE)); // 2 cases
        Assert.assertEquals(MOVE_NOT_ALLOWED, gameHandler.movePiece(H2, H3, WHITE));
        Assert.assertEquals(MOVE_NOT_ALLOWED, gameHandler.movePiece(H7, H5, BLACK)); // 2 cases
        Assert.assertEquals(MOVE_NOT_ALLOWED, gameHandler.movePiece(H7, H6, BLACK));

        //Can move
        Assert.assertEquals(PAWN_HOP, gameHandler.movePiece(A2, A4, WHITE)); // 2 cases
        Assert.assertEquals(NORMAL_MOVE, gameHandler.movePiece(B2, B3, WHITE));
        Assert.assertEquals(PAWN_HOP, gameHandler.movePiece(A7, A5, BLACK)); // 2 cases
        Assert.assertEquals(NORMAL_MOVE, gameHandler.movePiece(B7, B6, BLACK));

        //Cannot move by 2 position (not on the starting position)
        Assert.assertEquals(MOVE_NOT_ALLOWED, gameHandler.movePiece(B3, B5, WHITE));
        Assert.assertEquals(MOVE_NOT_ALLOWED, gameHandler.movePiece(B6, B4, WHITE));

        //Can move by one position
        Assert.assertEquals(NORMAL_MOVE, gameHandler.movePiece(B3, B4, WHITE));
        Assert.assertEquals(NORMAL_MOVE, gameHandler.movePiece(B6, B5, BLACK));

        //cannot move diagonally (without attack)
        Assert.assertEquals(MOVE_NOT_ALLOWED, gameHandler.movePiece(F2, E3, WHITE));
        Assert.assertEquals(MOVE_NOT_ALLOWED, gameHandler.movePiece(F2, G3, WHITE));
        Assert.assertEquals(MOVE_NOT_ALLOWED, gameHandler.movePiece(F2, D4, WHITE)); // 2 cases
        Assert.assertEquals(MOVE_NOT_ALLOWED, gameHandler.movePiece(F2, H4, WHITE)); // 2 cases
        Assert.assertEquals(MOVE_NOT_ALLOWED, gameHandler.movePiece(F7, E6, BLACK));
        Assert.assertEquals(MOVE_NOT_ALLOWED, gameHandler.movePiece(F7, G6, BLACK));
        Assert.assertEquals(MOVE_NOT_ALLOWED, gameHandler.movePiece(F7, D5, WHITE)); // 2 cases
        Assert.assertEquals(MOVE_NOT_ALLOWED, gameHandler.movePiece(F7, H5, WHITE)); // 2 cases

        //Kill in all direction
        gameHandler = FenGameParser.parse("8/8/2p3p1/3P1P2/8/3p1p2/2P3P1/8 w KQkq", kingHandler, playerHandler);
        Assert.assertEquals(CAPTURE, gameHandler.movePiece(D5, C6, WHITE));
        Assert.assertEquals(CAPTURE, gameHandler.movePiece(D3, C2, BLACK));
        Assert.assertEquals(CAPTURE, gameHandler.movePiece(F5, G6, WHITE));
        Assert.assertEquals(CAPTURE, gameHandler.movePiece(F3, G2, BLACK));
    }

    @Test
    public void pawnHopBlocked() throws FenParserException {
        FenPositionGameHandler gameHandler = FenGameParser.parse("k7/6p1/6n1/8/8/6N1/6P1/K7 w", kingHandler, playerHandler);

        assertThat(gameHandler.getAllAvailableMoves(G2, WHITE)).isEmpty();
        assertThat(gameHandler.getAllAvailableMoves(G7, BLACK)).isEmpty();
    }

    @Test
    public void pawnHopAndNormalBlackSide() throws FenParserException {
        FenPositionGameHandler gameHandler = FenGameParser.parse("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq", kingHandler, playerHandler);

        assertThat(gameHandler.getAllAvailableMoves(H2, WHITE)).isNotEmpty().containsOnly(H4, H3);
        assertThat(gameHandler.getAllAvailableMoves(D2, WHITE)).isNotEmpty().containsOnly(D4, D3);
    }

    @Test
    public void pawnHopAndNormalWhiteSide() throws FenParserException {
        FenPositionGameHandler gameHandler = FenGameParser.parse("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq", kingHandler, playerHandler);

        assertThat(gameHandler.getAllAvailableMoves(H7, BLACK)).isNotEmpty().containsOnly(H5, H6);
        assertThat(gameHandler.getAllAvailableMoves(D7, BLACK)).isNotEmpty().containsOnly(D5, D6);
    }

    @Test
    public void pawnHopAndNormalWhiteSideBlackKingCheckMate1() throws FenParserException {
        FenPositionGameHandler gameHandler = FenGameParser.parse("8/8/1ppp4/1pkp4/1p6/1P2P3/3P4/4K3 w KQkq", kingHandler, playerHandler);
        when(playerHandler.isPlayerTurn(any(Side.class))).thenReturn(true);


        assertThat(gameHandler.isKing(KingStatus.OK, BLACK)).isTrue();
        gameHandler.movePiece(D2, D4, WHITE);
        assertThat(gameHandler.isCheckMate(BLACK)).isTrue();
    }

    @Test
    public void pawnHopAndCheck_enPassantAndOk() throws FenParserException {
        FenPositionGameHandler gameHandler = FenGameParser.parse("8/8/4R3/kp6/p1p4R/8/1P6/RR5K w KQkq", kingHandler, playerHandler);

        assertThat(gameHandler.isKing(KingStatus.OK, BLACK)).isTrue();
        gameHandler.movePiece(B2, B4, WHITE);

        //Checked because of the pawn, can be killed with the en passant move (A4 & C4)
        assertThat(gameHandler.isCheck(BLACK)).isTrue();
    }


    @Test
    public void enPassantBlackSide() throws FenParserException {
        FenPositionGameHandler gameHandler = FenGameParser.parse("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq", kingHandler, playerHandler);
        when(playerHandler.isPlayerTurn(any(Side.class))).thenReturn(true);
        gameHandler.movePiece(H2, H4, WHITE);
        gameHandler.movePiece(H4, H5, WHITE);
        gameHandler.movePiece(G7, G5, BLACK); //Move by 2

        Assert.assertEquals(EN_PASSANT, gameHandler.movePiece(H5, G6, WHITE)); // En passant on the black pawn
        Assert.assertEquals(W_PAWN, gameHandler.getPiece(G6));
        Assert.assertEquals(new GameScoreResponse((short) 1, (short) 0), gameHandler.getGameScore());
    }

    @Test
    public void enPassantWhiteSide() throws FenParserException {
        FenPositionGameHandler gameHandler = FenGameParser.parse("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq", kingHandler, playerHandler);
        when(playerHandler.isPlayerTurn(any(Side.class))).thenReturn(true);
        gameHandler.movePiece(G7, G5, BLACK);
        gameHandler.movePiece(G5, G4, BLACK);
        gameHandler.movePiece(H2, H4, WHITE);

        Assert.assertEquals(EN_PASSANT, gameHandler.movePiece(G4, H3, BLACK)); // En passant on the white pawn
        Assert.assertEquals(B_PAWN, gameHandler.getPiece(H3));
        Assert.assertEquals(new GameScoreResponse((short) 0, (short) 1), gameHandler.getGameScore());
    }
}
