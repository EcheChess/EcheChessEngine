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

import ca.watier.echechessengine.exceptions.ChessException;
import ca.watier.echechessengine.game.GameConstraints;
import ca.watier.echechessengine.utils.PgnParser;
import ca.watier.echesscommon.impl.WebSocketServiceTestImpl;
import ca.watier.utils.EngineGameTest;
import ca.watier.utils.PgnGameLauncherWithNumberOfGames;
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
        PgnParser pgnParser = new PgnParser(new GameConstraints(), new WebSocketServiceTestImpl());
        try {
            pgnParser.parse("[]\n\n" + //Fake header
                    "1. e4 e5 2. f4 d6 3. Nf3 exf4 4. d4 Bg4 5. Bxf4 Nc6 6. Be2 h6 7. Nc3 a6 8. O-O Bxf3 9. Bxf3 Qd7 10. Qe2 O-O-O 11. Bg4 f5 12. Bxf5 Qxf5 13. exf5 Nxd4 1-0");
        } catch (ChessException e) {
            e.printStackTrace();
        }
    }

    @Test
    @Ignore("PgnTest.pgnManualTest - Not really unit tests, only used to test the stability of the engine by running a certain number of games")
    public void pgnWithNumberOfGame() {
        new PgnGameLauncherWithNumberOfGames(2500).start();
    }

    @Test
    public void pgnTest1() {
        PgnParser pgnParser = new PgnParser(CONSTRAINT_SERVICE, WEB_SOCKET_SERVICE);

        try {
            Assertions.assertThat(pgnParser.parse(gamesAsFile)).isNotEmpty();
        } catch (ChessException e) {
            e.printStackTrace();
        }
    }
}
