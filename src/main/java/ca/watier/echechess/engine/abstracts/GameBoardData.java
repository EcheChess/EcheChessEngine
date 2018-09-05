package ca.watier.echechess.engine.abstracts;

import ca.watier.echechess.common.enums.CasePosition;
import ca.watier.echechess.common.enums.KingStatus;
import ca.watier.echechess.common.enums.Pieces;
import ca.watier.echechess.common.enums.Side;
import ca.watier.echechess.common.interfaces.BaseUtils;
import ca.watier.echechess.common.pojos.MoveHistory;
import ca.watier.echechess.common.utils.Pair;
import ca.watier.echechess.engine.pojos.KingStatusHolderPojo;
import ca.watier.echechess.engine.utils.GameUtils;
import com.google.common.collect.ArrayListMultimap;

import java.io.Serializable;
import java.util.*;

import static ca.watier.echechess.common.enums.Side.WHITE;

public abstract class GameBoardData implements Cloneable, Serializable {

    private static final long serialVersionUID = -5242416504518941779L;
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
    private int totalMove;
    private short blackPlayerPoint;
    private short whitePlayerPoint;
    private boolean isGameDraw;
    private boolean isGamePaused;
    private boolean isWhiteQueenCastlingAvailable;
    private boolean isWhiteKingCastlingAvailable;
    private boolean isBlackQueenCastlingAvailable;
    private boolean isBlackKingCastlingAvailable;
    private List<MoveHistory> moveHistoryList;
    private KingStatusHolderPojo kingHolder;
    private Side currentAllowedMoveSide;

    public GameBoardData() {
        pawnPromotionMap = ArrayListMultimap.create();
        DEFAULT_POSITIONS = GameUtils.getDefaultGame();
        positionPiecesMap = GameUtils.getDefaultGame();
        isPiecesMovedMap = GameUtils.initNewMovedPieceMap(positionPiecesMap);
        isPawnUsedSpecialMoveMap = GameUtils.initPawnMap(positionPiecesMap);
        turnNumberPieceMap = GameUtils.initTurnMap(positionPiecesMap);
        kingHolder = new KingStatusHolderPojo();
        moveHistoryList = new ArrayList<>();
        blackPlayerPoint = 0;
        whitePlayerPoint = 0;
        currentAllowedMoveSide = WHITE;
        totalMove = 0;
        isGameDraw = false;
        isGamePaused = false;
        isWhiteQueenCastlingAvailable = true;
        isWhiteKingCastlingAvailable = true;
        isBlackQueenCastlingAvailable = true;
        isBlackKingCastlingAvailable = true;
    }

    protected List<Pair<CasePosition, CasePosition>> getPawnPromotionBySide(Side playerSide) {
        if (playerSide == null) {
            return null;
        }

        return BaseUtils.getSafeList(pawnPromotionMap.get(playerSide));
    }

    protected Pieces getPieceFromPosition(CasePosition position) {
        if (position == null) {
            return null;
        }

        return positionPiecesMap.get(position);
    }

    public final Map<CasePosition, Pieces> getPiecesLocation() {
        return Collections.unmodifiableMap(positionPiecesMap);
    }

    public Map<CasePosition, Pieces> getDefaultPositions() {
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


    protected Map<CasePosition, Boolean> getIsPiecesMovedMap() {
        return Collections.unmodifiableMap(isPiecesMovedMap);
    }


    protected Map<CasePosition, Boolean> getIsPawnUsedSpecialMoveMap() {
        return Collections.unmodifiableMap(isPawnUsedSpecialMoveMap);
    }


    protected Map<CasePosition, Integer> getTurnNumberPieceMap() {
        return Collections.unmodifiableMap(turnNumberPieceMap);
    }

    /**
     * Get the turn number based on a {@link CasePosition}
     *
     * @param position
     * @return
     */
    public final Integer getPieceTurn(CasePosition position) {
        if (position == null) {
            return null;
        }

        return turnNumberPieceMap.get(position);
    }

    protected void addPawnPromotionToMap(Side side, Pair<CasePosition, CasePosition> casePositionCasePositionPair) {
        if (side == null || casePositionCasePositionPair == null) {
            return;
        }

        pawnPromotionMap.put(side, casePositionCasePositionPair);
    }

    public final void removePiece(CasePosition from) {
        if (from == null) {
            return;
        }

        positionPiecesMap.remove(from);
    }

    protected void setPiecePositionWithoutMoveState(Pieces piece, CasePosition to) {
        if (piece == null || to == null) {
            return;
        }

        positionPiecesMap.put(to, piece);
    }

    /**
     * If it's the default from of the piece, mark this one as moved
     *
     * @param piece
     * @param from
     * @param to
     */
    protected void changeMovedStateOfPiece(Pieces piece, CasePosition from, CasePosition to) {
        if (piece == null || from == null || to == null) {
            return;
        }

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
    public final Boolean isPieceMoved(CasePosition position) {
        if (position == null) {
            return false;
        }

        return isPiecesMovedMap.get(position);
    }


    /**
     * Return true if the pawn used the special move
     *
     * @param position
     * @return
     */
    public final boolean isPawnUsedSpecialMove(CasePosition position) {
        if (position == null) {
            return false;
        }

        return BaseUtils.getSafeBoolean(isPawnUsedSpecialMoveMap.get(position));
    }

    protected void addPawnUsedSpecialMove(CasePosition to, boolean isValid) {
        if (to == null) {
            return;
        }

        isPawnUsedSpecialMoveMap.put(to, isValid);
    }

    protected void removePawnUsedSpecialMove(CasePosition from) {
        if (from == null) {
            return;
        }

        isPawnUsedSpecialMoveMap.remove(from);
    }

    protected void incrementWhiteTurnNumber() {
        whiteTurnNumber++;
    }

    protected void incrementBlackTurnNumber() {
        blackTurnNumber++;
    }

    protected void changePieceTurnNumber(CasePosition from, CasePosition to) {
        if (to == null || from == null) {
            return;
        }

        turnNumberPieceMap.remove(from);
        turnNumberPieceMap.put(to, totalMove);
    }

    protected void incrementTotalMove() {
        totalMove++;
    }

    public void addHistory(MoveHistory move) {
        moveHistoryList.add(move);
    }

    protected void setPiecesGameState(Map<CasePosition, Boolean> isPawnUsedSpecialMoveMap,
                                      Map<CasePosition, Integer> turnNumberPieceMap,
                                      Map<CasePosition, Boolean> isPiecesMovedMap) {

        this.isPawnUsedSpecialMoveMap = isPawnUsedSpecialMoveMap;
        this.turnNumberPieceMap = turnNumberPieceMap;
        this.isPiecesMovedMap = isPiecesMovedMap;
    }

    /**
     * Remove a piece from the board
     *
     * @param from
     */
    public final void removePieceFromBoard(CasePosition from) {
        if (from == null) {
            return;
        }

        positionPiecesMap.remove(from);
        isPiecesMovedMap.remove(from);
        isPawnUsedSpecialMoveMap.remove(from);
        turnNumberPieceMap.remove(from);
    }

    protected void removePawnPromotion(Pair<CasePosition, CasePosition> pair, Side side) {
        if (pair == null || side == null || Side.OBSERVER.equals(side)) {
            return;
        }

        pawnPromotionMap.remove(side, pair);
    }

    protected final void setPositionPiecesMap(Map<CasePosition, Pieces> positionPiecesMap) {
        if (positionPiecesMap == null || positionPiecesMap.isEmpty()) {
            return;
        }

        this.positionPiecesMap = positionPiecesMap;
        this.DEFAULT_POSITIONS.clear();
        this.DEFAULT_POSITIONS.putAll(positionPiecesMap);
        this.isPiecesMovedMap = GameUtils.initNewMovedPieceMap(positionPiecesMap);
        this.turnNumberPieceMap = GameUtils.initTurnMap(positionPiecesMap);
    }

    public boolean isGamePaused() {
        return isGamePaused;
    }

    public void setGamePaused(boolean gamePaused) {
        isGamePaused = gamePaused;
    }

    public List<MoveHistory> getMoveHistory() {
        return Collections.unmodifiableList(moveHistoryList);
    }

    public boolean isGameDraw() {
        return isGameDraw;
    }

    protected void restore(GameBoardData gameBoardData) {
        this.positionPiecesMap = gameBoardData.positionPiecesMap;
        this.isPiecesMovedMap = gameBoardData.isPiecesMovedMap;
        this.isPawnUsedSpecialMoveMap = gameBoardData.isPawnUsedSpecialMoveMap;
        this.turnNumberPieceMap = gameBoardData.turnNumberPieceMap;
        this.pawnPromotionMap = gameBoardData.pawnPromotionMap;
    }

    protected void setKingStatusBySide(KingStatus kingStatus, Side side) {
        kingHolder.setKingStatusBySide(kingStatus, side);
    }

    public KingStatus getEvaluatedKingStatusBySide(Side side) {
        return kingHolder.getKingStatusBySide(side);
    }

    @Override
    protected GameBoardData clone() throws CloneNotSupportedException {
        GameBoardData cloned = (GameBoardData) super.clone();
        cloned.positionPiecesMap = new EnumMap<>(this.positionPiecesMap);
        cloned.isPiecesMovedMap = new EnumMap<>(this.isPiecesMovedMap);
        cloned.isPawnUsedSpecialMoveMap = new EnumMap<>(this.isPawnUsedSpecialMoveMap);
        cloned.turnNumberPieceMap = new EnumMap<>(this.turnNumberPieceMap);
        cloned.pawnPromotionMap = ArrayListMultimap.create(this.pawnPromotionMap);
        cloned.totalMove = this.totalMove;
        cloned.blackTurnNumber = this.blackTurnNumber;
        cloned.whiteTurnNumber = this.whiteTurnNumber;
        cloned.isGameDraw = this.isGameDraw;
        cloned.isGamePaused = this.isGamePaused;
        cloned.kingHolder = this.kingHolder;
        cloned.moveHistoryList = new ArrayList<>(this.moveHistoryList);
        cloned.blackPlayerPoint = this.blackPlayerPoint;
        cloned.whitePlayerPoint = this.whitePlayerPoint;
        cloned.currentAllowedMoveSide = this.currentAllowedMoveSide;
        cloned.isWhiteQueenCastlingAvailable = this.isWhiteQueenCastlingAvailable;
        cloned.isWhiteKingCastlingAvailable = this.isWhiteKingCastlingAvailable;
        cloned.isBlackQueenCastlingAvailable = this.isBlackQueenCastlingAvailable;
        cloned.isBlackKingCastlingAvailable = this.isBlackKingCastlingAvailable;

        return cloned;
    }

    protected final void changeAllowedMoveSide() {
        currentAllowedMoveSide = Side.getOtherPlayerSide(currentAllowedMoveSide);
    }

    public Side getCurrentAllowedMoveSide() {
        return currentAllowedMoveSide;
    }

    protected final void setCurrentAllowedMoveSide(Side side) {
        this.currentAllowedMoveSide = side;
    }

    protected void addBlackPlayerPoint(byte point) {
        blackPlayerPoint += point;
    }

    protected void addWhitePlayerPoint(byte point) {
        whitePlayerPoint += point;
    }

    public short getBlackPlayerPoint() {
        return blackPlayerPoint;
    }

    public short getWhitePlayerPoint() {
        return whitePlayerPoint;
    }

    public final boolean isWhiteQueenCastlingAvailable() {
        return isWhiteQueenCastlingAvailable;
    }

    protected final void setWhiteQueenCastlingAvailable(boolean whiteQueenCastlingAvailable) {
        isWhiteQueenCastlingAvailable = whiteQueenCastlingAvailable;
    }

    public final boolean isWhiteKingCastlingAvailable() {
        return isWhiteKingCastlingAvailable;
    }

    protected final void setWhiteKingCastlingAvailable(boolean whiteKingCastlingAvailable) {
        isWhiteKingCastlingAvailable = whiteKingCastlingAvailable;
    }

    public final boolean isBlackQueenCastlingAvailable() {
        return isBlackQueenCastlingAvailable;
    }

    protected final void setBlackQueenCastlingAvailable(boolean blackQueenCastlingAvailable) {
        isBlackQueenCastlingAvailable = blackQueenCastlingAvailable;
    }

    public final boolean isBlackKingCastlingAvailable() {
        return isBlackKingCastlingAvailable;
    }

    protected final void setBlackKingCastlingAvailable(boolean blackKingCastlingAvailable) {
        isBlackKingCastlingAvailable = blackKingCastlingAvailable;
    }
}
