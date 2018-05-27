package ca.watier.testers.pgnlauncher;

import ca.watier.utils.PgnGameLauncher;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.Timer;

public class PgnLauncher extends Application {
    private final PgnGameLauncher PGN_GAME_LAUNCHER = new PgnGameLauncher();
    private Timer timer = new Timer();

    @FXML
    private Label totalGameLbl;

    @FXML
    private TextField nbOfThreadsInput;

    @FXML
    private Button startBtn;


    @FXML
    void whenStartButtonPressed(ActionEvent event) {
        startBtn.setDisable(true);
        String nbOfThreadsInputText = nbOfThreadsInput.getText();

        try {
            PGN_GAME_LAUNCHER.setMaximumPoolSize(Integer.parseInt(nbOfThreadsInputText));
            PGN_GAME_LAUNCHER.start();
            timer.schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            Platform.runLater(() -> {
                                totalGameLbl.setText(Integer.toString(PGN_GAME_LAUNCHER.getNbOfGames()));
                            });
                        }
                    },
                    0, 2500
            );

        } catch (NumberFormatException nfe) {
            nfe.printStackTrace();
        }
    }

    @FXML
    void whenStopButtonPressed(ActionEvent event) {
        stopTheThreads();
    }

    private void stopTheThreads() {
        timer.cancel();
        PGN_GAME_LAUNCHER.stop();
    }

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/pgnGameLauncherUi.fxml"));
        stage.setScene(new Scene(root, 239, 139, Color.grayRgb(93)));
        stage.setResizable(false);

        stage.setOnCloseRequest(we -> {
            stopTheThreads();
            Platform.exit();
            System.exit(0);
        });
        stage.show();
    }
}
