import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.VBox
import javafx.stage.Stage
import javafx.util.Duration
import javafx.animation.*

public class ValidateInputs {
    // Function to validate input
    static boolean validateNormalizedInput(inputText, resultLabel) {
        try {
            def vector = inputText.split(',').collect { it.trim().toDouble() }

            def (x, y, z) = vector.collect { it.toDouble() }  
            def magnitude = Math.sqrt(x*x + y*y + z*z)        // Compute magnitude            
            def roundedSum = (magnitude * 100).round() / 100
            println roundedSum

            if (roundedSum == 1.0) {
                resultLabel.setText("✅ Vector is normalized")
                return true
            } else {
                resultLabel.setText("❌ Normalized sum: $roundedSum (not 1)")
                return false
            }
        } catch (Exception e) {
            resultLabel.setText("❌ Invalid input format")
            return false
        }
    }

    static boolean validateTupleIntegers(inputText, resultLabel) {
        try {
            // Split input text into a list of strings and convert to a list of doubles
            def vector = inputText.split(',').collect { it.trim().toDouble() }

            // Check if all values are integers (e.g., no decimal points)
            if (vector.every { it == it.toInteger() }) {
                resultLabel.setText("")
                return true
            } else {
                resultLabel.setText("❌ Not all values are integers.")
                return false
            }
        } catch (Exception e) {
            resultLabel.setText("❌ Invalid input format.")
            return false
        }
    }

    static boolean validateInt(inputText, resultLabel) {
        String text = inputText.trim()
        try {
            Integer.parseInt(text)
            resultLabel.setText("")
            return true
        } catch (NumberFormatException e) {
            resultLabel.setText("❌ Invalid integer.")
            return false
        }
    }

    static boolean validateNumber(inputText, resultLabel){
        String text = inputText.trim();
        try {
            Double.parseDouble(text); // Try parsing it as a double
            resultLabel.setText(""); // Clear any previous error message
            return true;
        } catch (NumberFormatException e) {
            resultLabel.setText("❌ Invalid number.");
            return false;
        }
    }
}

