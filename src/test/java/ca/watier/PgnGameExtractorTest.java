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
import ca.watier.echechess.engine.utils.PgnGameExtractor;
import org.apache.commons.io.IOUtils;
import org.assertj.core.api.Assertions;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.nio.charset.Charset;

import static org.junit.Assert.fail;

@RunWith(MockitoJUnitRunner.class)
public class PgnGameExtractorTest {


    private static String gamesAsFile;

    static {
        try {
            gamesAsFile = IOUtils.toString(PgnGameExtractorTest.class.getResourceAsStream("/puzzles.pgn"), Charset.forName("UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Test
    @Ignore("PgnGameExtractorTest#pgnManualTest - Not really unit tests, only used to test the stability of the engine by running games with a string")
    public void pgnManualTest() {
        PgnGameExtractor pgnGameExtractor = new PgnGameExtractor();
        try {
            pgnGameExtractor.parseSingleGameWithoutHeader(
                    "1.c4 f5 2.d4 e6 3.Nc3 Bb4 4.e4 fxe4 5.Qg4 Qe7 6.Bg5 Nf6 7.Bxf6 Qxf6 8.Qxe4\n" +
                            "O-O 9.Nf3 Nc6 10.Bd3 g6 11.O-O Bxc3 12.bxc3 d6 13.Rae1 Bd7 14.c5 Rae8 15.\n" +
                            "cxd6 cxd6 16.Qg4 Nd8 17.Ng5 Bc6 18.f4 Kg7 19.Re3 Re7 20.Rh3 h5 21.Qd1 e5\n" +
                            "22.Nh7 Kxh7 23.fxe5 Qxf1+ 24.Bxf1 dxe5 25.d5 Bd7 26.Rh4 e4 27.Be2 Rf5 28.\n" +
                            "Qd4 Rfe5 29.Rf4 b6 30.Rf8 Re8 31.Rxe8 Rxe8 32.Qf6 Bf5 33.d6 Ne6 34.d7 Rh8\n" +
                            "35.Qe7+ Kh6 36.h3 Rh7 37.d8=Q Nxd8 38.Qxd8 Rd7 39.Qh8+ Kg5 40.Kf2 h4 41.g3\n" +
                            "hxg3+ 42.Kxg3 Be6 43.h4+ Kf5 44.Bg4# 1-0");
        } catch (ChessException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void pgnTestFromFile() {
        PgnGameExtractor pgnGameExtractor = new PgnGameExtractor();

        try {
            Assertions.assertThat(pgnGameExtractor.parseMultipleGameWithHeader(gamesAsFile)).isNotEmpty();
        } catch (ChessException e) {
            e.printStackTrace();
            fail();
        }
    }
}
