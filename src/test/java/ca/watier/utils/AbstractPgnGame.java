package ca.watier.utils;

import ca.watier.echesscommon.enums.PgnEndGameToken;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

public abstract class AbstractPgnGame {
    protected static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(AbstractPgnGame.class);
    protected static final String PGN_GAME_PATTERN = "%s\n\n%s\n\n";
    protected static final int SPACER_CHAR = 0x0A;

    protected final void startReading(InputStream inputStream, boolean removeGameComments, boolean removeUnknownDateToken, boolean removeChessSymbols, boolean onlyCheckmateGame) throws IOException {
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