package de.jreality.audio;

import de.jreality.scene.data.SampleReader;
import de.jreality.shader.EffectiveAppearance;

public interface SampleProcessor extends SampleReader {

	public class NullProcessor implements SampleProcessor {
		private SampleReader reader;
		public NullProcessor() {
			// do nothing
		}
		public NullProcessor(SampleReader reader) {
			initialize(reader);
		}
		public void initialize(SampleReader reader) {
			this.reader = reader;
		}
		public void setProperties(EffectiveAppearance app) {
			// do nothing
		}
		public void clear() {
			reader.clear();
		}
		public int getSampleRate() {
			return reader.getSampleRate();
		}
		public int read(float[] buffer, int initialIndex, int samples) {
			return reader.read(buffer, initialIndex, samples);
		}
	}
	
	public void initialize(SampleReader reader);
	public void setProperties(EffectiveAppearance app);
}
