package de.jreality.ui.sceneview;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;

import de.jreality.geometry.Primitives;
import de.jreality.scene.SceneGraphComponent;

public class SceneViewTest {

	public static void main(String[] args) {

		SceneView view = new SceneView();
		SceneViewContent content = new SceneViewContent();
		SceneViewBackground backgroundManager = new SceneViewBackground();

		content.install(view);
		backgroundManager.install(view);

		SceneGraphComponent sgc = new SceneGraphComponent();
		sgc.setGeometry(Primitives.cube(true));
		content.addContent(sgc);

		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(view.getViewer().getViewingComponent());
		JMenuBar mbar = new JMenuBar();
		JMenu menu = view.getMenu(); 
		menu.add(backgroundManager.getBackgroundColorMenu());
		mbar.add(menu);
		frame.setJMenuBar(mbar);
		frame.pack();
		frame.setVisible(true); 
	}
}
