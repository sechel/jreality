package de.jreality.audio;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 
 * An implementation of DistanceCue that holds a chain of distance cues, e.g., low-pass filtering and
 * attenuation, that are applied successively.  The chain [f_1, f_2, ..., f_n](v, r) evaluates to
 * fn(... f_2(f_1(v, r), r) ..., r); an empty chain acts as the identity.
 * 
 * Adding and removing cues is thread-safe.
 * 
 * @author brinkman
 *
 */
public class DistanceCueChain implements DistanceCue {

	private List<DistanceCue> cues = new CopyOnWriteArrayList<DistanceCue>(); // avoid sync when iterating over list...
	
	
	public DistanceCueChain() {
		// do nothing
	}

	public float nextValue(float v, float r) {
		for(DistanceCue cue: cues) {
			v = cue.nextValue(v, r);
		}
		return v;
	}

	public void reset() {
		for(DistanceCue cue: cues) {
			cue.reset();
		}
	}
	
	public boolean addCue(DistanceCue cue) {
		return cues.add(cue);
	}
	
	public boolean removeCue(DistanceCue cue) {
		return cues.remove(cue);
	}
}
