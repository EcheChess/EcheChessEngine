package ca.watier.utils;

import ca.watier.echechess.engine.exceptions.ChessException;
import ca.watier.echechess.engine.utils.PgnParser;

import java.io.FileInputStream;
import java.io.IOException;

public class PgnGameLauncherWithNumberOfGames extends AbstractPgnGame {

    private int numberOfGameToRun;
    private int currentNumberOfGame = 0;

    public PgnGameLauncherWithNumberOfGames(int numberOfGameToRun) {
        this.numberOfGameToRun = numberOfGameToRun;
    }

    @Override
    protected void parseGame(String header, String game) {
        try {
            new PgnParser(GAME_CONSTRAINTS, WEB_SOCKET_SERVICE).parse(String.format(PGN_GAME_PATTERN, header, game));
            currentNumberOfGame++;
            System.out.printf("%d of %d%n", currentNumberOfGame, numberOfGameToRun);
        } catch (ChessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start() {
        try {
            startReading(
                    new FileInputStream(System.getenv("pgn_game_file_from")),
                    true,
                    true,
                    true,
                    false,
                    true
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected boolean isEnded() {
        return numberOfGameToRun == currentNumberOfGame;
    }
}
