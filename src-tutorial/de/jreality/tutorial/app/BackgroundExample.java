package de.jreality.tutorial.app;

import java.io.IOException;

import de.jreality.geometry.Primitives;
import de.jreality.plugin.JRViewer;
import de.jreality.scene.Appearance;
import de.jreality.shader.ImageData;
import de.jreality.shader.RootAppearance;
import de.jreality.shader.ShaderUtility;
import de.jreality.util.Input;

public class BackgroundExample {


	   public static void main(String[] args) {
	      JRViewer.display(Primitives.coloredCube());
	      JRViewer view = JRViewer.getLastJRViewer();
	      ImageData id = null;
	      try {
	         id = ImageData.load(Input.getInput("textures/grid.jpeg"));
	      } catch (IOException e) {
	         e.printStackTrace();
	      }
	      Appearance rootApp = view.getViewer().getSceneRoot().getAppearance();
	      RootAppearance ra = ShaderUtility.createRootAppearance(rootApp);
	      ra.createBackgroundTexture2d().setImage(id);
	      
	   }


}
