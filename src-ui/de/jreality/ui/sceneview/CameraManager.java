package de.jreality.ui.sceneview;

import java.awt.Color;
import java.awt.geom.Rectangle2D;

import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Rn;
import de.jreality.scene.Camera;
import de.jreality.scene.PointLight;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Transformation;
import de.jreality.scene.pick.PickResult;
import de.jreality.scene.tool.Tool;
import de.jreality.scene.tool.ToolContext;
import de.jreality.tools.PointerDisplayTool;
import de.jreality.toolsystem.ToolUtility;
import de.jreality.ui.sceneview.SceneView.RunningEnvironment;

public class CameraManager {

	private static final double DEFAULT_CAMERA_LIGHT_INTENSITY = .3;
	
	private SceneView sceneView;
	private SceneGraphComponent cameraBase;
	private SceneGraphComponent cameraComponent;
	private SceneGraphPath cameraBasePath;
	private PointLight cameraLight;

	private SceneGraphPath cameraPath;

	public CameraManager(SceneView sceneView) {
		this.sceneView = sceneView;
		SceneView.RunningEnvironment environment = sceneView.getRunningEnvironment();
		boolean portal = environment == RunningEnvironment.PORTAL;
		boolean portalRemote = environment == RunningEnvironment.PORTAL_REMOTE;
		SceneGraphComponent sceneRoot = sceneView.getSceneRoot();
		
		cameraBase = new SceneGraphComponent("camera base");
		MatrixBuilder.euclidean().translate(0,0,25).assignTo(cameraBase);
		sceneRoot.addChild(cameraBase);
		
		cameraComponent = new SceneGraphComponent("camera");
		cameraComponent.setTransformation(new Transformation());
		Camera cam = new Camera("camera");
		cam.setNear(0.01);
		cam.setFar(1500);
		if (portal || portalRemote) {
			cam.setOnAxis(false);
			cam.setStereo(true);
			cam.setViewPort(new Rectangle2D.Double(-1, -1, 2, 2));
		}
		cameraComponent.setCamera(cam);
		cameraLight = new PointLight();
		cameraLight.setIntensity(DEFAULT_CAMERA_LIGHT_INTENSITY);
		cameraLight.setAmbientFake(true);
		cameraLight.setFalloff(1, 0, 0);
		cameraLight.setName("camera light");
		cameraLight.setColor(new Color(255,255,255,255));
		cameraComponent.setLight(cameraLight);

		cameraBase.addChild(cameraComponent);

		cameraBasePath = new SceneGraphPath();
		cameraBasePath.push(sceneRoot);
		cameraBasePath.push(cameraBase);
		
		cameraPath = new SceneGraphPath();
		cameraPath.push(sceneRoot);
		cameraPath.push(cameraBase);
		cameraPath.push(cameraComponent);
		cameraPath.push(cam);

		if (portal || portalRemote) {
			cameraBase.addTool(new PointerDisplayTool() {
				{
					setVisible(false);
					setHighlight(true);
				}
				@Override
				public void perform(ToolContext tc) {
					PickResult currentPick = tc.getCurrentPick();
					boolean visible = currentPick != null && currentPick.getPickPath().startsWith(tc.getAvatarPath());
					setVisible(visible);
					if (visible) {
						super.perform(tc);
						// compute length:
						 double[] pickAvatar = ToolUtility.worldToAvatar(tc, currentPick.getWorldCoordinates());
						 Matrix pointer = new Matrix(tc.getTransformationMatrix(AVATAR_POINTER));
						 double f = pointer.getEntry(3, 3);
						 pickAvatar[0]-=pointer.getEntry(0, 3)/f;
						 pickAvatar[1]-=pointer.getEntry(1, 3)/f;
						 pickAvatar[2]-=pointer.getEntry(2, 3)/f;
						 setLength(Rn.euclideanNorm(pickAvatar));
					} 
				}
			});
		} 
		if (portal) {
			try {
				Tool t = (Tool) Class.forName("de.jreality.tools.PortalHeadMoveTool").newInstance();
				cameraComponent.addTool(t);
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
		if (portalRemote) {
			try {
				Tool t = (Tool) Class.forName("de.jreality.tools.RemotePortalHeadMoveTool").newInstance();
				cameraComponent.addTool(t);
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}

		sceneView.setCameraPath(cameraPath);
	}

	public void unInstall() {
		sceneView.setCameraPath(null);
		sceneView.getSceneRoot().removeChild(cameraBase);
	}
	
	public SceneGraphComponent getCameraBase() {
		return cameraBase;
	}

	public SceneGraphComponent getCameraComponent() {
		return cameraComponent;
	}

	public SceneGraphPath getCameraBasePath() {
		return cameraBasePath;
	}
	
	public SceneGraphPath getCameraPath() {
		return cameraPath;
	}

}