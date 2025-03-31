import javafx.scene.control.Dialog
import javafx.scene.control.ProgressIndicator
import javafx.scene.control.Label
import javafx.scene.layout.VBox
import javafx.application.Platform
import javafx.stage.Window
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.stage.Stage
import javafx.stage.Modality
import qupath.lib.gui.QuPathGUI

public class ProgressFeedback {
    Stage stage
    VBox vbox = new VBox(10)
    Label label = new Label()
    ProgressIndicator progressIndicator
    ProgressFeedback() {
        Platform.runLater(() -> {
            stage = new Stage()
            progressIndicator = new ProgressIndicator()
            vbox.getChildren().addAll(label, progressIndicator)
            vbox.setAlignment(Pos.CENTER)
            vbox.setPrefWidth(300)

            Scene progressScene = new Scene(vbox, 300, 150)
            stage.setScene(progressScene)
                
            // Ensure it stays on top
            stage.initModality(Modality.WINDOW_MODAL)
            stage.setAlwaysOnTop(true)

            // Get the QuPath window and set it as owner
            def qupathStage = QuPathGUI.getInstance().getStage()
            if (qupathStage != null) {
                stage.initOwner(qupathStage)
            }
        })
    }

    void startProgress(){
        Platform.runLater(() -> {
            stage.setTitle("Computation Status");
            stage.show();
        })
    }

    void setProgress(String pipeline_step){        
        Platform.runLater(() -> {
            label.setText(pipeline_step)        
            vbox.setMinSize(VBox.USE_PREF_SIZE, VBox.USE_PREF_SIZE);
            vbox.setPrefSize(VBox.USE_COMPUTED_SIZE, VBox.USE_COMPUTED_SIZE);
            vbox.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);       
        })
    }

    void endProgress(){
        stage.close()
    }
}