import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class SaveTextFile {
    static boolean savePathFile(List<String> values) {
        def file = new File("saved_paths.txt")
        
        // Write the array values to the file, each on a new line
        file.withWriter { writer ->
            values.each { value ->
                writer.println(value)
            }
        }
        
        println "Paths saved successfully!"
        return true
    }
}
