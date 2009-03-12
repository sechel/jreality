package de.jreality.audio;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * An implementation of DistanceCue that holds a chain of distance cues that are applied successively,
 * e.g., low-pass filtering and attenuation, .  The chain [f_1, f_2, ..., f_n](v, r) evaluates to
 * fn(... f_2(f_1(v, r), r) ..., r).
 * 
 * @author brinkman
 *
 */
public final class DistanceCueChain implements DistanceCue {

	private final List<DistanceCue> cues = new ArrayList<DistanceCue>();
	
	
	private DistanceCueChain() {
		// do nothing
	}

	public static DistanceCue create(List<Class<? extends DistanceCue>> list) throws InstantiationException, IllegalAccessException {
		if (list==null || list.isEmpty()) {
			return new DistanceCue.CONSTANT();
		} else if (list.size()==1) {
			return list.get(0).newInstance();
		} else {
			DistanceCueChain chain = new DistanceCueChain();
			for(Class<? extends DistanceCue> clazz: list) {
				chain.add(clazz.newInstance());
			}
			return chain;
		}
	}
	
	public void setSampleRate(float sr) {
		for(DistanceCue cue: cues) {
			cue.setSampleRate(sr);
		}
	}
	
	public float nextValue(float v, float r, float xMic, float yMic, float zMic) {
		for(DistanceCue cue: cues) {
			v = cue.nextValue(v, r, xMic, yMic, zMic);
		}
		return v;
	}

	public void reset() {
		for(DistanceCue cue: cues) {
			cue.reset();
		}
	}
	
	public boolean hasMore() {
		for(DistanceCue cue: cues) {
			if (cue.hasMore()) {
				return true;
			}
		}
		return false;
	}
	
	private boolean add(DistanceCue cue) {
		return cues.add(cue);
	}
}
