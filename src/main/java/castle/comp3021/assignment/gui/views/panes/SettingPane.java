package castle.comp3021.assignment.gui.views.panes;

import castle.comp3021.assignment.gui.DurationTimer;
import castle.comp3021.assignment.gui.ViewConfig;
import castle.comp3021.assignment.gui.controllers.AudioManager;
import castle.comp3021.assignment.gui.controllers.SceneManager;
import castle.comp3021.assignment.gui.views.BigButton;
import castle.comp3021.assignment.gui.views.BigVBox;
import castle.comp3021.assignment.gui.views.NumberTextField;
import castle.comp3021.assignment.gui.views.SideMenuVBox;
import castle.comp3021.assignment.player.ConsolePlayer;
import castle.comp3021.assignment.player.SmartRandomPlayer;
import castle.comp3021.assignment.protocol.Configuration;
import castle.comp3021.assignment.protocol.Player;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class SettingPane extends BasePane {
    @NotNull
    private final Label title = new Label("Jeson Mor <Game Setting>");
    @NotNull
    private final Button saveButton = new BigButton("Save");
    @NotNull
    private final Button returnButton = new BigButton("Return");
    @NotNull
    private final Button isHumanPlayer1Button = new BigButton("Player 1: ");
    @NotNull
    private final Button isHumanPlayer2Button = new BigButton("Player 2: ");
    @NotNull
    private final Button toggleSoundButton = new BigButton("Sound FX: Enabled");

    @NotNull
    private final VBox leftContainer = new SideMenuVBox();

    @NotNull
    private final NumberTextField sizeFiled = new NumberTextField(String.valueOf(globalConfiguration.getSize()));

    @NotNull
    private final BorderPane sizeBox = new BorderPane(null, null, sizeFiled, null, new Label("Board size"));

    @NotNull
    private final NumberTextField durationField = new NumberTextField(String.valueOf(DurationTimer.getDefaultEachRound()));
    @NotNull
    private final BorderPane durationBox = new BorderPane(null, null, durationField, null,
            new Label("Max Duration (s)"));

    @NotNull
    private final NumberTextField numMovesProtectionField =
            new NumberTextField(String.valueOf(globalConfiguration.getNumMovesProtection()));
    @NotNull
    private final BorderPane numMovesProtectionBox = new BorderPane(null, null,
            numMovesProtectionField, null, new Label("Steps of protection"));

    @NotNull
    private final VBox centerContainer = new BigVBox();
    @NotNull
    private final TextArea infoText = new TextArea(ViewConfig.getAboutText());

    private Configuration tempConfiguration;
    private boolean isAudioEnabled;


    public SettingPane() {
        connectComponents();
        styleComponents();
        setCallbacks();
    }

    /**
     * Add components to corresponding containers
     */
    @Override
    void connectComponents() {
        //TODO
        fillValues();
        this.leftContainer.getChildren().addAll(title, sizeBox, numMovesProtectionBox, durationBox, isHumanPlayer1Button, isHumanPlayer2Button, toggleSoundButton, saveButton, returnButton);
        this.centerContainer.getChildren().add(infoText);
        this.setLeft(leftContainer);
        this.setCenter(centerContainer);
    }

    @Override
    void styleComponents() {
        infoText.getStyleClass().add("text-area");
        infoText.setEditable(false);
        infoText.setWrapText(true);
        infoText.setPrefHeight(ViewConfig.HEIGHT);
    }

    /**
     * Add handlers to buttons, textFields.
     * Hint:
     *  - Text of {@link SettingPane#isHumanPlayer1Button}, {@link SettingPane#isHumanPlayer2Button},
     *            {@link SettingPane#toggleSoundButton} should be changed accordingly
     *  - You may use:
     *      - {@link Configuration#isFirstPlayerHuman()},
     *      - {@link Configuration#isSecondPlayerHuman()},
     *      - {@link Configuration#setFirstPlayerHuman(boolean)}
     *      - {@link Configuration#isSecondPlayerHuman()},
     *      - {@link AudioManager#setEnabled(boolean)},
     *      - {@link AudioManager#isEnabled()},
     */
    @Override
    void setCallbacks() {
        //TODO
        this.isHumanPlayer1Button.setOnAction(actionEvent -> {
            boolean isFirstPlayerHuman = !tempConfiguration.isFirstPlayerHuman();
            this.tempConfiguration.setFirstPlayerHuman(isFirstPlayerHuman);
            String playerType = isFirstPlayerHuman ? "Human" : "Computer";
            this.isHumanPlayer1Button.setText("Player 1: " + playerType);
        });
        this.isHumanPlayer2Button.setOnAction(actionEvent -> {
            boolean isSecondPlayerHuman = !tempConfiguration.isSecondPlayerHuman();
            this.tempConfiguration.setSecondPlayerHuman(isSecondPlayerHuman);
            String playerType = isSecondPlayerHuman ? "Human" : "Computer";
            this.isHumanPlayer2Button.setText("Player 2: " + playerType);
        });
        this.toggleSoundButton.setOnAction(actionEvent -> {
            this.isAudioEnabled = !isAudioEnabled;
            String toggleText = isAudioEnabled ? "Enabled" : "Disabled";
            this.toggleSoundButton.setText("Sound FX: " + toggleText);
        });
        this.saveButton.setOnAction(actionEvent -> returnToMainMenu(true));
        this.returnButton.setOnAction(actionEvent -> returnToMainMenu(false));
    }

    /**
     * Fill in the default values for all editable fields.
     */
    private void fillValues() {
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
        this.tempConfiguration = new Configuration(globalSize, globalPlayers, globalNumMovesProtection);
        this.isAudioEnabled = AudioManager.getInstance().isEnabled();
        this.sizeFiled.setText(String.valueOf(globalConfiguration.getSize()));
        this.numMovesProtectionField.setText(String.valueOf(globalConfiguration.getNumMovesProtection()));
        String buttonText = globalConfiguration.isFirstPlayerHuman() ? "Human" : "Computer";
        this.isHumanPlayer1Button.setText("Player 1: " + buttonText);
        buttonText = globalConfiguration.isSecondPlayerHuman() ? "Human" : "Computer";
        this.isHumanPlayer2Button.setText("Player 2: " + buttonText);
        buttonText = isAudioEnabled ? "Enabled" : "Disabled";
        this.toggleSoundButton.setText("Sound FX: " + buttonText);
        durationField.setText(String.valueOf(DurationTimer.getDefaultEachRound()));
    }

    /**
     * Switches back to the {@link MainMenuPane}.
     *
     * @param writeBack Whether to save the values present in the text fields to their respective classes.
     */
    private void returnToMainMenu(final boolean writeBack) {
        //TODO
        if (writeBack) {
            Optional<String> errorMessage = validate(sizeFiled.getValue(), numMovesProtectionField.getValue(), durationField.getValue());
            if (errorMessage.isPresent()) {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setHeaderText("Validation Failed");
                errorAlert.setContentText(errorMessage.get());
                errorAlert.showAndWait();
                return;
            } else {
                globalConfiguration.setSize(sizeFiled.getValue());
                globalConfiguration.setNumMovesProtection(numMovesProtectionField.getValue());
                globalConfiguration.setFirstPlayerHuman(tempConfiguration.isFirstPlayerHuman());
                globalConfiguration.setSecondPlayerHuman(tempConfiguration.isSecondPlayerHuman());
                AudioManager.getInstance().setEnabled(isAudioEnabled);
                DurationTimer.setDefaultEachRound(durationField.getValue());
            }
        }
        fillValues();
        SceneManager.getInstance().showPane(MainMenuPane.class);
    }

    /**
     * Validate the text fields
     * The useful msgs are predefined in {@link ViewConfig#MSG_BAD_SIZE_NUM}, etc.
     * @param size number in {@link SettingPane#sizeFiled}
     * @param numProtection number in {@link SettingPane#numMovesProtectionField}
     * @param duration number in {@link SettingPane#durationField}
     * @return If validation failed, {@link Optional} containing the reason message; An empty {@link Optional}
     *      * otherwise.
     */
    public static Optional<String> validate(int size, int numProtection, int duration) {
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

        if (duration <= 0) {
            return Optional.of(ViewConfig.MSG_NEG_DURATION);
        }

        return Optional.empty();
    }
}
