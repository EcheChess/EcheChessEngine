package ca.watier.utils;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;

public class PgnGameFileGenerator extends AbstractPgnGame {

    private final File TO;

    public PgnGameFileGenerator() {
        TO = new File(System.getenv("pgn_game_file_to"));
    }

    @Override
    protected void parseGame(String header, String game) {
        try {
            FileUtils.writeStringToFile(TO, String.format(PGN_GAME_PATTERN, header, game), Charset.forName("UTF-8"), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start() {
        try {
            startReading(new FileInputStream(System.getenv("pgn_game_file_from")), true, true, true, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
