/*
 *    Copyright 2014 - 2018 Yannick Watier
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package ca.watier.utils;

import ca.watier.echechess.common.enums.CasePosition;
import ca.watier.echechess.common.enums.Pieces;
import ca.watier.echechess.engine.utils.GameUtils;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

import static ca.watier.echechess.common.enums.CasePosition.*;
import static ca.watier.echechess.common.enums.Pieces.*;

/**
 * Created by yannick on 4/28/2017.
 */
public class GameUtilsTest {

    @Test
    public void getPiecesPosition() {
        Map<CasePosition, Pieces> pieces = GameUtils.getDefaultGame();

        Assertions.assertThat(GameUtils.getPiecesPosition(W_PAWN, pieces)).isNotEmpty().hasSize(8);
        Assertions.assertThat(GameUtils.getPiecesPosition(W_ROOK, pieces)).isNotEmpty().hasSize(2);
        Assertions.assertThat(GameUtils.getPiecesPosition(W_KING, pieces)).isNotEmpty().hasSize(1);
    }

    @Test
    public void isOtherPiecesBetweenTarget() {

        Map<CasePosition, Pieces> defaultGame = GameUtils.getDefaultGame();
        Assert.assertTrue(GameUtils.isOtherPiecesBetweenTarget(H1, H7, defaultGame));
        Assert.assertFalse(GameUtils.isOtherPiecesBetweenTarget(H2, H7, defaultGame));
        Assert.assertTrue(GameUtils.isOtherPiecesBetweenTarget(H7, H1, defaultGame));
        Assert.assertFalse(GameUtils.isOtherPiecesBetweenTarget(H7, H2, defaultGame));

        Assert.assertTrue(GameUtils.isOtherPiecesBetweenTarget(A1, G7, defaultGame));
        Assert.assertFalse(GameUtils.isOtherPiecesBetweenTarget(B2, G7, defaultGame));
        Assert.assertTrue(GameUtils.isOtherPiecesBetweenTarget(G7, A1, defaultGame));
        Assert.assertFalse(GameUtils.isOtherPiecesBetweenTarget(G7, B2, defaultGame));

        Assert.assertFalse(GameUtils.isOtherPiecesBetweenTarget(H1, G1, defaultGame));
        Assert.assertFalse(GameUtils.isOtherPiecesBetweenTarget(G1, H1, defaultGame));

        Assert.assertTrue(GameUtils.isOtherPiecesBetweenTarget(H1, F1, defaultGame));
        Assert.assertTrue(GameUtils.isOtherPiecesBetweenTarget(F1, H1, defaultGame));

    }
}