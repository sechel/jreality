package de.jreality.audio.jass;

import jass.engine.InOut;
import jass.engine.SinkIsFullException;
import jass.patches.CombReverb;

import java.util.Arrays;

import de.jreality.scene.data.RingBuffer;
import de.jreality.scene.data.SampleReader;

public class JassTest {
	public static void main(String[] args) throws SinkIsFullException {
		int srate = 44100;
		int bufSize = 48;
		RingBuffer rb = new RingBuffer(srate);
		JassAdapter ja = new JassAdapter(rb, bufSize);
		InOut reverb = new CombReverb(bufSize, srate, 3);
		JassSource js = new JassSource("foo", srate, bufSize);
		SampleReader reader = js.createReader();
		reverb.addSource(ja);
		js.addSource(reverb);
		js.start();
		float[] inBuf = new float[] {0, 0, 0, 0, 1, 1, 1, 1};
		float[] outBuf = new float[8];
		while (true) {
			rb.write(inBuf, 0, 8);
			reader.read(outBuf, 0, 8);
			System.out.println(Arrays.toString(outBuf));
		}
	}
}
