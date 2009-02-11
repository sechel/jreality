package de.jreality.tutorial.intro;

import static de.jreality.shader.CommonAttributes.DIFFUSE_COLOR;
import static de.jreality.shader.CommonAttributes.LINE_SHADER;
import static de.jreality.shader.CommonAttributes.OPAQUE_TUBES_AND_SPHERES;
import static de.jreality.shader.CommonAttributes.POINT_RADIUS;
import static de.jreality.shader.CommonAttributes.POINT_SHADER;
import static de.jreality.shader.CommonAttributes.POLYGON_SHADER;
import static de.jreality.shader.CommonAttributes.SMOOTH_SHADING;
import static de.jreality.shader.CommonAttributes.TEXTURE_2D;
import static de.jreality.shader.CommonAttributes.TRANSPARENCY;
import static de.jreality.shader.CommonAttributes.TRANSPARENCY_ENABLED;
import static de.jreality.shader.CommonAttributes.TUBE_RADIUS;

import java.awt.Color;
import java.io.IOException;
import java.net.URL;

import de.jreality.geometry.Primitives;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.reader.Readers;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphNode;
import de.jreality.scene.data.AttributeEntityUtility;
import de.jreality.shader.DefaultGeometryShader;
import de.jreality.shader.DefaultLineShader;
import de.jreality.shader.DefaultPointShader;
import de.jreality.shader.DefaultPolygonShader;
import de.jreality.shader.ImageData;
import de.jreality.shader.RenderingHintsShader;
import de.jreality.shader.ShaderUtility;
import de.jreality.shader.Texture2D;
import de.jreality.tools.PickShowTool;
import de.jreality.ui.viewerapp.ViewerApp;
import de.jreality.util.CameraUtility;
import de.jreality.util.Input;
import de.jreality.util.SceneGraphUtility;

/**
 * This class contains a series of 8 simple introductory examples which mimic the
 * functionality of the 
 * <a href="http://www3.math.tu-berlin.de/jreality/mediawiki/index.php/User_Tutorial"> jReality User Tutorial 
 *</a>.  
 *
 *The program takes an integer argument between 0 and 7 inclusive which identifies which demo is
 * to be shown.  The possible values are:
 * <ul>
 * <li>0	Empty ViewerApp viewer</li>
 * <li>1	ViewerApp with dodecahedron, navigator, and bean shell</li>
 * <li>2	Same as (1) but material properties changed (using {@link Appearance#setAttribute(String, Object)})</li>
 * <li>3	Same as (1) but material properties changed using shader interfaces</li>
 * <li>4	Same as (3) but incorporates stereo</li>
 * <li>5	ViewerApp with white cylinder</li>
 * <li>6	ViewerApp with texture-mapped cylinder</li>
 * <li>7	ViewerApp with scaled, colored cube</li>
 * </ul>
 * 
 * When the sequence of examples is stable, I will probably split it up into 8 different self-contained classes.
 * 
 * @author Charles Gunn
 *
 */
public class ViewerAppDemo {

	private static DefaultGeometryShader dgs;
	private static DefaultLineShader dls;
	private static DefaultPointShader dpts;
	private static RenderingHintsShader rhs;
	private static DefaultPolygonShader dps;
	private final static String	textureFileURL = "http://www3.math.tu-berlin.de/jreality/downloads/data/gridSmall.jpg";

	public static void main(String[] args)	{
		int steps = 0, count = 0;
		if (args != null && args.length > 0) steps = Integer.parseInt(args[0]);
		switch(steps)	{
			case 0:		// 0 empty viewer
			{
				ViewerApp va = ViewerApp.display((SceneGraphNode) null);
			}
				break;
			case 1:		// 1 dodecahedron with navigator
			{
				ViewerApp va = ViewerApp.display(readDodec());
				va.setAttachNavigator(true);
				va.setExternalNavigator(false);
				va.update();
				CameraUtility.encompass(va.getCurrentViewer());
			}
				break;
			case 2:		// 2 change material properties using setAttribute()
			{
				SceneGraphComponent dodecSGC = readDodec();
				ViewerApp va = myViewerApp(dodecSGC);
				va.update();
				CameraUtility.encompass(va.getViewerSwitch());
				Appearance ap = dodecSGC.getAppearance();
				// change the color and size of the tubes and spheres
				// do so without using shader interfaces
				ap.setAttribute(LINE_SHADER+"."+DIFFUSE_COLOR, Color.yellow);
				ap.setAttribute(LINE_SHADER+"."+TUBE_RADIUS, .05);
				ap.setAttribute(POINT_SHADER+"."+DIFFUSE_COLOR, Color.red);
				ap.setAttribute(POINT_SHADER+"."+POINT_RADIUS, .1);
				ap.setAttribute(POLYGON_SHADER+"."+SMOOTH_SHADING, false);
				// turn on transparency for faces but keep tubes and spheres opaque
				ap.setAttribute(TRANSPARENCY_ENABLED, true);
				ap.setAttribute(OPAQUE_TUBES_AND_SPHERES, true);
				ap.setAttribute(POLYGON_SHADER+"."+TRANSPARENCY, .4);
			}
				break;
			case 3:	// 3 same as 2, using shader interfaces	
			case 4:	// same as 3 but also with stereo camera, and no faces shown
			{					
				SceneGraphComponent dodecSGC = readDodec();
				ViewerApp va = myViewerApp(dodecSGC);
				va.update();
				CameraUtility.encompass(va.getViewerSwitch());
				dodecSGC.addTool(new PickShowTool());
				Appearance ap = dodecSGC.getAppearance();
				dgs = ShaderUtility.createDefaultGeometryShader(ap, true);
				dls = (DefaultLineShader) dgs.createLineShader("default");
				dls.setDiffuseColor(Color.yellow);
				dls.setTubeRadius(.05);
				dpts = (DefaultPointShader) dgs.createPointShader("default");
				dpts.setDiffuseColor(Color.red);
				dpts.setPointRadius(.1);
				dps = (DefaultPolygonShader) dgs.createPolygonShader("default");
				dps.setSmoothShading(false);
				rhs = ShaderUtility.createDefaultRenderingHintsShader(ap, true);
				rhs.setTransparencyEnabled(true);
				rhs.setOpaqueTubesAndSpheres(true);
				dps.setTransparency(.5);			
				if (steps == 4) {
					dgs.setShowFaces(false);
					CameraUtility.getCamera(va.getViewerSwitch()).setStereo(true);					
				}
			}
				break;
			case 5:		// 5 cylinder without texture, activate bean shell
			case 6:		// 6 add texture
			{	  
				SceneGraphComponent myscene = SceneGraphUtility.createFullSceneGraphComponent("myscene");
				myscene.setGeometry(Primitives.cylinder(20));
				ViewerApp va = myViewerApp(myscene);
				va.update();
				CameraUtility.encompass(va.getViewerSwitch());
				Appearance ap = myscene.getAppearance();
				dgs = ShaderUtility.createDefaultGeometryShader(ap, true);
				dgs.setShowLines(false);
				dgs.setShowPoints(false);
				dps = (DefaultPolygonShader) dgs.createPolygonShader("default");
				dps.setDiffuseColor(Color.white);
				if (steps == 6)	{	
					// following code shows 2 different ways to create texture, one based on URL and 
					// the other based on file associated to the java package.
					// If the first fails, try the second.  
					Texture2D tex2d = null;
//					try {
//						tex2d = TextureUtility.createTexture(ap, POLYGON_SHADER,textureFileURL);
//					} catch (IOException e) {
//					}
					// DO it this way since the previous doesn't seem to throw an IOException even if the
					// URL cannot be loaded
					if (tex2d == null || tex2d.getImage() == null || tex2d.getImage().getWidth() == -1)	{
						tex2d = (Texture2D) AttributeEntityUtility.createAttributeEntity(Texture2D.class, 
								POLYGON_SHADER+"."+TEXTURE_2D,ap, true);
						URL is = ViewerAppDemo.class.getResource("gridSmall.jpg");
						ImageData id = null;
						try {
							id = ImageData.load(new Input(is));
						} catch (IOException ee) {
							ee.printStackTrace();
						}
					    tex2d.setImage(id);					
					}
					Matrix foo = new Matrix();
					MatrixBuilder.euclidean().scale(10, 5, 1).assignTo(foo);
					tex2d.setTextureMatrix(foo);
				}
			}				
				break;
			case 7:
			{		// 7 colored cube
				SceneGraphComponent myscene = SceneGraphUtility.createFullSceneGraphComponent("myscene");
				myscene.setGeometry(Primitives.coloredCube());
				ViewerApp va = myViewerApp(myscene);
				va.update();
				MatrixBuilder.euclidean().scale(2,.8,1).assignTo(myscene);
			}				
			break;
		}
	}

	private static SceneGraphComponent readDodec() {
		URL url = ViewerAppDemo.class.getResource("dodec.off");
		SceneGraphComponent scp = null;
		try {
			scp = Readers.read(Input.getInput(url));
// alternative to access the file as a URL
//			scp = Readers.read(Input.getInput("http://www3.math.tu-berlin.de/jreality/download/data/dodec.off"));
			scp.setName("Dodecahedron");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return scp;
	}

	private static ViewerApp myViewerApp(SceneGraphComponent myscene) {
		ViewerApp va = ViewerApp.display(myscene);
		va.setAttachNavigator(true);
		va.setExternalNavigator(false);
		return va;
	}



}
