import javafx.scene.control.Label
import javafx.scene.control.Tooltip
import javafx.geometry.Bounds
import javafx.scene.shape.Circle
import javafx.scene.layout.StackPane
import javafx.scene.layout.Region

class InfoIcon extends StackPane {
        InfoIcon(String tooltipText) {
        // Create the "ℹ" Label
        Label infoLabel = new Label('\u2139') // ℹ Unicode Info Symbol
        infoLabel.style = "-fx-font-size: 12; -fx-text-fill: #3498db; -fx-alignment: center;"
        infoLabel.setMinSize(8, 8)  // Ensure Label size is adequate for text

        // Create the Circle around the Label
        Circle circle = new Circle(8)  // radius of the circle
        circle.setStyle("-fx-fill: transparent; -fx-stroke: #3498db; -fx-cursor: hand; -fx-stroke-width: 2;")

        // Add the Circle and the Label to the StackPane
        this.getChildren().addAll(circle, infoLabel)
        circle.toFront()  // Make sure the circle is in front of the label

        // Set the StackPane's size explicitly to match the Circle size
        this.setMinSize(8, 8)  // Ensure the StackPane size matches the Circle diameter

        // Create Tooltip
        Tooltip tooltip = new Tooltip(tooltipText)
        tooltip.setStyle("-fx-font-size: 14px;")  

        // Set hover events for the Circle
        circle.setOnMouseEntered {
            Bounds bounds = circle.localToScreen(circle.boundsInLocal)
            tooltip.show(circle, bounds.getMaxX() + 10, bounds.getMinY())
        }

        circle.setOnMouseExited {
            tooltip.hide()
        }
    }
}
