package de.jreality.audio;

import java.util.ArrayList;
import java.util.List;

import de.jreality.math.Matrix;
import de.jreality.scene.AudioSource;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphNode;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.event.AudioEvent;
import de.jreality.scene.event.AudioListener;
import de.jreality.scene.proxy.tree.EntityFactory;
import de.jreality.scene.proxy.tree.ProxyTreeFactory;
import de.jreality.scene.proxy.tree.SceneGraphNodeEntity;
import de.jreality.scene.proxy.tree.SceneTreeNode;
import de.jreality.scene.proxy.tree.UpToDateSceneProxyBuilder;
import de.jreality.shader.EffectiveAppearance;

public class AudioBackend extends UpToDateSceneProxyBuilder {

	int samplerate;
	
	class AudioTreeNode extends SceneTreeNode {

		AudioSource audio;
		SoundPath soundPath;
		SampleReader reader;
	
		float[] sampleBuffer;

		Matrix p0 = new Matrix();
		Matrix p1 = new Matrix();
		
		private SceneGraphPath path;

		Matrix previousPosition;
		EffectiveAppearance eapp;
		
		protected AudioTreeNode(AudioSource audio) {
			super(audio);
			this.audio=audio;
			soundPath = new InstantSoundPath();
			reader = AudioReader.createReader(audio, samplerate);
		}
		
		void init(int frameSize) {
			if (sampleBuffer == null || sampleBuffer.length != frameSize) {
				sampleBuffer = new float[frameSize];
			}
			if (path == null) {
				path = toPath();
				eapp = EffectiveAppearance.create(path);
				previousPosition = new Matrix();
				path.getMatrix(previousPosition.getArray());
				previousPosition.multiplyOnLeft(micInvMatrix);
			}	
		}
		
		void encodeSound(SoundEncoder enc, int frameSize) {
			init(frameSize);
			p0.assignFrom(previousPosition);
			path.getMatrix(previousPosition.getArray());
			previousPosition.multiplyOnLeft(micInvMatrix);
			p1.assignFrom(previousPosition);
			int read = reader.read(sampleBuffer, 0, frameSize);
			soundPath.processSamples(sampleBuffer, read, p0, p1);
			enc.encodeSignal(sampleBuffer, read, p0, p1);
		}
	}
	
	private class AudioSourceEntity extends SceneGraphNodeEntity implements AudioListener {

		protected AudioSourceEntity(SceneGraphNode node) {
			super(node);
		}

		public void audioChanged(AudioEvent ev) {
			//System.out.println("AudioSourceEntity.audioChanged() "+ev+" src="+ev.getSourceNode());
		}
		
		@Override
		protected void addTreeNode(SceneTreeNode tn) {
			super.addTreeNode(tn);
			synchronized (audioSources) {
				audioSources.add((AudioTreeNode) tn);				
			}
		}
		
		@Override
		protected void removeTreeNode(SceneTreeNode tn) {
			super.removeTreeNode(tn);
			synchronized (audioSources) {
				audioSources.remove((AudioTreeNode) tn);
			}
		}
	}
	
	List<AudioTreeNode> audioSources = new ArrayList<AudioTreeNode>();
	
	EntityFactory factory = new EntityFactory() {
		{
			setUpdateAudioSource(true);
		}
		@Override
		protected SceneGraphNodeEntity produceAudioSourceEntity(AudioSource g) {
			return new AudioSourceEntity(g);
		}
	};

	private SceneGraphPath microphonePath;
	private Matrix micInvMatrix = new Matrix();
	
	public AudioBackend(SceneGraphComponent root, SceneGraphPath microphonePath, int samplerate) {
		super(root);
		this.microphonePath = microphonePath;
		microphonePath.getInverseMatrix(micInvMatrix.getArray());
		this.samplerate=samplerate;
		setEntityFactory(factory);
		
		setProxyTreeFactory(new ProxyTreeFactory() {
			@Override
			public void visit(AudioSource a) {
				proxyNode = new AudioTreeNode(a);
			}
		});
		super.createProxyTree();
	}
	
	public void encodeSound(SoundEncoder enc, int framesize) {
		enc.startFrame(framesize);
		microphonePath.getInverseMatrix(micInvMatrix.getArray());
		synchronized (audioSources) {
			for (AudioTreeNode node : audioSources) {
				node.encodeSound(enc, framesize);
			}
		}
		enc.finishFrame();
	}
	
}
