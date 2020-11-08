package castle.comp3021.assignment.gui.views.panes;

import castle.comp3021.assignment.gui.FXJesonMor;
import castle.comp3021.assignment.gui.ViewConfig;
import castle.comp3021.assignment.gui.controllers.AudioManager;
import castle.comp3021.assignment.gui.controllers.SceneManager;
import castle.comp3021.assignment.gui.views.BigButton;
import castle.comp3021.assignment.gui.views.BigVBox;
import castle.comp3021.assignment.protocol.*;
import castle.comp3021.assignment.protocol.exception.InvalidConfigurationError;
import castle.comp3021.assignment.protocol.exception.InvalidGameException;
import castle.comp3021.assignment.protocol.io.Deserializer;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;


public class ValidationPane extends BasePane{
    @NotNull
    private final VBox leftContainer = new BigVBox();
    @NotNull
    private final BigVBox centerContainer = new BigVBox();
    @NotNull
    private final Label title = new Label("Jeson Mor");
    @NotNull
    private final Label explanation = new Label("Upload and validation the game history.");
    @NotNull
    private final Button loadButton = new BigButton("Load file");
    @NotNull
    private final Button validationButton = new BigButton("Validate");
    @NotNull
    private final Button replayButton = new BigButton("Replay");
    @NotNull
    private final Button returnButton = new BigButton("Return");

    private Canvas gamePlayCanvas = new Canvas();

    /**
     * store the loaded information
     */
    private Configuration loadedConfiguration;
    private Integer[] storedScores;
    private FXJesonMor loadedGame;
    private Place loadedcentralPlace;
    private ArrayList<MoveRecord> loadedMoveRecords = new ArrayList<>();

    private BooleanProperty isValid = new SimpleBooleanProperty(false);

    private String onloadErrorMessage = null;
    private Thread runningThread = null;


    public ValidationPane() {
        connectComponents();
        styleComponents();
        setCallbacks();
    }

    @Override
    void connectComponents() {
        // TODO
        this.leftContainer.getChildren().addAll(title, explanation
                , loadButton, validationButton, replayButton, returnButton);
        this.centerContainer.getChildren().addAll(gamePlayCanvas);
        this.setLeft(leftContainer);
        this.setCenter(centerContainer);
    }

    @Override
    void styleComponents() {
        title.getStyleClass().add("head-size");
    }

    /**
     * Add callbacks to each buttons.
     * Initially, replay button is disabled, gamePlayCanvas is empty
     * When validation passed, replay button is enabled.
     */
    @Override
    void setCallbacks() {
        //TODO
        this.unloadFile();
        this.loadButton.setOnAction(actionEvent -> {
            this.replayButton.setDisable(true);
            if (loadFromFile()) {
                this.validationButton.setDisable(false);
            }
        });
        this.validationButton.setOnAction(actionEvent -> onClickValidationButton());
        this.replayButton.setOnAction(actionEvent -> onClickReplayButton());
        this.returnButton.setOnAction(actionEvent -> returnToMainMenu());
    }

    /**
     * load From File and deserializer the game by two steps:
     *      - {@link ValidationPane#getTargetLoadFile}
     *      - {@link Deserializer}
     * Hint:
     *      - Get file from {@link ValidationPane#getTargetLoadFile}
     *      - Instantiate an instance of {@link Deserializer} using the file's path
     *      - Using {@link Deserializer#parseGame()}
     *      - Initialize {@link ValidationPane#loadedConfiguration}, {@link ValidationPane#loadedcentralPlace},
     *                   {@link ValidationPane#loadedGame}, {@link ValidationPane#loadedMoveRecords}
     *                   {@link ValidationPane#storedScores}
     * @return whether the file and information have been loaded successfully.
     */
    private boolean loadFromFile() {
        //TODO
        File file = getTargetLoadFile();
        if (file == null) {
            return false;
        }
        try {
            Deserializer deserializer = new Deserializer(file.toPath());
            deserializer.parseGame();
            this.unloadFile();
            this.loadedConfiguration = deserializer.getLoadedConfiguration();
            this.storedScores = deserializer.getStoredScores();
            this.loadedMoveRecords = deserializer.getMoveRecords();
            this.loadedcentralPlace = loadedConfiguration.getCentralPlace();
        } catch (FileNotFoundException e) {
            this.showErrorConfiguration(e.getMessage());
            return false;
        } catch (InvalidConfigurationError | InvalidGameException e) {
            this.onloadErrorMessage = e.getMessage();
        }
        return true;
    }

    /**
     * When click validation button, validate the loaded game configuration and move history
     * Hint:
     *      - if nothing loaded, call {@link ValidationPane#showErrorMsg}
     *      - if loaded, check loaded content by calling {@link ValidationPane#validateHistory}
     *      - When the loaded file has passed validation, the "replay" button is enabled.
     */
    private void onClickValidationButton(){
        //TODO
        if (this.onloadErrorMessage != null) {
            this.showErrorConfiguration(onloadErrorMessage);
            return;
        }
        if (validateHistory()) {
            this.passValidationWindow();
            this.isValid.set(true);
            this.validationButton.setDisable(true);
            this.replayButton.setDisable(false);
        } else {
            this.unloadFile();
        }
    }

    /**
     * Display the history of recorded move.
     * Hint:
     *      - You can add a "next" button to render each move, or
     *      - Or you can refer to {@link Task} for implementation.
     */
    private void onClickReplayButton(){
        //TODO
        if (!(isValid.get()) || (this.runningThread != null && this.runningThread.isAlive())) {
            return;
        }
        for (Player player : loadedConfiguration.getPlayers()) {
            player.setScore(0);
        }
        Piece[][] board = loadedConfiguration.getInitialBoard();
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                board[i][j] = null;
            }
        }
        loadedConfiguration.setAllInitialPieces();
        this.loadedGame = new FXJesonMor(loadedConfiguration);
        int size = loadedConfiguration.getSize() * ViewConfig.PIECE_SIZE;
        this.gamePlayCanvas.setHeight(size);
        this.gamePlayCanvas.setWidth(size);
        this.loadedGame.renderBoard(gamePlayCanvas);
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                FXJesonMor game = loadedGame;
                for (MoveRecord moveRecord : loadedMoveRecords) {
                    Platform.runLater(() -> {
                        if (game != loadedGame) {
                            return;
                        }
                        AudioManager.getInstance().playSound(AudioManager.SoundRes.PLACE);
                        game.movePiece(moveRecord.getMove());
                        game.renderBoard(gamePlayCanvas);
                    });
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }
        };
        this.runningThread = new Thread(task);
        this.runningThread.start();
    }

    /**
     * Validate the {@link ValidationPane#loadedConfiguration}, {@link ValidationPane#loadedcentralPlace},
     *              {@link ValidationPane#loadedGame}, {@link ValidationPane#loadedMoveRecords}
     *              {@link ValidationPane#storedScores}
     * Hint:
     *      - validate configuration of game
     *      - whether each move is valid
     *      - whether scores are correct
     */
    private boolean validateHistory(){
        //TODO
        if (this.loadedConfiguration == null || this.loadedcentralPlace == null
                || this.loadedMoveRecords.size() <= 0 || this.storedScores == null) {
            this.showErrorMsg();
            return false;
        }
        try {
            int size = loadedConfiguration.getSize();
            Player[] players = loadedConfiguration.getPlayers();
            int numMovesProtection = loadedConfiguration.getNumMovesProtection();
            new Configuration(size, players, numMovesProtection);
        } catch (InvalidConfigurationError e) {
            this.showErrorConfiguration(e.getMessage());
            return false;
        }
        if (!(loadedConfiguration.getCentralPlace().equals(loadedcentralPlace))) {
            this.showErrorConfiguration("Invalid central place");
            return false;
        }
        this.loadedGame = new FXJesonMor(this.loadedConfiguration);
        String ruleViolationReason = null;
        Player winner = null;
        int numMoveRecords = loadedMoveRecords.size();
        for (int i = 0; i < numMoveRecords; i++) {
            MoveRecord moveRecord = loadedMoveRecords.get(i);
            Player player = loadedGame.getCurrentPlayer();
            Move move = moveRecord.getMove();
            ruleViolationReason = player.validateMove(loadedGame, move);
            if (ruleViolationReason != null) {
                break;
            }
            Piece piece = loadedGame.getPiece(move.getSource());
            try {
                this.loadedGame.movePiece(move);
            } catch (Exception e) {
                ruleViolationReason = e.getMessage();
                break;
            }
            this.loadedGame.updateScore(player, piece, move);
            this.loadedGame.switchPlayer();
            winner = loadedGame.getWinner(player, piece, move);
            if (winner != null) {
                if (i < numMoveRecords - 1) {
                    ruleViolationReason = "Game ended too early!";
                    break;
                }
                for (int j = 0; j < storedScores.length; j++) {
                    int playerScore;
                    try {
                        playerScore = loadedGame.getConfiguration().getPlayers()[j].getScore();
                    } catch (ArrayIndexOutOfBoundsException e) {
                        ruleViolationReason = "Too many stored scores!";
                        break;
                    }
                    if (this.storedScores[j] != playerScore) {
                        System.out.println("Stored: " + storedScores[j]);
                        System.out.println("Computed: " + playerScore);
                        ruleViolationReason = "Player score does not match";
                        break;
                    }
                }
                if (ruleViolationReason != null) {
                    break;
                }
            }
        }
        if (winner == null) {
            ruleViolationReason = "No winner produced!";
        }
        if (ruleViolationReason != null) {
            this.showErrorConfiguration(ruleViolationReason);
            return false;
        }
        this.loadedGame = null;
        return true;
    }

    /**
     * Popup window show error message
     * Hint:
     *      - title: Invalid configuration or game process!
     *      - HeaderText: Due to following reason(s):
     *      - ContentText: errorMsg
     * @param errorMsg error message
     */
    private void showErrorConfiguration(String errorMsg){
        // TODO
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Invalid configuration or game process!");
        alert.setHeaderText("Due to following reason(s):");
        alert.setContentText(errorMsg);
        alert.showAndWait();
    }

    /**
     * Pop up window to warn no record has been uploaded.
     * Hint:
     *      - title: Error!
     *      - ContentText: You haven't loaded a record, Please load first.
     */
    private void showErrorMsg(){
        //TODO
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error!");
        alert.setContentText("You haven't loaded a record, Please load first.");
        alert.showAndWait();
    }

    /**
     * Pop up window to show pass the validation
     * Hint:
     *     - title: Confirm
     *     - HeaderText: Pass validation!
     */
    private void passValidationWindow(){
        //TODO
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm");
        alert.setHeaderText("Pass validation!");
        alert.getButtonTypes().setAll(ButtonType.OK);
        alert.showAndWait();
    }

    /**
     * Return to Main menu
     * Hint:
     *  - Before return, clear the rendered canvas, and clear stored information
     */
    private void returnToMainMenu(){
        // TODO
        this.unloadFile();
        SceneManager.getInstance().showPane(MainMenuPane.class);
    }


    /**
     * Prompts the user for the file to load.
     * <p>
     * Hint:
     * Use {@link FileChooser} and {@link FileChooser#setSelectedExtensionFilter(FileChooser.ExtensionFilter)}.
     *
     * @return {@link File} to load, or {@code null} if the operation is canceled.
     */
    @Nullable
    private File getTargetLoadFile() {
        //TODO
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("Move Record File", "*.txt", "*.TXT");
        fileChooser.getExtensionFilters().add(extensionFilter);
        return fileChooser.showOpenDialog(null);
    }

    /**
     * Reset everything
     */
    private void unloadFile() {
        this.loadedConfiguration = null;
        this.storedScores = null;
        this.loadedMoveRecords = null;
        this.loadedcentralPlace = null;
        this.loadedGame = null;
        this.onloadErrorMessage = null;
        this.gamePlayCanvas.getGraphicsContext2D().clearRect(0, 0
                , gamePlayCanvas.getWidth(), gamePlayCanvas.getHeight());
        this.gamePlayCanvas.setHeight(0);
        this.gamePlayCanvas.setWidth(0);
        this.isValid.set(false);
        this.validationButton.setDisable(true);
        this.replayButton.setDisable(true);
    }

}
