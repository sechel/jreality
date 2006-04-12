package de.jreality.geometry;

import de.jreality.scene.SceneGraphComponent;
import de.jreality.ui.viewerapp.ViewerApp;

public class CoordinateSystemTest {

  /**
   * for testing
   * (add axes or box as children of a given SceneGraphComponent)
   */
  public static void main(String[] args) {
    
    //create a component
    SceneGraphComponent component = SphereUtility.tessellatedCubeSphere(2);
    component.setName("Sphere");
    
    //create coordinate system
    CoordinateSystemFactory coords = new CoordinateSystemFactory(component);
    
    //add box as a child
    component.addChild(coords.getBox());
//    component.addChild(coords.getBox(new double[]{-1,-1,-1}));
    
    //add axes as a child
//    component.addChild(coords.getAxes());
      
//    ViewerApp.display(coords.getBox());
    ViewerApp.display(component);
  }

}
