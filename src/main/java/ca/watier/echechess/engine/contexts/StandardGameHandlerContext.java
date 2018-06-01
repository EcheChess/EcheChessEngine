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

package ca.watier.echechess.engine.contexts;

import ca.watier.echechess.common.enums.CasePosition;
import ca.watier.echechess.common.enums.Pieces;
import ca.watier.echechess.common.interfaces.WebSocketService;
import ca.watier.echechess.common.sessions.Player;
import ca.watier.echechess.engine.game.CustomPieceWithStandardRulesHandler;
import ca.watier.echechess.engine.game.GameConstraints;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by yannick on 5/20/2017.
 */
public class StandardGameHandlerContext extends CustomPieceWithStandardRulesHandler {

    public StandardGameHandlerContext(GameConstraints gameConstraints, WebSocketService webSocketService) {
        super(gameConstraints, webSocketService);
        addBothPlayerToGameAndSetUUID();
    }

    private void addBothPlayerToGameAndSetUUID() {
        UUID uuid = UUID.randomUUID();
        setUuid(uuid.toString());
        playerBlack = new Player();
        playerBlack.addJoinedGame(uuid);
        playerWhite = new Player();
        playerWhite.addJoinedGame(uuid);
    }

    public StandardGameHandlerContext(@NotNull GameConstraints gameConstraints, @NotNull WebSocketService webSocketService, @NotNull Map<CasePosition, Pieces> positionPieces) {
        super(gameConstraints, webSocketService);
        assertThat(positionPieces).isNotEmpty();

        setPieces(positionPieces);
        addBothPlayerToGameAndSetUUID();
    }

    public StandardGameHandlerContext(GameConstraints gameConstraints, WebSocketService webSocketService, String positionPieces) {
        super(gameConstraints, webSocketService);
        assertThat(positionPieces).isNotEmpty();

        setPieces(positionPieces);
        addBothPlayerToGameAndSetUUID();
    }

    public void movePieceTo(@NotNull CasePosition from, @NotNull CasePosition to) {
        movePieceTo(from, to, getPiece(from));
    }
}
