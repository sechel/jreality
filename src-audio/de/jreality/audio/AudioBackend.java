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

/**
 * 
 * Audio backend; collects audio sources and appearances from the scene graph, keeps them up to date,
 * and provides an audio processing callback.
 *
 */
public class AudioBackend extends UpToDateSceneProxyBuilder {

	public AudioBackend(SceneGraphComponent root, SceneGraphPath microphonePath, int samplerate) {
		super(root);
		this.microphonePath = microphonePath;
		microphonePath.getInverseMatrix(micInvMatrix.getArray());
		this.samplerate=samplerate;
		setEntityFactory(factory);
		
		setProxyTreeFactory(new ProxyTreeFactory() {
			public void visit(AudioSource a) {
				proxyNode = new AudioTreeNode(a);
			}
		});
		super.createProxyTree();
	}
	
	public void processFrame(SoundEncoder enc, int framesize) {
		microphonePath.getInverseMatrix(micInvMatrix.getArray());
		
		enc.startFrame(framesize);
		synchronized (audioSources) {
			for (AudioTreeNode node : audioSources) {
				node.processFrame(enc, framesize);
			}
		}
		enc.finishFrame();
	}
	
	private int samplerate;
	private SceneGraphPath microphonePath;
	private Matrix micInvMatrix = new Matrix();
	private List<AudioTreeNode> audioSources = new ArrayList<AudioTreeNode>();
	
	private class AudioTreeNode extends SceneTreeNode {

		SoundPath soundPath;
		SampleReader reader;
		Matrix curPos = new Matrix();
		SceneGraphPath path;

		protected AudioTreeNode(AudioSource audio) {
			super(audio);

			soundPath = new InstantSoundPath();
			reader = AudioReader.createReader(audio, samplerate);
			path = toPath();
		}
		
		void processFrame(SoundEncoder enc, int frameSize) {
			path.getMatrix(curPos.getArray());
			
			// TODO: use a SceneGraphPathObserver to create a new EffectiveAppearance
			// only when appearances were added/removed along the path.
			// TODO: For this we need to extend SceneGraphPathObserver.
			soundPath.setFromEffectiveAppearance(EffectiveAppearance.create(path));
			soundPath.processFrame(reader, enc, frameSize, curPos, micInvMatrix);
		}
	}
	
	private EntityFactory factory = new EntityFactory() {
		{
			setUpdateAudioSource(true);
		}

		protected SceneGraphNodeEntity produceAudioSourceEntity(AudioSource g) {
			return new AudioSourceEntity(g);
		}
	};
	
	private class AudioSourceEntity extends SceneGraphNodeEntity implements AudioListener {

		protected AudioSourceEntity(SceneGraphNode node) {
			super(node);
		}

		public void audioChanged(AudioEvent ev) {
			//System.out.println("AudioSourceEntity.audioChanged() "+ev+" src="+ev.getSourceNode());
		}
		
		protected void addTreeNode(SceneTreeNode tn) {
			super.addTreeNode(tn);
			synchronized (audioSources) {
				audioSources.add((AudioTreeNode) tn);				
			}
		}
		
		protected void removeTreeNode(SceneTreeNode tn) {
			super.removeTreeNode(tn);
			synchronized (audioSources) {
				audioSources.remove((AudioTreeNode) tn);
			}
		}
	}
}
