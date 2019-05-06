/*
 *    Copyright 2014 - 2017 Yannick Watier
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

package ca.watier;

import ca.watier.echechess.engine.delegates.PieceMoveConstraintDelegate;
import ca.watier.echechess.engine.exceptions.ChessException;
import ca.watier.echechess.engine.utils.PgnGameExtractor;
import org.apache.commons.io.IOUtils;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.Charset;

import static org.junit.Assert.fail;

public class PgnGameExtractorTest {

    private static String gamesAsFile;

    static {
        try {
            gamesAsFile = IOUtils.toString(PgnGameExtractorTest.class.getResourceAsStream("/puzzles.pgn"), Charset.forName("UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private PieceMoveConstraintDelegate moveDelegate;

    @Before
    public void setUp() {
        moveDelegate = new PieceMoveConstraintDelegate();
    }

    @Test
    @Ignore("PgnTest.pgnManualTest - Not really unit tests, only used to test the stability of the engine by running games with a string")
    public void pgnManualTest() {
        PgnGameExtractor pgnGameExtractor = new PgnGameExtractor(moveDelegate);
        try {
            pgnGameExtractor.parseSingleGameWithoutHeader(
                    "1. e4 e5 2. Nf3 d6 3. Bc4 Bg4 4. h3 Bxf3 5. Qxf3 Nf6 6. d3 Nc6 7. Be3 Be7\n" +
                            "8. O-O O-O 9. Nc3 a6 10. Nd5 Nxd5 11. exd5 Na5 12. Bb3 Nxb3 13. axb3 b5\n" +
                            "14. c4 Bg5 15. Bxg5 Qxg5 16. Qe3 Qxe3 17. fxe3 bxc4 18. bxc4 a5 19. Ra2 a4\n" +
                            "20. Rfa1 Rfb8 21. Rb1 h6 22. Kf2 a3 23. b3 Rb4 24. Kf3 g5 25. g4 f6 26. d4 Rab8\n" +
                            "27. Rxa3 Kf7 28. dxe5 fxe5 29. Ke4 Kg6 30. Kd3 Rf8 31. Ra7 Rfb8 32. Kc3 R4b7\n" +
                            "33. Rxb7 Rxb7 34. b4 h5 35. b5 hxg4 36. hxg4 Kf6 37. Rf1+ Kg6 38. Rf8 Kg7\n" +
                            "39. Rc8 Kf6 40. Kb4 e4 41. Re8 c5+ 42. dxc6 Re7 43. Rxe7 Kxe7 44. b6 Ke6\n" +
                            "45. c7 Ke5 46. c8=Q d5 47. cxd5 Kxd5 48. Qf5+ 1-0");
        } catch (ChessException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void pgnTestFromFile() {
        PgnGameExtractor pgnGameExtractor = new PgnGameExtractor(moveDelegate);

        try {
            Assertions.assertThat(pgnGameExtractor.parseMultipleGameWithHeader(gamesAsFile)).isNotEmpty();
        } catch (ChessException e) {
            e.printStackTrace();
            fail();
        }
    }
}
