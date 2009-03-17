package de.jreality.audio.csound;

import java.io.IOException;
import java.util.concurrent.SynchronousQueue;

import csnd.CppSound;
import csnd.CsoundFile;
import csnd.CsoundMYFLTArray;
import csnd.SWIGTYPE_p_double;
import de.jreality.audio.RingBuffer;
import de.jreality.audio.RingBufferSource;
import de.jreality.scene.AudioSource;
import de.jreality.util.Input;

/**
 * Audio source that uses Csound as its synthesis engine.
 * 
 * @author brinkman
 *
 */
public class CsoundNode extends RingBufferSource {

	private RenderThread renderThread;
	private SynchronousQueue<Integer> qRequest = new SynchronousQueue<Integer>();
	private SynchronousQueue<Integer> qReport = new SynchronousQueue<Integer>();
	
	
	private class RenderThread extends Thread {

		private String[] args;
		private CppSound csnd;
		private CsoundFile csf;
		private CsoundMYFLTArray auxBuffer;
		private SWIGTYPE_p_double csOutBuffer;
		private float cumulativeBuffer[];
		private int ksmps;
		private int nchnls;
		private int bufSize;
		private float scale;
		
		private RenderThread(String[] args) {
			setDaemon(true);
			this.args = args;
		}
		
		public void run() {
			csnd = new CppSound();
			csf = csnd.getCsoundFile();
			if (args.length==1) {
				csf.setCSD(args[0]);
			} else {
				csf.setOrchestra(args[0]);
				csf.setScore(args[1]);
			}
			csf.setCommand("--nosound --nodisplays --output=null foo.orc foo.sco");
			csf.exportForPerformance();
			csnd.compile();
			ksmps = csnd.GetKsmps();
			nchnls = csnd.GetNchnls();
			bufSize = ksmps*nchnls;
			sampleRate = (int) csnd.GetSr();
			scale = (float) csnd.Get0dBFS();
			ringBuffer = new RingBuffer(sampleRate);
			cumulativeBuffer = new float[ksmps];
			csOutBuffer = csnd.GetSpout();
			auxBuffer = new CsoundMYFLTArray(bufSize);  // too many buffers...
			
			try {
				qReport.put(0);
			} catch (InterruptedException e) {
				// do nothing
			}
			renderLoop();
		}
		
		private void renderLoop() {
			while (true) {
				try {
					int nRequested = qRequest.take().intValue();
					if (nRequested>0) {
						int nWritten;
						for(nWritten = 0; nWritten<nRequested && csnd.PerformKsmps()==0; nWritten+=ksmps) {
							auxBuffer.SetValues(0, bufSize, csOutBuffer);
							for(int j=0; j<ksmps; j++) {
								double v = 0;
								for(int k=j; k<bufSize; k+=ksmps) {
									v += auxBuffer.GetValue(k);
								}
								cumulativeBuffer[j] = (float) (v/scale);
							}
							ringBuffer.write(cumulativeBuffer, 0, ksmps);
						}
						qReport.put(nWritten);
					} else if (nRequested==0) {
						csnd.RewindScore();
					} else {
						break;
					}
				} catch (InterruptedException e) {
					// do nothing; we won't interrupt this thread
				}
			}
		}
	}

	public CsoundNode(String name, Input csd) throws IOException {
		super(name);
		String[] args = {csd.getContentAsString()};
		initialize(args);
	}
	
	public CsoundNode(String name, Input orc, Input sco) throws IOException {
		super(name);
		String[] args = {orc.getContentAsString(), sco.getContentAsString()};
		initialize(args);
	}

	private void initialize(String[] args) {
		renderThread = new RenderThread(args);
		renderThread.start();
		try {
			qReport.take();  // wait for init to finish
		} catch (InterruptedException e) {
			// do nothing
		}
	}
	
	@Override
	protected void writeSamples(int n) {
		try {
			qRequest.put(n);
			int nWritten = qReport.take().intValue();
			if (nWritten==0) {
				state = AudioSource.State.STOPPED;
				reset();
				hasChanged = true;
			}
		} catch (InterruptedException e) {
			// do nothing; we won't interrupt this thread
		}
	}

	@Override
	protected void reset() {
		try {
			qRequest.put(0);
		} catch (InterruptedException e) {
			// do nothing
		}
	}
}
