package castle.comp3021.assignment.gui.views.panes;

import castle.comp3021.assignment.gui.FXJesonMor;
import castle.comp3021.assignment.gui.ViewConfig;
import castle.comp3021.assignment.gui.controllers.SceneManager;
import castle.comp3021.assignment.gui.views.BigButton;
import castle.comp3021.assignment.gui.views.BigVBox;
import castle.comp3021.assignment.gui.views.NumberTextField;
import castle.comp3021.assignment.player.ConsolePlayer;
import castle.comp3021.assignment.player.SmartRandomPlayer;
import castle.comp3021.assignment.protocol.Configuration;
import castle.comp3021.assignment.protocol.Player;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class GamePane extends BasePane {
    @NotNull
    private final VBox container = new BigVBox();
    @NotNull
    private final Label title = new Label("Jeson Mor");
    @NotNull
    private final Button playButton = new BigButton("Play");
    @NotNull
    private final Button returnButton = new BigButton("Return");
    @NotNull
    private final Button useDefaultButton = new BigButton("Use Default");
    @NotNull
    private final Button isHumanPlayer1Button = new BigButton("");
    @NotNull
    private final Button isHumanPlayer2Button = new BigButton("");

    @NotNull
    private final NumberTextField sizeFiled = new NumberTextField("");


    private final BorderPane sizeBox = new BorderPane(null, null, sizeFiled, null, new Label("Size of Board:"));

    @NotNull
    private final NumberTextField numMovesProtectionField = new NumberTextField("");

    @NotNull
    private final BorderPane numMovesProtectionBox = new BorderPane(null, null, numMovesProtectionField, null, new Label("Protection Moves:"));


    private FXJesonMor fxJesonMor = null;
    private Configuration localConfiguration;

    public GamePane() {
        fillValues();
        connectComponents();
        styleComponents();
        setCallbacks();
    }

    @Override
    void connectComponents() {
        //TODO
        this.container.getChildren().addAll(title, sizeBox, numMovesProtectionBox, isHumanPlayer1Button, isHumanPlayer2Button, useDefaultButton, playButton, returnButton);
        this.setCenter(container);
    }

    @Override
    void styleComponents() {
        title.getStyleClass().add("head-size");
    }

    /**
     * Set callbacks to buttons
     * Hint:
     * -  When fill in board size and step of protections, numbers need to validate
     * -  useDefaultButton: use default value for {@link GamePane#sizeFiled}, {@link GamePane#numMovesProtectionField}, and two players
     *    as they are saved in {@link SettingPane}
     * -  The current configuration (including {@link GamePane#sizeFiled}, {@link GamePane#numMovesProtectionField} and two players role)
     *    should not affect the default settings.
     * -  After clicking "play" button, the handler is implemented in {@link GamePane#startGame(FXJesonMor)},
     *    which links to {@link GamePlayPane} using the current configuration.
     */
    @Override
    void setCallbacks() {
        //TODO
        this.isHumanPlayer1Button.setOnAction(actionEvent -> {
            boolean isFirstPlayerHuman = !localConfiguration.isFirstPlayerHuman();
            this.localConfiguration.setFirstPlayerHuman(isFirstPlayerHuman);
            String playerType = isFirstPlayerHuman ? "Human" : "Computer";
            this.isHumanPlayer1Button.setText("Player 1: " + playerType);
        });
        this.isHumanPlayer2Button.setOnAction(actionEvent -> {
            boolean isSecondPlayerHuman = !localConfiguration.isSecondPlayerHuman();
            this.localConfiguration.setSecondPlayerHuman(isSecondPlayerHuman);
            String playerType = isSecondPlayerHuman ? "Human" : "Computer";
            this.isHumanPlayer2Button.setText("Player 2: " + playerType);
        });
        this.useDefaultButton.setOnAction(actionEvent -> fillValues());
        this.playButton.setOnAction(actionEvent -> {
            Optional<String> errorMessage = validate(sizeFiled.getValue(), numMovesProtectionField.getValue());
            if (errorMessage.isPresent()) {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setHeaderText("Validation Failed");
                errorAlert.setContentText(errorMessage.get());
                errorAlert.showAndWait();
            } else {
                Player[] players = localConfiguration.getPlayers();
                try {
                    players = new Player[]{players[0].clone(), players[1].clone()};
                } catch (CloneNotSupportedException e) {
                    e.printStackTrace();
                }
                Configuration configuration = new Configuration(sizeFiled.getValue(), players, numMovesProtectionField.getValue());
                this.fxJesonMor = new FXJesonMor(configuration);
                this.startGame(fxJesonMor);
            }
        });
        this.returnButton.setOnAction(actionEvent -> SceneManager.getInstance().showPane(MainMenuPane.class));
    }

    /**
     * Handler when clicking "play" button, using the current configuration to pass a {@link FXJesonMor} instance
     * Hint:
     *      - You may need to initialize and set up {@link GamePlayPane} by passing {@link FXJesonMor}
     * @param fxJesonMor an instance of {@link FXJesonMor}
     */
    void startGame(@NotNull FXJesonMor fxJesonMor) {
        final var gameplayPane = SceneManager.getInstance().<GamePlayPane>getPane(GamePlayPane.class);
        gameplayPane.initializeGame(fxJesonMor);
        SceneManager.getInstance().showPane(GamePlayPane.class);
    }

    /**
     * Fill in the default values for all editable fields.
     */
    void fillValues(){
        // TODO
        int globalSize = globalConfiguration.getSize();
        int globalNumMovesProtection = globalConfiguration.getNumMovesProtection();
        Player[] globalPlayers = globalConfiguration.getPlayers().clone();
        for (int i = 0; i < globalPlayers.length; i++) {
            Player newPlayer;
            if (globalPlayers[i] instanceof ConsolePlayer) {
                newPlayer = new ConsolePlayer(globalPlayers[i].getName(), globalPlayers[i].getColor());
            } else {
                newPlayer = new SmartRandomPlayer(globalPlayers[i].getName(), globalPlayers[i].getColor());
            }
            globalPlayers[i] = newPlayer;
        }
        this.localConfiguration = new Configuration(globalSize, globalPlayers, globalNumMovesProtection);
        this.sizeFiled.setText(String.valueOf(globalConfiguration.getSize()));
        this.numMovesProtectionField.setText(String.valueOf(globalConfiguration.getNumMovesProtection()));
        String playerType = globalConfiguration.isFirstPlayerHuman() ? "Human" : "Computer";
        this.isHumanPlayer1Button.setText("Player 1: " + playerType);
        playerType = globalConfiguration.isSecondPlayerHuman() ? "Human" : "Computer";
        this.isHumanPlayer2Button.setText("Player 2: " + playerType);
    }

    /**
     * Validate the text fields
     * The useful msgs are predefined in {@link ViewConfig#MSG_BAD_SIZE_NUM}, etc.
     * @param size number in {@link GamePane#sizeFiled}
     * @param numProtection number in {@link GamePane#numMovesProtectionField}
     * @return If validation failed, {@link Optional} containing the reason message; An empty {@link Optional}
     *      * otherwise.
     */
    public static Optional<String> validate(int size, int numProtection) {
        //TODO
        if (size < 3) {
            return Optional.of(ViewConfig.MSG_BAD_SIZE_NUM);
        }
        if (size % 2 != 1) {
            return Optional.of(ViewConfig.MSG_ODD_SIZE_NUM);
        }
        if (size > 26) {
            return Optional.of(ViewConfig.MSG_UPPERBOUND_SIZE_NUM);
        }

        if (numProtection < 0) {
            return Optional.of(ViewConfig.MSG_NEG_PROT);
        }

        return Optional.empty();
    }
}
