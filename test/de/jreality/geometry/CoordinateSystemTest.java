package de.jreality.geometry;

import de.jreality.math.FactoredMatrix;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.tool.ToolSystemViewer;
import de.jreality.ui.viewerapp.ViewerApp;
import de.jreality.util.SceneGraphUtility;

public class CoordinateSystemTest {

  /**
   * for testing
   * (add axes or box as children of a given SceneGraphComponent)
   */
  public static void main(String[] args) {
    
    //create a component
//  SceneGraphComponent component = SphereUtility.tessellatedCubeSphere(2); component.setName("Sphere");
    SceneGraphComponent component = parametricSurface();  //see below

    FactoredMatrix trans = new FactoredMatrix();
    trans.setRotation(Math.PI, 1, 1, 1);
    //trans.setTranslation(1,0,0);
    trans.assignTo(component);
    
    //create coordinate system
    CoordinateSystemFactory coords = new CoordinateSystemFactory(component);
    coords.setAxisScale(0.5);
    
    //display axes and box
    coords.displayAxes();
    coords.displayBox();
        
    //display component
    ToolSystemViewer currViewer = (ToolSystemViewer)ViewerApp.display(component)[1];
    
    
    //--------------------------------------------------
    //for testing the tool methods:
    
    //get paths of camera and object
    SceneGraphPath cameraPath = currViewer.getCameraPath();
    SceneGraphPath objectPath = new SceneGraphPath();
    SceneGraphComponent root = currViewer.getSceneRoot();
    SceneGraphComponent scene = root.getChildComponent(0);
    objectPath.push(root);
    objectPath.push(scene);
    objectPath.push(component);

    
    coords.updateBox(new double[]{-1,-1,1});  //specify a direction
//    coords.updateBox(cameraPath, objectPath);  //specify paths in scene graph
//    not working yet
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
