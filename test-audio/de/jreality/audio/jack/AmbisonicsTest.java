package de.jreality.audio.jack;

import de.gulden.framework.jjack.JJackAudioEvent;
import de.gulden.framework.jjack.JJackAudioProcessor;
import de.gulden.framework.jjack.JJackSystem;
import de.jreality.audio.AmbisonicsEncoder;

public class AmbisonicsTest {

	public static void main(String args[]) throws InterruptedException {
		System.setProperty("jjack.ports.in", "1");
		System.setProperty("jjack.ports.out", "4");
		System.setProperty("jjack.client.name", "AmbiTest");

		JJackSystem.setProcessor(new JJackAudioProcessor() {
			private final int bufSize = 4096;
			private float azimuth = 0.0f;
			private float inBuf[] = new float[bufSize];
			private float bw[] = new float[bufSize];
			private float bx[] = new float[bufSize];
			private float by[] = new float[bufSize];
			private float bz[] = new float[bufSize];
			private AmbisonicsEncoder enc = new AmbisonicsEncoder();

			public void process(JJackAudioEvent ev) {
				float x = 10*(float) Math.cos(azimuth);
				float y = 10*(float) Math.sin(azimuth);
				
				int n = ev.getInput(0).capacity();
				ev.getInput(0).get(inBuf, 0, n);
				
				AmbisonicsEncoder.clearBuffers(bw, bx, by, bz, n);
				enc.addSignal(inBuf, n, bw, bx, by, bz, x, y, 0.0f, true);
				ev.getOutput(0).put(bw, 0, n);
				ev.getOutput(1).put(bx, 0, n);
				ev.getOutput(2).put(by, 0, n);
				ev.getOutput(3).put(bz, 0, n);

				azimuth += (float) (2*Math.PI*n/JJackSystem.getSampleRate()/10);
				if (azimuth>2*Math.PI) {
					azimuth -= 2*Math.PI;
					System.out.println("full circle");
				}
			}
		});
		
		while (true) Thread.sleep(100);
	}
}
