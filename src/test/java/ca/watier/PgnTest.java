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

import ca.watier.echechess.engine.exceptions.ChessException;
import ca.watier.echechess.engine.constraints.DefaultGameConstraint;
import ca.watier.echechess.engine.utils.PgnParser;
import ca.watier.utils.EngineGameTest;
import org.apache.commons.io.IOUtils;
import org.assertj.core.api.Assertions;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.Charset;

import static org.junit.Assert.fail;

public class PgnTest extends EngineGameTest {

    private static String gamesAsFile;

    static {
        try {
            gamesAsFile = IOUtils.toString(PgnTest.class.getResourceAsStream("/puzzles.pgn"), Charset.forName("UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    @Ignore("PgnTest.pgnManualTest - Not really unit tests, only used to test the stability of the engine by running games with a string")
    public void pgnManualTest() {
        PgnParser pgnParser = new PgnParser(new DefaultGameConstraint());
        try {
            pgnParser.parseSingleGameWithoutHeader(
                    "1. e4 e5 2. d3 Nf6 3. Be2 c6 4. Bd2 Be7 5. Nf3 d6 6. Nc3 h6 7. h4 Nbd7 8. O-O Ng4 9. g3 Nf8 10. Nxe5 Nxe5 11. d4 Ned7 12. d5 Qc7 13. h5 Nf6 14. a4 a6 15. b3 N8d7 16. dxc6 bxc6 17. g4 Nh7 18. Be1 Ne5 19. f4 Nd7 20. e5 Bb7 21. exd6 Qb6+ 22. Kh2 Bf6 23. g5 hxg5 24. fxg5 Be5+ 25. Kh3 Nxg5+ 26. Kh4 f6 27. Ne4 Nxe4 28. Kg4 Bc8 29. Bf3 Ndc5+ 30. Kh4 g5# 0-1");
        } catch (ChessException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void pgnTest1() {
        PgnParser pgnParser = new PgnParser(CONSTRAINT_SERVICE);

        try {
            Assertions.assertThat(pgnParser.parseMultipleGameWithHeader(gamesAsFile)).isNotEmpty();
        } catch (ChessException e) {
            e.printStackTrace();
            fail();
        }
    }
}
