import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;

public class PathChooserHelper {

    public static HBox createDirectoryChooserBox(String promptText, String dirName, String path, double spacing, Window parentWindow) {
        int width = 300
        HBox hbox = new HBox(spacing);

        TextField dirField = new TextField(path);
        dirField.setPromptText(promptText);
        dirField.setEditable(false); // Prevent user from typing manually
        dirField.setPrefWidth(width);


        Button chooseDirButton = new Button("Set");
        chooseDirButton.setOnAction(ev -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Select Directory");

            // Set an initial directory (optional)
            File initialDir = new File(System.getProperty("user.home"));
            if (initialDir.exists()) {
                directoryChooser.setInitialDirectory(initialDir);
            }

            // Show the directory chooser and get the selected directory
            File selectedDir = directoryChooser.showDialog(parentWindow);
            if (selectedDir != null) {
                dirField.setText(selectedDir.getAbsolutePath());
            }
        });

        hbox.getChildren().addAll(dirField, chooseDirButton);
        return hbox;
    }



    public static HBox createFileChooserBox(String promptText, String fileName, String path, double spacing, Window parentWindow, filter, boolean save) {
        int width = 300
        HBox hbox = new HBox(spacing);

        TextField fileField = new TextField(path);
        fileField.setPromptText(promptText);
        fileField.setEditable(false); // Prevent user from typing manually
        fileField.setPrefWidth(width);


        Button chooseFileButton = new Button("Set");
        chooseFileButton.setOnAction(ev -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select File");

            // Set an initial directory (optional)
            File initialDir = new File(System.getProperty("user.home"));
            if (initialDir.exists()) {
                fileChooser.setInitialDirectory(initialDir);
            }

            // Set file filters (optional)
            fileChooser.getExtensionFilters().add(filter);

            // Show the file chooser and get the selected file
            File selectedFile = fileChooser.showOpenDialog(parentWindow);
            if (selectedFile != null) {
                fileField.setText(selectedFile.getAbsolutePath());
            }
        });

        hbox.getChildren().addAll(fileField, chooseFileButton);

        if(save){
            Button saveButton = new Button("Save")
            hbox.getChildren().add(saveButton);
        }
        return hbox;
    }
}
