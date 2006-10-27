/**
 *
 * This file is part of jReality. jReality is open source software, made
 * available under a BSD license:
 *
 * Copyright (c) 2003-2006, jReality Group: Charles Gunn, Tim Hoffmann, Markus
 * Schmies, Steffen Weissmann.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * - Neither the name of jReality nor the names of its contributors nor the
 *   names of their associated organizations may be used to endorse or promote
 *   products derived from this software without specific prior written
 *   permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 */


package de.jreality.renderman;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;


import de.jreality.geometry.BallAndStickFactory;
import de.jreality.geometry.GeometryUtility;
import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.geometry.IndexedLineSetUtility;
import de.jreality.geometry.PolygonalTubeFactory;
import de.jreality.geometry.TubeUtility;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.renderman.shader.DefaultPolygonShader;
import de.jreality.renderman.shader.RendermanShader;
import de.jreality.renderman.shader.ShaderLookup;
import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
import de.jreality.scene.ClippingPlane;
import de.jreality.scene.Cylinder;
import de.jreality.scene.Geometry;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.PointSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.SceneGraphVisitor;
import de.jreality.scene.Sphere;
import de.jreality.scene.Transformation;
import de.jreality.scene.Viewer;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.AttributeEntityUtility;
import de.jreality.scene.data.DataList;
import de.jreality.scene.data.DoubleArray;
import de.jreality.scene.data.DoubleArrayArray;
import de.jreality.scene.data.IntArray;
import de.jreality.scene.data.IntArrayArray;
import de.jreality.scene.data.StorageModel;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.CubeMap;
import de.jreality.shader.EffectiveAppearance;
import de.jreality.shader.ImageData;
import de.jreality.shader.ShaderUtility;
import de.jreality.shader.Texture2D;
import de.jreality.util.CameraUtility;


/**
 * A {@link de.jreality.scene.SceneGraphVisitor} for writing RenderMan<sup>TM</sup> rib files. 
 * <p>
 * <b>TODO list and Known issues</b>: 
 * <ul>
 * <li> "twoSided", "implode", "flat" polygon shaders not supported</li>
 * <li> Toplevel {@link de.jreality.scene.Appearance} fog attributes not implemented</li>
 * <li> Clipping planes written but not tested</li>
 * <li> Add control over global options using (something like) "renderingHints" shader {@link de.jreality.shader.RenderingHintsShader}</li>
 * <li> Add support for the differences between the various RenderMan renderers (Pixar, 3DLight, Asis)</li>
 * <li> Use the {@link de.jreality.shader.CommonAttributes#RMAN_GLOBAL_INCLUDE_FILE} to set global options: this is 
 * rib file that is read in at the top of the output rib file, and can be used to set all kinds of global
 * variables.  We should minimize trying to handle all these possibilities in this backend.</li>
 * <li> Writing ordinary texture files: 
 * <ul>
 * <li>Currently tries to write TIFF, if can't then writes PNG.</li>
 * <li> When TIFF is written, then it should be compressed (isn't currently)</li>
 * <li> Figure out how to write out the right filename suffix as a shader parameter: prman for example expects .tex format,
 * even though the file we write out is a .tiff file (which gets converted later to .tex)</li>
 * </ul>
 * <li>Writing reflection maps: </li>
 * <li>Make sure users understand what the "rgba" output format implies (no background)</li>
 * <li>Resolve issue with the alpha channel of colors appearing in shaders (see jReality Talk)</li>
 * </ul>
 * @author <a href="mailto:hoffmann@math.tu-berlin.de">Tim Hoffmann</a>, Charles Gunn
 * @see de.jreality.shader.CommonAttributes
 */
public class RIBVisitor extends SceneGraphVisitor {
	private SceneGraphComponent root;
	private SceneGraphPath cameraPath;
	private double[] world2Camera;
	private Camera camera;
    private int width =640;
    private int height = 480;
    private String ribFileName;
    private int[] maximumEyeSplits={10};
    boolean insidePointset = false;		// life gets complicated with appearance-generated tubes and spheres
    protected boolean shadowEnabled = false;
    protected  boolean fullSpotLight = false;
    protected  boolean retainGeometry = false;	// should geometry be saved using "Begin/EndArchive"?
    protected  boolean useProxyCommands = true;
    // user can specify that tubes and spheres drawn by the appearances are to be opaque
    // reqardless of the current transparency value
    protected boolean opaqueTubes = false;
    protected  String shaderPath = null;
    protected boolean handlingProxyGeometry = false;
    private boolean writeShadersToFile = true;
    private  String globalIncludeFile = "";
    private int rendererType = RIBViewer.TYPE_PIXAR;
    private int currentSignature = Pn.EUCLIDEAN;
    private String outputDisplayFormat = "rgb";
    protected String textureFileSuffix = "tex";  // When it's not prman, set to "tiff" probably.
    private boolean hasPw = false;
 
    transient protected EffectiveAppearance eAppearance = EffectiveAppearance.create();
    transient private int textureCount = 0;
    transient private Map<ImageData, String> textures =new HashMap<ImageData, String>();
    transient private Hashtable<PointSet, String> pointsets = new Hashtable<PointSet, String>();
    transient int pointsetCount = 0;
    transient protected Ri ri = new Ri();
    transient int whichEye = CameraUtility.MIDDLE_EYE;

    RenderScript renderScript;
    
 	public void visit(Viewer viewer, String name)	{
 		// handle the file name
		ribFileName = name;
	    if(!ribFileName.endsWith(".rib"))	ribFileName = ribFileName+".rib";
	    
	    // determine files for render-script
	    File ribF = new File(ribFileName);
	    File dir = ribF.getParentFile();
	    renderScript=new RenderScript(dir, ribF.getName(), rendererType);
	    
	    // register standard shaders for render script
	    renderScript.addShader("defaultpolygonshader.sl");
	    renderScript.addShader("constantTexture.sl");
	    
	    
        int index = ribFileName.lastIndexOf(File.separatorChar);
        outputFileName = ribFileName.substring(index+1,ribFileName.length()-3)+"tif";
     
	    root = viewer.getSceneRoot();
		cameraPath = viewer.getCameraPath();
		camera = CameraUtility.getCamera(viewer);
        rootAppearance = root.getAppearance();
        if (rootAppearance == null) rootAppearance = new Appearance();
		eAppearance = EffectiveAppearance.create();
		eAppearance = eAppearance.create(rootAppearance);

        world2Camera = cameraPath.getInverseMatrix(null);

 		if (writeShadersToFile)	{
 			writeStandardShaders(ribFileName);
 		}
		if (rootAppearance != null) handleRootAppearance();
   
		ri.begin(ribFileName);
		if (camera.isStereo())	{
			// Careful: the rest of the code expects left eye to be rendered first!
			whichEye = CameraUtility.LEFT_EYE;
		    index = ribFileName.lastIndexOf(File.separator);
		    outputFileName = ribFileName.substring(index+1,ribFileName.length()-4)+"L.tif";	
			ri.frameBegin(0);
			render();
			ri.frameEnd();
			whichEye = CameraUtility.RIGHT_EYE;
		    outputFileName = ribFileName.substring(index+1,ribFileName.length()-4)+"R.tif";	
			ri.frameBegin(1);
			render();
			ri.frameEnd();
		} else
			render();
		ri.end();
		
		renderScript.dumpScript();
    }

	/**
	 * Does the work in rendering the scene graph.
	 *
	 */
 	private void render() {

        handleGlobalSettings();
     
        // handle the camera model next
       // 
        // TODO handle negative far clipping plane as used in elliptic geometry
        //	(OpenGL supports this) (can we do this here by generating our own matrices?)
        // TODO figure out how to specify RI_INFINITY in a RIB file
        ri.clipping(camera.getNear(), (camera.getFar() > 0) ? camera.getFar() : 1000.0 );
        ri.depthOfField(camera.getFStop(), camera.getFocalLength(), camera.getFocus());
       boolean testCameraExplicit = false;
       double aspectRatio = ((double)width)/height;
       if (testCameraExplicit)	{
    	   // following experiment to set the camera to NDC transformation explicitly without
    	   // using RenderMan commands for doing that.  Doesn't work. -gunn 01.09.06
 //   	   camera.setNear( camera.getNear() * -1);
 //   	   camera.setFar(camera.getFar() * -1);
           // flip the z-direction
           ri.transform(RIBHelper.fTranspose(MatrixBuilder.euclidean().scale(1,1,-1).getArray()));
           ri.comment("Home-grown camera transformation");
    	   double[] c2ndc = CameraUtility.getCameraToNDC(camera, 1.0, currentSignature);
    	   ri.concatTransform(RIBHelper.fTranspose(c2ndc));
 //   	   camera.setNear( camera.getNear() * -1);
 //   	   camera.setFar( camera.getFar() * -1);
       } else {
           if (camera.isStereo())	{
               	double[] eyeP = CameraUtility.getEyePosition(camera, whichEye);
       			Rectangle2D vp = CameraUtility.getOffAxisViewPort(camera, CameraUtility.getViewport(camera, aspectRatio), eyeP);
       			ri.comment("Testing left eye stereo");
       			// can a stereo camera be non-perspective?
       			ri.projection(camera.isPerspective() ? "perspective" : "orthographic", null);
       			ri.screenWindow(vp);
                ri.concatTransform(RIBHelper.fTranspose(MatrixBuilder.euclidean().scale(1,1,-1).getArray()));
       			double[] moveToEye = Rn.inverse(null, 
       					P3.makeTranslationMatrix(null, eyeP, currentSignature ));
       			ri.concatTransform(RIBHelper.fTranspose(moveToEye));
           } else {
           		HashMap<String, Object> map = new HashMap<String, Object>();
           		float fov = (float) camera.getFieldOfView();
           		float a = 1.0f;
           		if(camera.isPerspective()) {
           			map.put("fov", new Float(fov));
           			ri.projection("perspective",map);
           		} else {
           			ri.projection("orthographic",map);    
           			a =(float) (1/((Math.tan((Math.PI/180.0)*camera.getFieldOfView()/2.0)*camera.getFocus())));
           			ri.concatTransform(new float[] {a,0,0,0,0,a,0,0,0,0,1,0,0,0,0,1});
           		}        	
                ri.concatTransform(RIBHelper.fTranspose(MatrixBuilder.euclidean().scale(1,1,-1).getArray()));
           }    	   
      }
  
       if (whichEye == CameraUtility.LEFT_EYE)	{
    	   ri.archiveBegin("world");
       } else if (whichEye == CameraUtility.RIGHT_EYE)	{
    	   ri.readArchive("world");
    	   return;
       }
       // handle the world
        ri.comment("world to camera");
        //       world2Camera = MatrixBuilder.euclidean().scale(1,1,-1).times(world2Camera).getArray();
        ri.concatTransform(RIBHelper.fTranspose(world2Camera));
        ri.worldBegin();
       // alpha in the output format means skip over any background settings
        if (outputDisplayFormat != "rgba")		handleBackground();
        // handle the lights
        new LightCollector(root, this);
        // finally render the scene graph
        root.accept(this);
        ri.worldEnd();
        if (whichEye == CameraUtility.LEFT_EYE)	{
        	ri.archiveEnd();
        	ri.readArchive("world");
        }
	}

	private void handleRootAppearance() {
		maximumEyeSplits[0] = ((Integer) eAppearance.getAttribute(CommonAttributes.RMAN_MAX_EYE_SPLITS, new Integer(maximumEyeSplits[0]))).intValue();
		shaderPath = (String) eAppearance.getAttribute(CommonAttributes.RMAN_SEARCHPATH_SHADER, "");
		textureFileSuffix = (String) eAppearance.getAttribute(CommonAttributes.RMAN_TEXTURE_FILE_SUFFIX, "tex");
//		if(rendererType==RIBViewer.TYPE_PIXAR)
//			  textureFileSuffix = (String) eAppearance.getAttribute(CommonAttributes.RMAN_TEXTURE_FILE_SUFFIX, "tex");
//	    else if(rendererType==RIBViewer.TYPE_3DELIGHT){
//	      textureFileSuffix = (String) eAppearance.getAttribute(CommonAttributes.RMAN_TEXTURE_FILE_SUFFIX, "tex");//"tdl");
//	    }else if(rendererType==RIBViewer.TYPE_AQSIS){
//	      textureFileSuffix = (String) eAppearance.getAttribute(CommonAttributes.RMAN_TEXTURE_FILE_SUFFIX, "tx");
//      }else if(rendererType==RIBViewer.TYPE_PIXIE){
//        textureFileSuffix = (String) eAppearance.getAttribute(CommonAttributes.RMAN_TEXTURE_FILE_SUFFIX, "tx");
//      }
//      else{
//	      System.err.println("no valid rendererType");
//	    }
		shadowEnabled = eAppearance.getAttribute(CommonAttributes.RMAN_SHADOWS_ENABLED, false);
		currentSignature = eAppearance.getAttribute(CommonAttributes.SIGNATURE, Pn.EUCLIDEAN);
      	outputDisplayFormat = (String) eAppearance.getAttribute(CommonAttributes.RMAN_OUTPUT_DISPLAY_FORMAT, "rgb");
      	globalIncludeFile = (String) eAppearance.getAttribute(CommonAttributes.RMAN_GLOBAL_INCLUDE_FILE, "");
      	System.err.println("Preamble is "+globalIncludeFile);
	}

	/**
	 * Write the top of the rib file
	 *
	 */
 	private void handleGlobalSettings() {
		HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("shader", ("".equals(shaderPath)?"":(shaderPath+":"))+".:&");
        ri.option( "searchpath", map);
        
        map.clear();
        map.put("eyesplits",maximumEyeSplits);
        ri.option("limits",map);
		ri.display(new File(outputFileName).getName(), "tiff", outputDisplayFormat,null);
        
        ri.format(width,height,1);
        // TODO make this a variable
        ri.shadingRate(1f);        	
        if (globalIncludeFile != "") ri.readArchive((String) globalIncludeFile);
	}

	/**
	 * Handle background specifications contained in the top-level appearance: skybox,
	 * flat background color, or gradient background colors
	 * TODO: fog
	 * @see de.jreality.shader.RootAppearance
	 */
	private void handleBackground() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		Appearance ap = root.getAppearance();
         if(ap!=null) { 
			if (AttributeEntityUtility.hasAttributeEntity(CubeMap.class,
				CommonAttributes.SKY_BOX, ap)) {
			CubeMap cm = (CubeMap) AttributeEntityUtility
					.createAttributeEntity(CubeMap.class,
							CommonAttributes.SKY_BOX, ap, true);
			RendermanSkyBox.render(this, world2Camera, cm);
		} else {
			Color[] clrs = new Color[1];
		     Object o =  eAppearance.getAttribute(CommonAttributes.BACKGROUND_COLORS, Appearance.DEFAULT, clrs.getClass());
		      if (o != Appearance.DEFAULT && o instanceof Color[])	 {
		    	  clrs = (Color[]) o;
		    	  double sx = eAppearance.getAttribute(CommonAttributes.BACKGROUND_COLORS_STRETCH_X, 1.0);
		    	  double sy = eAppearance.getAttribute(CommonAttributes.BACKGROUND_COLORS_STRETCH_Y, 1.0);
		    	  // insert a polygon at the back of the viewing frustrum
		    	  handleBackgroundColors(clrs, sx, sy, camera, cameraPath.getMatrix(null));
		      }
		      else {
		         Color clr  = (Color) eAppearance.getAttribute(CommonAttributes.BACKGROUND_COLOR,
		         		CommonAttributes.BACKGROUND_COLOR_DEFAULT);
		         float[] f = clr.getRGBColorComponents(null);     
		         map.put("color background", f);
		         ri.imager("background",map);        
		    }       			
		}
	}
}

    /**
     * Handle request for gradient like background image
     * @param colors
     * @param sx
     * @param sy
     * @param camera
     * @param w2c
     */
	private void handleBackgroundColors(Color[] colors, double sx, double sy, Camera camera, double[] w2c) {
		Rectangle2D vp = CameraUtility.getViewport(camera, ((double) width)/height);      	
		double z = camera.getFar() - 10E-4;
		double xmin = sx*vp.getMinX();
		double xmax = sx*vp.getMaxX();
		double ymin = sy*vp.getMinY();
		double ymax = sy*vp.getMaxY();
		double[][] pts = {
				{z*xmin, z*ymin, -z},
				{z*xmax, z*ymin, -z},
				{z*xmax, z*ymax, -z},
				{z*xmin, z*ymax, -z}
		};
		double[][] cd = new double[4][3];
		for (int i = 0; i<4; ++i)		{
			float[] foo = colors[i].getRGBComponents(null);
			for (int  j = 0; j<3; ++j) cd[(i+2)%4][j] = foo[j];
		}
		IndexedFaceSet bkgd = IndexedFaceSetUtility.constructPolygon(pts);
		bkgd.setVertexAttributes(Attribute.COLORS, StorageModel.DOUBLE_ARRAY.array(3).createReadOnly(cd));
		ri.attributeBegin();
		ri.concatTransform(RIBHelper.fTranspose(w2c));
 	    ri.comment("Disable shadows for background");
	    //ri.verbatim("Attribute \"visibility\"  \"int transmission\" [0]");
	    if(rendererType==RIBViewer.TYPE_PIXAR)
	        ri.verbatim("Attribute \"visibility\"  \"int transmission\" [0]");
	      else if(rendererType==RIBViewer.TYPE_3DELIGHT){
	        ri.verbatim("Attribute \"visibility\"  \"string transmission\" \"Os\"");
	      }else if(rendererType==RIBViewer.TYPE_AQSIS)
	        ri.verbatim("Attribute \"visibility\"  \"int transmission\" [0]");
	      else{
	        System.err.println("no valid rendererType");
	      }
		ri.surface("constant",null);
		pointPolygon(bkgd, null);
		ri.attributeEnd();
	}

	private static void writeStandardShaders(String name) {
		RIBHelper.writeShader(name, "defaultpolygonshader.sl");
		RIBHelper.writeShader(name, "constantTexture.sl");
	}

    /* 
     * Visit methods start here
     */
	public void  visit(SceneGraphComponent c) {
    	if (!c.isVisible()) return;
        EffectiveAppearance tmp =eAppearance;
        Appearance a = c.getAppearance();
        ri.attributeBegin(c.getName());
        if(a!= null ) {
            eAppearance = eAppearance.create(a);
         }
        c.childrenAccept(this);
        ri.attributeEnd(c.getName());
        // restore effective appearance
        eAppearance= tmp;
    }
	
    public void visit(Transformation t) {
         double[] mat = t.getMatrix();
          ri.concatTransform(RIBHelper.fTranspose(mat));
     }
 
    public void visit(Appearance a) {
    }

    private void setupShader(EffectiveAppearance eap, String type) {
        Map m = (Map) eap.getAttribute("rendermanAttribute",null, Map.class);
        if(m!=null) {
            for (Iterator i = m.keySet().iterator(); i.hasNext();) {
                String key = (String) i.next();
                ri.attribute(key,(Map)m.get(key));
            }
        }
        // read current values from the effective appearance
        currentSignature = eap.getAttribute(CommonAttributes.SIGNATURE, Pn.EUCLIDEAN);
        opaqueTubes = eap.getAttribute(CommonAttributes.OPAQUE_TUBES_AND_SPHERES, 
        		CommonAttributes.OPAQUE_TUBES_AND_SPHERES_DEFAULT);
        retainGeometry =  eap.getAttribute(CommonAttributes.RMAN_RETAIN_GEOMETRY,false); //false; //anyDisplayLists; // && !manyDisplayLists;
        
		transparencyEnabled = (boolean) eap.getAttribute(type+"."+CommonAttributes.TRANSPARENCY_ENABLED, true);
		double transparency = 0.0;
        if (transparencyEnabled) transparency = eap.getAttribute(type+"."+CommonAttributes.TRANSPARENCY,CommonAttributes.TRANSPARENCY_DEFAULT);

        Object color = eap.getAttribute(type+"."+CommonAttributes.DIFFUSE_COLOR,CommonAttributes.DIFFUSE_COLOR_DEFAULT);
        float colorAlpha = 1.0f;
        if(color!=Appearance.INHERITED) {
            float[] c =((Color)color).getRGBComponents(null);
            if (c.length == 4) colorAlpha = c[3];
            float[] rgb = new float[]{c[0],c[1],c[2]};
            ri.color(rgb);
        }
 
        currentOpacity = 1f - (float)transparency;
        currentOpacity *= colorAlpha;
        if ((handlingProxyGeometry && opaqueTubes)) currentOpacity = 1f;
        ri.opacity(currentOpacity);
        //System.err.println("currentOpacity is "+currentOpacity);
        SLShader slShader = (SLShader) eap.getAttribute(type+CommonAttributes.RMAN_DISPLACEMENT_SHADER,null,SLShader.class);
        if(slShader != null) {
            ri.displacement(slShader.getName(),slShader.getParameters());
        }
        // check to see if RMAN_SURFACE attribute is non-null and is of class SLShader; if so,
        // use it instead of the following call.
        RendermanShader polygonShader =(RendermanShader) ShaderLookup.getShaderAttr(this,eap, "", CommonAttributes.POLYGON_SHADER);        		
        ri.shader(polygonShader);

    }
    /**
     * @param tex
     * @return
     */
    public String writeTexture(Texture2D tex) {      
      ImageData data = tex.getImage();
      return writeTexture(data, tex.getRepeatS(), tex.getRepeatT());
    }

	public String writeTexture(ImageData data, int repeatS, int repeatT) {
		String noSuffix = (String) textures.get(data);
		if(noSuffix == null) {
			String texFileName = "_texture"+(textureCount++);
			noSuffix = ribFileName+texFileName;
		    RIBHelper.writeTexture(data, noSuffix, transparencyEnabled);
			textures.put(data, noSuffix);
			renderScript.addTexture(texFileName, repeatS, repeatT);
         }
		return noSuffix+"."+textureFileSuffix;		// should be dependent on the final renderman renderer
	}
    
    /**
     * Check to see if the Geometry has a proxy rib command attached to it.
     * @param g
     * @return
     */
	public boolean hasProxy(Geometry g)		{
    	if (!useProxyCommands) return false;
    	Object proxy = g.getGeometryAttributes("rendermanProxyCommand");
    	if (proxy != null)	{
    		if  (proxy instanceof String)  {
          		ri.verbatim((String) proxy);
          		//System.err.println("Found proxy "+proxy);
    		}
    		else if (proxy instanceof SceneGraphComponent) {
    			visit((SceneGraphComponent) proxy);
    			//System.err.println("RIBVisitor: Found sgc proxy");
    		}
       		return true;
    	}
    	return false;
     }
    
    public void visit(Geometry g)	{
    	hasProxy(g);
    	super.visit(g);
    	//System.err.println("Visiting geometry RIBVisitor");
    }
      
     public void visit(PointSet g) {
		ri.comment("PointSet "+g.getName());
     	if (!insidePointset)	{
    		// p is not a subclass of PointSet
     		insidePointset = true;
    		if (retainGeometry) {
   	    		Object which = pointsets.get(g);
   	  			if (which != null)	{
     	    		ri.readArchive((String) which);
    			} else {
    				ri.comment("Retained geometry "+g.getName());
    				String finalname = g.getName()+pointsetCount;
    				ri.archiveBegin(finalname);
    				_visit(g);
    				ri.archiveEnd();
    	    		ri.readArchive(finalname);
    				pointsets.put(g, finalname );
    				pointsetCount++;
    			} 
   		}
       		else
    			_visit(g);
   		}
    	else
    			_visit(g);
     	insidePointset = false;
    }
    
    private void _visit(PointSet p) {
        String geomShaderName = (String)eAppearance.getAttribute("geometryShader.name", "");
        if(eAppearance.getAttribute(ShaderUtility.nameSpace(geomShaderName, CommonAttributes.VERTEX_DRAW),CommonAttributes.VERTEX_DRAW_DEFAULT)) {
            int n= p.getNumPoints();
            DataList coord=p.getVertexAttributes(Attribute.COORDINATES);
            if(coord == null) return;
            ri.attributeBegin();
            float r = (float) eAppearance.getAttribute(ShaderUtility.nameSpace(CommonAttributes.POINT_SHADER,CommonAttributes.POINT_RADIUS),CommonAttributes.POINT_RADIUS_DEFAULT);
            //System.out.println("point radius is "+r);
            setupShader(eAppearance,CommonAttributes.POINT_SHADER);
            boolean drawSpheres = eAppearance.getAttribute(CommonAttributes.SPHERES_DRAW,CommonAttributes.SPHERES_DRAW_DEFAULT);
            if(drawSpheres) {
            	    int sig = eAppearance.getAttribute("signature", Pn.EUCLIDEAN);
                 Color cc = (Color) eAppearance.getAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR,  CommonAttributes.DIFFUSE_COLOR_DEFAULT );
                 //DoubleArrayArray a=coord.toDoubleArrayArray();
                 double[][] a=coord.toDoubleArrayArray(null);
                 float[] raw = new float[4];
                 cc.getRGBComponents(raw);
                if (!opaqueTubes) raw[3] = raw[3] * currentOpacity;
                ri.color(raw);
               double[] trns = new double[16];
                for (int i= 0; i < n; i++) { 
                    trns = MatrixBuilder.init(null, sig).translate(a[i]).getArray();
                    ri.transformBegin();
                    ri.concatTransform(RIBHelper.fTranspose(trns));
                    HashMap map =new HashMap();
                    ri.sphere(r,-r,r,360f,map);
                    ri.transformEnd();
                    //pipeline.processPoint(a, i);            
                }
            } else {
                HashMap map =new HashMap();
                double[] pc = new double[3*n];
                coord.toDoubleArray(pc);
                float[] pcf = new float[3*n];
                for (int i = 0; i < pcf.length; i++) {
                    pcf[i] = (float) pc[i];
                }
                map.put("P",pcf);
                map.put("constant float constantwidth",new Float(r));
                ri.points(n,map);
            }
                ri.attributeEnd();       	
        }
    }
    
    boolean testBallStick = true;
	private float currentOpacity;
	private Appearance rootAppearance;
	private String outputFileName;
	private boolean transparencyEnabled;
	private HashMap<ImageData, String> cubeMaps=new HashMap<ImageData, String>();
	private int cubeMapCount;
	private String cubeMapFileSuffix="env";
	
   /* (non-Javadoc)
     * @see de.jreality.scene.SceneGraphVisitor#visit(de.jreality.scene.IndexedLineSet)
     */
    public void visit(IndexedLineSet g) {
		ri.comment("IndexedLineSet "+g.getName());
		ri.attributeBegin();
		setupShader(eAppearance,CommonAttributes.POLYGON_SHADER);
		if (hasProxy((Geometry) g))	{
			insidePointset = false;
		} else {
	    	if (!insidePointset)	{
	      		insidePointset = true;
	    		// p is not a proper subclass of IndexedLineSet
	    		if (retainGeometry) {
	   	    		Object which = pointsets.get(g);
	   	  			if (which != null)	{
	     	    		ri.readArchive((String) which);
	    			} else {
	    				ri.comment("Retained geometry "+g.getName());
	    				String finalname = g.getName()+pointsetCount;
	    				ri.archiveBegin(finalname);
	    				_visit(g);
	    				ri.archiveEnd();
	    	    		ri.readArchive(finalname);
	    				pointsets.put(g, finalname );
	    				pointsetCount++;
	    			} 
	   		}
	       		else
	    			_visit(g);
	   		}
	    	else
	    			_visit(g);			
		}
       	ri.attributeEnd();
    }
    
    private void _visit(IndexedLineSet g)	{
       String geomShaderName = (String)eAppearance.getAttribute("geometryShader.name", "");
         if(eAppearance.getAttribute(ShaderUtility.nameSpace(geomShaderName, CommonAttributes.EDGE_DRAW),true)) {
        
        DataList dl = g.getEdgeAttributes(Attribute.INDICES);
        if(dl!=null){
            boolean tubesDraw = eAppearance.getAttribute(ShaderUtility.nameSpace(CommonAttributes.LINE_SHADER, CommonAttributes.TUBES_DRAW),CommonAttributes.TUBES_DRAW_DEFAULT);
            if (tubesDraw)  {
//           ribHelper.attributeBegin();
//            setupShader(eAppearance,CommonAttributes.LINE_SHADER);
               float r = (float) eAppearance.getAttribute(ShaderUtility.nameSpace(CommonAttributes.LINE_SHADER,CommonAttributes.TUBE_RADIUS),CommonAttributes.TUBE_RADIUS_DEFAULT);
               // A test to get tubes drawn correctly for non-euclidean case (also fixes some problems I've noticed with
               // the euclidean case too.  -gunn 20.04.06
               // can't use the Renderman Cylinder primitive since it doesn't correspond to equidistant surfaces in non-euclidean cases.
               if (testBallStick)		{
                    Color cc = (Color) eAppearance.getAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR,  CommonAttributes.DIFFUSE_COLOR_DEFAULT );
                     //TODO take into account  alpha channel of cc
                     // Following is an attempt to do so, but ignores the alpha of the color!
                    float[] raw = new float[4];
                    cc.getRGBComponents(raw);
                    if (!opaqueTubes) raw[3] *= currentOpacity;
                    cc = new Color(raw[0], raw[1], raw[2], raw[3]); 
                    //System.err.println("opaque tubes is "+opaqueTubes);
                    //System.err.println("alpha channel is "+cc.getAlpha());

                    Object ga = g.getGeometryAttributes(GeometryUtility.QUAD_MESH_SHAPE);
                    
                    if (ga == null || !( ga instanceof Dimension))	{
	                    BallAndStickFactory bsf = new BallAndStickFactory(g);
	               	  	bsf.setSignature(currentSignature);
	               	  	bsf.setStickRadius(r);
	                	bsf.setShowBalls(false);	// need to actually omit the balls
	               	  	bsf.setStickColor(cc);
	                	bsf.update();
	                	handlingProxyGeometry = true;
	               	  	visit(bsf.getSceneGraphComponent());
	               	  	handlingProxyGeometry = false;
            	   } else {
           				DataList edgec =  g.getEdgeAttributes(Attribute.COLORS);
            		   int n = g.getNumEdges();
            		   ri.color(cc);
            		   double[][] crossSection = TubeUtility.octagonalCrossSection;
            			Object foo = eAppearance.getAttribute("lineShader.crossSection", crossSection);
            			if (foo != crossSection)	{
            				crossSection = (double[][]) foo;
            			}
            			for (int i = 0; i<n; ++i)	{
             				if (edgec != null) {
               					double[] edgecolor = edgec.item(i).toDoubleArray(null);
               					ri.comment("Edge color");
               					ri.color(edgecolor);
               				}
             				double[][] oneCurve = null;
            				oneCurve = IndexedLineSetUtility.extractCurve(oneCurve, g, i);
            				PolygonalTubeFactory ptf = new PolygonalTubeFactory(oneCurve);
//            				ptf.setClosed(true);
            				ptf.setCrossSection(crossSection);
//            				ptf.setFrameFieldType(tubeStyle);
            				ptf.setSignature(currentSignature);
            				ptf.setRadius(r);
            				ptf.update();
            				IndexedFaceSet tube = ptf.getTube();
            				handlingProxyGeometry = true;
            				pointPolygon(tube, null);    
            				handlingProxyGeometry = false;
            		   }
              	   }
                } 
//               ribHelper.attributeEnd();
           }
 
         }
        }
 //       super.visit(g);
         _visit((PointSet) g);
     }

    
    
    public void visit(IndexedFaceSet g) {
		ri.comment("IndexedFaceSet "+g.getName());
		ri.attributeBegin();
		setupShader(eAppearance,CommonAttributes.POLYGON_SHADER);
		if (hasProxy((Geometry) g))	{
			insidePointset = false;
		} else {
	    	if (!insidePointset)	{
	      		insidePointset = true;
	  		// p is not a subclass of PointSet
	    		if (retainGeometry) {
	   	    		Object which = pointsets.get(g);
	   	  			if (which != null)	{
	     	    		ri.readArchive((String) which);
	    			} else {
	    				ri.comment("Retained geometry "+g.getName());
	    				String finalname = g.getName()+"_"+pointsetCount;
	    				ri.archiveBegin(finalname);
	    				_visit(g, null);
	    				ri.archiveEnd();
	    	    		ri.readArchive(finalname);
	    				pointsets.put(g, finalname );
	    				pointsetCount++;
	    			} 
	   		}
	       		else
	    			_visit(g, null);
	    		insidePointset = false;
	   		}
	    	else
	    			_visit(g, null);			
		}
     	ri.attributeEnd();
   }
   
 
   /**
    * The second argument here is a short-term solution to an apparent bug in the Renderman renderer
    * which makes it impossible to pass the transparency ("Os") field to the pointspolygon command on a
    * per-face basis.  ("Cs" works on a per face basis but any value for "Os" (even all 1's corresponding to
    * opaque surface) results in odd, incorrect results.  -gunn 9.6.6
    * @param i
    * @param color
    */
    protected void _visit(IndexedFaceSet i, float[] color) {
		String geomShaderName = (String)eAppearance.getAttribute("geometryShader.name", "");
		if(eAppearance.getAttribute(ShaderUtility.nameSpace(geomShaderName, CommonAttributes.FACE_DRAW),true)) {
//			ribHelper.attributeBegin();
//			setupShader(eAppearance,CommonAttributes.POLYGON_SHADER);
		   	DataList colors=i.getFaceAttributes( Attribute.COLORS );
//	    	if (colors !=null && currentOpacity != 1.0) { //GeometryUtility.getVectorLength(colors) == 4){
		    if (colors !=null && GeometryUtility.getVectorLength(colors) == 4){
	    		double[][] colorArray = colors.toDoubleArrayArray(null);
	    		int numFaces=i.getNumFaces();
	    		float[][] colorArrayf= new float[numFaces][4];
	    		for (int k=0;k<numFaces;k++){
	    			for (int j=0;j<4;j++)
	    				colorArrayf[k][j]=(float)colorArray[k][j];
	    		}
	    		IndexedFaceSet[] faceList=IndexedFaceSetUtility.splitIfsToPrimitiveFaces(i);
	    		for (int k=0;k<numFaces;k++){			
	    			pointPolygon(faceList[k],colorArrayf[k]);
	    		}
	    	}
	    	else{       
	    		if (hasProxy((Geometry) i))	{
	    			insidePointset = false;
	    		}
	    		else pointPolygon(i, color);
   		}
//   	    ribHelper.attributeEnd();
   	    	 
   	}
  		if (color == null && insidePointset) _visit((IndexedLineSet) i);
   }

	private void pointPolygon(IndexedFaceSet i, float[] color) {
		int npolys =i.getNumFaces();
		if(npolys!= 0) {
			HashMap<String, Object> map = new HashMap<String, Object>();
			boolean smooth = eAppearance.getAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.SMOOTH_SHADING,true);
			DataList coords = i.getVertexAttributes(Attribute.COORDINATES);
			DoubleArrayArray da = coords.toDoubleArrayArray();
			int pointlength = GeometryUtility.getVectorLength(coords);
			// We'd like to be able to use the "Pw" attribute which accepts 4-vectors for point coordinates, but 3Delight-5.0.1
			// does not support it ...  
			// As of now, now pixar renderer seems to able to handle the "Pw" parameter
			// correctly. 
			// See https://renderman.pixar.com/forum/showthread.php?s=&threadid=5935&highlight=tuberlin 
			// for a bug description
			// As a result, we set hasPw to false.
			if (!hasPw || pointlength == 3)	{
				float[] fcoords =new float[3*da.getLength()];
				for (int j = 0; j < da.getLength(); j++) {
					if (pointlength == 4)	{
						float w =(float)da.getValueAt(j,3);
						if (w != 0) w = 1.0f/w;
						else w = 10E10f;		// hack! but what else can you do?
						fcoords[3*j+0] =(float)da.getValueAt(j,0)*w;
						fcoords[3*j+1] =(float)da.getValueAt(j,1)*w;
						fcoords[3*j+2] =(float)da.getValueAt(j,2)*w;
					} else {
						fcoords[3*j+0] =(float)da.getValueAt(j,0);
						fcoords[3*j+1] =(float)da.getValueAt(j,1);
						fcoords[3*j+2] =(float)da.getValueAt(j,2);
					}
				}
			map.put("P",fcoords);       	
	     } else if (pointlength == 4)	{
			float[] fcoords =new float[4*da.getLength()];
			for (int j = 0; j < da.getLength(); j++) {
			    fcoords[4*j+0] =(float)da.getValueAt(j,0);
			    fcoords[4*j+1] =(float)da.getValueAt(j,1);
			    fcoords[4*j+2] =(float)da.getValueAt(j,2);
			    fcoords[4*j+3] =(float)da.getValueAt(j,3);
			}
			map.put("vertex hpoint P",fcoords);       	
	     }
 		 DataList normals = i.getVertexAttributes(Attribute.NORMALS);
		 if(smooth && normals!=null) {
			da = normals.toDoubleArrayArray();
			int n = da.getLengthAt(0);
			if (n == 4 && currentSignature == Pn.EUCLIDEAN)	{
				throw new IllegalStateException("4D normals only valid with non-euclidean signature");
			}
			float[] fnormals =new float[n*da.getLength()];
			for (int j = 0; j < da.getLength(); j++) {
				fnormals[n*j+0] =(float)da.getValueAt(j,0);
				fnormals[n*j+1] =(float)da.getValueAt(j,1);
				fnormals[n*j+2] =(float)da.getValueAt(j,2);
				if (n == 4){
					fnormals[n*j+3] =(float)da.getValueAt(j,3);					
				}
			}
			if (currentSignature == Pn.EUCLIDEAN) map.put("N",fnormals);
			else map.put("vertex hpoint Nw", fnormals);
		} else { //face normals
			normals = i.getFaceAttributes(Attribute.NORMALS);
			if (normals != null)	{
				da = normals.toDoubleArrayArray();
				int n = da.getLengthAt(0);
				float[] fnormals =new float[n*da.getLength()];
				for (int j = 0; j < da.getLength(); j++) {
					fnormals[n*j+0] =(float)da.getValueAt(j,0);
					fnormals[n*j+1] =(float)da.getValueAt(j,1);
					fnormals[n*j+2] =(float)da.getValueAt(j,2);
					if (n == 4){
						fnormals[n*j+3] =(float)da.getValueAt(j,3);					
					}
				}
				if (currentSignature == Pn.EUCLIDEAN) map.put("uniform normal N",fnormals);
				else map.put("uniform hpoint Nw", fnormals);
			}
		}
		// texture coords:
		DataList texCoords = i.getVertexAttributes(Attribute.TEXTURE_COORDINATES);
		if(texCoords!= null) {
			float[] ftex =new float[2*texCoords.size()];
			for (int j = 0; j < texCoords.size(); j++) {
				//ftex[j] =(float)d.getValueAt(j);
				DoubleArray l =texCoords.item(j).toDoubleArray();
    
				ftex[2*j] =(float)l.getValueAt(0);
				ftex[2*j+1] =(float)l.getValueAt(1);
				//ftex[2*j] =(float)d.getValueAt(j,0);
				//ftex[2*j+1] =(float)d.getValueAt(j,1);
			}
			map.put("st",ftex);
		}
 
		DataList vertexColors = i.getVertexAttributes(Attribute.COLORS);
	   	DataList faceColors=i.getFaceAttributes( Attribute.COLORS );
		if((smooth || faceColors == null) && vertexColors!= null) {
				int vertexColorLength=GeometryUtility.getVectorLength(vertexColors);
				float[] vCol =new float[3*vertexColors.size()];
				float[] vOp=null;
				if(vertexColorLength == 4 ) vOp = new float[3*vertexColors.size()];
				for (int j = 0; j < vertexColors.size(); j++) {
					//ftex[j] =(float)d.getValueAt(j);
				DoubleArray rgba = vertexColors.item(j).toDoubleArray();
    
				vCol[3*j]   =(float)rgba.getValueAt(0);
				vCol[3*j+1] =(float)rgba.getValueAt(1);
				vCol[3*j+2] =(float)rgba.getValueAt(2);
				if(vertexColorLength ==4) {
					vOp[3*j]    =(float)rgba.getValueAt(3);
					vOp[3*j+1]  =(float)rgba.getValueAt(3);
					vOp[3*j+2]  =(float)rgba.getValueAt(3);
				}
				//ftex[2*j] =(float)d.getValueAt(j,0);
				//ftex[2*j+1] =(float)d.getValueAt(j,1);
			}
			map.put("varying color Cs",vCol);
			if(vertexColorLength == 4 ) map.put("varying color Os",vOp);
		}
		else if (faceColors != null)		{
			int faceColorLength=GeometryUtility.getVectorLength(faceColors);
			float[] vCol =new float[3*faceColors.size()];
			float[] vOp=null;
			if(faceColorLength == 4 ) vOp = new float[3*faceColors.size()];
			for (int j = 0; j < faceColors.size(); j++) {
				//ftex[j] =(float)d.getValueAt(j);
				DoubleArray rgba = faceColors.item(j).toDoubleArray();
	
				vCol[3*j]   =(float)rgba.getValueAt(0);
				vCol[3*j+1] =(float)rgba.getValueAt(1);
				vCol[3*j+2] =(float)rgba.getValueAt(2);
				if(faceColorLength ==4) {
					vOp[3*j]    =(float)rgba.getValueAt(3);
					vOp[3*j+1]  =(float)rgba.getValueAt(3);
					vOp[3*j+2]  =(float)rgba.getValueAt(3);
				}
			}
			map.put("uniform color Cs",vCol);
			if(faceColorLength == 4 ) map.put("uniform color Os",vOp);
		}
 
			int[] nvertices =new int[npolys];
			int verticesLength =0;
			for(int k =0; k<npolys;k++) {
				IntArray fi = i.getFaceAttributes(Attribute.INDICES).item(k).toIntArray();
				nvertices[k] =fi.getLength();
				verticesLength+= nvertices[k];
			}
			int[] vertices =new int[verticesLength];
			int l =0;
			for(int k= 0;k<npolys;k++) {
				for(int m =0; m<nvertices[k];m++,l++) {
					IntArray fi = i.getFaceAttributes(Attribute.INDICES).item(k).toIntArray();
					vertices[l] = fi.getValueAt(m);
				}
			}
			// the following implements the work-around mentioned above caused by problems with 
			// setting opacity as a uniform color parameter inside the geometry
			if(color!=null){
				float[] f=new float[3];
				float thisOpacity = 1.0f;
				if (color.length==4){
					thisOpacity = color[3]*currentOpacity;
					f[0]=f[1] = f[2] = thisOpacity ;
					ri.opacity(f);
				}	
//					f[0]=thisOpacity*color[0];f[1]=thisOpacity*color[1];f[2]=thisOpacity*color[2];
				// TODO figure out work-around: this doesn't work
				// with PRMAN 12.5
				// Since there is a bug in PRMAN 12.5: see RenderMan Forums
				// https://renderman.pixar.com/forum/showthread.php?s=&threadid=5935&highlight=tuberlin
				ri.color(new float[]{color[0], color[1], color[2]});
			}
		ri.pointsPolygons(npolys,nvertices,vertices,map);
		}
	}
	// TODO figure out if this works!
    public void visit(ClippingPlane p) {
        ri.clippingPlane(0,0,1,0,0,0);
    }

    public void visit(Sphere s) {
    	if (hasProxy(s)) return;
        setupShader(eAppearance,CommonAttributes.POLYGON_SHADER);
        ri.sphere(1f,-1f,1f,360f,null);
    }
    
    public void visit(Cylinder c) {
    	if (hasProxy(c)) return;
        setupShader(eAppearance,CommonAttributes.POLYGON_SHADER);
        ri.cylinder(1f,-1f,1f,360f,null);
        // TODO Decide whether a jReality Cylinder is closed or not!
        ri.disk(-1f,1f,360f,null);
        ri.disk(1f,1f,360f,null);
        
    }

    /**
     * @return Returns the height.
     */
    public int getHeight() {
        return height;
    }

    /**
     * @param height The height to set.
     */
    public void setHeight(int height) {
        this.height = height;
    }

    /**
     * @return Returns the width.
     */
    public int getWidth() {
        return width;
    }

    /**
     * @param width The width to set.
     */
    public void setWidth(int width) {
        this.width = width;
    }
        
    /**
     * @param maximumEyeSplits.
     * @deprecated Use {@link CommonAttributes#RMAN_MAX_EYE_SPLITS} in root Appearance
     */
    public void setMaximumEyeSplits(int maximumEyeSplits){
    	this.maximumEyeSplits[0]=maximumEyeSplits;
    }
    
    /** 
     * The beginning of support for different renderers: currently not used for anything
     * @param rendererType
     */
    public void setRendererType(int rendererType) {
		this.rendererType = rendererType;
	}
    

    public int getRendererType() {
		return rendererType;
	}

	public String writeCubeMap(CubeMap reflectionMap) {
		String noSuffix = cubeMaps.get(reflectionMap.getTop());
		if(noSuffix == null) {
			String cubeMapFileName = "_cubeMap"+(cubeMapCount++);
			noSuffix = ribFileName+cubeMapFileName;
			cubeMaps.put(reflectionMap.getTop(), noSuffix);
			String top = new File(writeTexture(reflectionMap.getTop(), Texture2D.GL_CLAMP_TO_EDGE, Texture2D.GL_CLAMP_TO_EDGE)).getName();
			String bottom = new File(writeTexture(reflectionMap.getBottom(), Texture2D.GL_CLAMP_TO_EDGE, Texture2D.GL_CLAMP_TO_EDGE)).getName();
			String left = new File(writeTexture(reflectionMap.getLeft(), Texture2D.GL_CLAMP_TO_EDGE, Texture2D.GL_CLAMP_TO_EDGE)).getName();
			String right = new File(writeTexture(reflectionMap.getRight(), Texture2D.GL_CLAMP_TO_EDGE, Texture2D.GL_CLAMP_TO_EDGE)).getName();
			String front = new File(writeTexture(reflectionMap.getFront(), Texture2D.GL_CLAMP_TO_EDGE, Texture2D.GL_CLAMP_TO_EDGE)).getName();
			String back = new File(writeTexture(reflectionMap.getBack(), Texture2D.GL_CLAMP_TO_EDGE, Texture2D.GL_CLAMP_TO_EDGE)).getName();
			renderScript.addReflectionMap(cubeMapFileName, back, front, bottom, top, left, right);
        }
		return noSuffix+"."+cubeMapFileSuffix;
	}
}
