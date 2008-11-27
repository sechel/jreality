package de.jreality.ui.sceneview;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;

import de.jreality.geometry.Primitives;
import de.jreality.scene.SceneGraphComponent;

public class SceneViewTest {

	public static void main(String[] args) {

		SceneView sceneView = new SceneView();
		new CameraManager(sceneView);
		new LightManager(sceneView);

		Background background = new Background(sceneView);
		Content content = new Content();
		sceneView.getSceneRoot().addChild(content.getAlignmentComponent());
		new ContentTools(content);
		//ContentLoader loader = new ContentLoader(sceneView, content);
		ContentAppearance contentAppearance = new ContentAppearance(sceneView, content);

		SceneGraphComponent s = new SceneGraphComponent();
		s.setGeometry(Primitives.cube(true));
		content.setContent(s);
		
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(sceneView.getViewer().getViewingComponent());
		JMenuBar mbar = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		//fileMenu.add(loader.getMenuItem());
		mbar.add(fileMenu);
		JMenu viewMenu = sceneView.getMenu(); 
		viewMenu.add(background.getMenu());
		mbar.add(viewMenu);
		frame.setJMenuBar(mbar);
		frame.pack();
		frame.setVisible(true);
		
		JFrame appearanceFrame = new JFrame();
		appearanceFrame.add(contentAppearance.getPanel());
		appearanceFrame.setSize(250, 300);
		appearanceFrame.setVisible(true);
	}
}
