package castle.comp3021.assignment.gui.views.panes;

import castle.comp3021.assignment.gui.DurationTimer;
import castle.comp3021.assignment.gui.FXJesonMor;
import castle.comp3021.assignment.gui.ViewConfig;
import castle.comp3021.assignment.gui.controllers.AudioManager;
import castle.comp3021.assignment.gui.controllers.Renderer;
import castle.comp3021.assignment.gui.controllers.SceneManager;
import castle.comp3021.assignment.gui.views.BigButton;
import castle.comp3021.assignment.gui.views.BigVBox;
import castle.comp3021.assignment.gui.views.GameplayInfoPane;
import castle.comp3021.assignment.gui.views.SideMenuVBox;
import castle.comp3021.assignment.player.ConsolePlayer;
import castle.comp3021.assignment.protocol.*;
import castle.comp3021.assignment.protocol.io.Serializer;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.Text;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * This class implements the main playing function of Jeson Mor
 * The necessary components have been already defined (e.g., topBar, title, buttons).
 * Basic functions:
 *      - Start game and play, update scores
 *      - Restart the game
 *      - Return to main menu
 *      - Elapsed Timer (ticking from 00:00 -> 00:01 -> 00:02 -> ...)
 *          - The format is defined in {@link GameplayInfoPane#formatTime(int)}
 * Requirement:
 *      - The game should be initialized by configuration passed from {@link GamePane}, instead of the default configuration
 *      - The information of the game (including scores, current player name, ect.) is implemented in {@link GameplayInfoPane}
 *      - The center canvas (defined as gamePlayCanvas) should be disabled when current player is computer
 * Bonus:
 *      - A countdown timer (if this is implemented, then elapsed timer can be either kept or removed)
 *      - The format of countdown timer is defined in {@link GameplayInfoPane#countdownFormat(int)}
 *      - If one player runs out of time of each round {@link DurationTimer#getDefaultEachRound()}, then the player loses the game.
 * Hint:
 *      - You may find it useful to synchronize javafx UI-thread using {@link javafx.application.Platform#runLater}
 */ 

public class GamePlayPane extends BasePane {
    @NotNull
    private final HBox topBar = new HBox(20);
    @NotNull
    private final SideMenuVBox leftContainer = new SideMenuVBox();
    @NotNull
    private final Label title = new Label("Jeson Mor");
    @NotNull
    private final Text parameterText = new Text();
    @NotNull
    private final BigButton returnButton = new BigButton("Return");
    @NotNull
    private final BigButton startButton = new BigButton("Start");
    @NotNull
    private final BigButton restartButton = new BigButton("Restart");
    @NotNull
    private final BigVBox centerContainer = new BigVBox();
    @NotNull
    private final Label historyLabel = new Label("History");

    @NotNull
    private final Text historyFiled = new Text();
    @NotNull
    private final ScrollPane scrollPane = new ScrollPane();

    /**
     * time passed in seconds
     * Hint:
     *      - Bind it to time passed in {@link GameplayInfoPane}
     */
    private final IntegerProperty ticksElapsed = new SimpleIntegerProperty();

    @NotNull
    private final Canvas gamePlayCanvas = new Canvas();

    private GameplayInfoPane infoPane = null;

    /**
     * You can add more necessary variable here.
     * Hint:
     *      - the passed in {@link FXJesonMor}
     *      - other global variable you want to note down.
     */
    // TODO
    private FXJesonMor game = null;
    private Player winner = null;
    private Place moveSource = null;
    private Place moveDest = null;

    public GamePlayPane() {
        connectComponents();
        styleComponents();
        setCallbacks();
    }

    /**
     * Components are added, adjust it by your own choice
     */
    @Override
    void connectComponents() {
        //TODO
        this.topBar.getChildren().add(title);
        this.topBar.setAlignment(Pos.CENTER);
        this.leftContainer.getChildren().addAll(this.parameterText, this.historyLabel, this.scrollPane
                , this.startButton, this.restartButton, this.returnButton);
        this.setTop(topBar);
        this.setLeft(leftContainer);
        this.setCenter(centerContainer);
    }

    /**
     * style of title and scrollPane have been set up, no need to add more
     */
    @Override
    void styleComponents() {
        title.getStyleClass().add("head-size");
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefSize(ViewConfig.WIDTH / 4.0, ViewConfig.HEIGHT / 3.0 );
        scrollPane.setContent(historyFiled);
    }

    /**
     * The listeners are added here.
     */
    @Override
    void setCallbacks() {
        //TODO
        this.startButton.setDisable(false);
        this.restartButton.setDisable(true);
        this.startButton.setOnAction(actionEvent -> {
            this.startButton.setDisable(true);
            this.restartButton.setDisable(false);
            this.game.startCountdown();
            this.startGame();
        });
        this.restartButton.setOnAction(actionEvent -> this.onRestartButtonClick());
        this.returnButton.setOnAction(actionEvent -> this.doQuitToMenuAction());
        this.gamePlayCanvas.setOnMousePressed(this::onCanvasPressed);
        this.gamePlayCanvas.setOnMouseDragged(this::onCanvasDragged);
        this.gamePlayCanvas.setOnMouseReleased(this::onCanvasReleased);
    }

    /**
     * Set up necessary initialization.
     * Hint:
     *      - Set buttons enable/disable
     *          - Start button: enable
     *          - restart button: disable
     *      - This function can be invoked before {@link GamePlayPane#startGame()} for setting up
     *
     * @param fxJesonMor pass in an instance of {@link FXJesonMor}
     */
    void initializeGame(@NotNull FXJesonMor fxJesonMor) {
        //TODO
        if (this.game != null) {
            this.endGame();
        }
        this.game = fxJesonMor;
        this.infoPane = new GameplayInfoPane(game.getPlayer1Score(), game.getPlayer2Score()
                , game.getCurPlayerName(), ticksElapsed);
        HBox.setHgrow(infoPane, Priority.ALWAYS);
        this.centerContainer.getChildren().addAll(gamePlayCanvas, infoPane);
        this.startButton.setDisable(false);
        this.restartButton.setDisable(true);
        this.disnableCanvas();
        int length = game.getConfiguration().getSize() * ViewConfig.PIECE_SIZE;
        this.gamePlayCanvas.setWidth(length);
        this.gamePlayCanvas.setHeight(length);
        this.game.addOnTickHandler(() -> Platform.runLater(() -> {
            this.ticksElapsed.set(ticksElapsed.get() + 1);
            this.startGame();
        }));
        this.game.addOnTimeUpHandler(() -> Platform.runLater(this::onTimeUp));
        Configuration configuration = game.getConfiguration();
        int size = configuration.getSize();
        int numMoveProtection = configuration.getNumMovesProtection();
        Player[] players = configuration.getPlayers();
        String firstPlayerType = configuration.isFirstPlayerHuman() ? "(human)" : "(computer)";
        String secondPlayerType = configuration.isSecondPlayerHuman() ? "(human)" : "(computer)";
        String parameterText = "Parameters:\n\nSize of board: " + size
                + "\nNum of protection moves: " + numMoveProtection
                + "\nPlayer " + players[0].getName() + firstPlayerType
                + "\nPlayer " + players[1].getName() + secondPlayerType + "\n";
        this.parameterText.setText(parameterText);
        this.parameterText.setDisable(true);
        this.game.renderBoard(gamePlayCanvas);
    }

    /**
     * enable canvas clickable
     */
    private void enableCanvas(){
        gamePlayCanvas.setDisable(false);
    }

    /**
     * disable canvas clickable
     */
    private void disnableCanvas(){
        gamePlayCanvas.setDisable(true);
    }

    /**
     * After click "start" button, everything will start from here
     * No explicit skeleton is given here.
     * Hint:
     *      - Give a carefully thought to how to activate next round of play
     *      - When a new {@link Move} is acquired, it needs to be check whether this move is valid.
     *          - If is valid, make the move, render the {@link GamePlayPane#gamePlayCanvas}
     *          - If is invalid, abort the move
     *          - Update score, add the move to {@link GamePlayPane#historyFiled}, also record the move
     *          - Move forward to next player
     *      - The player can be either computer or human, when the computer is playing, disable {@link GamePlayPane#gamePlayCanvas}
     *      - You can add a button to enable next move once current move finishes.
     *          - or you can add handler when mouse is released
     *          - or you can take advantage of timer to automatically change player. (Bonus)
     */
    public void startGame() {
        //TODO
        if (this.game == null) {
            return;
        }
        this.game.renderBoard(gamePlayCanvas);
        Player currentPlayer = game.getCurrentPlayer();
        Move[] availableMoves = game.getAvailableMoves(currentPlayer);
        if (availableMoves.length <= 0) {
            this.showInvalidMoveMsg("No available moves for the player " + currentPlayer.getName());
            Player[] players = game.getConfiguration().getPlayers();
            int player1Score = players[0].getScore();
            int player2Score = players[1].getScore();
            if (player1Score < player2Score) {
                this.winner = players[0];
            } else if (player1Score > player2Score) {
                this.winner = players[1];
            } else {
                this.winner = currentPlayer;
            }
        } else if (this.winner == null) {
            Move nextMove = null;
            if (currentPlayer instanceof ConsolePlayer) {
                this.enableCanvas();
                if (this.moveSource != null && this.moveDest != null) {
                    nextMove = new Move(moveSource, moveDest);
                    this.moveSource = null;
                    this.moveDest = null;
                    this.disnableCanvas();
                }
            } else {
                this.disnableCanvas();
                nextMove = currentPlayer.nextMove(game, availableMoves);
            }
            if (nextMove != null) {
                Piece nextPiece = game.getPiece(nextMove.getSource());
                if (nextPiece == null) {
                    this.showInvalidMoveMsg("the source of move should have a piece");
                } else if (!(nextPiece.getPlayer().equals(currentPlayer))) {
                    this.showInvalidMoveMsg("The piece you moved does not belong to you!");
                } else {
                    String ruleViolationReason = currentPlayer.validateMove(game, nextMove);
                    if (ruleViolationReason != null) {
                        this.showInvalidMoveMsg(ruleViolationReason);
                    } else {
                        AudioManager.getInstance().playSound(AudioManager.SoundRes.PLACE);
                        Piece nextMovePiece = game.getPiece(nextMove.getSource());
                        this.game.movePiece(nextMove);
                        this.game.switchPlayer();
                        this.ticksElapsed.set(0);
                        this.game.updateScore(currentPlayer, nextMovePiece, nextMove);
                        this.winner = game.getWinner(currentPlayer, nextMovePiece, nextMove);
                        this.updateHistoryField(nextMove);
                        this.checkWinner();
                    }
                }
            }
        } else {
            this.checkWinner();
        }
    }

    /**
     * Restart the game
     * Hint: end the current game and start a new game
     */
    private void onRestartButtonClick(){
        //TODO
        this.endGame();
        Configuration configuration = game.getConfiguration();
        Piece[][] board = configuration.getInitialBoard();
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                board[i][j] = null;
            }
        }
        configuration.setAllInitialPieces();
        Player[] players = configuration.getPlayers();
        for (Player player : players) {
            player.setScore(0);
        }
        this.initializeGame(new FXJesonMor(configuration));
    }

    /**
     * Add mouse pressed handler here.
     * Play click.mp3
     * draw a rectangle at clicked board tile to show which tile is selected
     * Hint:
     *      - Highlight the selected board cell using {@link Renderer#drawRectangle(GraphicsContext, double, double)}
     *      - Refer to {@link GamePlayPane#toBoardCoordinate(double)} for help
     * @param event mouse click
     */
    private void onCanvasPressed(MouseEvent event){
        // TODO
    }

    /**
     * When mouse dragging, draw a path
     * Hint:
     *      - When mouse dragging, you can use {@link Renderer#drawOval(GraphicsContext, double, double)} to show the path
     *      - Refer to {@link GamePlayPane#toBoardCoordinate(double)} for help
     * @param event mouse position
     */
    private void onCanvasDragged(MouseEvent event){
        //TODO
    }

    /**
     * Mouse release handler
     * Hint:
     *      - When mouse released, a {@link Move} is completed, you can either validate and make the move here, or somewhere else.
     *      - Refer to {@link GamePlayPane#toBoardCoordinate(double)} for help
     *      - If the piece has been successfully moved, play place.mp3 here (or somewhere else)
     * @param event mouse release
     */
    private void onCanvasReleased(MouseEvent event){
        // TODO
    }

    /**
     * Creates a popup which tells the winner
     */
    private void createWinPopup(String winnerName){
        //TODO
    }


    /**
     * check winner, if winner comes out, then play the win.mp3 and popup window.
     * The window has three options:
     *      - Start New Game: the same function as clicking "restart" button
     *      - Export Move Records: Using {@link castle.comp3021.assignment.protocol.io.Serializer} to write game's configuration to file
     *      - Return to Main menu, using {@link GamePlayPane#doQuitToMenuAction()}
     */
    private void checkWinner(){
        //TODO
    }

    /**
     * Popup a window showing invalid move information
     * @param errorMsg error string stating why this move is invalid
     */
    private void showInvalidMoveMsg(String errorMsg){
        //TODO
    }

    /**
     * Before actually quit to main menu, popup a alert window to double check
     * Hint:
     *      - title: Confirm
     *      - HeaderText: Return to menu?
     *      - ContentText: Game progress will be lost.
     *      - Buttons: CANCEL and OK
     *  If click OK, then refer to {@link GamePlayPane#doQuitToMenu()}
     *  If click Cancle, than do nothing.
     */
    private void doQuitToMenuAction() {
        // TODO
    }

    /**
     * Update the move to the historyFiled
     * @param move the last move that has been made
     */
    private void updateHistoryField(Move move){
        //TODO
    }

    /**
     * Go back to main menu
     * Hint: before quit, you need to end the game
     */
    private void doQuitToMenu() {
        // TODO
    }

    /**
     * Converting a vertical or horizontal coordinate x to the coordinate in board
     * Hint:
     *      The pixel size of every piece is defined in {@link ViewConfig#PIECE_SIZE}
     * @param x coordinate of mouse click
     * @return the coordinate on board
     */
    private int toBoardCoordinate(double x){
        // TODO
        return 0;
    }

    /**
     * Handler of ending a game
     * Hint:
     *      - Clear the board, history text field
     *      - Reset buttons
     *      - Reset timer
     *
     */
    private void endGame() {
        //TODO
    }
}
