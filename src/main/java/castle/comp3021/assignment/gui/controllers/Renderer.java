package castle.comp3021.assignment.gui.controllers;

import castle.comp3021.assignment.gui.ViewConfig;
import castle.comp3021.assignment.protocol.Piece;
import castle.comp3021.assignment.protocol.Place;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import org.jetbrains.annotations.NotNull;


/**
 * This class render images
 *  - All image resources can be found in main/resources/assets/images folder.
 *  - The size of piece is defined in gui/ViewConfig
 * Helper class for render operations on a {@link Canvas}.
 * Hint:
 * Necessary functions:
 * - Render chess pieces with different kinds and colors
 * - Render chess board
 *     - There are two kinds of chess board image: lightBoard.png and darkBoard.png.
 *     - They should take turn to appear
 * - Highlight the selected board (can be implemented with rectangle)
 * - Highlight the path when mouse moves (can be implemented with oval with a small radius
 */
public class Renderer {
    /**
     * An image of a cell, with support for rotated images.
     */
    public static class CellImage {

        /**
         * Image of the cell.
         */
        @NotNull
        final Image image;
        /**
         * @param image    Image of the cell.
         */
        public CellImage(@NotNull Image image) {
            this.image = image;
        }
    }

    /**
     * Draws a rotated image onto a {@link GraphicsContext}.
     * The radius = 12
     * Color = rgb(255, 255, 220)
     * @param gc    Target Graphics Context.
     * @param x     X-coordinate relative to the graphics context to draw the oval.
     * @param y     Y-coordinate relative to the graphics context to draw the oval.
     */
    public static void drawOval(@NotNull GraphicsContext gc, double x, double y) {
        // TODO
        gc.save();
        gc.setFill(Color.rgb(255, 255, 220));
        gc.fillOval(x, y, 12, 12);
        gc.fill();
        gc.restore();
    }

    /**
     * Draw a rectangle to show mouse dragging path
     * The width and height are set to be PIECE_SIZE in {@link castle.comp3021.assignment.gui.ViewConfig}
     * @param gc the graphicsContext of canvas
     * @param x X-coordinate relative to the graphics context to draw the rectangle.
     * @param y Y-coordinate relative to the graphics context to draw the rectangle.
     */
    public static void drawRectangle(@NotNull GraphicsContext gc, double x, double y){
        //TODO
        gc.save();
        gc.setFill(Color.rgb(255, 255, 220));
        int size = ViewConfig.PIECE_SIZE;
        gc.fillRect(x * size, y * size, size, size);
        gc.fill();
        gc.restore();
    }

    /**
     * Render chess board
     *     - There are two kinds of chess board image: lightBoard.png and darkBoard.png.
     *     - They should take turn to appear
     * @param canvas given canvas
     * @param boardSize the size of board
     * @param centerPlace the central place
     */
    public static void renderChessBoard(@NotNull Canvas canvas, int boardSize, Place centerPlace){
        //TODO
        GraphicsContext gc = canvas.getGraphicsContext2D();
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                char boardCellColor = ((i + j) % 2 == 0) ? 'l' : 'd';
                Image boardCellImage = ResourceLoader.getImage(boardCellColor);
                double x = i * ViewConfig.PIECE_SIZE;
                double y = j * ViewConfig.PIECE_SIZE;
                drawImage(gc, boardCellImage, x, y);
            }
        }
        Image centralPlaceImage = ResourceLoader.getImage('c');
        double x = centerPlace.x() * ViewConfig.PIECE_SIZE;
        double y = centerPlace.y() * ViewConfig.PIECE_SIZE;
        drawImage(gc, centralPlaceImage, x, y);
    }

    /**
     * Render pieces on the chess board
     * @param canvas given canvas
     * @param board board with pieces
     */
    public static void renderPieces(@NotNull Canvas canvas, @NotNull Piece[][] board) {
        //TODO
        GraphicsContext gc = canvas.getGraphicsContext2D();
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                Piece piece = board[i][j];
                if (piece != null) {
                    CellImage cellImage = piece.getImageRep();
                    double x = i * ViewConfig.PIECE_SIZE;
                    double y = j * ViewConfig.PIECE_SIZE;
                    drawImage(gc, cellImage.image, x, y);
                }
            }
        }
    }

    /**
     * Helper method to draw image on canvas
     * @param gc the graphics context of the canvas
     * @param image the image to be drawn
     * @param x x-coordinate relative to gc
     * @param y y-coordinate relative to gc
     */
    private static void drawImage(@NotNull GraphicsContext gc, Image image, double x, double y) {
        gc.save();
        gc.drawImage(image, x, y);
        gc.restore();
    }

}
