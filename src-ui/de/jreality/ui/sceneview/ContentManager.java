package de.jreality.ui.sceneview;

import javax.swing.event.ChangeListener;

import de.jreality.scene.SceneGraphComponent;

public interface ContentManager {

		public void setContent(SceneGraphComponent content);
		
		public void alignContent();
		
		public void setContentSize(double size);
		
		public double getContentScale();
		
		public void addChangeListener(ChangeListener listener);
		
		public void removeChangeListener(ChangeListener listener);
		
		public void setPath(
				SceneGraphComponent alignmentComponent,
				SceneGraphComponent contentParent
		);
}
