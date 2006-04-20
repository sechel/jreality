package de.jreality.geometry;

import de.jreality.scene.SceneGraphComponent;
import de.jreality.ui.viewerapp.ViewerApp;
import de.jreality.util.SceneGraphUtility;

public class CoordinateSystemTest {

  /**
   * for testing
   * (add axes or box as children of a given SceneGraphComponent)
   */
  public static void main(String[] args) {
    
    //create a component
//    SceneGraphComponent component = SphereUtility.tessellatedCubeSphere(2);
//    component.setName("Sphere");
    SceneGraphComponent component = parametricSurface();  //see below
    
    //create coordinate system
    CoordinateSystemFactory coords = new CoordinateSystemFactory(component);
    coords.setAxisScale(0.5);
  
    
    //add box as a child
//    component.addChild(coords.getBox());
    component.addChild(coords.getBox(new double[]{-1,-1,-1}));  //specify a direction
    
    //add axes as a child
    component.addChild(coords.getAxes());
      
//    ViewerApp.display(coords.getBox());
    ViewerApp.display(component);
  }
  
  
  
  
  
  private static SceneGraphComponent parametricSurface() {

	  ParametricSurfaceFactory.Immersion immersion = new ParametricSurfaceFactory.Immersion() {

			public void evaluate(double u, double v, double[] xyz, int index) {
				double x = u;
				double y = v;
				
  //edit this line:
				double z = u*v;

				xyz[index + 0] = x;
				xyz[index + 1] = y;
				xyz[index + 2] = z;
			}

			public int getDimensionOfAmbientSpace() {
				return 3;
			}

			public boolean isImmutable() {
				return false;
			}
	  };
	  
	  ParametricSurfaceFactory psf = new ParametricSurfaceFactory();
	  psf.setGenerateVertexNormals( true );
	  psf.setGenerateEdgesFromFaces( true );

	  psf.setUMin( -1 );
	  psf.setUMax(  2 );
	  psf.setVMin( -2 );
	  psf.setVMax(  1 );

	  psf.setImmersion( immersion );
	  psf.update();
	  
	  SceneGraphComponent parametricSurface = SceneGraphUtility.createFullSceneGraphComponent("ParametricSurface");
	  parametricSurface.setGeometry(psf.getIndexedFaceSet());
	  
	  return parametricSurface;
  }

}
