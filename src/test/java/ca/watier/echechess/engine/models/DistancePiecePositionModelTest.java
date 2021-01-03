package ca.watier.echechess.engine.models;

import ca.watier.echechess.common.enums.CasePosition;
import ca.watier.echechess.common.enums.Pieces;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.TreeSet;

public class DistancePiecePositionModelTest {
    @Test
    public void compareTo() {
        DistancePiecePositionModel value1 = new DistancePiecePositionModel(1, Pieces.B_BISHOP, CasePosition.A1);
        DistancePiecePositionModel value2 = new DistancePiecePositionModel(3, Pieces.B_BISHOP, CasePosition.A1);
        DistancePiecePositionModel value3 = new DistancePiecePositionModel(4, Pieces.B_BISHOP, CasePosition.A1);
        DistancePiecePositionModel value4 = new DistancePiecePositionModel(5, Pieces.B_BISHOP, CasePosition.A1);
        DistancePiecePositionModel value5 = new DistancePiecePositionModel(10, Pieces.B_BISHOP, CasePosition.A1);


        Set<DistancePiecePositionModel> toTest = new TreeSet<>();
        toTest.add(value5);
        toTest.add(value2);
        toTest.add(value1);
        toTest.add(value3);
        toTest.add(value4);

        Assertions.assertThat(toTest).containsSequence(value1, value2, value3, value4, value5);
    }
}