package ca.watier.game;

import ca.watier.echechess.common.sessions.Player;
import ca.watier.echechess.engine.game.SimpleCustomPositionGameHandler;
import ca.watier.utils.EngineGameTest;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static ca.watier.echechess.common.enums.Side.OBSERVER;
import static ca.watier.echechess.common.enums.SpecialGameRules.NO_PLAYER_TURN;
import static org.assertj.core.api.Assertions.assertThat;

public class GenericGameHandlerTest extends EngineGameTest {

    private SimpleCustomPositionGameHandler simpleCustomPositionGameHandler;
    private Player playerOne, playerTwo;

    @Before
    public void setUp() {
        playerOne = new Player(UUID.randomUUID().toString());
        playerTwo = new Player(UUID.randomUUID().toString());


        simpleCustomPositionGameHandler = new SimpleCustomPositionGameHandler(CONSTRAINT_SERVICE);
        simpleCustomPositionGameHandler.addSpecialRule(NO_PLAYER_TURN);

        simpleCustomPositionGameHandler.setPlayerToSide(playerOne, WHITE);
        simpleCustomPositionGameHandler.setPlayerToSide(playerTwo, BLACK);
    }

    @Test
    public void changePlayerSide() {
        //The player are already set
        assertThat(simpleCustomPositionGameHandler.hasPlayer(playerOne)).isTrue(); //White
        assertThat(simpleCustomPositionGameHandler.hasPlayer(playerTwo)).isTrue(); //Black

        assertThat(simpleCustomPositionGameHandler.setPlayerToSide(playerOne, WHITE)).isFalse();
        assertThat(simpleCustomPositionGameHandler.setPlayerToSide(playerOne, OBSERVER)).isTrue(); //The player white to observe
        assertThat(simpleCustomPositionGameHandler.getPlayerSide(playerOne)).isEqualByComparingTo(OBSERVER);

        assertThat(simpleCustomPositionGameHandler.setPlayerToSide(playerTwo, WHITE)).isTrue(); //Change the black to white
        assertThat(simpleCustomPositionGameHandler.getPlayerSide(playerTwo)).isEqualByComparingTo(WHITE);
        assertThat(simpleCustomPositionGameHandler.setPlayerToSide(playerOne, BLACK)).isTrue(); //Change the observer to black
        assertThat(simpleCustomPositionGameHandler.getPlayerSide(playerOne)).isEqualByComparingTo(BLACK);
    }
}
