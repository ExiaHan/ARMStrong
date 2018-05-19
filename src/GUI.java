import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Stage;
import simulator.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.prefs.Preferences;

/**
 * GUI is the class responsible of handling the JaveFX Graphical User Interface.
 */
public class GUI extends Application {

	private ArmSimulator theArmSimulator;

	private Scene scene;
	private Stage stage;

	//the code editor
	private TextArea codingTextArea;
	private TextFlow executionModeTextFlow;
	private AtomicBoolean executionMode;
	private AtomicBoolean running;
	private File programFilePath;


	private GUIMenuBar theGUIMenuBar;
	private GUIMemoryView theGUIMemoryView;
	private GUIRegisterView theGUIRegisterView;
	
	private String lastFilePath;
	
	private Preferences prefs;
	
	private Set<String> themes;
	private static final String DEFAULT_THEME = "red";
	
	private List<Text> instructionsAsText;
	
	public void startGUI() {
		launch(null);
	}

	@Override
	public void start(Stage stage) throws Exception {

		Font.loadFont(getClass().getResource("QuickSand.ttf").toExternalForm(), 10);
		
		theArmSimulator = new ArmSimulator();
		this.programFilePath = null;
        this.lastFilePath = null;

		this.executionMode = new AtomicBoolean(false);
		this.running = new AtomicBoolean(false);

		//setting static elements

		Parent root;

		this.stage = stage;

		root = FXMLLoader.load(getClass().getResource("ihmv4.fxml"));
		scene = new Scene(root, 700, 275);

		stage.setMaximized(true);
		stage.setMinHeight(800);
		stage.setMinWidth(800);
		stage.setTitle("#@RM");
		stage.setScene(scene);

		//Add the CSS file
		/*String css = getClass().getResource("css.css").toExternalForm();
		scene.getStylesheets().addAll(css);*/

		//Change the icon of the application
		Image applicationIcon = new Image("file:logo.png");
		stage.getIcons().add(applicationIcon);
		
		prefs = Preferences.userNodeForPackage(this.getClass());

		stage.show(); //to be sure the scene.lookup() works properly //////////////////////////////////////////////////////////////////////

		themes = new HashSet<>();
		themes.add("red");
		themes.add("blue");
		themes.add("green");

		updateTheme();
		
		boolean quicksandFont = prefs.getBoolean("FONT", true);	
		if(quicksandFont) {
			root.setStyle("-fx-font-family: \"Quicksand\";");
		}else {
			root.setStyle("-fx-font-family: \"\";");
		}	
		
		
		theGUIMenuBar = new GUIMenuBar((MenuBar) scene.lookup("#theMenuBar"));
		theGUIMemoryView = new GUIMemoryView(scene, theArmSimulator);
		theGUIRegisterView = new GUIRegisterView(scene, theArmSimulator);

		//THE CODING AREA
		codingTextArea = (TextArea) scene.lookup("#codeTexArea");
		executionModeTextFlow = (TextFlow) scene.lookup("#executionModeTextFlow");


		//THE ACTION EVENTS
		theGUIMenuBar.getPreferencesMenuItem().setOnAction((ActionEvent actionEvent) -> {
        	final Stage preferencesDialog = new Stage();
            preferencesDialog.initModality(Modality.APPLICATION_MODAL);
            preferencesDialog.initOwner(stage);
            VBox dialogVbox = new VBox(20);
           
            ChoiceBox<String> theme = new ChoiceBox<>();
            
            theme.getItems().addAll(themes);

            theme.setTooltip(new Tooltip("Select a theme"));
            
            theme.setValue(prefs.get("THEME", ""));
            
            theme.setId("choiceboxPreferences");

            Button button1 = new Button("Apply and Close");
            button1.setId("applyAndClosePreferences");
            
            Button button2 = new Button("Apply");
            button2.setId("applyPreferences");
            
            Label labelTheme = new Label();
        	labelTheme.setText("Choose a theme:");
        	labelTheme.setId("labelThemePreferences");
        	
        	Label labelQuicksand = new Label();
        	labelQuicksand.setText("Choose Quicksand font:");
        	labelQuicksand.setId("labelQuicksandPreferences");
         	
         	CheckBox checkBoxFont = new CheckBox();
        	
        	boolean quicksandFontV = prefs.getBoolean("FONT", true);	
    		if(quicksandFontV) {
    			checkBoxFont.setSelected(true);
    		}else {
    			checkBoxFont.setSelected(false);
    		}	
        	
    		Text lineBreak = new Text();
    		lineBreak.setFont(new Font(20));
    		lineBreak.setText("\n");
    		
            button2.setOnAction((ActionEvent e) -> {
            	   String themeChoose =  theme.getValue();
	        		     	        	    
 	        	   if(themeChoose != null) {
     	        	    prefs.put("THEME", themeChoose);
     	        	    updateTheme();
 	        	   }
 	        	    
 	        	   if(checkBoxFont.isSelected()) {
 	        			prefs.putBoolean("FONT", true);
 	        			root.setStyle("-fx-font-family: \"Quicksand\";");	
 	        		   
  	        	   }
  	        	   else{
  	        		   prefs.putBoolean("FONT", false);
  	        		   root.setStyle("-fx-font-family: \"\";");
  	        	   }
 	        	   
        	   
            });
            
            button1.setOnAction((ActionEvent e) -> {
                	String themeChoose =  theme.getValue();
 	        		     	        	    
 	        	    if(themeChoose != null) {
     	        	    prefs.put("THEME", themeChoose);
     	        	    updateTheme();      	    
 	        	    }
 	        	    
 	        	   if(checkBoxFont.isSelected()) {
 	        			prefs.putBoolean("FONT", true);
 	     	        	root.setStyle("-fx-font-family: \"Quicksand\";");
  	        	   }
  	        	   else{
  	        		   prefs.putBoolean("FONT", false);
  	        		   root.setStyle("-fx-font-family: \"\";");
  	        	   }
 	        	   
 	        	   preferencesDialog.close();
            });
            GridPane pane = new GridPane();
            pane.setAlignment(Pos.CENTER);
            pane.setHgap(5);
            pane.setVgap(5);
            pane.setPadding(new Insets(25,25,25,25)); 

            pane.add(labelTheme, 0, 1);
            pane.add(theme, 1, 1);
            
            pane.add(labelQuicksand, 0, 2);
            pane.add(checkBoxFont, 1, 2);
            
            pane.add(lineBreak, 0, 3);
            
            pane.add(button2, 0, 4);
            pane.add(button1, 1, 4);
            
            dialogVbox.getChildren().add(pane);   
            
            Scene preferencesDialogScene = new Scene(dialogVbox, 600, 400);
            preferencesDialog.setScene(preferencesDialogScene);
           
            String cssPreferences = getClass().getResource("css.css").toExternalForm();
            preferencesDialogScene.getStylesheets().addAll(cssPreferences);
    		
            preferencesDialog.setTitle("Preferences");
            Image preferencesIcon = new Image("file:logo.png");
            preferencesDialog.getIcons().add(preferencesIcon);
            
            preferencesDialog.show();
            
		});	

		theGUIMenuBar.getEnterExecutionModeMenuItem().setOnAction((ActionEvent actionEvent) -> {
				String programString = codingTextArea.getText();

				try {
					theArmSimulator.setProgramString(programString);
					enterExecutionMode(programString);
				} catch (AssemblyException e1) {
					System.out.println(e1.toString());
				}
		});
		theGUIMenuBar.getExitExecutionModeMenuItem().setOnAction((ActionEvent actionEvent) -> exitExecutionMode());
		theGUIMenuBar.getRunMenuItem().setOnAction((ActionEvent actionEvent) -> {
				if (executionMode.get() && !(running.get())){
					new Thread(() -> {
						this.running.set(true);
						theArmSimulator.run();
						
						Platform.runLater(() -> {
							theGUIRegisterView.updateRegisters();
							//theGUIMemoryView.updateMemoryView();
							stage.show();
						});
						
						this.running.set(false);
					}).start();
				}
		});
		theGUIMenuBar.getRunSingleMenuItem().setOnAction((ActionEvent actionEvent) -> {
				if (executionMode.get() && !(running.get())){
					new Thread(() -> {
						theArmSimulator.runStep();
						this.running.set(true);
						Platform.runLater(() -> {
							highlightCurrentLine(theArmSimulator.getCurrentLine());
							theGUIRegisterView.updateRegisters();
							//theGUIMemoryView.updateMemoryView();
							stage.show();
						});
						this.running.set(false);
					}).start();
				}
		});
		theGUIMenuBar.getReloadProgramMenuItem().setOnAction((ActionEvent actionEvent) -> theGUIMenuBar.getEnterExecutionModeMenuItem().fire());
		theGUIMenuBar.getOpenMenuItem().setOnAction((ActionEvent actionEvent) -> {
				FileChooser fileChooser = new FileChooser();
				if(lastFilePath != null) {
					fileChooser.setInitialDirectory(new File (new File(lastFilePath).getParent()));
				}

				fileChooser.setInitialFileName("test");
				fileChooser.setTitle("Open a source File");
				fileChooser.getExtensionFilters().addAll(
			         new ExtensionFilter("#@rm Files", "*.S")
			    );

				String path = fileChooser.showOpenDialog(stage).getAbsolutePath();
				if(path != null) {
					try {
						codingTextArea.setText(new String(Files.readAllBytes(Paths.get(path)), "UTF-8"));
						programFilePath = new File(path);
						lastFilePath = path;
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
		});
		theGUIMenuBar.getSaveAsMenuItem().setOnAction((ActionEvent actionEvent) -> {
				FileChooser fileChooser = new FileChooser();
				fileChooser.setTitle("Save assembly program");
				fileChooser.getExtensionFilters().addAll(
				         new ExtensionFilter("#@rm Files", "*.S")
				);
				File chosenFile = fileChooser.showSaveDialog(stage);
				saveFile(codingTextArea.getText(), chosenFile);
		});
		theGUIMenuBar.getSaveMenuItem().setOnAction((ActionEvent actionEvent) -> {
				if (programFilePath != null){
					saveFile(codingTextArea.getText(), programFilePath);
				}
				else {
					theGUIMenuBar.getSaveAsMenuItem().fire();
				}
		});
		theGUIMenuBar.getNewMenuItem().setOnAction((ActionEvent actionEvent) -> {
				Stage confirmBox = new Stage();
				confirmBox.initModality(Modality.APPLICATION_MODAL);
				confirmBox.initOwner(stage);
				VBox dialogVbox = new VBox(20);
				Label exitLabel = new Label("All unsaved work will be lost");

				Button yesBtn = new Button("Yes");

                 yesBtn.setOnAction((ActionEvent arg0) -> {
						confirmBox.close();
						codingTextArea.setText("");
						programFilePath = null;
                });
                Button noBtn = new Button("No");

                noBtn.setOnAction((ActionEvent arg0) ->	confirmBox.close());

				HBox hBox = new HBox();
				hBox.getChildren().addAll(yesBtn, noBtn);
				VBox vBox = new VBox();
				vBox.getChildren().addAll(exitLabel, hBox);

				confirmBox.setScene(new Scene(vBox));
				confirmBox.show();
		});
		theGUIMenuBar.getHelpMenuItem().setOnAction((ActionEvent actionEvent) -> {
				final Stage helpPopup = new Stage();
				helpPopup.initModality(Modality.APPLICATION_MODAL);
				helpPopup.initOwner(stage);

				VBox dialogVbox = new VBox(20);
				dialogVbox.getChildren().add(new Text("This is very helpful help wow"));
				Scene dialogScene = new Scene(dialogVbox, 300, 200);

				helpPopup.setScene(dialogScene);
				helpPopup.show();
		});
		theGUIMenuBar.getDocumentationMenuItem().setOnAction((ActionEvent actionEvent) -> showDocumentation());

		// Several keyboard shortcut
		scene.setOnKeyPressed((KeyEvent ke) -> {
            	final KeyCombination ctrlS = new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN);
            	final KeyCombination ctrlShiftS = new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN);
            	final KeyCombination ctrlO = new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN);
            	final KeyCombination ctrlN = new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN);
            	final KeyCombination f5 = new KeyCodeCombination(KeyCode.F5);
            	final KeyCombination f11 = new KeyCodeCombination(KeyCode.F11);
            	final KeyCombination ctrlE = new KeyCodeCombination(KeyCode.E, KeyCombination.CONTROL_DOWN);
            	
            	if (ctrlS.match(ke)) { 
            		theGUIMenuBar.getSaveMenuItem().fire();
            	}
            	if (ctrlShiftS.match(ke)) { 
            		theGUIMenuBar.getSaveAsMenuItem().fire();
            	}
            	if (ctrlO.match(ke)) { 
            		theGUIMenuBar.getOpenMenuItem().fire();
            	}
            	if (ctrlN.match(ke)) { 
            		theGUIMenuBar.getNewMenuItem().fire();
            	}
            	if (f5.match(ke)) { 
            		theGUIMenuBar.getRunMenuItem().fire();
            	}
            	if (ctrlE.match(ke)) { 
            		if(executionMode.get()) {
            			theGUIMenuBar.getExitExecutionModeMenuItem().fire();
            		}
            		else {
            			theGUIMenuBar.getEnterExecutionModeMenuItem().fire();
            		}
            	}
            	if (f11.match(ke)) { 
            		theGUIMenuBar.getRunSingleMenuItem().fire();
            	}
		});

		//THE CONSOLE OUTPUT
		OutputStream consoleOut = new OutputStream() {
			@Override
			public void write(int b) throws IOException {
				Platform.runLater(() ->{
					TextFlow consoleTextFlow = (TextFlow) scene.lookup("#consoleTextFlow");
					consoleTextFlow.getChildren().add(new Text((String.valueOf((char) b))));
				});
			}
		};
		System.setOut(new PrintStream(consoleOut, true));


		//theGUIMemoryView.updateMemoryView();
		theGUIRegisterView.updateRegisters();
		stage.show();

	}

    private void highlightCurrentLine(int currentLine) {
        for(int i=0; i<instructionsAsText.size(); i++){
			instructionsAsText.get(i).setFill(Color.BLACK);
        }
        
        // TODO Can we do something better?
		try{
			instructionsAsText.get(currentLine).setFill(Color.RED);
		}
        catch (IndexOutOfBoundsException e){
        	//the program is arrived at the end, the next line to color does not exist
		}
	}

    private void exitExecutionMode() {
		this.executionMode.set(false);;
		codingTextArea.setEditable(true);
		codingTextArea.setVisible(true);
		theGUIMenuBar.exitExecMode();
	}

	private void enterExecutionMode(String program) { //TODO parametre peut etre temoraire
		this.executionMode.set(true);
		codingTextArea.setEditable(false);
		codingTextArea.setVisible(false);
		theGUIMenuBar.setExecMode();

        executionModeTextFlow.getChildren().clear();

		String[] instructionsAsStrings = program.split("\\r?\\n");
		instructionsAsText = new ArrayList<Text>();

		for(int line=0; line<instructionsAsStrings.length; line++){
			instructionsAsText.add(new Text ((line+1) + "\t" + instructionsAsStrings[line] + '\n'));
			executionModeTextFlow.getChildren().add(instructionsAsText.get(line));
		}
		highlightCurrentLine(0);
		//theGUIMemoryView.updateMemoryView();
		theGUIRegisterView.updateRegisters();
/*
		try {
            for(line=0; true; line++){
                instructions.add(new Text(line + "\t" +program.substring(startingPos, program.indexOf("\n", startingPos)+1)));
                executionModeTextFlow.getChildren().add(instructions.get(line));
                startingPos=program.indexOf("\n", startingPos) + 1;
            }
        }
        catch (Exception e){
		    //oulala https://www.e4developer.com/2018/05/13/how-to-write-horrible-java/ //TODO
        }
        line++;
        instructions.add(new Text(line + "\t" +program.substring(startingPos, program.length())));
        executionModeTextFlow.getChildren().add(instructions.get(line));
        */
	}

	private void saveFile(String content, File theFile){
		if (theFile != null) {
			try (FileWriter outputStream = new FileWriter(theFile)) {
				outputStream.write(content);
				this.programFilePath = theFile;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void showDocumentation(){
		Stage docWindow = new Stage();

		docWindow.initModality(Modality.APPLICATION_MODAL);
		docWindow.initOwner(stage);
		docWindow.setTitle("Documentation");

		Parent container;
		Scene docScene;

		// TODO To remove once documentation.fxml is made
		try {
			container = FXMLLoader.load(getClass().getResource("documentation.fxml"));
			docScene = new Scene(container, 100, 100);
			docWindow.setScene(docScene);
			docWindow.show();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void updateTheme(){
		
		String currentTheme = prefs.get("THEME", "");	
		
		scene.getStylesheets().removeAll();
		
		String css = getClass().getResource("css.css").toExternalForm();
		scene.getStylesheets().addAll(css);

		if (!themes.contains(currentTheme)) {
			currentTheme = GUI.DEFAULT_THEME;
		}
		
		css = getClass().getResource(currentTheme+".css").toExternalForm();
		scene.getStylesheets().addAll(css);
	}

}