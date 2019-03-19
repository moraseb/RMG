package rmg;

import java.util.LinkedList;

import javax.sound.midi.Track;

/* Class that contains all the info for the insertion
 * of notes to the midi track
 */
public class Info {
	
	private Track t;
	private int bars;
	private LinkedList<Integer> note;
	private LinkedList<Integer> key;
	private LinkedList<Integer> octave;
	private LinkedList<Long> note_dur;
	private LinkedList<Long> rest_dur;
	
	/* Constructor */
	public Info() {
		t = null;
		bars = 0;
		note = null;
		key = null;
		octave = null;
		note_dur = null;
		rest_dur = null;
	}

	/* Getters and Setters for fields*/
	
	public Track getT() {
		return t;
	}

	public void setT(Track t) {
		this.t = t;
	}

	public int getBars() {
		return bars;
	}

	public void setBars(int bars) {
		this.bars = bars;
	}

	public LinkedList<Integer> getNote() {
		return note;
	}

	public void setNote(LinkedList<Integer> note) {
		this.note = note;
	}

	public LinkedList<Integer> getKey() {
		return key;
	}

	public void setKey(LinkedList<Integer> key) {
		this.key = key;
	}

	public LinkedList<Integer> getOctave() {
		return octave;
	}

	public void setOctave(LinkedList<Integer> octave) {
		this.octave = octave;
	}

	public LinkedList<Long> getNote_dur() {
		return note_dur;
	}

	public void setNote_dur(LinkedList<Long> note_dur) {
		this.note_dur = note_dur;
	}

	public LinkedList<Long> getRest_dur() {
		return rest_dur;
	}

	public void setRest_dur(LinkedList<Long> rest_dur) {
		this.rest_dur = rest_dur;
	}

}
