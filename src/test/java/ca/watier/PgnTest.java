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

import ca.watier.echechessengine.game.GameConstraints;
import ca.watier.echechessengine.utils.PgnParser;
import ca.watier.echesscommon.impl.WebSocketServiceTestImpl;
import ca.watier.utils.EngineGameTest;
import ca.watier.utils.PgnGameLauncher;
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
    @Ignore("PgnTest.pgnLauncherTest - Not really unit tests, only used to test the stability of the engine by running games with a file")
    public void pgnLauncherTest() {
        PgnGameLauncher pgnGameLauncher = new PgnGameLauncher();
        pgnGameLauncher.start();
    }

    @Test
    @Ignore("PgnTest.pgnManualTest - Not really unit tests, only used to test the stability of the engine by running games with a string")
    public void pgnManualTest() {
        PgnParser pgnParser = new PgnParser(new GameConstraints(), new WebSocketServiceTestImpl());
        pgnParser.parse("[]\n\n" + //Fake header
                "1. d4 d5 2. c4 Bf5 3. Nc3 e6 4. Nf3 Nc6 5. h3 h5 6. Bg5 f6 7. Bh4 g5 8. Bg3 h4 9. Bh2 Bb4 10. cxd5 Bxc3+ " +
                "11. bxc3 Qxd5 12. e3 g4 13. c4 Qd7 14. Nd2 gxh3 15. g4 hxg3 16. fxg3 O-O-O 17. g4 Bg6 18. Qf3 f5 19. Bxh3 " +
                "fxg4 20. Bxg4 Nh6 21. Nb3 Nxg4 22. Qxg4 Bf5 23. Qf4 Rdf8 24. Nc5 Qg7 25. Rg1 Qh7 26. Rh1 Be4 27. Nxe4 Rxf4 " +
                "28. exf4 Qxe4+ 29. Kf2 Qxd4+ 30. Kf3 Qxc4 31. Rhe1 Rxh2 32. Rac1 Qd3+ 33. Re3 Rh3+ 34. Kf2 Qxe3+ 35. Kg2 Qxc1 " +
                "36. Kxh3 Qxf4 37. a4 e5 38. a5 e4 39. a6 b6 40. Kg2 e3 41. Kh3 e2 42. Kg2 Qf1+ 43. Kg3 e1=Q+ 44. Kg4 Qee2+ " +
                "45. Kg5 Qeg2+ 0-1");
    }

    @Test
    public void pgnTest1() {
        PgnParser pgnParser = new PgnParser(CONSTRAINT_SERVICE, WEB_SOCKET_SERVICE);

        Assertions.assertThat(pgnParser.parse(gamesAsFile)).isNotEmpty();
    }
}
