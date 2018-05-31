package ca.watier.echechess.engine.abstracts;

import ca.watier.echechess.engine.utils.GameUtils;
import ca.watier.echesscommon.enums.CasePosition;
import ca.watier.echesscommon.enums.Pieces;
import ca.watier.echesscommon.enums.Side;
import ca.watier.echesscommon.interfaces.BaseUtils;
import ca.watier.echesscommon.utils.Assert;
import ca.watier.echesscommon.utils.Pair;
import com.google.common.collect.ArrayListMultimap;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public abstract class GameBoardData {

    //The default position of the board
    private final Map<CasePosition, Pieces> DEFAULT_POSITIONS;
    //The pieces position on the board
    private Map<CasePosition, Pieces> positionPiecesMap;
    //Used to check if the piece have moved
    private Map<CasePosition, Boolean> isPiecesMovedMap;
    //Used to check if the pawn used it's special ability to move by two case
    private Map<CasePosition, Boolean> isPawnUsedSpecialMoveMap;
    //Used to track the turn that the piece have moved
    private Map<CasePosition, Integer> turnNumberPieceMap;
    //Used to track the pawn promotions
    private ArrayListMultimap<Side, Pair<CasePosition, CasePosition>> pawnPromotionMap;
    //Used to track the number of turn of each player
    private int blackTurnNumber;
    private int whiteTurnNumber;
    private int totalMove = 0;
    private boolean isGameDraw = false;
    private boolean isGamePaused = false;

    public GameBoardData() {
        pawnPromotionMap = ArrayListMultimap.create();
        DEFAULT_POSITIONS = GameUtils.getDefaultGame();
        positionPiecesMap = GameUtils.getDefaultGame();
        isPiecesMovedMap = GameUtils.initNewMovedPieceMap(positionPiecesMap);
        isPawnUsedSpecialMoveMap = GameUtils.initPawnMap(positionPiecesMap);
        turnNumberPieceMap = GameUtils.initTurnMap(positionPiecesMap);
    }

    protected @NotNull List<Pair<CasePosition, CasePosition>> getPawnPromotionBySide(@NotNull Side playerSide) {
        return BaseUtils.getSafeList(pawnPromotionMap.get(playerSide));
    }

    protected Pieces getPieceFromPosition(@NotNull CasePosition position) {
        return positionPiecesMap.get(position);
    }

    public final @NotNull Map<CasePosition, Pieces> getPiecesLocation() {
        return Collections.unmodifiableMap(positionPiecesMap);
    }

    public @NotNull Map<CasePosition, Pieces> getDefaultPositions() {
        return Collections.unmodifiableMap(DEFAULT_POSITIONS);
    }

    public int getBlackTurnNumber() {
        return blackTurnNumber;
    }

    public int getWhiteTurnNumber() {
        return whiteTurnNumber;
    }

    public int getNbTotalMove() {
        return totalMove;
    }

    @NotNull
    protected Map<CasePosition, Boolean> getIsPiecesMovedMap() {
        return Collections.unmodifiableMap(isPiecesMovedMap);
    }

    @NotNull
    protected Map<CasePosition, Boolean> getIsPawnUsedSpecialMoveMap() {
        return Collections.unmodifiableMap(isPawnUsedSpecialMoveMap);
    }

    @NotNull
    protected Map<CasePosition, Integer> getTurnNumberPieceMap() {
        return Collections.unmodifiableMap(turnNumberPieceMap);
    }

    /**
     * Get the turn number based on a {@link CasePosition}
     *
     * @param position
     * @return
     */
    public final Integer getPieceTurn(@NotNull CasePosition position) {
        return turnNumberPieceMap.get(position);
    }

    protected void addPawnPromotionToMap(@NotNull Side side, @NotNull Pair<CasePosition, CasePosition> casePositionCasePositionPair) {
        pawnPromotionMap.put(side, casePositionCasePositionPair);
    }

    public final void removePiece(@NotNull CasePosition from) {
        positionPiecesMap.remove(from);
    }

    protected void setPiecePositionWithoutMoveState(@NotNull Pieces piece, @NotNull CasePosition to) {
        positionPiecesMap.put(to, piece);
    }

    /**
     * If it's the default from of the piece, mark this one as moved
     *
     * @param piece
     * @param from
     * @param to
     */
    protected void changeMovedStateOfPiece(@NotNull Pieces piece, @NotNull CasePosition from, @NotNull CasePosition to) {

        boolean isValid = BaseUtils.getSafeBoolean(isPieceMoved(from)) || GameUtils.isDefaultPosition(from, piece, this);
        isPiecesMovedMap.put(to, isValid);
        isPiecesMovedMap.remove(from);
    }

    /**
     * Check if the piece is moved, return null if the position is invalid
     *
     * @param position
     * @return
     */
    public final Boolean isPieceMoved(@NotNull CasePosition position) {
        return isPiecesMovedMap.get(position);
    }


    /**
     * Return true if the pawn used the special move
     *
     * @param position
     * @return
     */
    public final boolean isPawnUsedSpecialMove(@NotNull CasePosition position) {
        return BaseUtils.getSafeBoolean(isPawnUsedSpecialMoveMap.get(position));
    }

    protected void addPawnUsedSpecialMove(@NotNull CasePosition to, boolean isValid) {
        isPawnUsedSpecialMoveMap.put(to, isValid);
    }

    protected void removePawnUsedSpecialMove(@NotNull CasePosition from) {
        isPawnUsedSpecialMoveMap.remove(from);
    }

    protected void incrementWhiteTurnNumber() {
        whiteTurnNumber++;
    }

    protected void incrementBlackTurnNumber() {
        blackTurnNumber++;
    }

    protected void changePieceTurnNumber(@NotNull CasePosition from, @NotNull CasePosition to) {
        turnNumberPieceMap.remove(from);
        turnNumberPieceMap.put(to, totalMove);
    }

    protected void incrementTotalMove() {
        totalMove++;
    }

    protected void setPiecesGameState(@NotNull Map<CasePosition, Boolean> isPawnUsedSpecialMoveMap,
                                      @NotNull Map<CasePosition, Integer> turnNumberPieceMap,
                                      @NotNull Map<CasePosition, Boolean> isPiecesMovedMap) {

        this.isPawnUsedSpecialMoveMap = isPawnUsedSpecialMoveMap;
        this.turnNumberPieceMap = turnNumberPieceMap;
        this.isPiecesMovedMap = isPiecesMovedMap;
    }

    /**
     * Remove a piece from the board
     *
     * @param from
     */
    public final void removePieceFromBoard(@NotNull CasePosition from) {
        positionPiecesMap.remove(from);
        isPiecesMovedMap.remove(from);
        isPawnUsedSpecialMoveMap.remove(from);
        turnNumberPieceMap.remove(from);
    }

    protected void removePawnPromotion(@NotNull Pair<CasePosition, CasePosition> pair, @NotNull Side side) {

        if (Side.OBSERVER.equals(side)) {
            return;
        }

        pawnPromotionMap.remove(side, pair);
    }

    protected final void setPositionPiecesMap(Map<CasePosition, Pieces> positionPiecesMap) {
        Assert.assertNotEmpty(positionPiecesMap);

        this.positionPiecesMap = positionPiecesMap;
        this.DEFAULT_POSITIONS.clear();
        this.DEFAULT_POSITIONS.putAll(positionPiecesMap);
        this.isPiecesMovedMap = GameUtils.initNewMovedPieceMap(positionPiecesMap);
        this.turnNumberPieceMap = GameUtils.initTurnMap(positionPiecesMap);
    }

    public void setGamePaused(boolean gamePaused) {
        isGamePaused = gamePaused;
    }

    public boolean isGameDraw() {
        return isGameDraw;
    }
}
