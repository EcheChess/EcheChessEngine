package ca.watier.echechess.engine.utils;

import ca.watier.echechess.engine.delegates.PieceMoveConstraintDelegate;
import ca.watier.echechess.engine.exceptions.FenParserException;
import ca.watier.echechess.engine.game.FenPositionGameHandler;
import ca.watier.echechess.engine.handlers.GameEventEvaluatorHandlerImpl;
import ca.watier.echechess.engine.handlers.PlayerHandlerImpl;
import ca.watier.echechess.engine.interfaces.GameEventEvaluatorHandler;
import ca.watier.echechess.engine.interfaces.PlayerHandler;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
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

    public static FenPositionGameHandler parse(String fen, PieceMoveConstraintDelegate pieceDelegate, PlayerHandler playerHandler, GameEventEvaluatorHandler gameEventEvaluatorHandler) throws FenParserException {
        FenPositionGameHandler fenPositionGameHandler = new FenPositionGameHandler(pieceDelegate, playerHandler, gameEventEvaluatorHandler);
        return parse(fen, fenPositionGameHandler);
    }

    public static FenPositionGameHandler parse(String fen) throws FenParserException {
        PieceMoveConstraintDelegate defaultGameMoveDelegate = new PieceMoveConstraintDelegate();
        PlayerHandler playerHandler = new PlayerHandlerImpl();
        GameEventEvaluatorHandler gameEventEvaluatorHandler = new GameEventEvaluatorHandlerImpl();

        FenPositionGameHandler fenPositionGameHandler = new FenPositionGameHandler(defaultGameMoveDelegate, playerHandler, gameEventEvaluatorHandler);
        return parse(fen, fenPositionGameHandler);
    }


    private static FenPositionGameHandler parse(String fen, FenPositionGameHandler fenPositionGameHandler) throws FenParserException {
        if (StringUtils.isBlank(fen) || !fen.matches(VALID_FEN_PATTERN)) {
            throw new FenParserException();
        }

        Matcher matcher = FEN_SECTION_SEPARATOR_MATCHER.matcher(fen);

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

                Deque<String> endingSectionStack = new ArrayDeque<>(Arrays.asList(endingSections));
                String rawCastlingAvailableMoves = endingSectionStack.pop();

                if (StringUtils.isNotBlank(rawCastlingAvailableMoves)) {
                    for (byte availCastling : rawCastlingAvailableMoves.getBytes()) {
                        switch (availCastling) {
                            case 'K' -> isWhiteKingMoveAvail = true;
                            case 'Q' -> isWhiteQueenMoveAvail = true;
                            case 'k' -> isBlackKingMoveAvail = true;
                            case 'q' -> isBlackQueenMoveAvail = true;
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
