package ca.watier.echechess.engine.interfaces;

import ca.watier.echechess.common.enums.Side;
import ca.watier.echechess.common.sessions.Player;

import java.util.List;

public interface PlayerHandler extends GenericHandler {
    void removePlayerFromWhite(Player player);
    boolean changePlayerToBlack(Player player);
    void removePlayerFromBlack(Player player);
    boolean changePlayerToWhite(Player player);
    boolean setPlayerToSide(Player player, Side side);

    /**
     * Get the side of the player, null if not available
     *
     * @param player
     * @return
     */
    Side getPlayerSide(Player player);
    boolean hasPlayer(Player player);
    Player getPlayerWhite();
    Player getPlayerBlack();
    List<Player> getObserverList();
    boolean isPlayerTurn(Side sideFrom);
}
