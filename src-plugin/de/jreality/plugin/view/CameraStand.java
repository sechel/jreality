package de.jreality.plugin.view;

import static de.jreality.geometry.BoundingBoxUtility.calculateBoundingBox;
import static de.jreality.geometry.BoundingBoxUtility.removeZeroExtends;

import java.awt.geom.Rectangle2D;

import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Rn;
import de.jreality.plugin.view.AlignedContent.ContentDelegate;
import de.jreality.plugin.view.View.RunningEnvironment;
import de.jreality.plugin.view.image.ImageHook;
import de.jreality.scene.Camera;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Transformation;
import de.jreality.scene.pick.PickResult;
import de.jreality.scene.tool.Tool;
import de.jreality.scene.tool.ToolContext;
import de.jreality.tools.EncompassTool;
import de.jreality.tools.PointerDisplayTool;
import de.jreality.toolsystem.ToolUtility;
import de.jreality.util.Rectangle3D;
import de.varylab.jrworkspace.plugin.Controller;
import de.varylab.jrworkspace.plugin.Plugin;
import de.varylab.jrworkspace.plugin.PluginInfo;

public class CameraStand extends Plugin implements ContentDelegate {

	private View sceneView;
	private SceneGraphComponent cameraBase;
	private SceneGraphComponent cameraComponent;
	private Camera camera;
	private SceneGraphPath cameraBasePath;
	private SceneGraphPath cameraPath;
	private AlignedContent alignedContent;
	private SceneGraphComponent content;
	private SceneGraphComponent scalingComponent;
	private Rectangle3D bounds;
	private SceneGraphComponent transformationComponent;
	private boolean manageContent;
	private EncompassTool encompassTool;

	public CameraStand() {
		
		this(true);
	}
	
	public CameraStand(boolean manageContent) {
		System.out.println("CameraStand.CameraStand("+manageContent+")");
		this.manageContent = manageContent;
		cameraBase = new SceneGraphComponent("camera base");
		MatrixBuilder.euclidean().translate(0,0,50).assignTo(cameraBase);
		cameraBase.getTransformation().setName("camera base transformation");
		cameraComponent = new SceneGraphComponent("camera");
		cameraComponent.setTransformation(new Transformation("camera transformation"));

		camera = new Camera("camera");
		camera.setNear(0.01);
		camera.setFar(1500);
		camera.setFieldOfView(30);
		cameraComponent.setCamera(camera);

//		cameraLight = new PointLight();
//		cameraLight.setIntensity(.3);
//		cameraLight.setAmbientFake(true);
//		cameraLight.setFalloff(1, 0, 0);
//		cameraLight.setName("camera light");
//		cameraLight.setColor(new Color(255,255,255,255));
//		cameraComponent.setLight(cameraLight);

		cameraBase.addChild(cameraComponent);
		
		if (manageContent) {
			transformationComponent = new SceneGraphComponent("transformation");
			transformationComponent = new SceneGraphComponent("transformation");
			transformationComponent.setTransformation(
					new Transformation("content transformation")
			);
			scalingComponent = new SceneGraphComponent("scaling");
			scalingComponent.setTransformation(new Transformation(
			"scaling transformation")
			);
			transformationComponent.addChild(scalingComponent);
		} else {
			encompassTool = new EncompassTool();
		}
	}

	public void install(View sceneView, AlignedContent alignedContent) {
		this.sceneView = sceneView;
		View.RunningEnvironment environment = sceneView.getRunningEnvironment();
		boolean portal = environment == RunningEnvironment.PORTAL;
		boolean portalRemote = environment == RunningEnvironment.PORTAL_REMOTE;

		if (portal || portalRemote) {
			camera.setOnAxis(false);
			camera.setStereo(true);
			camera.setViewPort(new Rectangle2D.Double(-1, -1, 2, 2));
		}
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
					if (visible && currentPick != null) {
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

		SceneGraphComponent sceneRoot = sceneView.getSceneRoot();
		sceneRoot.addChild(cameraBase);

		cameraBasePath = new SceneGraphPath();
		cameraBasePath.push(sceneRoot);
		cameraBasePath.push(cameraBase);

		cameraPath = new SceneGraphPath();
		cameraPath.push(sceneRoot);
		cameraPath.push(cameraBase);
		cameraPath.push(cameraComponent);
		cameraPath.push(camera);

		sceneView.setCameraPath(cameraPath);

		if (alignedContent != null) {
			this.alignedContent = alignedContent;
			if (manageContent) {
				alignedContent.setContentDelegate(this);
			} else {
				alignedContent.getAppearanceComponent().addTool(encompassTool);
			}
		}
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

	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo();
		info.name = "Camera";
		info.vendorName = "Ulrich Pinkall";
		info.icon = ImageHook.getIcon("Camera-2-48x48.png");
		return info;
	}

	@Override
	public void install(Controller c) throws Exception {
		install(c.getPlugin(View.class), c.getPlugin(AlignedContent.class));
	} 

	@Override
	public void uninstall(Controller c) throws Exception {
		sceneView.setCameraPath(null);
		sceneView.getSceneRoot().removeChild(cameraBase);
		if (manageContent) {
			alignedContent.getAppearanceComponent().removeChild(transformationComponent);
			alignedContent.setContentDelegate(null);
		} else {
			alignedContent.getAppearanceComponent().removeTool(encompassTool);
		}
	}

	public void contentChanged() {
		SceneGraphComponent newContent = alignedContent.getContent();
		if (newContent != content) {
			if (content != null) {
				scalingComponent.removeChild(content);
			}
			if (newContent != null) {
				bounds = calculateBoundingBox(newContent);
				removeZeroExtends(bounds);
				double[] e = bounds.getExtent();
				double[] center = bounds.getCenter();
				double objectSize = Math.max(Math.max(e[0], e[1]), e[2]);
				double contentScale = 20/objectSize;
				center[0] *= -contentScale;
				center[1] *= -contentScale;
				center[2] *= -contentScale;
				Matrix matrix = MatrixBuilder.euclidean().scale(
						contentScale
				).translate(
						center
				).getMatrix();
				matrix.assignTo(scalingComponent);

				// translate contentComponent
				bounds = bounds.transformByMatrix(
						bounds,
						matrix.getArray()
				);
				center = bounds.getCenter();
				Matrix m = MatrixBuilder.euclidean().translate(
						-center[0], 
						-center[1],
						-center[2]
				).getMatrix();
				m.assignTo(transformationComponent);
				bounds = bounds.transformByMatrix(
						bounds,
						m.getArray()
				);				
				scalingComponent.addChild(newContent);
			} else {
				bounds = null;
			}
			content = newContent;
		}
	}

	public Rectangle3D getBounds() {
		return bounds;
	}

	public void setAlignedContent(AlignedContent alignedContent) {
		if (this.alignedContent != null) {
			this.alignedContent.getAppearanceComponent().removeChild(transformationComponent);
		}
		if (alignedContent != null) {
			alignedContent.getAppearanceComponent().addChild(transformationComponent);
		}
		this.alignedContent = alignedContent;
	}
}