package de.jreality.ui.plugin;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.jreality.geometry.BoundingBoxUtility;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Transformation;
import de.jreality.ui.plugin.image.ImageHook;
import de.jreality.util.Rectangle3D;
import de.jtem.beans.ChangeEventMulticaster;
import de.varylab.jrworkspace.plugin.Controller;
import de.varylab.jrworkspace.plugin.Plugin;
import de.varylab.jrworkspace.plugin.PluginInfo;

public class AlignedContent extends Plugin {

	private View view;
	private SceneGraphComponent sceneRoot;

	private SceneGraphComponent appearanceComponent;
	private SceneGraphComponent transformationComponent;
	private SceneGraphComponent scalingComponent;

	private SceneGraphComponent content;
	private double contentScale = 1;
	private ContentDelegate contentDelegate;
	private Rectangle3D bounds;
	private transient ChangeListener changeListener;


	public static interface ContentDelegate {

		public void setAlignedContent(AlignedContent alignedContent);

		public void contentChanged();

		public double getScale();

		public Rectangle3D getBounds();
	}


	public AlignedContent() {

		appearanceComponent = new SceneGraphComponent("content");
		appearanceComponent.setAppearance(new Appearance("content appearance"));

		transformationComponent = new SceneGraphComponent("transformation");
		transformationComponent.setTransformation(
				new Transformation("content transformation")
		);
		appearanceComponent.addChild(transformationComponent);

		scalingComponent = new SceneGraphComponent("scaling");
		scalingComponent.setTransformation(new Transformation(
		"scaling transformation")
		);
		scalingComponent.setAppearance(new Appearance("scaled appearance"));
		transformationComponent.addChild(scalingComponent);
	}

	public SceneGraphComponent getAppearanceComponent() {
		return appearanceComponent;
	}

	public SceneGraphComponent getTransformationComponent() {
		return transformationComponent;
	}

	public SceneGraphComponent getScalingComponent() {
		return scalingComponent;
	}

	public void setContentDelegate(ContentDelegate delegate) {
		if (contentDelegate != null) {
			contentDelegate.setAlignedContent(null);
		}
		contentDelegate = delegate;
		if (contentDelegate != null) {
			contentDelegate.setAlignedContent(this);
		}
	}

	public SceneGraphComponent getContent() {
		return content;
	}

	public void setContent(final SceneGraphComponent content) {
		if (this.content != content) {
			if (this.content != null) {
				scalingComponent.removeChild(AlignedContent.this.content);
			}
			if (content != null) {
				scalingComponent.addChild(content);
			}
			this.content = content;
			contentChanged();
		}
	}

	public Rectangle3D getBounds() {
		if (contentDelegate != null) {
			return contentDelegate.getBounds();
		} else {
			return bounds;
		}
	}

	public void contentChanged() {
		if (contentDelegate != null) {
			contentDelegate.contentChanged();
			contentScale = contentDelegate.getScale();
		} else {
			bounds =
				BoundingBoxUtility.calculateBoundingBox(content);
			double[] e = bounds.getExtent();
			if (e[0] < 1E-20 || e[1] < 1E-20 || e[2] < 1E-20) {
				return;
			}
			double[] center = bounds.getCenter();
			double objectSize = Math.max(Math.max(e[0], e[1]), e[2]);
			contentScale = 20/objectSize;
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
		}
		fireStateChanged();
	}

	public double getContentScale() {
		return contentScale;
	}

	public void fireStateChanged() {
		if (changeListener != null) {
			changeListener.stateChanged(new ChangeEvent(this));
		}
	}

	public void addChangeListener(ChangeListener l) {
		changeListener = ChangeEventMulticaster.add(changeListener, l);
	}

	public void removeChangeListener(ChangeListener listener) {
		changeListener=ChangeEventMulticaster.remove(changeListener, listener);
	}

	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo();
		info.name = "Content";
		info.vendorName = "Ulrich Pinkall";
		info.isDynamic = false;
		info.icon = ImageHook.getIcon("content.png");
		return info;
	}

	public void install(View view) {
		sceneRoot = view.getSceneRoot();
		sceneRoot.addChild(appearanceComponent);
		SceneGraphPath path = new SceneGraphPath();
		path.push(view.getSceneRoot());
		path.push(appearanceComponent);
		path.push(transformationComponent);
		view.setEmptyPickPath(path);
	}

	@Override
	public void install(Controller c) throws Exception {
		install(c.getPlugin(View.class));
	}

	@Override
	public void uninstall(Controller c) throws Exception {
		view.getSceneRoot().removeChild(appearanceComponent);
	}

}
