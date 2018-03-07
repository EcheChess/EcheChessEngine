package ca.watier.echechessengine.abstracts;

import ca.watier.echechessengine.utils.GameUtils;
import ca.watier.echesscommon.enums.CasePosition;
import ca.watier.echesscommon.enums.Pieces;
import ca.watier.echesscommon.enums.Side;
import ca.watier.echesscommon.interfaces.BaseUtils;
import ca.watier.echesscommon.utils.Assert;
import ca.watier.echesscommon.utils.MultiArrayMap;
import ca.watier.echesscommon.utils.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public abstract class GameBoardData {

    //The default position of the board
    private final Map<CasePosition, Pieces> defaultPositions;
    //The pieces position on the board
    private Map<CasePosition, Pieces> positionPiecesMap;
    //Used to check if the piece have moved
    private Map<CasePosition, Boolean> isPiecesMovedMap;
    //Used to check if the pawn used it's special ability to move by two case
    private Map<CasePosition, Boolean> isPawnUsedSpecialMoveMap;
    //Used to track the turn that the piece have moved
    private Map<CasePosition, Integer> turnNumberPieceMap;
    //Used to track the pawn promotions
    private MultiArrayMap<Side, Pair<CasePosition, CasePosition>> pawnPromotionMap;
    //Used to track the number of turn of each player
    private int blackTurnNumber;
    private int whiteTurnNumber;
    private int totalMove = 0;

    private boolean isGameDraw = false;
    private boolean isGamePaused = false;

    public GameBoardData() {
        defaultPositions = new EnumMap<>(CasePosition.class);
        positionPiecesMap = GameUtils.getDefaultGame();
        defaultPositions.putAll(positionPiecesMap);
        isPiecesMovedMap = GameUtils.initNewMovedPieceMap(positionPiecesMap);
        isPawnUsedSpecialMoveMap = GameUtils.initPawnMap(positionPiecesMap);
        turnNumberPieceMap = GameUtils.initTurnMap(positionPiecesMap);
        pawnPromotionMap = new MultiArrayMap<>();
    }

    protected @NotNull List<Pair<CasePosition, CasePosition>> getPawnPromotionBySide(@NotNull Side playerSide) {
        return BaseUtils.getSafeList(pawnPromotionMap.get(playerSide));
    }

    protected Pieces getPieceFromPosition(@NotNull CasePosition position) {
        return positionPiecesMap.get(position);
    }

    protected void addPawnPromotionToMap(@NotNull Side side, @NotNull Pair<CasePosition, CasePosition> casePositionCasePositionPair) {
        pawnPromotionMap.put(side, casePositionCasePositionPair);
    }

    protected void addPieceToBoard(@NotNull CasePosition to, @NotNull Pieces piece) {
        positionPiecesMap.put(to, piece);
    }

    public final @NotNull Map<CasePosition, Pieces> getPiecesLocation() {
        return Collections.unmodifiableMap(positionPiecesMap);
    }

    public @NotNull Map<CasePosition, Pieces> getDefaultPositions() {
        return Collections.unmodifiableMap(defaultPositions);
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

    public @NotNull Map<CasePosition, Boolean> getIsPiecesMovedMap() {
        return Collections.unmodifiableMap(isPiecesMovedMap);
    }

    public void setIsPiecesMovedMap(@NotNull Map<CasePosition, Boolean> isPiecesMovedMap) {
        this.isPiecesMovedMap = isPiecesMovedMap;
    }

    public @NotNull Map<CasePosition, Boolean> getIsPawnUsedSpecialMoveMap() {
        return Collections.unmodifiableMap(isPawnUsedSpecialMoveMap);
    }

    public void setIsPawnUsedSpecialMoveMap(@NotNull Map<CasePosition, Boolean> isPawnUsedSpecialMoveMap) {
        this.isPawnUsedSpecialMoveMap = isPawnUsedSpecialMoveMap;
    }

    public @NotNull Map<CasePosition, Integer> getTurnNumberPieceMap() {
        return Collections.unmodifiableMap(turnNumberPieceMap);
    }

    public void setTurnNumberPieceMap(@NotNull Map<CasePosition, Integer> turnNumberPieceMap) {
        this.turnNumberPieceMap = turnNumberPieceMap;
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

    public final void removePiece(@NotNull CasePosition from) {
        positionPiecesMap.remove(from);
    }

    protected void setIsGamePaused(boolean isGamePaused) {
        this.isGamePaused = isGamePaused;
    }

    protected void changePieceTurnNumber(@NotNull CasePosition from, @NotNull CasePosition to) {
        turnNumberPieceMap.remove(from);
        turnNumberPieceMap.put(to, totalMove);
    }

    protected void incrementWhiteTurnNumber() {
        whiteTurnNumber++;
    }


    protected void incrementBlackTurnNumber() {
        blackTurnNumber++;
    }

    protected Boolean isPanwUsedSpecialMove(@NotNull CasePosition from) {
        return isPawnUsedSpecialMoveMap.get(from);
    }

    protected void addPawnUsedSpecialMove(@NotNull CasePosition to, boolean isValid) {
        isPawnUsedSpecialMoveMap.put(to, isValid);
    }

    protected void removePawnUsedSpecialMove(@NotNull CasePosition from) {
        isPawnUsedSpecialMoveMap.remove(from);
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

    protected void removePawnPromotion(@NotNull Pair<CasePosition, CasePosition> pair, @NotNull Side side) {

        if (Side.OBSERVER.equals(side)) {
            return;
        }

        pawnPromotionMap.removeFromList(side, pair);
    }

    public final void setPositionPiecesMap(Map<CasePosition, Pieces> positionPiecesMap) {
        Assert.assertNotEmpty(positionPiecesMap);

        this.positionPiecesMap = positionPiecesMap;
        this.defaultPositions.clear();
        this.defaultPositions.putAll(positionPiecesMap);
        this.isPiecesMovedMap = GameUtils.initNewMovedPieceMap(positionPiecesMap);
        this.turnNumberPieceMap = GameUtils.initTurnMap(positionPiecesMap);
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

    /**
     * Get the turn number based on a {@link CasePosition}
     *
     * @param position
     * @return
     */
    public final Integer getPieceTurn(@NotNull CasePosition position) {
        return turnNumberPieceMap.get(position);
    }

    protected void incrementTotalMove() {
        totalMove++;
    }

    public boolean isGamePaused() {
        return isGamePaused;
    }

    public void setGamePaused(boolean gamePaused) {
        isGamePaused = gamePaused;
    }

    public boolean isGameDraw() {
        return isGameDraw;
    }
}
