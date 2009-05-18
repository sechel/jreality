package de.jreality.plugin.basic;

import de.jreality.geometry.BoundingBoxUtility;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphNode;
import de.jreality.scene.SceneGraphPath;
import de.jreality.util.Rectangle3D;
import de.jreality.util.SceneGraphUtility;
import de.varylab.jrworkspace.plugin.Controller;
import de.varylab.jrworkspace.plugin.PluginInfo;

public class BoxAlignedContent extends AbstractContent {

	double size = 5;
		
	SceneGraphComponent alignment=new SceneGraphComponent("alignment");
	
	private SceneGraphComponent oldContent;

	private View view;
	
	public void setContent(SceneGraphNode content) {
		SceneGraphComponent cmp;
		if (content instanceof SceneGraphComponent) cmp = (SceneGraphComponent) content;
		else {
			cmp = new SceneGraphComponent("wrapper");
			SceneGraphUtility.addChildNode(cmp, content);
		}

		if (oldContent != null) alignment.removeChild(oldContent);
		oldContent = cmp;
		alignment.addChild(cmp);
		
		Rectangle3D bds = BoundingBoxUtility.calculateBoundingBox(cmp);
		
		double[] ext = bds.getExtent();
		double objectSize = Math.max(Math.max(ext[0], ext[1]), ext[2]);
		
		double[] c = bds.getCenter();

		MatrixBuilder.euclidean().scale(size/objectSize).translate(-c[0], -c[1], -c[2]).assignTo(alignment);
		
	}

	@Override
	public void install(Controller c) throws Exception {
		view = c.getPlugin(View.class);
		alignment.setAppearance(new Appearance("content app"));
		view.getSceneRoot().addChild(alignment);
		view.setEmptyPickPath(new SceneGraphPath(view.getSceneRoot(), alignment));
	}

	@Override
	public PluginInfo getPluginInfo() {
		return new PluginInfo("aligned content");
	}

	@Override
	protected SceneGraphComponent getToolCmp() {
		return alignment;
	}

	public void contentChanged() {
		setContent(oldContent);
	}

	public Appearance getContentAppearance() {
		return alignment.getAppearance();
	}
	
}
