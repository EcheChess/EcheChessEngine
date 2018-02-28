package ca.watier.utils;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public class PgnGameExtractor {

    private static final String PGN_GAME_PATTERN = "%s\n\n%s\n\n";
    private static final int SPACER_CHAR = 0x0A;

    public static void main(String[] args) {
        String from = System.getenv("pgn_game_file_from");
        String to = System.getenv("pgn_game_file_to");

        PgnGameExtractor.generatePgnFileWithCheckMate(from, to, true, true, true);
    }

    private static void generatePgnFileWithCheckMate(String from, String to, boolean removeGameComments, boolean removeUnknownDateToken, boolean removeChessSymbols) {
        try {
            File toAsFile = new File(to);
            InputStream inputStream = new FileInputStream(from);
            StringBuilder currentSectionBuilder = new StringBuilder();

            boolean isHeader = true;
            int i;
            char current, last = '\0';
            String header = null, game;
            while ((i = inputStream.read()) != -1) {
                current = (char) i;
                currentSectionBuilder.append(current);

                if (current == SPACER_CHAR && last == SPACER_CHAR) {

                    if (!isHeader) { //Write the header and the game to the file
                        game = currentSectionBuilder.toString().trim();

                        game = game.replaceAll("\\d*\\.\\.\\.",""); //remove black player move marker, if present

                        if (removeGameComments) {
                            game = game.replaceAll(" \\{.*?} ", " ");
                        }

                        if(removeUnknownDateToken) {
                            game = game.replaceAll("\\?", "");
                        }

                        if(removeChessSymbols) {
                            game = game.replaceAll("!", "");
                            game = game.replaceAll("\\?", "");
                        }

                        if(game.contains("#")) { //Checkmate
                            appendToFile(toAsFile, header, game);
                        }
                    } else {
                        header = currentSectionBuilder.toString().trim();
                    }

                    currentSectionBuilder.setLength(0);
                    isHeader = !isHeader;
                }

                last = current;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void appendToFile(File to, String header, String game) throws IOException {
        FileUtils.writeStringToFile(to, String.format(PGN_GAME_PATTERN, header, game), Charset.forName("UTF-8"), true);
    }
}
