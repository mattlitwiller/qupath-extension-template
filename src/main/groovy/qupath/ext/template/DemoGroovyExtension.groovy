package qupath.ext.template;

import javafx.scene.control.MenuItem;
import qupath.lib.common.Version;
import qupath.lib.gui.QuPathGUI;
import qupath.fx.dialogs.Dialogs;
import qupath.lib.gui.extensions.QuPathExtension;

import javafx.concurrent.Task
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.scene.Scene
import javafx.stage.Stage
import javafx.geometry.Insets
import javafx.geometry.Pos
import java.io.File
import javafx.stage.DirectoryChooser
import javafx.stage.FileChooser;



/**
 * This is a demo to provide a template for creating a new QuPath extension in Groovy.
 * <p>
 * <b>Important!</b> For your extension to work in QuPath, you need to make sure the name & package
 * of this class is consistent with the file
 * <pre>
 *     /resources/META-INF/services/qupath.lib.gui.extensions.QuPathExtension
 * </pre>
 */
class DemoGroovyExtension implements QuPathExtension {

	// Setting the variables here is enough for them to be available in the extension
	String name = "Ki-67 Assessment"
	String description = "This extension assesses the quality of Ki-67 staining"
	Version QuPathVersion = Version.parse("v0.4.0")


	// Paths
	// def path_file_path = "D:/Qupath/v0.6.0-rc1/saved_paths.txt"
	def path_file_path = ""
	def persisted_path_file = false
	def prediction_dir = ""
	def qupath_cli = ""
	def groovy_dir = ""
	def python_dir = ""

	// def prediction_dir = ""	// will be set later once we run for a project
	// def qupath_cli = qupath_dir + "/v0.6.0-rc1/QuPath-0.6.0-rc1 (console).exe"
	// def groovy_dir = "D:/Qupath-files/groovy_scripts"
	// def python_dir = "D:/Qupath-files/python_scripts"

	//default values
	String defaultDownsample = "8"
	String defaultHematoxylinVector = "0.636, 0.715, 0.289"
	String defaultDabVector = "0.438, 0.65, 0.621"
	String defaultBackgroundVector = "233, 232, 240"
	boolean defaultLzCheckbox = true
	boolean defaultDzCheckbox = true
	boolean defaultGcCheckbox = true
	boolean defaultMCheckbox = false
	String defaultDabThreshold = "0.15"
	String defaultLzThreshold = "55" 
	String defaultDzThreshold = "85"
	String defaultGcThreshold = "70"
	String defaultBlurrinessThreshold = "5"

	//key value pair of step string value and groovy script name
	def commandMap = [
		"WSItoJpg": "Yolo Prediction",
		"env_setup": "Python Environment Setup",
		"yolo_predictions": "Yolo Prediction",
		"import_geojson_annotations" : "Import Predictions",
		"set_manual_stain_vectors": "Set Stain Vectors",
		"create_roi_from_yolo": "Clean Detections",
		"filter_annotations_ROI_only": "Clean Detections",
		"clean_detections": "Clean Detections",
		"instanseg": "Nuclei Segmentation",
		"cell_classification": "Cell Classification",
		"description_output_single": "Analysis Output"
	]

	@Override
	void installExtension(QuPathGUI qupath) {
		addMenuItem(qupath)
	}

	private String[] setPathsFromFile(String file) throws Exception {
		def path_file = new File(file)
		try{
			if (path_file.exists()) {
				def lines = path_file.readLines()
				println "File read successfully! Lines: "
				qupath_cli = lines[0]
				groovy_dir = lines[1]
				python_dir = lines[2]
				println lines
				persisted_path_file = true
				return lines
			} else {
				println "Error: File does not exist at ${path_file_path}"
			}	
		}catch(Exception e){
			pathSuccess = false
			Dialogs.showErrorMessage("Error", "Problematic file/directory paths");
			return 
		}
	}

	private void fillPathTextFields(pathTexts, String[] paths){
		for(int i=0; i< paths.length; i++){
		    pathTexts[i].setText(paths[i]);	
		}
	}
	

	private void pipeline(pipeline_calls, project_dir, project_path) {

		println "before dirs"
		println project_dir
		println project_path
		// def project_dir = qupath_dir + "/" + project
		// def qpproj_dir = project_dir + "/project.qpproj"

		println "after dirs"
		
		for (call in pipeline_calls) {	
			println "call loop"
			// Construct the command
			def command = [
				qupath_cli.toString(), 
				"script", 
				groovy_dir.toString() + "/" + call[0] + ".groovy", 
				"-p", project_path.toString(), 
				"-s"
			]

			println command
			
			// add arguments to command
			if(call[1] != null){
				command.add(3, "-a")
				command.add(4, call[1])
			}

			println("command: ")
			for (c in command){
				println c 
			}

			// Execute the command
			if(call[2].equals("true")) {

				def exitCode = 0
				//the only python script we will need
				if(call[0].equals("env_setup")){
					if(call[1] == null){
						throw new Exception("No model directory set for yolo predictions")
					}else{
						def args = call[1].split(";")
						command = "python -u " + python_dir.toString() + "/env_setup.py " + args[0] + " " + project_dir.toString() + " " + python_dir.toString() + " " + args[1].toDouble() + " " + args[2]
						println command
						def process = command.execute()

						// Capture standard output and error streams
						process.inputStream.eachLine { println it}
						process.errorStream.eachLine { line ->
							println "Error: $line"
						}

						exitCode = process.waitFor()
					}
				}else{
					println command
					def process = new ProcessBuilder(command)
						.redirectErrorStream(true)
						.start()

					// Read and print output
					process.inputStream.eachLine { println it }

					// Wait for the process to complete
					exitCode = process.waitFor()
				}
				if (exitCode !== 0){
					println "Something went wrong in step '" + commandMap.get(call[0]) + "' (groovy script '"+ call[0] + "')."
					throw new RuntimeException("Something went wrong in step '" + commandMap.get(call[0]) + "' (groovy script '"+ call[0] + "').")
				}
			}
		}
	}

		private void addMenuItem(QuPathGUI qupath) {
		def menu = qupath.getMenu("Extensions>${name}", true)
		def menuItem = new MenuItem("Run assessment")
		def spacing = 10

		menuItem.setOnAction(e -> {
				// Create a new stage (dialog box)
				Stage dialog = new Stage()
				dialog.setTitle("Ki-67 Assessment Settings")
				dialog.initOwner(QuPathGUI.getInstance().getStage()) // Ensure it behaves as a child of QuPath
				dialog.setResizable(true) // Prevent resizing  
							
				// Main layout for the dialog
				VBox mainLayout = new VBox(spacing)
				mainLayout.setPadding(new Insets(15))

				Label setupLabel = new Label("Ensure the following directories & file paths are set")
				HBox cli_hbox = PathChooserHelper.createFileChooserBox("QuPath Executable Location", "QP .exe file","", spacing, dialog, new FileChooser.ExtensionFilter("Executable Files", "*.exe"), false)
				HBox groovy_hbox = PathChooserHelper.createDirectoryChooserBox("Groovy Script Directory", "groovy script directory" ,"", spacing, dialog)
				HBox python_hbox = PathChooserHelper.createDirectoryChooserBox("Python Script Directory", "python script directory", "", spacing, dialog)
				Label alternativeSetupLabel = new Label("Or import paths from file")
				HBox path_hbox = PathChooserHelper.createFileChooserBox("File Path Location", "path file","", spacing, dialog, new FileChooser.ExtensionFilter("Text Files", "*.txt"), true)

				path_hbox.getChildren().get(0).textProperty().addListener((observable, oldValue, newValue) -> {
					System.out.println("path hbox value changed from " + oldValue + " to " + newValue);
					String[] paths = setPathsFromFile(newValue)
					
					// fill text fields
					cli_hbox.getChildren().get(0).setText(paths[0])
					groovy_hbox.getChildren().get(0).setText(paths[1])
					python_hbox.getChildren().get(0).setText(paths[2])

						
					// fillPathTextFields(textFields, paths)
				})

				path_hbox.getChildren().get(2).setOnAction(ev -> {
					def save_loc = path_hbox.getChildren().get(0).getText()
					if(save_loc != ""){
						def temp_qpcli = null
						def temp_groovydir = null
						def temp_pythondir = null

						if(cli_hbox.getChildren().get(0).getText() != "") temp_qpcli = cli_hbox.getChildren().get(0).getText()
						if(groovy_hbox.getChildren().get(0).getText() != "") temp_groovydir = groovy_hbox.getChildren().get(0).getText()
						if(python_hbox.getChildren().get(0).getText() != "") temp_pythondir = python_hbox.getChildren().get(0).getText()
						
						if(temp_qpcli != null && temp_groovydir != null && temp_pythondir != null) {
							def success = SaveTextFile.savePathFile([temp_qpcli, temp_groovydir, temp_pythondir])
							if(success) Dialogs.showMessageDialog("Success", "File & directory paths saved to " + save_loc)
						}
					}else{
						Dialogs.showErrorMessage("Error", "Ensure all paths are set");
						return 
					}
				})

				VBox pathSection = new VBox(spacing)
				pathSection.getChildren().addAll(setupLabel, cli_hbox, groovy_hbox, python_hbox, alternativeSetupLabel, path_hbox)
				TitledPane pathPane = new TitledPane("Extension Setup", pathSection)
				pathPane.setExpanded(false)


				// Yolo prediction
				VBox yoloPredictionSection = new VBox(spacing)
				Label downsampleLabel = new Label("Image Downsample Value:")
				InfoIcon downsampleInfo = new InfoIcon("Downsample of 1 maintains image quality but will be computationally intensive or infeasible")
				HBox downsampleLabelHbox = new HBox(spacing)
				downsampleLabelHbox.getChildren().addAll(downsampleLabel, downsampleInfo)
				TextField downsampleField = new TextField(defaultDownsample)
				HBox downsampleHbox = new HBox(spacing)
				boolean validFieldDownsample = true
				Label downsampleMessageLabel = new Label("")
				downsampleField.textProperty().addListener { _, oldVal, newVal -> validFieldDownsample = ValidateInputs.validateInt(newVal, downsampleMessageLabel)}
				downsampleHbox.getChildren().addAll(downsampleField, downsampleMessageLabel)
				downsampleField.setPromptText("Downsample (default: " + defaultDownsample + ")")

				CheckBox convertToJpg = new CheckBox("Convert WSI to JPG")
				convertToJpg.setSelected(false)

				CheckBox yoloPredictions = new CheckBox("Perform YOLO Predictions")
				yoloPredictions.setSelected(false)

				Label modelDirLabel = new Label("Model Directory:")
				TextField modelDirField = new TextField()
				
				String model_dir = ""
				HBox model_hbox = PathChooserHelper.createDirectoryChooserBox("YOLO Model location", "","", spacing, dialog)
				model_hbox.getChildren().get(0).textProperty().addListener((observable, oldValue, newValue) -> {
					System.out.println("Model directory changed from " + oldValue + " to " + newValue);
					model_dir = newValue
				})

				Label requirementsLabel = new Label("Import Python environment requirements file:")
				InfoIcon requirementsInfo = new InfoIcon("Requires conda installation to setup python environment for YOLO predictions")
				HBox requirementsLabelHbox = new HBox(spacing)
				requirementsLabelHbox.getChildren().addAll(requirementsLabel, requirementsInfo)
				String requirements_path = ""
				HBox requirements_hbox = PathChooserHelper.createFileChooserBox("Anaconda dependency file location", "requirements.txt file","", spacing, dialog, new FileChooser.ExtensionFilter("Text Files", "*.txt"), false)
				requirements_hbox.getChildren().get(0).textProperty().addListener((observable, oldValue, newValue) -> {
					System.out.println("requirements.txt value changed from " + oldValue + " to " + newValue);
					requirements_path = newValue
				})

				CheckBox importPredictions = new CheckBox("Import YOLO Predictions")
				importPredictions.setSelected(false)	

				yoloPredictionSection.getChildren().addAll(downsampleLabelHbox, downsampleHbox, convertToJpg, yoloPredictions, modelDirLabel, model_hbox, requirementsLabelHbox, requirements_hbox, importPredictions)
				TitledPane yoloPredictionPane = new TitledPane("Produding YOLO Predictions", yoloPredictionSection)
				yoloPredictionPane.setExpanded(false)


				// Stain vector section
				def vectorWidth = 250
				def validInputs = true
				VBox stainSection = new VBox(spacing)
				CheckBox setVectors = new CheckBox("Set Stain Vector")
				setVectors.setSelected(false)	
				Label stainVectorLabel = new Label("Stain Vectors & Background (ensure comma-separated values)")
				TextField hematoxylinVectorField = new TextField(defaultHematoxylinVector)
				Label hematoxylinVectorLabel = new Label("")
				hematoxylinVectorField.setPrefWidth(vectorWidth)
				hematoxylinVectorField.setPromptText("Hematoxylin (default: " + defaultHematoxylinVector + ")")
				boolean validFieldHematoxylin = true 
				hematoxylinVectorField.textProperty().addListener { _, oldVal, newVal -> validFieldHematoxylin = ValidateInputs.validateNormalizedInput(newVal, hematoxylinVectorLabel)}
				HBox hematoxylinVectorHbox = new HBox(spacing)
				hematoxylinVectorHbox.getChildren().addAll(hematoxylinVectorField, hematoxylinVectorLabel)
				TextField dabVectorField = new TextField(defaultDabVector)
				Label dabVectorLabel = new Label("")
				dabVectorField.setPromptText("DAB (default: " + defaultDabVector + ")")
				dabVectorField.setPrefWidth(vectorWidth)
				boolean validFieldDab = true 
        		dabVectorField.textProperty().addListener { _, oldVal, newVal -> validFieldDab = ValidateInputs.validateNormalizedInput(newVal, dabVectorLabel)}
				HBox dabVectorHbox = new HBox(spacing)
				dabVectorHbox.getChildren().addAll(dabVectorField, dabVectorLabel)
				TextField backgroundVectorField = new TextField(defaultBackgroundVector)
				Label backgroundVectorLabel = new Label("")
				backgroundVectorField.setPrefWidth(vectorWidth)
				backgroundVectorField.setPromptText("Background (default: " + defaultBackgroundVector + ")")
				boolean validFieldBackground = true 
				backgroundVectorField.textProperty().addListener { _, oldVal, newVal -> validFieldBackground = ValidateInputs.validateTupleIntegers(newVal, backgroundVectorLabel)}
				HBox backgroundVectorHbox = new HBox(spacing)
				backgroundVectorHbox.getChildren().addAll(backgroundVectorField, backgroundVectorLabel)
				stainSection.getChildren().addAll(setVectors, stainVectorLabel, hematoxylinVectorHbox, dabVectorHbox, backgroundVectorHbox)
				TitledPane stainPane = new TitledPane("Stain Vectors", stainSection)
				stainPane.setExpanded(false)


				// Clean annotations 
				CheckBox cleanAnnotations = new CheckBox("Clean YOLO predictions")
				cleanAnnotations.setSelected(false)		
				InfoIcon cleanAnnotationsInfo = new InfoIcon(
"""Cleans YOLO predictions (annotations) by:
- Filtering predictions to keep only predictions within tonsil and appendix regions
- Combining predictions
- Filling holes in predictions
- Ensuring dark and light zones do not overlap
- Removing small predictions
- Ensuring LZ/DZ exist only within GC
- Ensuring no mantle region overlaps with a GC""")
				HBox cleanAnnotationsHbox = new HBox(spacing)
				cleanAnnotationsHbox.getChildren().addAll(cleanAnnotations, cleanAnnotationsInfo)
				TitledPane cleanAnnotationPane = new TitledPane("Post-Processing", cleanAnnotationsHbox)
				cleanAnnotationPane.setExpanded(false)
				

				// InstanSeg regions to perform detections
				VBox instansegSection = new VBox(spacing)
				HBox instansegRow1 = new HBox(spacing)
				HBox instansegRow2 = new HBox(spacing)
				CheckBox runInstanseg = new CheckBox("Run InstanSeg")
				runInstanseg.setSelected(false)	
				InfoIcon instansegInfo = new InfoIcon("Requires Instanseg extension to be configured. Will run on GPU if enabled, otherwise CPU")
				HBox instansegHbox = new HBox(spacing)
				instansegHbox.getChildren().addAll(runInstanseg, instansegInfo)
				Label instansegRegionsLabel = new Label("InstanSeg segmentation in the following regions:")
				CheckBox lzCheckbox = new CheckBox("Light Zones")
				lzCheckbox.setSelected(defaultLzCheckbox)
				CheckBox dzCheckbox = new CheckBox("Dark Zones")
				dzCheckbox.setSelected(defaultDzCheckbox)
				CheckBox gcCheckbox = new CheckBox("Germinal Centers")
				gcCheckbox.setSelected(defaultGcCheckbox)
				CheckBox mCheckbox = new CheckBox("Mantle")
				mCheckbox.setSelected(defaultMCheckbox)
				instansegRow1.getChildren().addAll(lzCheckbox, dzCheckbox)
				instansegRow2.getChildren().addAll(gcCheckbox, mCheckbox)
				instansegSection.getChildren().addAll(instansegHbox, instansegRegionsLabel, instansegRow1, instansegRow2)
				TitledPane instansegPane = new TitledPane("InstanSeg", instansegSection)
				instansegPane.setExpanded(false)

				// Cell classification section
				VBox classificationSection = new VBox(spacing)
				HBox thresholdHbox = new HBox(spacing)
				CheckBox classifyCells = new CheckBox("Classify Cells")
				classifyCells.setSelected(false)	
				Label dabThresholdLabel = new Label("DAB Positivity Threshold:")
				TextField dabThresholdField = new TextField(defaultDabThreshold)
				Label dabThresholdMessage = new Label("")
				dabThresholdField.setPromptText("Threshold (default: " + defaultDabThreshold + ")")
				dabThresholdField.textProperty().addListener { _, oldVal, newVal -> validFieldBackground = ValidateInputs.validateNumber(newVal, dabThresholdMessage)}
				thresholdHbox.getChildren().addAll(dabThresholdField, dabThresholdMessage)
				classificationSection.getChildren().addAll(classifyCells, dabThresholdLabel, thresholdHbox)
				TitledPane classificationPane = new TitledPane("Cell Classification", classificationSection)
				classificationPane.setExpanded(false)


				// Slide Flagging section
				VBox outputSection = new VBox(spacing)
				CheckBox descriptionOutput = new CheckBox("Output Results")
				descriptionOutput.setSelected(false)
				Label positivityThresholdLabel = new Label("Set Minimum Positivity of Regions (LZ, GC, DZ):")
							
				HBox lzMinHbox = new HBox(spacing)
				TextField lzField = new TextField(defaultLzThreshold)				
				Label lzLabel = new Label("")
				lzField.setPromptText("LZ threshold (default: " + defaultLzThreshold + ")")
				lzField.textProperty().addListener { _, oldVal, newVal -> validFieldBackground = ValidateInputs.validateNumber(newVal, lzLabel)}
				lzMinHbox.getChildren().addAll(lzField, lzLabel)

				HBox gcMinHbox = new HBox(spacing)
				TextField gcField = new TextField(defaultGcThreshold)
				Label gcLabel = new Label("")
				gcField.setPromptText("GC threshold (default: " + defaultGcThreshold + ")")
				gcField.textProperty().addListener { _, oldVal, newVal -> validFieldBackground = ValidateInputs.validateNumber(newVal, gcLabel)}
				gcMinHbox.getChildren().addAll(gcField, gcLabel)

				HBox dzMinHbox = new HBox(spacing)
				TextField dzField = new TextField(defaultDzThreshold)
				Label dzLabel = new Label("")
				dzField.setPromptText("DZ threshold (default: " + defaultDzThreshold + ")")
				dzField.textProperty().addListener { _, oldVal, newVal -> validFieldBackground = ValidateInputs.validateNumber(newVal, dzLabel)}
				dzMinHbox.getChildren().addAll(dzField, dzLabel)
				
				HBox blurrinessLabelHbox = new HBox(spacing)
				HBox blurrinessHbox = new HBox(spacing)
				Label blurrinessLabel = new Label("Blurriness Threshold:")
				Label blurrinessMessage = new Label("")
				TextField blurrinessField = new TextField(defaultBlurrinessThreshold)
				InfoIcon blurrinessInfo = new InfoIcon("Low blurriness indicates low detection density and likely presence of blurred image")
				blurrinessField.setPromptText("Threshold (default: " + defaultBlurrinessThreshold + ")")
				blurrinessField.textProperty().addListener { _, oldVal, newVal -> validFieldBackground = ValidateInputs.validateNumber(newVal, blurrinessMessage)}
				blurrinessLabelHbox.getChildren().addAll(blurrinessLabel, blurrinessInfo)
				blurrinessHbox.getChildren().addAll(blurrinessField, blurrinessMessage)

				outputSection.getChildren().addAll(descriptionOutput, positivityThresholdLabel, lzMinHbox, gcMinHbox, dzMinHbox, blurrinessLabelHbox, blurrinessHbox)
				TitledPane outputPane = new TitledPane("Results", outputSection)
				outputPane.setExpanded(false)
				

				//Pipeline modularity
				HBox modularitySection = new HBox(spacing)
				Label pipelineModularityLabel = new Label("Pipeline modularity")
				InfoIcon modularityInfo = new InfoIcon(
"""- The complete pipeline execution order is laid out with default values
- Pipeline steps can be toggled for intermediate results
- The only step that depends on stain vectors is InstanSeg""")
				modularitySection.getChildren().addAll(pipelineModularityLabel, modularityInfo)

				mainLayout.getChildren().addAll(
					pathPane,
					new Separator(),
					modularitySection,
					yoloPredictionPane, 
					cleanAnnotationPane, 
					stainPane, 
					instansegPane,
					classificationPane,
					outputPane
				)

				mainLayout.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE) // Auto-size

				// Add a submit button
				Button submitButton = new Button("Submit")
				submitButton.setOnAction(ev -> {
					// Retrieve and log user inputs
					def pathSuccess = true	

					try{
						qupath_cli = cli_hbox.getChildren().get(0).getText()
						groovy_dir = groovy_hbox.getChildren().get(0).getText()
						python_dir = python_hbox.getChildren().get(0).getText()
					}catch(Exception exc){
						pathSuccess = false
						Dialogs.showErrorMessage("Error", "Problematic file/directory paths. Configure in Extension Setup pane.");
						return 
					}

					if(qupath_cli == "" || groovy_dir == "" || python_dir == "") {
						Dialogs.showErrorMessage("Error", "Problematic file/directory paths. Configure in Extension Setup pane.");
						return 	
					}
						
					String downsample = downsampleField.getText()
					String hematoxylinVector = hematoxylinVectorField.getText()
					String dabVector = dabVectorField.getText()
					String backgroundVector = backgroundVectorField.getText()
    				boolean lzSegmentation = lzCheckbox.isSelected()    				
					boolean dzSegmentation = dzCheckbox.isSelected()
    				boolean gcSegmentation = gcCheckbox.isSelected()
    				boolean mSegmentation = mCheckbox.isSelected()
					String dabThreshold = dabThresholdField.getText()
					String dzThreshold = dzField.getText()
					String lzThreshold = lzField.getText()
					String gcThreshold = gcField.getText()
					String blurrinessThreshold = blurrinessField.getText()

					boolean boolConvertToJpg = convertToJpg.isSelected()
					boolean boolYoloPredictions = yoloPredictions.isSelected()    				
					boolean boolImportGeojson = importPredictions.isSelected()    				
					boolean boolSetVectors = setVectors.isSelected()    				
					boolean boolCleanDetections = cleanDetections.isSelected()    				
					boolean boolRunInstanseg = runInstanseg.isSelected()    				
					boolean boolClassifyCells = classifyCells.isSelected()   
					boolean boolDescriptionOutput = descriptionOutput.isSelected() 	

					List<CheckBox> checkboxes = [convertToJpg, yoloPredictions, importPredictions, setVectors, cleanDetections, runInstanseg, classifyCells, descriptionOutput]

					//Form validation stain vector
					if((boolConvertToJpg || boolYoloPredictions) && !validFieldDownsample) {
						Dialogs.showErrorMessage("Error", "Form validation failed for Producing YOLO predictions (downsample)");
						return				
					}

					if(boolSetVectors && (!validFieldHematoxylin || !validFieldDab || !validFieldBackground)) {
						Dialogs.showErrorMessage("Error", "Form validation failed for stain vectors");
						return
					}



					// Project Information
					def proj = qupath.getProject()
					println proj

					if(proj == null){
						Dialogs.showErrorMessage("Error", "No project found. Ensure a project is open");
						return
					}				
					if(!checkboxes.any { it.selected }) {
						Dialogs.showMessageDialog("Error", "No computations have been performed. Select at least one step in the pipeline.");
						return;
					}

					def proj_path = proj.getPath()
					println proj_path

					def proj_dir = new File(proj_path.toString()).getParent()
					println proj_dir

					def proj_name = proj.getName().split("/")[0] //Without splitting we would get proj_name/project.qpproj
					println(proj_name)
					prediction_dir = proj_dir + "/output"	
					// prediction_dir = qupath_dir + "/" + project + "/output"

					if(boolYoloPredictions && model_dir.equals("")){
						Dialogs.showMessageDialog("Error", "No model directory selected for YOLO predictions. Set model directory or uncheck YOLO predictions step.")
						return
					}

					if(boolYoloPredictions && requirements_path.equals("")){
						Dialogs.showMessageDialog("Error", "No requirements file selected for conda environment. Set requirements file or uncheck YOLO predictions step.")
						return
					}

					// Log other inputs
					println "Stain Vectors: $hematoxylinVector; $dabVector; $backgroundVector"
					println "Segmentation regions: $lzSegmentation; $dzSegmentation; $gcSegmentation, $mSegmentation"
					println "DAB Positivity Threshold: $dabThreshold"
					println "Positivity Thresholds - DZ: $dzThreshold, LZ: $lzThreshold, GC: $gcThreshold"
					println "Blurriness Threshold: $blurrinessThreshold"

					String stainVector = "dummy value"

					// Define the task for the computation
					Task computationTask = new Task() {
						@Override
						protected Void call() throws Exception {
							try{
								println "setting up pipeline"
								def pipeline_calls = [
									["WSItoJpg", downsample, boolConvertToJpg.toString()],
									["env_setup", model_dir + ";" + downsample + ";" + requirements_path, boolYoloPredictions.toString()], //for only this script (since it's python and not groovy), set the parameters differently
									["import_geojson_annotations", prediction_dir, boolImportGeojson.toString()], 
									["set_manual_stain_vectors", hematoxylinVector + ";" + dabVector + ";" + backgroundVector, boolSetVectors.toString()], 
									["create_roi_from_yolo", null, boolCleanDetections.toString()], 
									["filter_annotations_ROI_only", null, boolCleanDetections.toString()], 
									["clean_detections", null, boolCleanDetections.toString()], 
									["instanseg", lzSegmentation.toString() + ";" + dzSegmentation.toString() + ";" + gcSegmentation.toString() + ";" + mSegmentation.toString(), boolRunInstanseg.toString()], 
									["cell_classification", dabThreshold, boolClassifyCells.toString()],
									["description_output_single", dzThreshold + ";" + lzThreshold + ";" + gcThreshold, boolDescriptionOutput.toString()]
								]
								pipeline(pipeline_calls, proj_dir, proj_path)
								updateMessage("Progress: Completed!")
							} catch (Exception ex) {
								ex.printStackTrace(); // Print the error to debug
								updateMessage("Error occurred")
								throw ex
							}
							return null
						}
					}
										
					// Handle task completion
					computationTask.setOnSucceeded(taskEv -> {
						Dialogs.showMessageDialog("Computation Complete", "All steps completed successfully!")
					})
					
					computationTask.setOnFailed(taskEv -> {
						println "=====ComputationTask encountered an error that was caught!====="
						Throwable error = computationTask.getException(); // Get the exception
						String errorMessage = (error != null) ? error.getMessage() : "Unknown error";
    					Dialogs.showErrorMessage("Error - See console", errorMessage);
					})
					
					// Start the computation in a background thread
					new Thread(computationTask).start()
				})
				
				mainLayout.getChildren().add(submitButton)
				mainLayout.setAlignment(Pos.CENTER)

				// Set up the scene and show the dialog
				ScrollPane scrollPane = new ScrollPane(mainLayout)
				scrollPane.setFitToWidth(true)
				scrollPane.setPrefHeight(600)
				scrollPane.setPrefWidth(550)

				Scene dialogScene = new Scene(scrollPane)
				dialog.setScene(dialogScene)
				dialog.sizeToScene(); 
				dialog.show()
			})
		menu.getItems() << menuItem
	}
	
}