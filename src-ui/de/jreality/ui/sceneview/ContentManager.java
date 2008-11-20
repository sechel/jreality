package de.jreality.ui.sceneview;

import javax.swing.event.ChangeListener;

import de.jreality.scene.SceneGraphComponent;

public interface ContentManager {

		public void setContent(SceneGraphComponent content);
		
		public double getContentScale();
		
		public void addChangeListener(ChangeListener listener);
		
		public void removeChangeListener(ChangeListener listener);
}
