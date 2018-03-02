package ca.watier.utils;

import ca.watier.echechessengine.game.GameConstraints;
import ca.watier.echechessengine.utils.PgnParser;
import ca.watier.echesscommon.impl.WebSocketServiceTestImpl;

import java.io.FileInputStream;
import java.io.IOException;

public class PgnGameLauncher extends AbstractPgnGame {

    private final PgnParser PGN_PARSER;
    private int nbOfGameRun = 1;

    public PgnGameLauncher() {
        PGN_PARSER = new PgnParser(new GameConstraints(), new WebSocketServiceTestImpl());
    }

    @Override
    protected void parseGame(String header, String game) {
        LOGGER.info("============================= Game ({}) =============================", nbOfGameRun);
        LOGGER.info(header);
        LOGGER.info(game);
        PGN_PARSER.parse(String.format(PGN_GAME_PATTERN, header, game));
        nbOfGameRun++;
    }

    @Override
    public void start() {
        try {
            startReading(
                    new FileInputStream(System.getenv("pgn_game_file_from")),
                    true,
                    true,
                    true,
                    false
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
