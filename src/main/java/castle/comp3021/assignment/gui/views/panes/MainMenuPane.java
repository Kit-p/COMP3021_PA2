package castle.comp3021.assignment.gui.views.panes;

import castle.comp3021.assignment.gui.controllers.SceneManager;
import castle.comp3021.assignment.gui.views.BigButton;
import castle.comp3021.assignment.gui.views.BigVBox;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.jetbrains.annotations.NotNull;

public class MainMenuPane extends BasePane {
    @NotNull
    private final VBox container = new BigVBox();
    @NotNull
    private final Label title = new Label("Jeson Mor");
    @NotNull
    private final Button playButton = new BigButton("Play Game");

    @NotNull
    private final Button settingsButton = new BigButton("Settings / About ");
    @NotNull
    private final Button validationButtion = new BigButton("Validation");
    @NotNull
    private final Button quitButton = new BigButton("Quit");

    public MainMenuPane() {
        connectComponents();
        styleComponents();
        setCallbacks();
    }

    @Override
    void connectComponents() {
        // TODO
        this.container.getChildren().addAll(title, playButton, settingsButton, validationButtion, quitButton);
        this.setCenter(container);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void styleComponents() {
        title.getStyleClass().add("head-size");
    }

    /**
     * add callbacks to buttons
     * Hint:
     *      - playButton -> {@link GamePane}
     *      - settingsButton -> {@link SettingPane}
     *      - validationButtion -> {@link ValidationPane}
     *      - quitButton -> quit the game
     */
    @Override
    void setCallbacks() {
        //TODO
        this.playButton.setOnAction(actionEvent -> {
            GamePane gamePane = SceneManager.getInstance().getPane(GamePane.class);
            gamePane.fillValues();
            SceneManager.getInstance().showPane(GamePane.class);
        });
        this.settingsButton.setOnAction(actionEvent -> SceneManager.getInstance().showPane(SettingPane.class));
        this.validationButtion.setOnAction(actionEvent -> SceneManager.getInstance().showPane(ValidationPane.class));
        this.quitButton.setOnAction(actionEvent -> Platform.exit());
    }

}
