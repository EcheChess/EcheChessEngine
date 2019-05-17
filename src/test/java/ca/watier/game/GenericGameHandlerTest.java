package ca.watier.game;

import ca.watier.echechess.common.sessions.Player;
import ca.watier.echechess.engine.engines.GenericGameHandler;
import ca.watier.echechess.engine.handlers.GamePropertiesHandlerImpl;
import ca.watier.echechess.engine.handlers.KingHandlerImpl;
import ca.watier.echechess.engine.handlers.PlayerHandlerImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.UUID;

import static ca.watier.echechess.common.enums.Side.*;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class GenericGameHandlerTest {

    private GenericGameHandler genericGameHandler;
    private Player playerOne, playerTwo;

    @Spy
    private PlayerHandlerImpl playerHandler;
    @Spy
    private KingHandlerImpl kingHandler;
    @Spy
    private GamePropertiesHandlerImpl gamePropertiesHandler;

    @Before
    public void setUp() {
        playerOne = new Player(UUID.randomUUID().toString());
        playerTwo = new Player(UUID.randomUUID().toString());

        genericGameHandler = new GenericGameHandler(kingHandler, playerHandler, gamePropertiesHandler);
        genericGameHandler.setPlayerToSide(playerOne, WHITE);
        genericGameHandler.setPlayerToSide(playerTwo, BLACK);
    }

    @Test
    public void changePlayerSide() {
        //The player are already set
        assertThat(genericGameHandler.hasPlayer(playerOne)).isTrue(); //White
        assertThat(genericGameHandler.hasPlayer(playerTwo)).isTrue(); //Black

        assertThat(genericGameHandler.setPlayerToSide(playerOne, WHITE)).isFalse();
        assertThat(genericGameHandler.setPlayerToSide(playerOne, OBSERVER)).isTrue(); //The player white to observe
        assertThat(genericGameHandler.getPlayerSide(playerOne)).isEqualByComparingTo(OBSERVER);

        assertThat(genericGameHandler.setPlayerToSide(playerTwo, WHITE)).isTrue(); //Change the black to white
        assertThat(genericGameHandler.getPlayerSide(playerTwo)).isEqualByComparingTo(WHITE);
        assertThat(genericGameHandler.setPlayerToSide(playerOne, BLACK)).isTrue(); //Change the observer to black
        assertThat(genericGameHandler.getPlayerSide(playerOne)).isEqualByComparingTo(BLACK);
    }
}
