package de.jreality.audio;

import java.util.ArrayList;
import java.util.List;

import de.jreality.scene.data.SampleReader;
import de.jreality.shader.EffectiveAppearance;

public class ProcessorChain implements SampleProcessor {
	
	private List<SampleProcessor> procs = new ArrayList<SampleProcessor>();
	private SampleReader last;
	
	private ProcessorChain() {
		// do nothing
	}
	
	public static SampleProcessor create(List<Class<? extends SampleProcessor>> list) throws InstantiationException, IllegalAccessException {
		if (list==null || list.isEmpty()) {
			return new NullProcessor();
		} else if (list.size()==1) {
			return list.get(0).newInstance();
		} else {
			ProcessorChain chain = new ProcessorChain();
			for(Class<? extends SampleProcessor> clazz: list) {
				chain.procs.add(clazz.newInstance());
			}
			return chain;
		}
	}
	
	public void initialize(SampleReader reader) {
		for(SampleProcessor proc: procs) {
			proc.initialize(reader);
			reader = proc;
		}
		last = reader;
	}

	public void setProperties(EffectiveAppearance app) {
		for(SampleProcessor proc: procs) {
			proc.setProperties(app);
		}
	}

	public void clear() {
		last.clear();
	}

	public int getSampleRate() {
		return last.getSampleRate();
	}

	public int read(float[] buffer, int initialIndex, int samples) {
		return last.read(buffer, initialIndex, samples);
	}
}
