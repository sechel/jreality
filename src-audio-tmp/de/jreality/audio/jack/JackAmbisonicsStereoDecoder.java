package de.jreality.audio.jack;

import java.nio.FloatBuffer;

import de.gulden.framework.jjack.JJackAudioEvent;
import de.gulden.framework.jjack.JJackAudioProcessor;
import de.gulden.framework.jjack.JJackSystem;

public class JackAmbisonicsStereoDecoder {

	private static final float wScale = (float) Math.sqrt(0.5);
	private static final float yScale = 0.5f;
	
	private JackAmbisonicsStereoDecoder() {
		// not to be instantiated; needs to run in its own JVM
	}
	
	public static void main(String args[]) throws InterruptedException {
		System.setProperty("jjack.ports.in", "4");
		System.setProperty("jjack.ports.out", "2");
		System.setProperty("jjack.client.name", "StereoDecoder");
		System.setProperty("jjack.ports.out.autoconnect", "true");
		
		JJackSystem.setProcessor(new JJackAudioProcessor() {
			public void process(JJackAudioEvent ev) {
				FloatBuffer bw = ev.getInput(0);
				FloatBuffer by = ev.getInput(2);
				FloatBuffer left = ev.getOutput(0);
				FloatBuffer right = ev.getOutput(1);

				int n = left.capacity();
				for(int i = 0; i<n; i++) {
					float w = bw.get()*wScale;
					float y = by.get()*yScale;
					
					left.put(w+y);
					right.put(w-y);
				}
			}
		});
		
		while (true) Thread.sleep(100);
	}
}
