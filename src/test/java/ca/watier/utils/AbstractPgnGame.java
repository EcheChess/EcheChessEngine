package ca.watier.utils;

import ca.watier.echechessengine.game.GameConstraints;
import ca.watier.echesscommon.enums.PgnEndGameToken;
import ca.watier.echesscommon.impl.WebSocketServiceTestImpl;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

public abstract class AbstractPgnGame {
    protected static final GameConstraints GAME_CONSTRAINTS = new GameConstraints();
    protected static final WebSocketServiceTestImpl WEB_SOCKET_SERVICE = new WebSocketServiceTestImpl();
    protected static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(AbstractPgnGame.class);
    protected static final String PGN_GAME_PATTERN = "%s\n\n%s\n\n";
    protected static final int SPACER_CHAR = 0x0A;


    protected final void startReading(final InputStream inputStream, final boolean removeGameComments, final boolean removeUnknownDateToken, final boolean removeChessSymbols, final boolean onlyCheckmateGame) {
        new Thread(() -> {
            try {
                StringBuilder currentSectionBuilder = new StringBuilder();

                boolean isHeader = true;
                int i;
                char current, last = '\0';
                String header = null, game;
                boolean isNotAbandonnedGame;
                while ((i = inputStream.read()) != -1) {
                    current = (char) i;
                    currentSectionBuilder.append(current);

                    if (current == SPACER_CHAR && last == SPACER_CHAR) {

                        if (!isHeader) { //Write the header and the game to the file
                            game = replaceInvalidCharacters(removeGameComments, removeUnknownDateToken, removeChessSymbols, currentSectionBuilder.toString());

                            isNotAbandonnedGame = PgnEndGameToken.UNKNOWN.equals(PgnEndGameToken.getEndGameTokenByAction(game));

                            if (isNotAbandonnedGame && (!onlyCheckmateGame || game.contains("#"))) { //Checkmate
                                parseGame(header, game);
                            }
                        } else {
                            header = currentSectionBuilder.toString().trim();
                        }

                        currentSectionBuilder.setLength(0);
                        isHeader = !isHeader;
                    }

                    last = current;
                }
            } catch (IOException io) {
                LOGGER.error(io.getMessage(), io);
            }
        }).start();
    }

    private String replaceInvalidCharacters(boolean removeGameComments, boolean removeUnknownDateToken, boolean removeChessSymbols, String game) {
        game = game.replaceAll("\\d*\\.\\.\\.", ""); //remove black player move marker, if present

        if (removeGameComments) {
            game = game.replaceAll(" \\{.*?} ", " ");
        }

        if (removeUnknownDateToken) {
            game = game.replaceAll("\\?", "");
        }

        if (removeChessSymbols) {
            game = game.replaceAll("!", "");
            game = game.replaceAll("\\?", "");
        }

        return game.trim();
    }

    protected abstract void parseGame(String header, String game);

    public abstract void start();
}
