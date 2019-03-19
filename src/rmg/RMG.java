package rmg;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;
import java.io.*;

import javax.sound.midi.*;
import javafx.application.Application;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

/* A Random Music Generator (RMG) that creates music using
 * a midi file. It takes user input to determine the 
 */
public class RMG extends Application {

	// Final static values for readability
	final static long resolution = 960;
	final static long max_dur = 30720;
	final static int num_notes = 12;
	final static int num_lengths = 10;
	final static int num_octaves = 11;
	final static int padding = 15;
	final static int num_options = 3;
	final static int default_tempo = 120;
	final static int default_bars = 16;
	final static int microseconds = 60000000;
	final static int rest = -1;
	final static int triplet = -2;
	final static int multiplier = 100;
	final static int num_inst = 128;
	final static int num_algs = 3; // Change later
	
	final static double sceneWidth = 780;
	final static double sceneHeight = 480;
	final static double button_width = 100.0;
	final static double font_size = 12.5;

	final static String[] btn_names = 
		{"Create", "Save", "Play", "Stop", "Apply", "Apply", "?"};
	final static String[] opt_names =
		{"Single (.)", "Double (..)", "Triplet"};
	final static String[] note_lengths = 
		{"8", "4", "2", "1", "1/2", "1/4", "1/8", "1/16", "1/32", "1/64"};
	final static String[] txt_names = 
		{"Interval:", "Keys(s):", "Note\nLength(s):", "Rest\nLength(s):", "", ""};
	final static String[] note_names = 
		{"C", "C#/Db", "D", "D#/Eb", "E", "F", "F#/Gb", "G", "G#/Ab", "A", "A#/Bb", "B"};
	final static String[] oct_names = 
		{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11"};

	// Arrays that contain the values of the notes, durations, octaves, keys
	Info trackInfo;
	LinkedList<Long> note_dur = new LinkedList<>();
	LinkedList<Long> rest_dur = new LinkedList<>();
	LinkedList<Integer> note = new LinkedList<>();
	LinkedList<Integer> key = new LinkedList<>();
	LinkedList<Integer> octave = new LinkedList<>();

	// Variables that will be altered in the methods
	Stage stage;
	GridPane pane;
	GridPane info;
	
	Random random;
	int tempo;
	int bars;
	int algorithm;
	int instrument;
	long position = 0;
	
	Sequence seq;
	Sequencer sqr;
	
	Text[] txts = new Text[txt_names.length];
	Button[] btns = new Button[btn_names.length];
	CheckBox[] note_cb = new CheckBox[num_notes];
	CheckBox[] key_cb = new CheckBox[num_notes];
	CheckBox[] len_cb = new CheckBox[num_lengths];
	CheckBox[] rest_cb = new CheckBox[num_lengths];
	CheckBox[] oct_cb = new CheckBox[oct_names.length];
	CheckBox[] option_cb = new CheckBox[num_options];

	/* Launches the program */
	public static void main(String[] args) {
		launch(args);
	}

	/* Main method that builds the GUI of the application and 
	 * calls the correct methods that build, play, and save the midi file
	 */
	public void start(Stage primaryStage) {

		// Naming application
		stage = primaryStage;
		primaryStage.setTitle("RMG");

		// Creating GridPane for layout
		pane = new GridPane();
		pane.setAlignment(Pos.CENTER);
		pane.setPadding(new Insets(padding));
		pane.setStyle("-fx-background-color: rgb(55, 60, 67)");
		pane.setHgap(10);
		pane.setVgap(10);

		// Showing grid lines for debugging
		pane.setGridLinesVisible(false);

		// Initializing global variables
		trackInfo = new Info();
		random = new Random();
		tempo = default_tempo;
		bars = default_bars;
		algorithm = 1;
		instrument = 0;
		
		// Getting sequencer from system to be used when the midi file is played
		try {
			sqr = MidiSystem.getSequencer();
			sqr.setLoopStartPoint(0);
			sqr.open();
		} catch (MidiUnavailableException mue) {
			mue.printStackTrace();
		}
		
		// Calling method to create empty track
		defaultTrack();

		// Creating labels for the UI controls
		initLbls();

		// Creating array of check boxes for the notes/keys that are valid in the array
		// and adding them to the pane
		initNoteCB();
		
		// Creating check boxes for the options (Dotted notes and triplets)
		// and adding them to the pane
		initOptCB();

		// Creating array of check boxes for the notes/rest lengths that are 
		// valid in the array and adding them to the pane
		initLenCB();
		
		// Creating array of check boxes for the valid octaves and adding them to
		// the pane
		initOctCB();
		
		// Creates the buttons for all the actions and adds them to the pane
		initBtns();
		
		// Creates the choice boxes to choose the instrument and algorithm
		initChB();

		// Creates a rectangle that will hold the info 
		initInfo();

		// Adding pane to a new scene and the scene to the stage
		Scene scene = new Scene(pane, sceneWidth, sceneHeight);
		primaryStage.setScene(scene);
		primaryStage.setResizable(false);
		primaryStage.show();

	}

	/* Initializes the checkboxes that determine the length of 
	 * the notes/rest in the program
	 */
	private void initLenCB() {

		// Creating checkboxes with the correct formatting
		for(int i = 0; i < num_lengths; i++) {
			len_cb[i] = new CheckBox(note_lengths[i]);
			rest_cb[i] = new CheckBox(note_lengths[i]);
			len_cb[i].setAllowIndeterminate(false);
			rest_cb[i].setAllowIndeterminate(false);
			len_cb[i].setFont(Font.font("Menlo", FontWeight.NORMAL, font_size));
			rest_cb[i].setFont(Font.font("Menlo", FontWeight.NORMAL, font_size));
			len_cb[i].setTextFill(Color.FLORALWHITE);
			rest_cb[i].setTextFill(Color.FLORALWHITE);
			pane.add(len_cb[i], 6, i + 1);
			pane.add(rest_cb[i], 7, i + 1);
		}

	}

	/* Initializes the checkboxes that determine the valid notes/keys
	 * in the program.
	 */
	private void initNoteCB() {

		// Creates check boxes for the note/rest length with 
		// correct formatting 
		for(int i = 0; i < num_notes; i++) {
			note_cb[i] = new CheckBox(oct_names[i]);
			key_cb[i] = new CheckBox(note_names[i]);
			note_cb[i].setAllowIndeterminate(false);
			key_cb[i].setAllowIndeterminate(false);
			note_cb[i].setFont(Font.font("Menlo", FontWeight.NORMAL, font_size));
			key_cb[i].setFont(Font.font("Menlo", FontWeight.NORMAL, font_size));
			note_cb[i].setTextFill(Color.FLORALWHITE);
			key_cb[i].setTextFill(Color.FLORALWHITE);
			pane.add(note_cb[i], 0, i + 1);
			pane.add(key_cb[i], 1, i + 1);
		}

	}
	
	/* Initializes the checkboxes that determine if dotted notes
	 * and triplets are allowed
	 */
	private void initOptCB(){
		
		// Creates check boxes for the note/rest length with 
		// correct formatting 
		for(int i = 0; i < num_options; i++) {
			option_cb[i] = new CheckBox(opt_names[i]);
			option_cb[i] = new CheckBox(opt_names[i]);
			option_cb[i].setAllowIndeterminate(false);
			option_cb[i].setFont(Font.font("Menlo", FontWeight.NORMAL, font_size));
			option_cb[i].setTextFill(Color.FLORALWHITE);
		}
		
		// Creating texts for labeling
		Text optTxt = new Text("Options:");
		Text dotTxt = new Text("Dotted Notes:");
		optTxt.setFill(Color.FLORALWHITE);
		optTxt.setFont(Font.font("Menlo", FontWeight.SEMI_BOLD, font_size));
		dotTxt.setFill(Color.FLORALWHITE);
		dotTxt.setFont(Font.font("Menlo", FontWeight.NORMAL, font_size));
		optTxt.setUnderline(true);
		
		// Adding text to pane
		pane.add(optTxt, 3, 7, 2, 1);
		pane.add(dotTxt, 2, 8);
		GridPane.setHalignment(optTxt, HPos.CENTER);
		GridPane.setHalignment(dotTxt, HPos.RIGHT);
		
		// Adding option checkboxes to the pane
		pane.add(option_cb[0], 3, 8);
		pane.add(option_cb[1], 3, 9, 2, 1);
		pane.add(option_cb[2], 4, 8, 2, 1);
		GridPane.setHalignment(option_cb[2], HPos.CENTER);
		
	}
	
	/* Initializes the checkboxes that determine which 
	 * octaves are allowed
	 */
	private void initOctCB() {
		
		// Creates check boxes for the octaves
		for(int i = 0; i < oct_names.length; i++) {
			oct_cb[i] = new CheckBox();
			oct_cb[i].setAllowIndeterminate(false);
			oct_cb[i].setFont(Font.font("Menlo", FontWeight.NORMAL, font_size));
			oct_cb[i].setTextFill(Color.FLORALWHITE);
		}
		
		// Setting text	
		oct_cb[0].setText(oct_names[0]);
		oct_cb[1].setText(oct_names[1]);
		oct_cb[2].setText(oct_names[2]);
		oct_cb[3].setText(oct_names[3]);
		oct_cb[4].setText(oct_names[4]);
		oct_cb[5].setText(oct_names[5]);
		oct_cb[6].setText(oct_names[6]);
		oct_cb[7].setText(oct_names[7]);
		oct_cb[8].setText(oct_names[8]);
		oct_cb[9].setText(oct_names[9]);
		oct_cb[10].setText(oct_names[10]);

		// Creating text for the octaves
		Text optTxt = new Text("Octaves:");
		optTxt.setFont(Font.font("Menlo", FontWeight.NORMAL, font_size));
		optTxt.setFill(Color.FLORALWHITE);

		// Adding and aligning the text
		pane.add(optTxt, 2, 5);
		GridPane.setHalignment(optTxt, HPos.CENTER);

		// Adding checkboxes to the pane
		pane.add(oct_cb[0], 2, 5, 2, 1);
		pane.add(oct_cb[1], 2, 6, 2, 1);
		pane.add(oct_cb[2], 3, 5, 1, 1);
		pane.add(oct_cb[3], 3, 6, 1, 1);
		pane.add(oct_cb[4], 3, 5, 2, 1);
		pane.add(oct_cb[5], 3, 6, 2, 1);
		pane.add(oct_cb[6], 4, 5, 1, 1);
		pane.add(oct_cb[7], 4, 6, 1, 1);
		pane.add(oct_cb[8], 4, 5, 2, 1);
		pane.add(oct_cb[9], 4, 6, 2, 1);
		pane.add(oct_cb[10], 5, 5, 1, 1);

		// Aligning checkboxes
		
		GridPane.setHalignment(oct_cb[0], HPos.CENTER);
		GridPane.setHalignment(oct_cb[1], HPos.CENTER);
		GridPane.setHalignment(oct_cb[2], HPos.CENTER);
		GridPane.setHalignment(oct_cb[3], HPos.CENTER);
		GridPane.setHalignment(oct_cb[4], HPos.CENTER);
		GridPane.setHalignment(oct_cb[5], HPos.CENTER);
		GridPane.setHalignment(oct_cb[6], HPos.CENTER);
		GridPane.setHalignment(oct_cb[7], HPos.CENTER);
		GridPane.setHalignment(oct_cb[8], HPos.CENTER);
		GridPane.setHalignment(oct_cb[9], HPos.CENTER);
		GridPane.setHalignment(oct_cb[10], HPos.CENTER);
		
	}

	/* Initializes the labels in the program
	 */
	private void initLbls() {

		// Creates labels and formats them
		for(int i = 0; i < txt_names.length; i++) {
			txts[i] = new Text(txt_names[i]);
			txts[i].setFill(Color.FLORALWHITE);
			txts[i].setFont(Font.font("Menlo", FontWeight.MEDIUM, font_size));
		}

		// Creating Tooltips for the labels
		Tooltip noteTip = new Tooltip("Choose the valid notes/scale, in the key of C,"
				+ "\nthat the program will use to generate music."
				+ "\nIf left empty, it will default to the harmonic "
				+ "\nminor scale");
		Tooltip keyTip = new Tooltip("Choose the valid key(s) the notes/scale chosen"
				+ "\nwill be used to create the music. If left empty,"
				+ "\nit will default to the key of F#");
		String lengthTxt = new String("\n8 = Large"
				+ "\n4 = Long"
				+ "\n2 = Double Whole Note"
				+ "\n1 = Whole Note"
				+ "\n1/2 = Half Note"
				+ "\n1/4 = Quarter Note"
				+ "\n1/8 = Eighth Note"
				+ "\n1/16 = Sixteenth Note"
				+ "\n1/32 = Thirty-second Note"
				+ "\n1/64 = Sixty-fourth Note");
		Tooltip nLenTip = new Tooltip();
		Tooltip rLenTip = new Tooltip();
		nLenTip.setText("Choose the length of the note(s)."
				+ "\nIf no boxes are checked, the default note lengths"
				+ "\nwill be (1/16, 1/8, 1/4, 1/2)" 
				+ lengthTxt);
		rLenTip.setText("Choose the length of the rest(s)."
				+ "\nIf no boxes are checked, there will be no rests." 
				+ lengthTxt);
		

		// Adding labels to pane
		pane.add(txts[0], 0, 0);
		pane.add(txts[1], 1, 0);
		pane.add(txts[2], 6, 0);
		pane.add(txts[3], 7, 0);
		pane.add(txts[4], 2, 2, 3, 1);
		pane.add(txts[5], 2, 4, 3, 1);
		GridPane.setHalignment(txts[4], HPos.CENTER);
		GridPane.setHalignment(txts[5], HPos.CENTER);
		
	}
	
	/* Initializes choice boxes for algorithms and instrument 
	 */
	private void initChB() {
		
		// Creating CheckBoxes for algorithm and instrument
		ChoiceBox<String> alg = new ChoiceBox<>();
		ChoiceBox<String> inst = new ChoiceBox<>();
		
		// Lists that will hold the instrument names and algorithm 
		ArrayList<String> algList = new ArrayList<>(num_algs);
		ArrayList<String> instList = new ArrayList<>(num_inst);
		
		// Getting all instruments from the MidiSystem and 
		// adding them to the instList
		try {
			
			// Getting system's midi synthesizer and opening it
			Synthesizer synth = MidiSystem.getSynthesizer();
  		synth.open();
  		
  		// Getitng all instruments from synthesizer
  		Instrument[] instruments = synth.getAvailableInstruments();
  		
  		// Adding all instruments to the array list
  		for(int i = 0; i < num_inst; i++) {
  			instList.add(instruments[i].getName());
  		}
			
		} catch(MidiUnavailableException e) {
			e.printStackTrace();
		}
		
		// Adding instruments to the choice box
		inst.setItems(FXCollections.observableArrayList((instList)));
		inst.setValue(instList.get(0));

		// Updating the instrument when a choice is selected
		inst.getSelectionModel().selectedIndexProperty().addListener(
				(ObservableValue<? extends Number> ov,
						Number old_val, Number new_val) -> {
							instrument = new_val.intValue();
						});
		
		// Adding strings to the algorithm list
		for(int i = 0; i < num_algs; i++) {
			algList.add(Integer.toString(i + 1));
		}

		// Adding algorithm numbers to the choice box
		alg.setItems(FXCollections.observableArrayList((algList)));
		alg.setValue(algList.get(0));

		// Updating the instrument when a choice is selected
		alg.getSelectionModel().selectedIndexProperty().addListener(
				(ObservableValue<? extends Number> ov,
						Number old_val, Number new_val) -> {
							algorithm = new_val.intValue() + 1;
						});
		
		// Creating texts for the choice boxes
		Text aTxt = new Text("Algorithm:");
		Text iTxt = new Text("Instrument:");
		aTxt.setFill(Color.FLORALWHITE);
		iTxt.setFill(Color.FLORALWHITE);
		aTxt.setFont(Font.font("Menlo", FontWeight.NORMAL, font_size));
		iTxt.setFont(Font.font("Menlo", FontWeight.NORMAL, font_size));
		
		// Adding the texts and choice boxes to the pane
		pane.add(alg, 2, 11, 2, 1);
		pane.add(inst, 4, 11, 2, 1);
		pane.add(aTxt, 2, 10, 2, 1);
		pane.add(iTxt, 4, 10, 2, 1);
		GridPane.setHalignment(alg, HPos.CENTER);
		GridPane.setHalignment(inst, HPos.CENTER);
		GridPane.setHalignment(aTxt, HPos.CENTER);
		GridPane.setHalignment(iTxt, HPos.CENTER);
		
	}
	
	/* Creates buttons and sets their actions
	 */
	private void initBtns(){

		// Creating TextFields for the tempo and # of bars in the the midi file
		TextField tempoTxt = new TextField();
		TextField barTxt = new TextField();
		tempoTxt.setPromptText("Enter Tempo in BPM");
		barTxt.setPromptText("Enter Number of Bars");
		Text temp_text = new Text("Tempo:");
		Text bar_text = new Text("Bars:");
		temp_text.setFill(Color.FLORALWHITE);
		bar_text.setFill(Color.FLORALWHITE);
		temp_text.setFont(Font.font("Menlo", FontWeight.NORMAL, font_size));
		bar_text.setFont(Font.font("Menlo", FontWeight.NORMAL, font_size));

		// Adding TextFields to pane
		pane.add(tempoTxt, 3, 1, 2, 1);
		pane.add(barTxt, 3, 3, 2, 1);
		pane.add(temp_text, 2, 1);
		pane.add(bar_text, 2, 3);
		GridPane.setHalignment(temp_text, HPos.RIGHT);
		GridPane.setHalignment(bar_text, HPos.RIGHT);

		// Setting the buttons' name and width
		for(int i = 0; i < btn_names.length; i++) {
			btns[i] = new Button();
			btns[i].setText(btn_names[i]);
			btns[i].setFont(Font.font("Menlo", FontWeight.NORMAL, font_size));
			btns[i].setPrefWidth(button_width);
		}

		// Making the button a square
		btns[6].setPrefWidth(btns[6].getPrefHeight());

		// Setting the action of the create button
		btns[0].setOnAction((ActionEvent e) -> {

			// Updating the boolean arrays that are checked by the user
			update();

			// Checking that lists are not empty
			check();

			// Building midi file
			build(); 

		});

		// Setting the action of the save button
		btns[1].setOnAction((ActionEvent e) -> {

			// Creating FileChooser that creates dialog for saving the midi file
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Save Midi File");
			fileChooser.getExtensionFilters().addAll( 
					new ExtensionFilter("Midi File", "*.mid") );

			// Getting file from the user input
			File file = fileChooser.showSaveDialog(stage);

			// Writing the file if it is not null
			if (file != null) {
				try {
					MidiSystem.write(seq,1,file);
				} catch (IOException ex) {
					System.out.println(ex.getMessage());
				}

			}

		});

		// Setting the action of the play button
		btns[2].setOnAction((ActionEvent e) -> {

			// Creating Sequencer that will play the music.
			try {
				// Setting sequencer's (sqr) to the sequence of the current track 
				sqr.setSequence(seq);
				// Resetting tick position
				if(position == sqr.getTickLength()) {
					position = 0;
				}
				sqr.setTickPosition(position);
				sqr.start();
			} catch (Exception midex) {
				midex.printStackTrace();
			}

		});

		// Setting action of the stop button
		btns[3].setOnAction((ActionEvent e) -> {

			// Stopping the sequencer if it is playing
			try{
				position = sqr.getTickPosition();
				sqr.stop();
			} catch(IllegalStateException ie) {
				ie.printStackTrace();
			}

		});

		// Setting action of the button that determines tempo
		btns[4].setOnAction((ActionEvent e) -> {

			// Getting text from tempo text field
			String text = tempoTxt.getText();
			int input = default_tempo;

			// Trying to turn user input to an int
			try{
				input = Integer.parseInt(text);
				tempo = input;
				txts[4].setText("");
			} catch(NumberFormatException numException) {
				// Printing warning message if the value is not a number
				txts[4].setText("Please enter a whole number");
			}

			// Printing warning message if the value is not positive or 0
			if(input < 1) {
				txts[4].setText("Please enter a positive value");
				tempo = default_tempo;
			}

		});

		// Setting action of the button that determines the number of bars
		btns[5].setOnAction((ActionEvent e) -> {

			// Getting text from bar text field
			String text = barTxt.getText();
			int input = default_bars;

			// Trying to turn user input to an int
			try{
				input = Integer.parseInt(text);
				bars = input;
				txts[5].setText("");
			} catch(NumberFormatException numException) {
				// Printing warning message if the value is not a number
				txts[5].setText("Please enter a whole number");
			}

			// Printing warning message if the value is not positive or 0
			if(input < 1) {
				txts[5].setText("Please enter a positive value");
				tempo = default_bars;
			}

		});

		// Setting action of pane that displays info
		btns[6].setOnAction((ActionEvent e) -> {
				info.setVisible(true);
		});

		// Adding buttons to pane
		pane.add(btns[0], 2, 13);
		pane.add(btns[1], 3, 13);
		pane.add(btns[2], 4, 13);
		pane.add(btns[3], 5, 13);
		pane.add(btns[4], 5, 1);
		pane.add(btns[5], 5, 3);
		pane.add(btns[6], 7, 14);
		GridPane.setHalignment(btns[6], HPos.RIGHT);
		
	}
		
	/* Initializes a rectangle and text that will
	 * be present when the info button is pressed
	 */
	private void initInfo() {
		
		// Creating GridPane for info window
		info = new GridPane();
		info.setVisible(false);
		info.setPadding(new Insets(padding));
		info.setAlignment(Pos.CENTER);
		info.setHgap(10);
		info.setVgap(10);
		
		// Creating rectangle that spans the entire pane
		Rectangle rec = new Rectangle(sceneWidth, sceneHeight);
		rec.setFill(Color.LIGHTCYAN);
		rec.setOpacity(0.97);
		
		// Adding rectangle to pane
		info.add(rec, 0, 0, 8, 15);
		GridPane.setHalignment(rec, HPos.CENTER);
		GridPane.setValignment(rec, VPos.CENTER);
		
		// Creating help text
		Text[] txt = new Text[10];
		txt[0] = new Text("Scale - ");
		txt[1] = new Text("Choose the valid notes/scale, in the key of C, "
				+ "that the program will use to generate music."
				+ "\nIf left empty, it will default to the harmonic "
				+ "minor scale");
		txt[2] = new Text("Keys - ");
		
		Text space = new Text("  ");
		space.setFont(Font.font("Menlo", FontWeight.MEDIUM, 10));
		
		// Adding texts to pane
		info.add(space, 0, 1);
		info.add(txt[0], 1, 1);
		info.add(txt[1], 2, 1, 1, 2);
		
		// Setting the info pane to invisible when the pane is clicked
		info.setOnMouseClicked(MouseEvent -> {
			info.setVisible(false);
		});

		// Adding info pane to pane
		pane.add(info, 0, 0, 8, 15);
		GridPane.setHalignment(info, HPos.CENTER);
		
		// For debugging
		info.setGridLinesVisible(false);
		
	}
	
	/* Updates the lists that determine if the user values
	 * are valid in the creation of the track based on the 
	 * checkboxes selected
	 */
	private void update() {
		
		// Will be used to set the lengths of the notes
		long cur_dur = max_dur;
		
		// Resetting the lists to be empty
		note_dur.clear();
		rest_dur.clear();
		octave.clear();
		note.clear();
		key.clear();
		
		// Updating the valid notes/keys
		for(int i = 0; i < num_notes; i++) {
			
			// Adding the note to the list if it is checked
			if(note_cb[i].isSelected()){
				note.add(new Integer(i));
			}
			
			// Adding the key to the list if it is checked
			if(key_cb[i].isSelected()){
				key.add(new Integer(i));
			}
			
		}
		
		// Updating valid octaves
		for(int i = 0; i < num_octaves; i++) {
			
			// Adding octaves to list if they are checked
			if(oct_cb[i].isSelected()) {
				octave.add(new Integer(i));
			}
			
		}
		
		// Updating the valid note lengths
		for(int i = 0; i < num_lengths; i++) {

			// Setting the length of the note duration if the checkbox is selected
			if(len_cb[i].isSelected()){
				note_dur.add(new Long(cur_dur));
			}
			
			// Setting the length of the note duration if the checkbox is selected
			if(rest_cb[i].isSelected()){
				rest_dur.add(new Long(cur_dur));
			}
			
			// Incrementing last note value
			cur_dur = cur_dur / 2;

		}
		
		// Checking if the check box for dotted notes is selected
		if(option_cb[0].isSelected()) {
			
			// Holds the value of the dotted note
			long dotVal = 0;
			
			// Getting size of list
			int size = note_dur.size();

			// Adding dotted notes of all the notes in the list to the list
			for(int i = 0; i < size; i++) {
				dotVal = note_dur.get(i).intValue();
				dotVal = dotVal + (dotVal / 2);
				note_dur.add(new Long(dotVal));
			}
			
			// Getting size of list
			size = rest_dur.size();

			// Adding dotted rests of all the rests in the list to the list
			for(int i = 0; i < size; i++) {
				dotVal = rest_dur.get(i).intValue();
				dotVal += (dotVal / 2);
				rest_dur.add(new Long(dotVal));
			}

		}

		// Checking if the check box for double dotted notes is selected
		if(option_cb[1].isSelected()) {

			// Holds the value of the double dotted note
			long dotVal = 0;
			
			// Getting size of list
			int size = note_dur.size();

			// Adding double dotted notes of all the notes in the list to the list
			for(int i = 0; i < size; i++) {
				dotVal = note_dur.get(i).intValue();
				dotVal += (dotVal / 2) + (dotVal / 4);
				note_dur.add(new Long(dotVal));
			}
			
			// Getting size of list
			size = rest_dur.size();

			// Adding double dotted rests of all the rests in the list to the list
			for(int i = 0; i < size; i++) {
				dotVal = rest_dur.get(i).intValue();
				dotVal += (dotVal / 2) + (dotVal / 4);
				rest_dur.add(new Long(dotVal));
			}

		}
		
		// Checking if triplets will be allowed
		if(option_cb[2].isSelected()) {
			
			// Adding int to list that indicates a triplet if the list is not empty
			if(!note_dur.isEmpty()) {
				note_dur.add(new Long(triplet));
			}
			
		}
		
	}
	
	
	/* Checks if the linked lists are empty and sets
	 * them to default values if they are empty so that
	 * when the track is built, it doesn't loop infinitely
	 */
	private void check() {
		
		// Sets valid notes to default scale (harmonic minor)
		// if all checkboxes are unchecked
		if(note.isEmpty()) {
			note.add(new Integer(0));  // C
			note.add(new Integer(2));  // D
			note.add(new Integer(3));  // Eb
			note.add(new Integer(5));  // F
			note.add(new Integer(7));  // G
			note.add(new Integer(8));  // Ab
			note.add(new Integer(11)); // B
		}
		
		// Sets valid keys to default key (F#) if all 
		// checkboxes are unchecked
		if(key.isEmpty()){
			key.add(new Integer(6)); // 6 = F#
		}
		
		// Sets octaves to default range (2-4)
		// if no checkbox is selected
		if(octave.isEmpty()) {
			octave.add(new Integer(3));
			octave.add(new Integer(4));
		}
		
		// Sets valid note length to default note lengths 
		// (1/16, 1/8) if all checkboxes are unchecked
		if(note_dur.isEmpty()){
			note_dur.add(new Long(240));  // 240 = 1/16
			note_dur.add(new Long(480));  // 480 = 1/8
		}
		
		// Adds a value (-1) to the valid note list if
		// at least one of the rest boxes are selected
		if(!rest_dur.isEmpty()){
			note.add(new Integer(rest)); // -1 = rest
		}
			
	}
	
	
	/* Method that takes an int that denotes the type of algorithm
	 * 1 - Equal probability of note/keys/length. Single notes only.
	 * 2 - Equal probability of note/keys/length. Chords allowed.
	 * 3 - Biased probability of notes so that the note switches keys
	 *     when it reaches common note to another key. Single notes only
	 * 4 - Same as algorithm 3, but with chords allowed
	 * 5 - 
	 */
	private long algorithm(int alg, Track trk) {
		
		/* For debugging purposes
		System.out.println(note.toString());
		System.out.println(key.toString());
		System.out.println(octave.toString());
		System.out.println(note_dur.toString());
		System.out.println(rest_dur.toString()); */
		
		// Updating the info for the track
		trackInfo.setT(trk);
		trackInfo.setBars(bars);
		trackInfo.setNote(note);
		trackInfo.setKey(key);
		trackInfo.setOctave(octave);
		trackInfo.setNote_dur(note_dur);
		trackInfo.setRest_dur(rest_dur);
		
		// Switch case statement that calls correct method depending on
		// parameter alg
		switch(alg) {
			case 1: return Algorithm.one(trackInfo);
			case 2: return Algorithm.two(trackInfo);
			case 3: return Algorithm.three(trackInfo);
		  //case 4: return Algorithm.four(trk, bars, note, key, octave, note_dur, rest_dur);
			default: return Algorithm.one(trackInfo);
		}
		
	}

	
	/* Builds the midi file of random music based on the user input
	 */
	public void build() {
		
		try {

			// Creating sequencer, measured in 960 ticks per quarter note
			seq = new Sequence(Sequence.PPQ, (int) resolution);

			// Creating track from sequence
			Track track = seq.createTrack();

			// General MIDI sysex. Turn on General MIDI sound set
			byte[] b = {(byte)0xF0, 0x7E, 0x7F, 0x09, 0x01, (byte)0xF7};
			SysexMessage sm = new SysexMessage();
			sm.setMessage(b, 6);
			MidiEvent me = new MidiEvent(sm,(long)0);
			track.add(me);

			// int for tempo in microseconds per quarter note
			int mpq = (int) (microseconds / tempo); 

			// Set tempo (meta event)
			MetaMessage mt = new MetaMessage();
			byte[] bt = {
					(byte) (mpq >> 16 & 0xff),
					(byte) (mpq >> 8 & 0xff), 
					(byte) (mpq & 0xff)
			};
			mt.setMessage(0x51 ,bt, 3);
			me = new MidiEvent(mt,(long)0);
			track.add(me);

			// Set track name (meta event)
			mt = new MetaMessage();
			String TrackName = new String("midifile track");
			mt.setMessage(0x03 ,TrackName.getBytes(), TrackName.length());
			me = new MidiEvent(mt,(long)0);
			track.add(me);

			// Set omni on 
			ShortMessage mm = new ShortMessage();
			mm.setMessage(0xB0, 0x7D,0x00);
			me = new MidiEvent(mm,(long)0);
			track.add(me);

			// Set poly on
			mm = new ShortMessage();
			mm.setMessage(0xB0, 0x7F,0x00);
			me = new MidiEvent(mm,(long)0);
			track.add(me);

			// Set instrument to Piano
			mm = new ShortMessage();
			mm.setMessage(0xC0, instrument, 0x00);
			me = new MidiEvent(mm,(long)0);
			track.add(me);
			
			// Calling method that adds notes to the track, depending on the 
			// chosen algorithm
			long time = algorithm(algorithm, track);
			
			// Set end of track
			mt = new MetaMessage();
			byte[] bet = {};
			mt.setMessage(0x2F, bet, 0);
			me = new MidiEvent(mt, (long) time + resolution);
			track.add(me);

		} catch (Exception e) {
			e.printStackTrace(); // Printing out exception
		}
		
	}
	

	/* Builds default track that only plays the middle c note
	 */
	private void defaultTrack() { 
		try{

			// Creating sequencer, measured in 960 ticks per quarter note
			seq = new Sequence(Sequence.PPQ, (int) resolution);

			// Creating track from sequence
			Track track = seq.createTrack();

			// General MIDI sysex. Turn on General MIDI sound set
			byte[] b = {(byte)0xF0, 0x7E, 0x7F, 0x09, 0x01, (byte)0xF7};
			SysexMessage sm = new SysexMessage();
			sm.setMessage(b, 6);
			MidiEvent me = new MidiEvent(sm,(long)0);
			track.add(me);

			// int for tempo in microseconds per quarter note
			int mpq = (int) (microseconds / default_tempo); 

			// Set tempo (meta event)
			MetaMessage mt = new MetaMessage();
			byte[] bt = {
					(byte) (mpq >> 16 & 0xff),
					(byte) (mpq >> 8 & 0xff), 
					(byte) (mpq & 0xff)
			};
			mt.setMessage(0x51 ,bt, 3);
			me = new MidiEvent(mt,(long)0);
			track.add(me);

			// Set track name (meta event)
			mt = new MetaMessage();
			String TrackName = new String("midifile track");
			mt.setMessage(0x03 ,TrackName.getBytes(), TrackName.length());
			me = new MidiEvent(mt,(long)0);
			track.add(me);

			// Set omni on 
			ShortMessage mm = new ShortMessage();
			mm.setMessage(0xB0, 0x7D,0x00);
			me = new MidiEvent(mm,(long)0);
			track.add(me);

			// Set poly on
			mm = new ShortMessage();
			mm.setMessage(0xB0, 0x7F,0x00);
			me = new MidiEvent(mm,(long)0);
			track.add(me);

			// Set instrument to Piano
			mm = new ShortMessage();
			mm.setMessage(0xC0, 0x00, 0x00);
			me = new MidiEvent(mm,(long)0);
			track.add(me);

			// Note on - middle C
			mm = new ShortMessage();
			mm.setMessage(0x90,0x3C,0x60);
			me = new MidiEvent(mm,(long)0);
			track.add(me);

			// Note off - middle C - a whole note later
			mm = new ShortMessage();
			mm.setMessage(0x80,0x3C,0x40);
			me = new MidiEvent(mm,(long)(resolution*4));
			track.add(me);

			// Set end of track (meta event) a quarter note after
			mt = new MetaMessage();
			byte[] bet = {}; // empty array
			mt.setMessage(0x2F,bet,0);
			me = new MidiEvent(mt, (long)(resolution*5));
			track.add(me);

			// Setting sequencer's (sqr) to the sequence of the current track 
			sqr.setSequence(seq);

		} catch(Exception e) {
			e.printStackTrace(); // Printing error message
		}

	}

}
