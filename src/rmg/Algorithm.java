package rmg;

import java.util.LinkedList;
import java.util.Random;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

/* Static class that takes input from RMG and 
 * adds notes to the track.
 */
public class Algorithm {

	/* Every algorithm has the following parameters
	 * @param t Track that notes will be added to
	 * @param bars Number of bars that the track has
	 * @param note List of valid notes
	 * @param key List of valid keys
	 * @param octave List of valid octaves
	 * @param note_dur List of valid note durations
	 * @param rest_dur List of valid rest durations
	 * @return The time that the last note ends
	 */

	// Variables for readability and functionality
	final static Random random = new Random();
	final static int rest = -1;
	final static long triplet = -2;
	final static int max = 127;
	final static int num_notes = 12;
	final static int multiplier = 100;
	final static long resolution = 960;

	/* Algorithm that chooses a random note in a random key
	 * and random octave and adds it to the track.
	 */
	public static long one(Info info) {

		// Getting track info into variables
		Track t = info.getT();
		int bars = info.getBars();
		LinkedList<Integer> note = info.getNote();
		LinkedList<Integer> key = info.getKey();
		LinkedList<Integer> octave = info.getOctave();
		LinkedList<Long> note_dur = info.getNote_dur();
		LinkedList<Long> rest_dur = info.getRest_dur();

		// Variables that keep track of current position in the track, the end
		// of the track, the key, the note, the octave, and the note/rest length.
		long time = 0;
		long length = 0;
		long end = (long) (resolution * 4) * bars;
		int keyVal = 0;
		int octVal = 0;
		int noteVal = 0;

		// Adds notes to the track while the current time is less than 
		// the number of bars specified by the user
		while(time < end) {

			System.out.print("Time: " + time + " ");

			// Getting a random note
			keyVal = random.nextInt(key.size());
			octVal = random.nextInt(octave.size());
			noteVal = random.nextInt(note.size());

			// Getting the int value of the random integer in the list.
			// If the value indicates a rest, then pick the corresponding rest length.
			if(note.get(noteVal).intValue() == rest) {
				length = rest_dur.get(random.nextInt(rest_dur.size()));
				time += length;
				System.out.print("Rest: " + length + "\n");
				continue;
			}

			// Getting random note length
			length = note_dur.get(random.nextInt(note_dur.size()));

			// Adding three notes if the 
			if(length == triplet) {

				// Getting other length that is not a triplet
				long total = note_dur.get(random.nextInt(note_dur.size() - 1));
				total *= 2;
				length = (long) total / 3;

				// Adding note three times, each with random notes
				for(int i = 0; i < 3; i++) {

					// Getting random note
					keyVal = random.nextInt(key.size());
					octVal = random.nextInt(octave.size());

					// Getting note that is not a rest
					do {
						noteVal = random.nextInt(note.size());
					} while ((note.get(noteVal).intValue()) == rest);

					// Getting note in correct key, octave, and note
					keyVal = key.get(keyVal).intValue();
					octVal = octave.get(octVal).intValue();
					noteVal = note.get(noteVal).intValue() + keyVal + (num_notes * octVal);

					// Choosing different time length if it is the last loop
					if(i == 2) {
						addNote(t, noteVal, time, total - (2 * length));
					}
					else {
						addNote(t, noteVal, time, length);
					}

					System.out.print("Time: " + time + " ");
					System.out.print("Note: " + noteVal + " ");

					// Incrementing time
					time += length;

				}

				System.out.print("\n");
				continue;

			}

			// Getting note in correct key, octave, and note
			keyVal = key.get(keyVal).intValue();
			octVal = octave.get(octVal).intValue();
			noteVal = note.get(noteVal).intValue() + keyVal + (num_notes * octVal);

			System.out.print("Note: " + noteVal + "\n");

			// Adding note to track
			addNote(t, noteVal, time, length);

			// Incrementing track length
			time += length;

		}

		return time;

	}

	/* Algorithm that chooses random chords with random notes 
	 * in any of the keys and octaves that are note
	 */
	public static long two(Info info) {

		// Getting track info into variables
		Track t = info.getT();
		int bars = info.getBars();
		LinkedList<Integer> note = info.getNote();
		LinkedList<Integer> key = info.getKey();
		LinkedList<Integer> octave = info.getOctave();
		LinkedList<Long> note_dur = info.getNote_dur();
		LinkedList<Long> rest_dur = info.getRest_dur();

		// Variables that keep track of current position in the track, the end
		// of the track, the key, the note, the octave, and the note/rest length.
		long time = 0;
		long length = 0;
		long end = (long) (resolution * 4) * bars;
		int keyVal = 0;
		int octVal = 0;
		int noteVal = 0;
		int chord = 0;

		// Adds notes to the track while the current time is less than 
		// the number of bars specified by the user
		while(time < end) {

			System.out.print("Time: " + time + " ");

			// Getting a random note that is in the 
			keyVal = random.nextInt(key.size());
			octVal = random.nextInt(octave.size());
			noteVal = random.nextInt(note.size());

			// Getting the int value of the random integer in the list.
			// If the value indicates a rest, then pick the corresponding rest length.
			if(note.get(noteVal).intValue() == rest) {
				length = rest_dur.get(random.nextInt(rest_dur.size()));
				time += length;
				System.out.println("Rest: " + length + "\n");
				continue;
			}

			// Getting random note length
			length = note_dur.get(random.nextInt(note_dur.size())).longValue();

			// Adding three notes if the 
			if(length == triplet) {

				// Getting other length that is not a triplet
				long total = note_dur.get(random.nextInt(note_dur.size() - 1));
				total *= 2;
				length = (long) total / 3;

				// Adding note three times, each with random notes
				for(int i = 0; i < 3; i++) {

					// Getting chord size ranging from 1 to 6 notes
					chord =  random.nextInt(7) + 1;

					// Adding notes to the track with the same start time and
					// length, effectively creating a chord
					for(int j = 0; j < chord; j++) {

						// Getting random note
						keyVal = random.nextInt(key.size());
						octVal = random.nextInt(octave.size());

						// Getting note that is not a rest
						do {
							noteVal = random.nextInt(note.size());
						} while ((note.get(noteVal).intValue()) == rest);

						// Getting note in correct key, octave, and note
						keyVal = key.get(keyVal).intValue();
						octVal = octave.get(octVal).intValue();
						noteVal = note.get(noteVal).intValue() + keyVal + (num_notes * octVal);

						if(i == 2) {
							addNote(t, noteVal, time, total - (2 * length));
						}
						else {
							addNote(t, noteVal, time, length);
						}

						System.out.print("Time: " + time + " ");
						System.out.println("Note: " + noteVal + " ");

					}

					// Incrementing time
					time += length;

				}

				System.out.print("\n");
				continue;

			}

			// Getting chord size ranging from 1 to 10 notes
			chord =  random.nextInt(10) + 1;

			// Adding notes to the track with the same start time and
			// length, effectively creating a chord
			for(int j = 0; j < chord; j++) {

				// Getting note that is not a rest
				do {
					noteVal = random.nextInt(note.size());
				} while((note.get(noteVal).intValue()) == rest);

				// Getting note in correct key, and octave
				keyVal = random.nextInt(key.size());
				octVal = random.nextInt(octave.size());
				keyVal = key.get(keyVal).intValue();
				octVal = octave.get(octVal).intValue();
				noteVal = note.get(noteVal).intValue() + keyVal + (num_notes * octVal);

				System.out.print("Note: " + noteVal + "\n");

				// Adding note to track
				addNote(t, noteVal, time, length);

			}

			// Incrementing track length
			time += length;

		}

		return time;

	}

	/* Algorithm that chooses random notes in a key and switches
	 * the key if that note is also present in another key
	 */
	public static long three(Info info) {

		// Getting track info into variables
		Track t = info.getT();
		int bars = info.getBars();
		LinkedList<Integer> note = info.getNote();
		LinkedList<Integer> key = info.getKey();
		LinkedList<Integer> octave = info.getOctave();
		LinkedList<Long> note_dur = info.getNote_dur();
		LinkedList<Long> rest_dur = info.getRest_dur();

		// Variables that keep track of current position in the track, the end
		// of the track, the key, the note, the octave, and the note/rest length.
		long time = 0;
		long length = 0;
		long end = (long) (resolution * 4) * bars;
		int keyVal = key.get(random.nextInt(key.size())).intValue();
		int octVal = 0;
		int noteVal = 0;

		// Matrix that contains the number of common notes between keys
		@SuppressWarnings("unchecked")
		LinkedList<Integer>[] common = new LinkedList[num_notes];

		// Adding the key to the list in the array corresponding with the note
		for(int i = 0; i < key.size(); i++) {

			for(int j = 0; j < note.size(); j++) {

				// Getting note in key and adding the key to the corresponding
				// linked list in the array
				keyVal = key.get(i).intValue();
				noteVal = note.get(j).intValue();
				
				// Skipping iteration if the note is a rest (-1)
				if(noteVal == rest) {
					continue;
				}
				
				// Getting real note value
				noteVal = (noteVal + keyVal) % num_notes;

				// Creating new LinkedList if it does not exist
				if(common[noteVal] == null) {
					common[noteVal] = new LinkedList<Integer>();
				}

				// Adding key to array
				common[noteVal].add(new Integer(keyVal));

			}

		}
		
		/* debug */
		for(int i = 0; i < common.length; i++) {
			System.out.print("Note: " + i);
			if(common[i] != null) {
				for(int j = 0; j < common[i].size(); j++) {
					System.out.print("Key: " + common[i].get(j));
				}
			}
			System.out.print("\n");
		}

		// Adds notes to the track while the current time is less than 
		// the number of bars specified by the user
		while(time < end) {

			System.out.print("Time: " + time + " ");

			// Getting a random note
			noteVal = random.nextInt(note.size());

			// Getting the int value of the random integer in the list.
			// If the value indicates a rest, then pick the corresponding rest length.
			if(note.get(noteVal).intValue() == rest) {
				length = rest_dur.get(random.nextInt(rest_dur.size()));
				time += length;
				System.out.print("Rest: " + length + "\n");
				continue;
			}

			// Getting random note length
			length = note_dur.get(random.nextInt(note_dur.size()));

			// Adding three notes if the 
			if(length == triplet) {

				// Getting other length that is not a triplet
				long total = note_dur.get(random.nextInt(note_dur.size() - 1));
				total *= 2;
				length = (long) total / 3;

				// Adding note three times, each with random notes
				for(int i = 0; i < 3; i++) {

					// Getting random octave
					octVal = random.nextInt(octave.size());

					// Getting note that is not a rest
					do {
						noteVal = random.nextInt(note.size());
					} while ((note.get(noteVal).intValue()) == rest);

					// Getting note in correct key and octave
					octVal = octave.get(octVal).intValue();
					noteVal = note.get(noteVal).intValue() + keyVal + (num_notes * octVal);

					// Choosing different time length if it is the last loop
					if(i == 2) {
						addNote(t, noteVal, time, total - (2 * length));
					}
					else {
						addNote(t, noteVal, time, length);
					}

					System.out.print("Time: " + time + " ");
					System.out.print("Note: " + noteVal + " ");

					// Incrementing time
					time += length;

				}

				System.out.print("\n");
				continue;

			}

			// Getting note in correct key, and octave
			octVal = random.nextInt(octave.size());
			octVal = octave.get(octVal).intValue();
			noteVal = note.get(noteVal).intValue() + keyVal + (num_notes * octVal);

			System.out.print("Note: " + noteVal + "\n");

			// Adding note to track
			addNote(t, noteVal, time, length);

			// Incrementing track length
			time += length;
			
			noteVal = noteVal % num_notes;
			
			System.out.print("NoteVal = " + noteVal + "\n");
				
			// Determining if there should be a key change
			if(random.nextBoolean() && random.nextBoolean()) {

				// Getting new key from common array
				keyVal = random.nextInt(common[noteVal].size());
				keyVal = common[noteVal].get(keyVal).intValue();
				
				System.out.println("\nKey Changed to " + keyVal);

			}

		}

		return time;

	}
	
	/* Algorithm that chooses random chords in a key and switches
	 * the key if one of the notes in the chord is also 
	 * present in another key.
	 */
	public static long four(Info info) {
		return 0;
	}

	/* Adds a note to the track */
	private static void addNote(Track track, int note, long time, long length) {

		// Subtracting 12 from the note value if it 
		// is out of the range of valid notes
		while(note > max) {
			note -= num_notes;
		}

		// Adding note to track
		try{

			// Turning note on
			ShortMessage sm = new ShortMessage();
			sm.setMessage(0x90, note, 0x60);
			track.add(new MidiEvent(sm, time));

			// Turning note off
			sm = new ShortMessage();
			sm.setMessage(0x80, note, 0x40);
			track.add(new MidiEvent(sm, (time + length)));

		} catch (InvalidMidiDataException e) {
			e.printStackTrace();
		}

	}

}
