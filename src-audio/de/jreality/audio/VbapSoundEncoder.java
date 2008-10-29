package de.jreality.audio;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.jreality.math.Matrix;

public abstract class VbapSoundEncoder implements SoundEncoder {
	
	int channels;
	
	List<float[]> speakerInverseMatrices = new ArrayList<float[]>();
	double[][] speakers;
	float[] speakerDistances;
	
	protected float[] buf;

	private int[] channelIDs;
	
	/**
	 * 
	 * @param speakers An array containing position vectors of the speakers
	 * in cyclic order.
	 * @param channelIDs the corresponding channel IDs for the given speakers.
	 */
	public VbapSoundEncoder(int numSpeakers, double[][] speakers, int[] channelIDs) {
		this.channels=numSpeakers;
		setSpeakerIDs(channelIDs);
		setSpeakerPositions(speakers);
	}
	
	public synchronized void setSpeakerPositions(double[][] speakers) {
		System.out.println("new speaker pos: "+Arrays.toString(speakers));
		
		speakerInverseMatrices.clear();
		
		this.speakers=speakers;
		for (int i=0; i<channels; i++) {
			this.speakers[i][0]=speakers[i][0];
			this.speakers[i][1]=speakers[i][1];
		}
		speakerDistances = new float[channels];
		float maxDist=0;
		for (int i=0; i<channels; i++) {
			double[] s1 = speakers[i];
			float n1=(float) Math.sqrt(s1[0]*s1[0]+s1[1]*s1[1]);
			double[] s2 = speakers[(i+1)%channels];
			float n2=(float) Math.sqrt(s2[0]*s2[0]+s2[1]*s2[1]);
			float[] m = new float[]{(float) s1[0]/n1, (float) s2[0]/n2, (float) s1[1]/n1, (float) s2[1]/n2};
			double d = 1./(m[0]*m[3]-m[1]*m[2]);
			float[] mi = new float[]{(float) (d*m[3]), (float) (-d*m[1]), (float) (-d*m[2]), (float) (d*m[0])};
			speakerInverseMatrices.add(mi);
			speakerDistances[i]=n1;
			maxDist=Math.max(maxDist, n1);
		}
		for (int i=0; i<channels; i++) speakerDistances[i]/=maxDist;
	}

	public synchronized double[][] getSpeakerPositions() {
		return speakers;
	}
	
	public synchronized int[] getSpeakerIDs() {
		return channelIDs;
	}
	
	public synchronized void setSpeakerIDs(int[] channelIDs) {
		this.channelIDs = channelIDs;
	}
	
	@Override
	public synchronized void encodeSignal(float[] samples, int nSamples, Matrix p0, Matrix p1) {
		
		// read start and dest directions from matrices: 
		float x0 = (float) p0.getEntry(0, 3);
		float y0 = (float) p0.getEntry(1, 3);
		float z0 = (float) p0.getEntry(2, 3);

		float x1 = (float) p1.getEntry(0, 3);
		float y1 = (float) p1.getEntry(1, 3);
		float z1 = (float) p1.getEntry(2, 3);

		float dx = (x1-x0)/nSamples;
		float dy = (y1-y0)/nSamples;
		float dz = (z1-z0)/nSamples;
		
		float[] g = new float[2];
		
		for(int i = 0; i<nSamples; i++) {
			x0 += dx;
			y0 += dy;
			z0 += dz;

			float r = (float) (Math.sqrt(x0*x0+y0*y0+z0*z0)+1e-5);
			
			float v = samples[i];
			
			float x = -z0/r;
			float y = -x0/r;
			
			int j;
			for (j=0; j<channels; j++) {
				if (solve(g, speakerInverseMatrices.get(j), x, y)) break;
			}
			
			//System.out.println("j="+j+" g="+Arrays.toString(g));
			
			int jn = (j+1)%channels; 
			
			buf[i*channels+channelIDs[j]] += speakerDistances[j]*v*g[0];
			buf[i*channels+channelIDs[jn]] += speakerDistances[jn]*v*g[1];
			
//			System.out.println("ch["+ch1id+"]="+v*g[0]);
//			System.out.println("ch["+ch2id+"]="+v*g[1]);
			
		}
	}

	private boolean solve(float[] g, float[] m, float x, float y) {
		g[0]=m[0]*x+m[1]*y;
		g[1]=m[2]*x+m[3]*y;
		if (g[0]>=0 && g[1]>=0) {
			float n = (float) Math.sqrt(g[0]*g[0]+g[1]*g[1]);
			g[0]/=n;
			g[1]/=n;
			return true;
		}
		return false;
	}

	public abstract void finishFrame();

	@Override
	public void startFrame(int framesize) {
		if (buf == null || buf.length != framesize*channels) buf = new float[framesize*channels];
		else Arrays.fill(buf, 0f);
	}

}
