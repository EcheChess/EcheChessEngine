package ca.watier.echechess.engine.handlers;

import ca.watier.echechess.common.enums.Side;
import ca.watier.echechess.common.sessions.Player;
import ca.watier.echechess.common.utils.ObjectUtils;
import ca.watier.echechess.engine.interfaces.PlayerHandler;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

import static ca.watier.echechess.common.enums.Side.*;

public class PlayerHandlerImpl implements PlayerHandler {

    @Serial
    private static final long serialVersionUID = -1267409003065720156L;

    private final List<Player> observerList = new ArrayList<>();
    private Player playerWhite;
    private Player playerBlack;

    @Override
    public void removePlayerFromWhite(Player player) {
        if (player != null && player.equals(playerWhite)) {
            playerWhite = null;
        }
    }

    @Override
    public boolean changePlayerToBlack(Player player) {
        if (playerBlack == null) {
            playerBlack = player;
            return true;
        }

        return false;
    }

    @Override
    public void removePlayerFromBlack(Player player) {
        if (playerBlack == player) {
            playerBlack = null;
        }
    }

    @Override
    public boolean changePlayerToWhite(Player player) {
        if (playerWhite == null) {
            playerWhite = player;
            return true;
        }

        return false;
    }

    @Override
    public boolean setPlayerToSide(Player player, Side side) {
        if (ObjectUtils.hasNull(player, side)) {
            return false;
        }

        boolean value;

        switch (side) {
            case BLACK -> {
                removePlayerFromWhite(player);
                value = changePlayerToBlack(player);
                observerList.remove(player);
            }
            case WHITE -> {
                removePlayerFromBlack(player);
                value = changePlayerToWhite(player);
                observerList.remove(player);
            }
            default -> {
                removePlayerFromWhite(player);
                removePlayerFromBlack(player);
                observerList.add(player);
                value = true;
            }
        }

        return value;
    }

    @Override
    public Side getPlayerSide(Player player) {
        if (player == null) {
            return null;
        }

        if (player.equals(playerWhite)) {
            return WHITE;
        } else if (player.equals(playerBlack)) {
            return BLACK;
        } else if (observerList.contains(player)) {
            return OBSERVER;
        } else {
            return null;
        }
    }

    @Override
    public boolean hasPlayer(Player player) {
        if (player == null) {
            return false;
        }

        return observerList.contains(player) || player.equals(playerBlack) || player.equals(playerWhite);
    }

    @Override
    public Player getPlayerWhite() {
        return playerWhite;
    }

    @Override
    public Player getPlayerBlack() {
        return playerBlack;
    }

    @Override
    public List<Player> getObserverList() {
        return observerList;
    }
}
