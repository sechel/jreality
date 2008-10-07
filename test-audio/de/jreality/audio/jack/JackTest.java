package de.jreality.audio.jack;

import java.nio.FloatBuffer;
import java.util.LinkedList;
import java.util.List;

import de.gulden.framework.jjack.JJackAudioEvent;
import de.jreality.audio.AudioReader;
import de.jreality.audio.SampleReader;
import de.jreality.audio.javasound.CachedAudioInputStreamSource;
import de.jreality.scene.AudioSource;
import de.jreality.util.Input;

public class JackTest implements JackSink {

	List<AudioSource> nodes = new LinkedList<AudioSource>();
	List<SampleReader> readers = new LinkedList<SampleReader>();
	
	float samples[];
	float output[];

	public void addNode(AudioSource node) {
		nodes.add(node);
	}
	
	public int highestPort() {
		return 0;
	}

	public void init(int sampleRate) {
		for(AudioSource node: nodes) {
			readers.add(AudioReader.createReader(node, sampleRate));
		}
		samples = new float[sampleRate];  // way too big, but who cares...
		output = new float[sampleRate];
	}

	public void process(JJackAudioEvent ev) {
		FloatBuffer buf = ev.getOutput(0);
		int n = buf.capacity();
		
		for(int i = 0; i<n; i++) {
			output[i] = 0;
		}
		
		for(SampleReader reader: readers) {
			int nRead = reader.read(samples, 0, n);
			for(int i = 0; i<nRead; i++) output[i] += samples[i];
		}
		
		for(int i = 0; i<n; i++) {
			if (output[i]<-1) output[i] = -1;
			else if (output[i]>1) output[i] = 1;
		}
		
		buf.clear();
		buf.put(output, 0, n);
	}
	
	public static void main(String args[]) throws Exception {
		AudioSource s1 = new JackNode("jack node 1", 0);
		AudioSource s2 = new JackNode("jack node 2", 1);
		AudioSource s3 = new CachedAudioInputStreamSource("wavnode", Input.getInput("data/waterdrop.wav"), true);
		
		s1.start();
		s2.start();
		s3.start();
		
		JackTest sink = new JackTest();
		
		sink.addNode(s1);
		sink.addNode(s2);
		sink.addNode(s3);
		
		JackHub.setSink(sink);
		
		JackHub.initializeClient("jReality");
		
		while (true) Thread.sleep(100);
	}
}
