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

import ca.watier.echechess.common.impl.WebSocketServiceTestImpl;
import ca.watier.echechess.engine.exceptions.ChessException;
import ca.watier.echechess.engine.game.GameConstraints;
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
        PgnParser pgnParser = new PgnParser(new GameConstraints(), new WebSocketServiceTestImpl());
        try {
            pgnParser.parseSingleGameWithoutHeader(
                    "1. e4 e6 2. d3 d5 3. Nd2 Nd7 4. Ngf3 Bd6 5. g3 Ne7 6. Bg2 O-O 7. O-O c6 8. d4\n" +
                            "dxe4 9. Nxe4 Bc7 10. c4 h6 11. Qc2 Nf5 12. Rd1 Nf6 13. Nc3 Bb6 14. Ne2 Bd7 15.\n" +
                            "Bf4 Rc8 16. b4 Bc7 17. a4 a5 18. bxa5 Bxf4 19. Nxf4 Qxa5 20. Qb3 Rfd8 21. Nd3\n" +
                            "Be8 22. Qb2 Qa7 23. c5 Nd5 24. Nde5 b6 25. cxb6 Nxb6 26. a5 Nd5 27. Nd3 Rb8 28.\n" +
                            "Qc2 Rb5 29. a6 Nxd4 30. Nxd4 Qxd4 31. Bf1 Qa7 32. Ne5 Rb6 33. Qc5 Ra8 34. Rdc1\n" +
                            "f6 35. Ng4 Bh5 36. Ra4 Bg6 37. Qd6 Bf7 38. Rxc6 Rxc6 39. Qxc6 h5 40. Ne3 Nxe3\n" +
                            "41. fxe3 Qxe3+ 42. Kg2 Ra7 43. Qf3 Qd2+ 44. Qf2 Qd7 45. Rd4 Qe7 46. Kg1 Bg6 47.\n" +
                            "Rd1 e5 48. Qb6 Kh7 49. Qd6 Qf7 50. Qd5 Qc7 51. Qd6 Qc3 52. Qd2 Qb3 53. Re1 Qb6+\n" +
                            "54. Qe3 Rxa6 55. Bxa6 Qxa6 56. Rc1 Qb7 57. Re1 Qd5 58. h3 Bf5 59. Kh2 Qe6 60.\n" +
                            "Qf3 Kh6 61. Qe3+ Kg6 62. h4 Bg4 63. Qe4+ Kh6 64. Qe3+ Kh7 65. Qe4+ Bf5 66. Qf3\n" +
                            "Bg4 67. Qe4+ Kh6 68. Qe3+ Kg6 69. Qe4+ Bf5 70. Qe3 Qc4 71. Re2 Qg4 72. Re1 Kh7\n" +
                            "73. Kg1 Qc4 74. Kh2 Qc2+ 75. Re2 Qd1 76. Re1 Qd5 77. Re2 Kg6 78. Re1 Kf7 79. Re2\n" +
                            "Bg4 80. Rd2 Qe6 81. Qa7+ Kg6 82. Qe3 Qc6 83. Rf2 Qc4 84. Kg1 Bf5 85. Re2 Bd3 86.\n" +
                            "Re1 Bf5 87. Re2 Qd5 88. Re1 Kf7 89. Qa7+ Kg6 90. Qe3 Qb7 91. Kh2 Kf7 92. Kg1 Bg4\n" +
                            "93. Qe4 Qb6+ 94. Qe3 Qc6 95. Qb3+ Kg6 96. Qe3 Bf3 97. Rc1 Qd5 98. Qc5 Qd7 99.\n" +
                            "Qf2 e4 100. Kh2 Qd3 101. Re1 Kh7 102. Re3 Qd1 103. Re1 Qd6 104. Qe3 Qe5 105. Qf2\n" +
                            "Qc7 106. Kg1 f5 107. Kh2 Qb8 108. Qd2 Qe5 109. Qf2 Qf6 110. Qd2 Qg6 111. Qf4 Qf6\n" +
                            "112. Qd2 Qb6 113. Kh3 Qc5 114. Qe3 Qc2 115. Rg1 Bg4# 1-0");
        } catch (ChessException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void pgnTest1() {
        PgnParser pgnParser = new PgnParser(CONSTRAINT_SERVICE, WEB_SOCKET_SERVICE);

        try {
            Assertions.assertThat(pgnParser.parseMultipleGameWithHeader(gamesAsFile)).isNotEmpty();
        } catch (ChessException e) {
            e.printStackTrace();
        }
    }
}
