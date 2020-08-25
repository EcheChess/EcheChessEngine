package ca.watier.echechess.engine.models.enums;

import java.util.Objects;

public enum MoveStatus {
    VALID_MOVE, INVALID_MOVE,
    VALID_ATTACK, INVALID_ATTACK,
    ENEMY_KING_PARTIAL_CHECK,
    CAN_PROTECT_FRIENDLY,
    KING_ATTACK_KING;

    public static boolean isMoveValid(MoveStatus moveStatus) {
        switch (moveStatus) {
            case ENEMY_KING_PARTIAL_CHECK:
            case VALID_MOVE:
            case VALID_ATTACK:
                return true;
            default:
                return false;
        }
    }

    public static boolean isAttack(MoveStatus moveStatus) {
        return VALID_ATTACK.equals(moveStatus) ||
                INVALID_ATTACK.equals(moveStatus);
    }

    public static boolean isMove(MoveStatus moveStatus) {
        return VALID_MOVE.equals(moveStatus) ||
                INVALID_MOVE.equals(moveStatus);
    }

    public static MoveStatus getInvalidMoveStatusBasedOnTarget(Object target) {
        if (Objects.isNull(target)) {
            return INVALID_MOVE;
        } else {
            return INVALID_ATTACK;
        }
    }

    public static MoveStatus getValidMoveStatusBasedOnTarget(Object target) {
        if (Objects.isNull(target)) {
            return VALID_MOVE;
        } else {
            return VALID_ATTACK;
        }
    }
}
