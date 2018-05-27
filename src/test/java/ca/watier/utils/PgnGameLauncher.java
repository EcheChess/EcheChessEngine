package ca.watier.utils;

import ca.watier.echechessengine.exceptions.ChessException;
import ca.watier.echechessengine.utils.PgnParser;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

public class PgnGameLauncher extends AbstractPgnGame {
    private static final int DEFAULT_N_THREADS = 4;
    private ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(DEFAULT_N_THREADS);
    private AtomicInteger nbOfGames = new AtomicInteger(0);

    public void setMaximumPoolSize(int max) {
        if (DEFAULT_N_THREADS != max) {
            threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(max);
        }
    }

    @Override
    protected void parseGame(String header, String game) {
        threadPoolExecutor.submit(() -> {
            LOGGER.info(header);
            LOGGER.info(game);
            try {
                new PgnParser(GAME_CONSTRAINTS, WEB_SOCKET_SERVICE).parse(String.format(PGN_GAME_PATTERN, header, game));
                nbOfGames.incrementAndGet();
            } catch (ChessException e) {
                threadPoolExecutor.shutdown();
                e.printStackTrace();
            }
        });
    }

    @Override
    public void start() {
        try {
            startReading(
                    new FileInputStream(System.getenv("pgn_game_file_from")),
                    true,
                    true,
                    true,
                    false
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getNbOfGames() {
        return nbOfGames.get();
    }

    public void stop() {
        threadPoolExecutor.shutdownNow();
    }
}
