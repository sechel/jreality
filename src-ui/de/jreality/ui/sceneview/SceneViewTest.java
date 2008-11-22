package de.jreality.ui.sceneview;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;

public class SceneViewTest {

	public static void main(String[] args) {
		JFrame frame = new JFrame();
		
		SceneView sceneView = new SceneView();
		SceneViewBackground backgroundManager = new SceneViewBackground();
		backgroundManager.install(sceneView);
		SceneViewContent sceneViewContent = new SceneViewContent();
		sceneViewContent.install(sceneView);
		ContentLoader loader = new ContentLoader(frame);
		ContentAppearanceManager contentAppearanceManager = new ContentAppearanceManager();
		contentAppearanceManager.install(sceneView, sceneViewContent);
		loader.install(sceneViewContent);

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(sceneView.getViewer().getViewingComponent());
		JMenuBar mbar = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		fileMenu.add(loader.getMenuItem());
		mbar.add(fileMenu);
		JMenu viewMenu = sceneView.getMenu(); 
		viewMenu.add(backgroundManager.getBackgroundColorMenu());
		mbar.add(viewMenu);
		frame.setJMenuBar(mbar);
		frame.pack();
		frame.setVisible(true);
		
		JFrame appearanceFrame = new JFrame();
		appearanceFrame.add(contentAppearanceManager.getAppearancePanel());
		appearanceFrame.setSize(250, 300);
		appearanceFrame.setVisible(true);
	}
}
