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
                    "1. d4 c5 2. d5 e6 3. e4 Bd6 4. dxe6 dxe6 5. Bb5+ Nc6 6. Nf3 Qa5+ 7. Nc3 Ke7 8. O-O h5 9. e5 Bc7 10. Bxc6 bxc6 11. Bd2 Qb6 12. Na4 Qa6 13. Nxc5 Qb5 14. Nb3 f6 15. exf6+ Nxf6 16. a3 Ba6 17. Bb4+ Kf7 18. Ng5+ Qxg5 19. Re1 Rad8 20. Qf3 h4 21. Qxc6 Bxh2+ 22. Kh1 Bc8 23. Rad1 h3 24. Rxd8 Rxd8 25. Kxh2 Ng4+ 26. Kg1 h2+ 27. Kf1 h1=Q+ 28. Ke2 Qe5+ 29. Kf3 Qhxe1 0-1");
        } catch (ChessException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void pgnTest1() {
        PgnParser pgnParser = new PgnParser(CONSTRAINT_SERVICE);

        try {
            Assertions.assertThat(pgnParser.parseMultipleGameWithHeader(gamesAsFile)).isNotEmpty();
        } catch (ChessException e) {
            e.printStackTrace();
        }
    }
}
