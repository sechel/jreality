package de.jreality.audio;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.WeakHashMap;

import de.jreality.math.Matrix;
import de.jreality.scene.AudioSource;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.SceneGraphVisitor;

/* TODO: do one traversal of the scene graph before audio rendering begins, in order to avoid the creation
 * of readers, buffers, etc. in the audio rendering callback; the current version sometimes zombifies the
 * jack client on startup.
 */
public class AmbisonicsVisitor extends SceneGraphVisitor {
	
	private static class PathEnc extends AmbisonicsEncoder {
		SceneGraphPath path;
		public PathEnc(SceneGraphPath path) {
			super();
			this.path = path;
		}
		boolean matches(SceneGraphPath p) {
			return path.isEqual(p);
		}
	}
	
	private int sampleRate;
	
	private WeakHashMap<AudioSource, SampleReader> readers = new WeakHashMap<AudioSource, SampleReader>();
	private WeakHashMap<AudioSource, List<PathEnc>> encoderLists = new WeakHashMap<AudioSource, List<PathEnc>>();
	private WeakHashMap<AudioSource, float[]> buffers = new WeakHashMap<AudioSource, float[]>();
	private HashSet<AudioSource> processedSources = new HashSet<AudioSource>();
	
	private SceneGraphPath microphonePath;
	private Matrix inverseMicrophoneMatrix = new Matrix();
	private Matrix pathMatrix = new Matrix();
	
	private SceneGraphComponent root;
	private SceneGraphPath currentPath = new SceneGraphPath(); 
	
	private float[] bw;
	private float[] bx;
	private float[] by;
	private float[] bz;

	private int frameSize;
	
	public AmbisonicsVisitor(int sampleRate) {
		this.sampleRate = sampleRate;
		inverseMicrophoneMatrix.assignIdentity();
	}
	
	public void setRoot(SceneGraphComponent root) {
		this.root = root;
	}
	
	public void setMicrophonePath(SceneGraphPath microphonePath) {
		this.microphonePath = microphonePath;
		if (microphonePath == null) {
			inverseMicrophoneMatrix.assignIdentity();
		}
	}

	public void collateAmbisonics(float bw[], float bx[], float by[], float bz[], int frameSize) {
		this.frameSize = frameSize;
		this.bw = bw;
		this.bx = bx;
		this.by = by;
		this.bz = bz;
		AmbisonicsEncoder.clearBuffers(bw, bx, by, bz, frameSize);
		processedSources.clear();
		if (microphonePath != null) {
			microphonePath.getInverseMatrix(inverseMicrophoneMatrix.getArray());
		}
		root.accept(this);
	}
	
	@Override
	public void visit(SceneGraphComponent c) {
		currentPath.push(c);
		c.childrenAccept(this);
		currentPath.pop();
	}
	
	@Override
	public void visit(AudioSource snd) {
		currentPath.getMatrix(pathMatrix.getArray());
		Matrix currentMatrix = Matrix.times(inverseMicrophoneMatrix, pathMatrix);

		float[] frame = buffers.get(snd);
		if (frame == null || frame.length < frameSize) {
			frame = new float[frameSize];
			buffers.put(snd, frame);
		}
		
		if (!processedSources.contains(snd)) {
			SampleReader r = readers.get(snd);
			if (r == null) {
				r = AudioReader.createReader(snd, sampleRate);
				readers.put(snd, r);
			}
			int read = r.read(frame, 0, frameSize);
			if (read < frameSize) {
				for (int i=read; i<frameSize; i++) frame[i] = 0.0f;
			}
			processedSources.add(snd);
		}
		
		double x = currentMatrix.getEntry(0, 3);
		double y = currentMatrix.getEntry(1, 3);
		double z = currentMatrix.getEntry(2, 3);
		
		List<PathEnc> encList = encoderLists.get(snd);
		if (encList == null) {
			encList = new LinkedList<PathEnc>();
			encoderLists.put(snd, encList);
		}
		PathEnc enc = null;
		loop: for (PathEnc pe : encList) {
			if (pe.matches(currentPath)) {
				enc = pe;
				break loop;
			}
		}
		if (enc == null) {
			enc = new PathEnc(currentPath);
			encList.add(enc);
		}
		// (x, y, z) in graphics corresponds to (-z, -x, y) in Ambisonics.
		enc.addSignal(frame, frameSize, bw, bx, by, bz, (float) -z, (float) -x, (float) y, true);
	}
}
