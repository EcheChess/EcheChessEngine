package ca.watier.echechess.engine.utils;

import ca.watier.echechess.engine.exceptions.FenParserException;
import ca.watier.echechess.engine.factories.GameConstraintFactory;
import ca.watier.echechess.engine.game.FenPositionGameHandler;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class FenGameParser {
    //language=regexp
    private static final String VALID_FEN_PATTERN = "(?i)^(([rqkbnp]|[1-8]){1,8}/){7}(([rqkbnp]|[1-8]){1,8}) [w|b]( [kq]{1,4})*$";
    //language=regexp
    private static final String FEN_SECTION_SEPARATOR = " ((?i)[wb]) *";
    private static final Pattern FEN_SECTION_SEPARATOR_MATCHER = Pattern.compile(FEN_SECTION_SEPARATOR);

    private FenGameParser() {
    }

    public static FenPositionGameHandler parse(String fen) throws FenParserException {
        if (StringUtils.isBlank(fen) || !fen.matches(VALID_FEN_PATTERN)) {
            throw new FenParserException();
        }

        Matcher matcher = FEN_SECTION_SEPARATOR_MATCHER.matcher(fen);

        FenPositionGameHandler fenPositionGameHandler = new FenPositionGameHandler(GameConstraintFactory.getDefaultGameMoveDelegate());

        if (matcher.find()) {
            char sideToPlay = StringUtils.trim(matcher.group(0)).charAt(0);

            String[] sections = fen.split(FEN_SECTION_SEPARATOR);

            if (ArrayUtils.isEmpty(sections)) {
                throw new FenParserException();
            }

            boolean isWhiteQueenMoveAvail = false;
            boolean isWhiteKingMoveAvail = false;
            boolean isBlackQueenMoveAvail = false;
            boolean isBlackKingMoveAvail = false;

            if (sections.length == 2) { //Castling flags

                String rawEnding = StringUtils.trim(sections[1]);
                String[] endingSections = rawEnding.split(" ");

                if (ArrayUtils.isEmpty(endingSections)) {
                    throw new FenParserException();
                }

                Stack<String> endingSectionStack = new Stack<>();
                endingSectionStack.addAll(Arrays.asList(endingSections));
                String rawCastlingAvailableMoves = endingSectionStack.pop();

                if (StringUtils.isNotBlank(rawCastlingAvailableMoves)) {
                    for (byte availCastling : rawCastlingAvailableMoves.getBytes()) {
                        switch (availCastling) {
                            case 'K':
                                isWhiteKingMoveAvail = true;
                                break;
                            case 'Q':
                                isWhiteQueenMoveAvail = true;
                                break;
                            case 'k':
                                isBlackKingMoveAvail = true;
                                break;
                            case 'q':
                                isBlackQueenMoveAvail = true;
                                break;
                        }
                    }
                }
            }

            fenPositionGameHandler.init(
                    sideToPlay,
                    isWhiteQueenMoveAvail,
                    isWhiteKingMoveAvail,
                    isBlackQueenMoveAvail,
                    isBlackKingMoveAvail
            );

            String rawBoard = StringUtils.trim(sections[0]);
            fenPositionGameHandler.setPieces(rawBoard.split("/"));
        } else {
            throw new FenParserException();
        }


        return fenPositionGameHandler;
    }
}
