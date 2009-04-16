package de.jreality.audio;

import java.util.List;

import de.jreality.scene.data.SampleReader;
import de.jreality.shader.EffectiveAppearance;

public class SampleProcessorChain implements SampleProcessor {
	
	private final List<SampleProcessor> procs;
	private SampleReader last;
	
	private SampleProcessorChain(List<SampleProcessor> procs) {
		this.procs = procs;
	}
	
	public static SampleProcessor create(List<SampleProcessor> list) {
		if (list==null || list.isEmpty()) {
			return new NullProcessor();
		} else if (list.size()==1) {
			return list.get(0);
		} else {
			return new SampleProcessorChain(list);
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
	
	public String toString() {
		StringBuilder sb = new StringBuilder("SampleProcessorChain:\n");
		for(SampleProcessor proc: procs) {
			sb.append("   "+proc+"\n");
		}
		return sb.toString();
	}
}
