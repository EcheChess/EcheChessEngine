package ca.watier.game;

import ca.watier.echechessengine.contexts.StandardGameHandlerContext;
import ca.watier.echesscommon.sessions.Player;
import ca.watier.utils.EngineGameTest;
import org.junit.Before;
import org.junit.Test;

import static ca.watier.echesscommon.enums.Side.OBSERVER;
import static org.assertj.core.api.Assertions.assertThat;

public class GenericGameHandlerTest extends EngineGameTest {

    private StandardGameHandlerContext context;
    private Player playerOne, playerTwo;

    @Before
    public void setUp() {
        context = new StandardGameHandlerContext(CONSTRAINT_SERVICE, WEB_SOCKET_SERVICE);
        playerOne = context.getPlayerWhite();
        playerTwo = context.getPlayerBlack();
    }

    @Test
    public void changePlayerSide() {
        //The player are already set
        assertThat(context.hasPlayer(playerOne)).isTrue(); //White
        assertThat(context.hasPlayer(playerTwo)).isTrue(); //Black

        assertThat(context.setPlayerToSide(playerOne, WHITE)).isFalse();
        assertThat(context.setPlayerToSide(playerOne, OBSERVER)).isTrue(); //The player white to observe
        assertThat(context.getPlayerSide(playerOne)).isEqualByComparingTo(OBSERVER);

        assertThat(context.setPlayerToSide(playerTwo, WHITE)).isTrue(); //Change the black to white
        assertThat(context.getPlayerSide(playerTwo)).isEqualByComparingTo(WHITE);
        assertThat(context.setPlayerToSide(playerOne, BLACK)).isTrue(); //Change the observer to black
        assertThat(context.getPlayerSide(playerOne)).isEqualByComparingTo(BLACK);
    }
}
