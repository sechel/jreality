package de.jreality.audio.csound;

import csnd.Csound;
import de.jreality.scene.AudioSource;
import de.jreality.scene.data.RingBuffer;

/**
 * Not finished yet!!!
 * 
 * @author brinkman
 *
 */


public class CsoundNode extends AudioSource {

	private Csound csnd;
	private CsoundBuffer csBuf;
	private float fBuf[];
	private int ksmps;
	private int nchnls;
	
	public CsoundNode(String name, String csd) {
		super(name);
		csnd = new Csound();
		csnd.Compile(csd, "-n", "-d");
		ksmps = csnd.GetKsmps();
		nchnls = csnd.GetNchnls();
		sampleRate = (int) csnd.GetSr();
		ringBuffer = new RingBuffer(sampleRate);
		fBuf = new float[ksmps];
		csBuf = new CsoundBuffer(csnd.GetSpout());
		csBuf.reserve(ksmps*nchnls);
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
			}
			System.err.println("***************************** buf cap: "+csBuf.capacity());
			for(int j=0; j<ksmps; j++) {
				float v = 0.0f;
				for(int k=0; k<nchnls; k++) {
					v += csBuf.get(j+k*ksmps);
				}
				fBuf[j] = v;
			}
			ringBuffer.write(fBuf, 0, ksmps);
		}
	}
	
	public static void main(String args[]) {
		CsoundNode csn = new CsoundNode("csnode", "/Users/brinkman/Documents/Csound/tr.csd");
		Csound cs = csn.getCsound();
		int ksmps = cs.GetKsmps();
		float buf[] = new float[ksmps];
		RingBuffer.Reader r = csn.createReader();
		
		csn.start();
		
		while (csn.getState() == AudioSource.State.RUNNING) {
			csn.writeSamples(ksmps);
			r.read(buf, 0, ksmps);
			System.out.println(buf);
		}
	}
}
