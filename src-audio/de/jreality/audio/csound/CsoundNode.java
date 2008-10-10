package de.jreality.audio.csound;

import csnd.Csound;
import csnd.CsoundMYFLTArray;
import csnd.SWIGTYPE_p_float;
import de.jreality.scene.AudioSource;
import de.jreality.scene.data.RingBuffer;

/**
 * Audio source that uses Csound as its synthesis engine.
 * 
 * @author brinkman
 *
 */
public class CsoundNode extends AudioSource {

	private Csound csnd;
	private CsoundMYFLTArray auxBuffer;
	private SWIGTYPE_p_float csOutBuffer;
	private float cumulativeBuffer[];
	private int ksmps;
	private int nchnls;
	private int bufSize;
	private float scale;
	
	public CsoundNode(String name, String csd) {
		super(name);
		csnd = new Csound();
		csnd.Compile(csd, "-n", "-d");
		ksmps = csnd.GetKsmps();
		nchnls = csnd.GetNchnls();
		bufSize = ksmps*nchnls;
		sampleRate = (int) csnd.GetSr();
		scale = csnd.Get0dBFS();
		ringBuffer = new RingBuffer(sampleRate);
		cumulativeBuffer = new float[ksmps];
		csOutBuffer = csnd.GetSpout();
		auxBuffer = new CsoundMYFLTArray(bufSize);  // too many buffers...
	}

	public Csound getCsound() {
		return csnd;
	}
	
	@Override
	protected void reset() {
		csnd.RewindScore();
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
			if (csnd.PerformKsmps()!=0) {
				state = State.STOPPED;
				reset();
				hasChanged = true;
				return; // TODO: find out whether there are still useful samples in csOutBuffer
			}
			auxBuffer.SetValues(0, bufSize, csOutBuffer);
			for(int j=0; j<ksmps; j++) {
				float v = 0.0f;
				for(int k=j; k<bufSize; k+=ksmps) {
					v += auxBuffer.GetValue(k);
				}
				cumulativeBuffer[j] = v/scale;
			}
			ringBuffer.write(cumulativeBuffer, 0, ksmps);
		}
	}
}
