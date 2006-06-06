package de.jreality.geometry;

import java.awt.Color;
import java.awt.Font;
import de.jreality.math.FactoredMatrix;
import de.jreality.math.Rn;
import de.jreality.scene.Geometry;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.SceneGraphPathObserver;
import de.jreality.scene.event.TransformationEvent;
import de.jreality.scene.event.TransformationListener;
import de.jreality.scene.tool.ToolSystemViewer;
import de.jreality.ui.viewerapp.ViewerApp;


public class CoordinateSystemTest {

		
  /**
   * for testing
   * (add axes or box as children of a given SceneGraphComponent)
   */
  public static void main(String[] args) {
    
    //create a component
//  SceneGraphComponent component = SphereUtility.tessellatedCubeSphere(2); component.setName("Sphere");
	SceneGraphComponent component = parametricSurface();

    FactoredMatrix trans = new FactoredMatrix();
    trans.setRotation(Math.PI, 1, 1, 1);
    //trans.setTranslation(1,0,0);
    trans.assignTo(component);

    //create coordinate system
    final CoordinateSystemFactory coords = new CoordinateSystemFactory(component);
    //SET PROPERTIES:
    //coords.setAxisScale(0.3);
    //coords.setLabelScale(0.005);
    //coords.showBoxArrows(true);
    //coords.showAxesArrows(false);
    //coords.showLabels(false);
    //coords.setColor(Color.RED);
    coords.setGridColor(Color.GRAY);
    //coords.setLabelColor(Color.MAGENTA);
    //coords.setLabelFont(new Font("Sans Serif", Font.PLAIN, 72));
    
    
    //display axes/box/grid
    coords.showAxes(false);
    coords.showBox(true);
    coords.showGrid(true);
    
    
    //display component
    ToolSystemViewer currViewer = (ToolSystemViewer)ViewerApp.display(component)[1];
    
        
    //COORDINATE SYSTEM BEAUTIFIER:
    //get paths of camera and object
    final SceneGraphPath cameraPath = currViewer.getCameraPath();
    final SceneGraphPath objectPath = new SceneGraphPath();
    SceneGraphComponent root = currViewer.getSceneRoot();
    SceneGraphComponent scene = root.getChildComponent(0);
    objectPath.push(root);
    objectPath.push(scene);
    objectPath.push(component);

    double[] cameraToRoot = cameraPath.getMatrix(null);
    double[] rootToObject = objectPath.getInverseMatrix(null);
    double[] cameraToObject = Rn.times(null, rootToObject, cameraToRoot);
	coords.updateBox(cameraToObject);
    
    //SceneGraphPathObserver cpObserver = new SceneGraphPathObserver(cameraPath);
    SceneGraphPathObserver opObserver = new SceneGraphPathObserver(objectPath);
    opObserver.addTransformationListener(new TransformationListener(){
    	public void transformationMatrixChanged(TransformationEvent ev){
    	    double[] cameraToRoot = cameraPath.getMatrix(null);
    	    double[] rootToObject = objectPath.getInverseMatrix(null);
    	    double[] cameraToObject = Rn.times(null, rootToObject, cameraToRoot);
    		coords.updateBox(cameraToObject);
    	}
    });
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
	  //psf.setMeshSize(5, 5);
	  psf.setGenerateVertexNormals( true );
	  psf.setGenerateEdgesFromFaces( true );

	  psf.setUMin( -1 );
	  psf.setUMax(  2 );
	  psf.setVMin( -2 );
	  psf.setVMax(  1 );

	  psf.setImmersion( immersion );
	  psf.update();
	  
	  SceneGraphComponent parametricSurface = new SceneGraphComponent();
	  parametricSurface.setName("ParametricSurface");
	  Geometry geom = psf.getIndexedFaceSet();
	  geom.setName("parametricSurface");
	  parametricSurface.setGeometry(geom);
	  
	  return parametricSurface;
  }

}