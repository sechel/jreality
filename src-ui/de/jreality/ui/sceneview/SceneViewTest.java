package de.jreality.ui.sceneview;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;

public class SceneViewTest {

	public static void main(String[] args) {
		JFrame frame = new JFrame();
		
		SceneView view = new SceneView();
		SceneViewBackground backgroundManager = new SceneViewBackground();
		backgroundManager.install(view);
		SceneViewContentManager contentManager = new SceneViewContentManager();
		contentManager.install(view);
		SceneContentLoader loader = new SceneContentLoader(frame);
		loader.install(contentManager);

//		SceneGraphComponent sgc = new SceneGraphComponent();
//		sgc.setGeometry(Primitives.cube(true));
//		view.getContentParent().addChild(sgc);


		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(view.getViewer().getViewingComponent());
		JMenuBar mbar = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		fileMenu.add(loader.getMenuItem());
		mbar.add(fileMenu);
		JMenu viewMenu = view.getMenu(); 
		viewMenu.add(backgroundManager.getBackgroundColorMenu());
		mbar.add(viewMenu);
		frame.setJMenuBar(mbar);
		frame.pack();
		frame.setVisible(true); 
	}
}
