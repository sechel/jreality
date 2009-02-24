package de.jreality.audio;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import de.jreality.math.Matrix;
import de.jreality.scene.AudioSource;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphNode;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.AudioSource.State;
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

	private int sampleRate;
	private SceneGraphPath microphonePath;
	private Matrix micInvMatrix = new Matrix();
	private List<AudioTreeNode> audioSources = new CopyOnWriteArrayList<AudioTreeNode>(); // don't want to sync traversal
	
	public AudioBackend(SceneGraphComponent root, SceneGraphPath microphonePath, int sampleRate) {
		super(root);
		this.microphonePath = microphonePath;
		this.sampleRate = sampleRate;
		setEntityFactory(new EntityFactory() {
			{
				setUpdateAudioSource(true);
			}

			protected SceneGraphNodeEntity produceAudioSourceEntity(AudioSource g) {
				return new AudioSourceEntity(g);
			}
		});
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
		for (AudioTreeNode node : audioSources) {
			node.processFrame(enc, framesize);
		}
		enc.finishFrame();
	}

	private class AudioTreeNode extends SceneTreeNode implements AudioListener {

		private SoundPath soundPath;
		private Matrix curPos = new Matrix();
		private SceneGraphPath path;
		
		private boolean nodeActive = false;
		private boolean pathActive = false;

		protected AudioTreeNode(AudioSource audio) {
			super(audio);

//			soundPath = new InstantaneousPath(new AudioReader(audio), sampleRate);
			soundPath = new DelayPath(new AudioReader(audio), sampleRate);
			
			audio.addAudioListener(this);
			audioChanged(null);
		}
		
		void processFrame(SoundEncoder enc, int frameSize) {
			if (nodeActive || pathActive) {
				if (path==null) {
					path = toPath();
				}
				path.getMatrix(curPos.getArray());

				// TODO: use a SceneGraphPathObserver to create a new EffectiveAppearance
				// only when appearances were added/removed along the path.
				// TODO: For this we need to extend SceneGraphPathObserver.
				soundPath.setProperties(EffectiveAppearance.create(path));
				pathActive = soundPath.processFrame(enc, frameSize, curPos, micInvMatrix);
			}
		}

		public void audioChanged(AudioEvent ev) {
			nodeActive = ((AudioSource) getNode()).getState() == State.RUNNING;
		}
	}
	
	private class AudioSourceEntity extends SceneGraphNodeEntity implements AudioListener {

		protected AudioSourceEntity(SceneGraphNode node) {
			super(node);
		}

		public void audioChanged(AudioEvent ev) {
			//System.out.println("AudioSourceEntity.audioChanged() "+ev+" src="+ev.getSourceNode());
		}

		protected void addTreeNode(SceneTreeNode tn) {
			super.addTreeNode(tn);
			audioSources.add((AudioTreeNode) tn);
		}

		protected void removeTreeNode(SceneTreeNode tn) {
			super.removeTreeNode(tn);
			audioSources.remove((AudioTreeNode) tn);
		}
	}
}
