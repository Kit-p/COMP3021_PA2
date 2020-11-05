package castle.comp3021.assignment.protocol.io;


import castle.comp3021.assignment.gui.FXJesonMor;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * This class exports the entire game configuration and procedure to file
 * You need to overwrite .toString method for the class that will be serialized
 * Hint:
 *      - The output folder should be selected in a popup window {@link javafx.stage.FileChooser}
 *      - Read file with {@link java.io.BufferedWriter}
 */
public class Serializer {
    @NotNull
    private static final Serializer INSTANCE = new Serializer();

    /**
     * @return Singleton instance of this class.
     */
    @NotNull
    public static Serializer getInstance() {
        return INSTANCE;
    }


    /**
     * Save a {@link castle.comp3021.assignment.textversion.JesonMor} to file.
     * @param fxJesonMor a fxJesonMor instance under export
     * @throws IOException if an I/O exception has occurred.
     */
    public void saveToFile(FXJesonMor fxJesonMor) throws IOException {
        //TODO
        FileChooser fileChooser = new FileChooser();
        var mapFilter = new FileChooser.ExtensionFilter("Move Record File", "*.map");
        fileChooser.setSelectedExtensionFilter(mapFilter);
        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            writeToFile(file, fxJesonMor.toString());
        }
    }

    /**
     * Helper method for writing content to a file.
     * @param file the file to be written to
     * @param content the content to be written to
     */
    private static void writeToFile(File file, String content) throws IOException {
        boolean isSuccess;
        if (file.exists()) {
            isSuccess = file.delete();
            if (!isSuccess) {
                throw new IOException("Cannot delete existing file!");
            }
        }
        isSuccess = file.createNewFile();
        if (!isSuccess) {
            throw new IOException("Cannot create new file!");
        }
        try {
            PrintWriter writer = new PrintWriter(file);
            writer.print(content);
        } catch (FileNotFoundException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Cannot save move record to file!");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

}
