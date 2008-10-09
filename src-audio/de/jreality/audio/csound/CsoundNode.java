package de.jreality.audio.csound;

import csnd.Csound;
import csnd.MyfltVector;
import de.jreality.scene.AudioSource;
import de.jreality.scene.data.RingBuffer;

/**
 * This is merely a stub and not quite functional yet!  Don't even think about using this one right now.
 * 
 * @author brinkman
 *
 */
public class CsoundNode extends AudioSource {

	private Csound csnd;
	private int ksmps;
	private MyfltVector csBuf;
	
	public CsoundNode(String name, String csd) {
		super(name);
		csnd = new Csound();
		csnd.Compile(csd);  // command line options? file vs InputStream?
		csnd.GetOutputBuffer(); // now what?
		csBuf = new MyfltVector(); // how to initialize this one?
		ksmps = csnd.GetKsmps();
		// sampleRate = ???
		ringBuffer = new RingBuffer(sampleRate);
	}

	@Override
	protected void reset() {
		csnd.Reset(); // or maybe csnd.Rewind()?
	}

	@Override
	protected void setParameterImpl(String name, Object value) {
		if (value instanceof String) {
			csnd.SetChannel(name, (String) value);
		} else if (value instanceof Double) {
			csnd.SetChannel(name, ((Double) value).doubleValue());
		} else {
			throw new IllegalArgumentException("parameters must be String or Double");
		}
		// any other options for real-time interaction?
	}

	@Override
	protected void writeSamples(int n) {
		for(int i=0; i<n; i+=ksmps) {
			csnd.PerformKsmps();
			// how to get the samples out of the Csound buffer and into the ring buffer?
		}
	}

}
