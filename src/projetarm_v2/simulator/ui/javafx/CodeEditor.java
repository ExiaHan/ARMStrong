/*
 * Copyright (c) 2018-2019 Valentin D'Emmanuele, Gilles Mertens, Dylan Fraisse, Hugo Chemarin, Nicolas Gervasi
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package projetarm_v2.simulator.ui.javafx;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.dockfx.DockNode;
import projetarm_v2.simulator.boilerplate.ArmSimulator;

import java.util.ArrayList;
import java.util.List;

public class CodeEditor {

    private Pane mainPane;
    private ScrollPane scrollPane;
    private DockNode dockNode;

    private TextArea textArea;
    private TextFlow textFlow;
    private ObservableList<Node> visibleNodes;
    private List<Text> instructionsAsText;

    private ArmSimulator armSimulator;

    /**
     * Creates a new instance of a code editor
     * @param armSimulator the simulator
     */
    public CodeEditor(ArmSimulator armSimulator) {

        mainPane = new AnchorPane();

        this.armSimulator = armSimulator;
        this.textArea = new TextArea();
        this.textFlow = new TextFlow();
        this.scrollPane = new ScrollPane(this.textFlow);
        
        visibleNodes = FXCollections.observableArrayList();
        
        dockNode = new DockNode(mainPane, "Editor");
        dockNode.setPrefSize(300, 100);
        dockNode.setClosable(false);
        
        AnchorPane.setBottomAnchor(this.textArea, 0D);
        AnchorPane.setLeftAnchor(this.textArea, 0D);
        AnchorPane.setRightAnchor(this.textArea, 0D);
        AnchorPane.setTopAnchor(this.textArea, 0D);
        this.mainPane.setMinSize(300, 300);
        
        AnchorPane.setBottomAnchor(this.scrollPane, 0D);
        AnchorPane.setLeftAnchor(this.scrollPane, 0D);
        AnchorPane.setRightAnchor(this.scrollPane, 0D);
        AnchorPane.setTopAnchor(this.scrollPane, 0D);
        
        this.mainPane.getChildren().addAll(this.scrollPane, this.textFlow, this.textArea);
        this.dockNode.getStylesheets().add("/resources/style.css");
        mainPane.setMaxHeight(Double.MAX_VALUE);
        mainPane.setMaxWidth(Double.MAX_VALUE);
        
        scrollPane.vvalueProperty().addListener((obs) -> {
			checkVisible(scrollPane);
		});
        scrollPane.hvalueProperty().addListener((obs) -> {
			checkVisible(scrollPane);
		});
    }
    
    private void checkVisible(ScrollPane pane) {
		visibleNodes.setAll(getVisibleElements(pane));
	}
    
    private List<Node> getVisibleElements(ScrollPane pane) {
		List<Node> visibleNodes = new ArrayList<Node>();
		Bounds paneBounds = pane.localToScene(pane.getBoundsInParent());
		if (pane.getContent() instanceof Parent) {
			for (Node n : ((Parent) pane.getContent()).getChildrenUnmodifiable()) {
				Bounds nodeBounds = n.localToScene(n.getBoundsInLocal());
				if (paneBounds.intersects(nodeBounds)) {
					visibleNodes.add(n);
				}
			}
		}
		return visibleNodes;
	}

    /**
     * highlight a line in the simulation mode
     * @param line the line to highlight
     */
    public void highlightLine(int line) {
        for (Text anInstructionsAsText : instructionsAsText) {
            anInstructionsAsText.setFill(Color.BLACK);
        }

        if(line > 0) {
            instructionsAsText.get(line - 1).setFill(Color.RED);
        }
    }


    /**
     * set the code editor in execution or edition mode
     * @param executionMode
     */
    public void setExecutionMode(boolean executionMode){
        this.textArea.setEditable(!executionMode);
        this.textArea.setVisible(!executionMode);

        if(executionMode){
            String[] instructionsAsStrings = this.textArea.getText().split("\\r?\\n");
            this.instructionsAsText = new ArrayList<>();
            this.textFlow.getChildren().clear();
            for (int lineNumber = 1; lineNumber <= instructionsAsStrings.length; lineNumber++) {
            	String address = "\t";
            	int longAddress = armSimulator.getAddressFromLine(lineNumber);
            	if (longAddress != 0) {
            		address = String.format("%08x:%08x", longAddress, armSimulator.getRamWord(longAddress));
            	}
                String line = lineNumber + "\t" + address + "\t\t" + instructionsAsStrings[lineNumber-1] + '\n';
                this.instructionsAsText.add(new Text(line));
                this.textFlow.getChildren().add(this.instructionsAsText.get(lineNumber-1));
            }
            highlightLine(1);
            this.dockNode.setTitle("Simulator");
        }else{
            this.dockNode.setTitle("Editor");
        }
    }

    /**
     * get the dock node of the code editor
     * @return
     */
    public DockNode getNode() {
        return this.dockNode;
    }

    /**
     * get the program contained in the editor
     * @return the program as text
     */
    public String getProgramAsString() {
    	this.textArea.setText(this.textArea.getText().replaceAll(";", System.lineSeparator()));
        return textArea.getText();
    }

    /**
     * set a program in the editor
     * @param program
     */
    public void setProgramAsString(String program){
        this.textArea.setText(program);
    }
}
