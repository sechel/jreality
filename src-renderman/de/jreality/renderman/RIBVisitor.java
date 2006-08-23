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
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;

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
import de.jreality.util.LoggingSystem;


/**
 * A Visitor for writing renderman<sup>TM</sup> rib files. At the moment the following 
 * things do not work as expected:
 * <ul>
 * <li>twoside shading is not supported</li>
 * <li>clipping planes are written but Icould not test them since neither 
 * 3delight<sup>TM</sup>  nor aqsis do support them at the moment</li>
 * <li>lots of other stuff I just did not check...</li>
 * <li>...</li>
 * </ul>
 * Other TODO's (more on the software engineering side)
 *   put constant strings into CommonAttributes so people can write 
 *     into Appearances and "be heard" (or make an interface with AttributeEntity)
 *   add control over global options using (something like) "renderingHints" shader
 *   minimize use of SLShader class as an Appearance attribute:
 *   	as in other backends, the RIB backend shaders should set themselves by reading
 *   	from an effective appearance.  The attributes should be set there.
 *  * @version 1.0
 * @author <a href="mailto:hoffmann@math.tu-berlin.de">Tim Hoffmann</a>
 *
 */
public class RIBVisitor extends SceneGraphVisitor {
	private SceneGraphComponent root;
	private SceneGraphPath cameraPath;
	private double[] world2Camera;
	private Camera camera;
     private int width =640;
    private int height = 480;
    private String ribFileName;
    protected EffectiveAppearance eAppearance;
    private int textureCount = 0;
    private Map<ImageData, String> textures =new HashMap<ImageData, String>();
    private Hashtable pointsets = new Hashtable();
    int pointsetCount = 0;
    private int[] maximumEyeSplits={10};
    boolean insidePointset = false;
    public boolean shadowEnabled = false;
    public static boolean fullSpotLight = false;
    public static boolean retainGeometry = false;
    public static boolean useProxyCommands = true;
    public  String shaderPath = null;
    private  String preamble = "";
    private int rendererType = RIBViewer.TYPE_PIXAR;
    private int currentSignature = Pn.EUCLIDEAN;
    String outputDisplayFormat = "rgb";
    private boolean hasPw = false;
    int whichEye = CameraUtility.MIDDLE_EYE;
    protected Ri ribHelper = new Ri();
    
    /**
     * 
     */
    public RIBVisitor() {
        super();
        eAppearance=EffectiveAppearance.create();
    }
    
    public void setRendererType(int rendererType) {
		this.rendererType = rendererType;
	}
    
    // TODO create an appearance attribute to control following boolean
    boolean copyShader = false;
	public void visit(Viewer viewer, String name)	{
		ribFileName = name;
	    if(!ribFileName.endsWith(".rib"))	ribFileName = ribFileName+".rib";

	    root = viewer.getSceneRoot();
		cameraPath = viewer.getCameraPath();
		camera = CameraUtility.getCamera(viewer);
        rootAppearance = root.getAppearance();
        if (rootAppearance == null) rootAppearance = new Appearance();
		eAppearance = EffectiveAppearance.create();
		eAppearance = eAppearance.create(rootAppearance);

        world2Camera = cameraPath.getInverseMatrix(null);

 		if (copyShader)		writeStandardShader(ribFileName);        	
		if (rootAppearance != null) 	handleRootAppearance();
   
        int index = ribFileName.lastIndexOf('/');
        outputFileName = ribFileName.substring(index+1,ribFileName.length()-3)+"tif";
     
		ribHelper.begin(ribFileName);
		if (camera.isStereo())	{
			// Careful: the rest of the code expects left eye to be rendered first!
			whichEye = CameraUtility.LEFT_EYE;
		    index = ribFileName.lastIndexOf('/');
		    outputFileName = ribFileName.substring(index+1,ribFileName.length()-4)+"L.tif";	
			ribHelper.frameBegin(0);
			render();
			ribHelper.frameEnd();
			whichEye = CameraUtility.RIGHT_EYE;
		    outputFileName = ribFileName.substring(index+1,ribFileName.length()-4)+"R.tif";	
			ribHelper.frameBegin(1);
			render();
			ribHelper.frameEnd();
		} else
			render();
		ribHelper.end();
    }

	private void render() {

        handleGlobalSettings();
     
       // 
        // TODO handle negative far clipping plane (by generating your own matrices?
        // TODO figure out how to specify RI_INFINITY in a RIB file
        ribHelper.clipping(camera.getNear(), (camera.getFar() > 0) ? camera.getFar() : 1000.0 );
        ribHelper.depthOfField(camera.getFStop(), camera.getFocalLength(), camera.getFocus());
       boolean testCameraExplicit = false;
       double aspectRatio = ((double)width)/height;
       if (testCameraExplicit)	{
 //   	   camera.setNear( camera.getNear() * -1);
 //   	   camera.setFar(camera.getFar() * -1);
           // flip the z-direction
           ribHelper.transform(fTranspose(MatrixBuilder.euclidean().scale(1,1,-1).getArray()));
           ribHelper.comment("Home-grown camera transformation");
    	   double[] c2ndc = CameraUtility.getCameraToNDC(camera, 1.0, currentSignature);
    	   ribHelper.concatTransform(fTranspose(c2ndc));
 //   	   camera.setNear( camera.getNear() * -1);
 //   	   camera.setFar( camera.getFar() * -1);
       } else {
           if (camera.isStereo())	{
               	double[] eyeP = CameraUtility.getEyePosition(camera, whichEye);
       			Rectangle2D vp = CameraUtility.getOffAxisViewPort(camera, CameraUtility.getViewport(camera, aspectRatio), eyeP);
       			ribHelper.comment("Testing left eye stereo");
       			// can a stereo camera be non-perspective?
       			ribHelper.projection(camera.isPerspective() ? "perspective" : "orthographic", null);
       			ribHelper.screenWindow(vp);
                ribHelper.concatTransform(fTranspose(MatrixBuilder.euclidean().scale(1,1,-1).getArray()));
       			double[] moveToEye = Rn.inverse(null, 
       					P3.makeTranslationMatrix(null, eyeP, currentSignature ));
       			ribHelper.concatTransform(fTranspose(moveToEye));
           } else {
           		HashMap map = new HashMap();
           		float fov = (float) camera.getFieldOfView();
           		float a = 1.0f;
           		if(camera.isPerspective()) {
           			map.put("fov", new Float(fov));
           			ribHelper.projection("perspective",map);
           		} else {
           			ribHelper.projection("orthographic",map);    
           			a =(float) (1/((Math.tan((Math.PI/180.0)*camera.getFieldOfView()/2.0)*camera.getFocus())));
           			ribHelper.concatTransform(new float[] {a,0,0,0,0,a,0,0,0,0,1,0,0,0,0,1});
           		}        	
                ribHelper.concatTransform(fTranspose(MatrixBuilder.euclidean().scale(1,1,-1).getArray()));
           }    	   
      }
  
       if (whichEye == CameraUtility.LEFT_EYE)	{
    	   ribHelper.archiveBegin("world");
       } else if (whichEye == CameraUtility.RIGHT_EYE)	{
    	   ribHelper.readArchive("world");
    	   return;
       }
        ribHelper.comment("world to camera");
        //       world2Camera = MatrixBuilder.euclidean().scale(1,1,-1).times(world2Camera).getArray();
        ribHelper.concatTransform(fTranspose(world2Camera));
        ribHelper.worldBegin();
       // alpha in the output format means skip over any background settings
        if (outputDisplayFormat != "rgba")		handleBackground();
        new LightCollector(root, this);
        root.accept(this);
        ribHelper.comment("end world to camera");
        ribHelper.worldEnd();
        if (whichEye == CameraUtility.LEFT_EYE)	{
        	ribHelper.archiveEnd();
        	ribHelper.readArchive("world");
        }
	}

	private void handleGlobalSettings() {
		HashMap map = new HashMap();
        map.put("shader", (shaderPath!=null?(shaderPath+":"):"")+".:&");
        ribHelper.option( "searchpath", map);
        
        map.clear();
        map.put("eyesplits",maximumEyeSplits);
        ribHelper.option("limits",map);
		ribHelper.display(outputFileName, "tiff", outputDisplayFormat,null);
        
        ribHelper.format(width,height,1);
        // TODO make this a variable
        ribHelper.shadingRate(1f);        	
        if (preamble != "") ribHelper.readArchive((String) preamble);
	}

	private void handleRootAppearance() {
		shaderPath = (String) eAppearance.getAttribute(CommonAttributes.RMAN_SEARCHPATH_SHADER, "");
		shadowEnabled = eAppearance.getAttribute(CommonAttributes.RMAN_SHADOWS_ENABLED, false);
		currentSignature = eAppearance.getAttribute(CommonAttributes.SIGNATURE, Pn.EUCLIDEAN);
      	outputDisplayFormat = (String) eAppearance.getAttribute(CommonAttributes.RMAN_OUTPUT_DISPLAY_FORMAT, "rgb");
      	preamble = (String) eAppearance.getAttribute(CommonAttributes.RMAN_PREAMBLE, "");
      	System.err.println("Preamble is "+preamble);
	}

	private void handleBackground() {
		HashMap map;
		map = new HashMap();
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
		         ribHelper.imager("background",map);        
		    }       			
		}

			}
	}

	private void writeStandardShader(String name) {
		try {
		    File file = new File(name);
		    System.out.println("writing in  "+name);
		    file = new File(file.getParent(),"transformedpaintedplastic.sl");
		    System.out.println("checking on "+file+" exists "+file.exists());
		    if(!file.exists()) {
		       // file.createNewFile();
		    OutputStream os = new FileOutputStream(file);
		    InputStream is = getClass().getResourceAsStream("transformedpaintedplastic.sl");
		    
		    int c = 0;
		    while((c =is.read())!=-1) {
		        os.write(c);
////                    System.out.print((char)c);
		    }
		    os.close();
		    is.close();
		    }
		} catch (FileNotFoundException e) {
		    e.printStackTrace();
		} catch (IOException e) {
		    e.printStackTrace();
		}
	}
	
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
		ribHelper.attributeBegin();
		ribHelper.concatTransform(fTranspose(w2c));
 	    ribHelper.comment("Disable shadows for background");
	    ribHelper.verbatim("Attribute \"visibility\"  \"int transmission\" [0]");
		ribHelper.surface("constant",null);
		pointPolygon(bkgd, null);
		ribHelper.attributeEnd();
	}

     /**
     * @param cam
     * @return
     */
    public static float[] fTranspose(double[] mat) {
        float[] tmat = new float[16];
        for (int i = 0; i < 4; i++) 
            for (int j = 0;j<4;j++){
                tmat[i + 4*j] = (float) mat[j+4*i];
            }
        return tmat;
    }
    
    public void  visit(SceneGraphComponent c) {
    	if (!c.isVisible()) return;
        ribHelper.comment("Begin "+c.getName());        
        EffectiveAppearance tmp =eAppearance;
        Appearance a = c.getAppearance();
        boolean attrblock = false;
        ribHelper.attributeBegin();
        if(a!= null ) {
            eAppearance = eAppearance.create(a);
 //           if (c.getGeometry() != null)	{
 //               setupShader(eAppearance,CommonAttributes.POLYGON_SHADER);    
 //               attrblock = true;
 //           }
             //FIXME: omit this call to avoid writing attributes twice???
         }
        c.childrenAccept(this);
 //       if (attrblock) 
        ribHelper.comment("End "+c.getName());        
        ribHelper.attributeEnd();
        
        eAppearance= tmp;
    }
     public void visit(Transformation t) {
         double[] mat = t.getMatrix();
          ribHelper.concatTransform(fTranspose(mat));
     }
    /* (non-Javadoc)
     * @see de.jreality.scene.SceneGraphVisitor#visit(de.jreality.scene.Appearance)
     */
    public void visit(Appearance a) {
       /// eAppearance = eAppearance.create(a);
        
        //super.visit(a);
    }

    private void setupShader(EffectiveAppearance eap, String type) {
        // Attribute
        Map m = (Map) eap.getAttribute("rendermanAttribute",null, Map.class);
        if(m!=null) {
            for (Iterator i = m.keySet().iterator(); i.hasNext();) {
                String key = (String) i.next();
                ribHelper.attribute(key,(Map)m.get(key));
            }
        }
        currentSignature = eap.getAttribute(CommonAttributes.SIGNATURE, Pn.EUCLIDEAN);
        Object color = eap.getAttribute(type+"."+CommonAttributes.DIFFUSE_COLOR,CommonAttributes.DIFFUSE_COLOR_DEFAULT);
        float colorAlpha = 1.0f;
        if(color!=Appearance.INHERITED) {
            float[] c =((Color)color).getRGBComponents(null);
            if (c.length == 4) colorAlpha = c[3];
//           ribHelper.color(new float[] {c[0]*colorAlpha,c[1]*colorAlpha,c[2]*colorAlpha});
            c[3] *= currentOpacity;
          ribHelper.color(c);
        }
 
        // interpret rendering hints to decide whether to do object instancing
        // currently there's a problem with instancing combined with face colors 
        // due to a bug in the renderman proserver renderer (I believe so anyway)
        // so I've disabled this feature until I figure that out. -gunn
//        boolean anyDisplayLists = eap.getAttribute(CommonAttributes.ANY_DISPLAY_LISTS,true);
//        boolean manyDisplayLists = eap.getAttribute(CommonAttributes.MANY_DISPLAY_LISTS,false);
        retainGeometry =  eap.getAttribute(CommonAttributes.RMAN_RETAIN_GEOMETRY,false); //false; //anyDisplayLists; // && !manyDisplayLists;
        
       double transparency = eap.getAttribute(type+"."+CommonAttributes.TRANSPARENCY,CommonAttributes.TRANSPARENCY_DEFAULT);
        currentOpacity = 1f - (float)transparency;
		//currentOpacity *= colorAlpha;
        ribHelper.opacity(new float[] 
           {currentOpacity*colorAlpha,currentOpacity*colorAlpha,currentOpacity*colorAlpha});
        //System.out.println("transparency is "+type+" is "+transparency);
        SLShader slShader = (SLShader) eap.getAttribute(type+CommonAttributes.RMAN_DISPLACEMENT,null,SLShader.class);
        if(slShader != null) {
            ribHelper.displacement(slShader.getName(),slShader.getParameters());
        }
        RendermanShader polygonShader =(RendermanShader) ShaderLookup.getShaderAttr(this,eap, "", CommonAttributes.POLYGON_SHADER);        		
        ribHelper.shader(polygonShader);

    }
    /**
     * @param tex
     * @return
     */
    public String writeTexture(Texture2D tex) {
            ImageData data = tex.getImage(); 
            return writeTexture(data);
    }
    public String writeTexture(ImageData data){
        BufferedImage img;
        String noSuffix = (String) textures.get(data);
        for (Iterator iter = ImageIO.getImageWritersByMIMEType("image/tiff"); iter.hasNext(); ) {
         	System.err.println("Writer: "+((ImageWriter) iter.next()).getClass().getName());
        }
        if(noSuffix == null) {
            noSuffix = ribFileName+"_texture"+(textureCount++);
            if (true) {
                // TODO temporary as long as ImageData does not return a propper BufferedImage
                byte[] byteArray = data.getByteArray();
                int dataHeight = data.getHeight();
                int dataWidth = data.getWidth();
                img = new BufferedImage(dataWidth, dataHeight,
                        BufferedImage.TYPE_INT_ARGB);
                WritableRaster raster = img.getRaster();
                int[] pix = new int[4];
                for (int y = 0, ptr = 0; y < dataHeight; y++) {
                    for (int x = 0; x < dataWidth; x++, ptr += 4) {
                        pix[3] = byteArray[ptr + 3];
                        pix[0] = byteArray[ptr];
                        pix[1] = byteArray[ptr + 1];
                        pix[2] = byteArray[ptr + 2];
                        raster.setPixel(x, y, pix);
                    }
                }
            } else {
            	img = (BufferedImage) data.getImage();
            }
            // force alpha channel to be "pre-multiplied"
		    img.coerceData(true);

            boolean worked=true;
			try {
				// TODO: !!!
				//worked = ImageIO.write(img, "TIFF", new File(noSuffix+".tiff"));
				Method cm = Class.forName("javax.media.jai.JAI").getMethod("create", new Class[]{String.class, RenderedImage.class, Object.class, Object.class});
				cm.invoke(null, new Object[]{"filestore", img, noSuffix+".tiff", "tiff"});
//				Statement stm = new Statement(, "create", new Object[]{"filestore", img, noSuffix+".tiff", "tiff"});
//				stm.execute();
			} catch(Throwable e) {
				worked=false;
	            LoggingSystem.getLogger(this).log(Level.CONFIG, "could not write TIFF: "+noSuffix+".tiff", e);
			}
            if (!worked) {
              try {
				worked =ImageIO.write(img, "PNG", new File(noSuffix+".png"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
              if (!worked) 
                  LoggingSystem.getLogger(this).log(Level.CONFIG, "could not write PNG: {0}.png", noSuffix);
            }
        } 
        textures.put(data,noSuffix);
        return noSuffix+".tex";		// should be dependent on the final renderman renderer
        							// prman from Pixar only knows the proprietary "tex" format
    }
    
    public boolean hasProxy(Geometry g)		{
    	if (!useProxyCommands) return false;
    	Object proxy = g.getGeometryAttributes("rendermanProxyCommand");
    	if (proxy != null)	{
    		if  (proxy instanceof String)  {
          		ribHelper.verbatim((String) proxy);
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
     /**
     * @param ds
     * @param r
     */
    private void cylinder(DoubleArray p1, DoubleArray p2, float r) {
        double d[] =new double[3];
//        d[0] =ds[3] - ds[0];
//        d[1] =ds[4] - ds[1];
//        d[2] =ds[5] - ds[2];
        d[0] = p2.getValueAt(0) - p1.getValueAt(0);
        d[1] = p2.getValueAt(1) - p1.getValueAt(1);
        d[2] = p2.getValueAt(2) - p1.getValueAt(2);
        float l =(float) Rn.euclideanNorm(d);
        d[0]/= l;
        d[1]/= l;
        d[2]/= l;
		double[] mat = MatrixBuilder.euclidean().translate(p1.getValueAt(0),p1.getValueAt(1),p1.getValueAt(2)).getMatrix().getArray();
        
        dirToEuler(d);

		double[] rot = MatrixBuilder.euclidean().rotateZ(d[2]).rotateY(d[1]).rotateX(d[0] - Math.PI/2.).getMatrix().getArray();
		
        ribHelper.transformBegin();
        ribHelper.concatTransform(fTranspose(mat));
        ribHelper.cylinder(r,0,l,360,null);
        ribHelper.transformEnd();
    }
        
    
    /* (non-Javadoc)
     * @see de.jreality.scene.SceneGraphVisitor#visit(de.jreality.scene.PointSet)
     */
    public void visit(PointSet g) {
		ribHelper.comment("PointSet "+g.getName());
     	if (!insidePointset)	{
    		// p is not a subclass of PointSet
     		insidePointset = true;
    		if (retainGeometry) {
   	    		Object which = pointsets.get(g);
   	  			if (which != null)	{
     	    		ribHelper.readArchive((String) which);
    			} else {
    				ribHelper.comment("Retained geometry "+g.getName());
    				String finalname = g.getName()+pointsetCount;
    				ribHelper.archiveBegin(finalname);
    				_visit(g);
    				ribHelper.archiveEnd();
    	    		ribHelper.readArchive(finalname);
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
    
    public void _visit(PointSet p) {
        String geomShaderName = (String)eAppearance.getAttribute("geometryShader.name", "");
        if(eAppearance.getAttribute(ShaderUtility.nameSpace(geomShaderName, CommonAttributes.VERTEX_DRAW),CommonAttributes.VERTEX_DRAW_DEFAULT)) {
            int n= p.getNumPoints();
            DataList coord=p.getVertexAttributes(Attribute.COORDINATES);
            if(coord == null) return;
            ribHelper.attributeBegin();
            float r = (float) eAppearance.getAttribute(ShaderUtility.nameSpace(CommonAttributes.POINT_SHADER,CommonAttributes.POINT_RADIUS),CommonAttributes.POINT_RADIUS_DEFAULT);
            //System.out.println("point radius is "+r);
            setupShader(eAppearance,CommonAttributes.POINT_SHADER);
            boolean drawSpheres = eAppearance.getAttribute(CommonAttributes.SPHERES_DRAW,CommonAttributes.SPHERES_DRAW_DEFAULT);
            if(drawSpheres) {
            	    int sig = eAppearance.getAttribute("signature", Pn.EUCLIDEAN);
                 Color cc = (Color) eAppearance.getAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR,  CommonAttributes.DIFFUSE_COLOR_DEFAULT );
                 //DoubleArrayArray a=coord.toDoubleArrayArray();
                 double[][] a=coord.toDoubleArrayArray(null);
                 // TODO pre-multiply color with current alpha
                 // Following is an attempt to do so, but ignores the alpha of the color!
                 float[] raw = new float[4];
                 cc.getRGBComponents(raw);
//                 for (int k=0;k<4; ++k)	raw[k] = (float) (raw[k]*currentOpacity);
// 					cc = new Color(raw[0], raw[1], raw[2], raw[3]); 
                 raw[3] = raw[3] * currentOpacity;
                ribHelper.color(raw);
               double[] trns = new double[16];
                for (int i= 0; i < n; i++) { 
                    trns = MatrixBuilder.init(null, sig).translate(a[i]).getArray();
                    ribHelper.transformBegin();
                    ribHelper.concatTransform(fTranspose(trns));
                    HashMap map =new HashMap();
                    ribHelper.sphere(r,-r,r,360f,map);
                    ribHelper.transformEnd();
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
                ribHelper.points(n,map);
            }
                ribHelper.attributeEnd();       	
        }
    }
    
    boolean testBallStick = true;
	private float currentOpacity;
	private Appearance rootAppearance;
	private String outputFileName;
   /* (non-Javadoc)
     * @see de.jreality.scene.SceneGraphVisitor#visit(de.jreality.scene.IndexedLineSet)
     */
    public void visit(IndexedLineSet g) {
		ribHelper.comment("IndexedLineSet "+g.getName());
		ribHelper.attributeBegin();
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
	     	    		ribHelper.readArchive((String) which);
	    			} else {
	    				ribHelper.comment("Retained geometry "+g.getName());
	    				String finalname = g.getName()+pointsetCount;
	    				ribHelper.archiveBegin(finalname);
	    				_visit(g);
	    				ribHelper.archiveEnd();
	    	    		ribHelper.readArchive(finalname);
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
       	ribHelper.attributeEnd();
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
//                    for (int k=0;k<4; ++k)	raw[k] = (float) (raw[k]*currentOpacity);
                    cc = new Color(raw[0], raw[1], raw[2], raw[3]*currentOpacity); 
                    if (g instanceof IndexedLineSet)	{
	                    BallAndStickFactory bsf = new BallAndStickFactory(g);
	               	  	bsf.setSignature(currentSignature);
	               	  	bsf.setStickRadius(r);
	                	bsf.setShowBalls(false);	// need to actually omit the balls
	               	  	bsf.setStickColor(cc);
	                	bsf.update();
	               	  	visit(bsf.getSceneGraphComponent());
            	   } else {
           				DataList edgec =  g.getEdgeAttributes(Attribute.COLORS);
            		   int n = g.getNumEdges();
            		   ribHelper.color(cc);
            		   double[][] crossSection = TubeUtility.octagonalCrossSection;
            			Object foo = eAppearance.getAttribute("lineShader.crossSection", crossSection);
            			if (foo != crossSection)	{
            				crossSection = (double[][]) foo;
            			}
           		   for (int i = 0; i<n; ++i)	{
             				if (edgec != null) {
               					double[] edgecolor = edgec.item(i).toDoubleArray(null);
               					ribHelper.comment("Edge color");
               					ribHelper.color(edgecolor);
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
            				pointPolygon(tube, null);            			   
            		   }
              	   }
                } else {
                   IntArrayArray edgeIndices= dl.toIntArrayArray();
                   DoubleArrayArray edgeColors=null;
                   dl = g.getEdgeAttributes(Attribute.COLORS);
                   if(dl != null) 
                       edgeColors = dl.toDoubleArrayArray();
                   DoubleArrayArray vertices=g.getVertexAttributes(Attribute.COORDINATES) .toDoubleArrayArray();
                   for (int i= 0, n=edgeIndices.size(); i < n; i++)
                   {
                       if(edgeColors!= null) {
                           float[] f = new float[3];
                           f[0] = (float) edgeColors.getValueAt(i,0);
                           f[1] = (float) edgeColors.getValueAt(i,1);
                           f[2] = (float) edgeColors.getValueAt(i,2);
                           ribHelper.color(f);
                       }
                       IntArray edge=edgeIndices.item(i).toIntArray();
                       for(int j = 0; j<edge.getLength()-1;j++) {
                           DoubleArray p1=vertices.item(edge.getValueAt(j)).toDoubleArray();
                           DoubleArray p2=vertices.item(edge.getValueAt(j+1)).toDoubleArray();
                       //pipeline.processLine(p1, p2);
                       //pipeline.processPseudoTube(p1, p2);
                           cylinder(p1,p2,r);
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
		ribHelper.comment("IndexedFaceSet "+g.getName());
		ribHelper.attributeBegin();
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
	     	    		ribHelper.readArchive((String) which);
	    			} else {
	    				ribHelper.comment("Retained geometry "+g.getName());
	    				String finalname = g.getName()+"_"+pointsetCount;
	    				ribHelper.archiveBegin(finalname);
	    				_visit(g, null);
	    				ribHelper.archiveEnd();
	    	    		ribHelper.readArchive(finalname);
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
     	ribHelper.attributeEnd();
   }
   
 
   /**
    * The second argument here is a short-term solution to an apparent bug in the Renderman renderer
    * which makes it impossible to pass the transparency ("Os") field to the pointspolygon command on a
    * per-face basis.  ("Cs" works on a per face basis but any value for "Os" (even all 1's corresponding to
    * opaque surface) results in odd, incorrect results.  -gunn 9.6.6
    * @param i
    * @param color
    */public void _visit(IndexedFaceSet i, float[] color) {
		String geomShaderName = (String)eAppearance.getAttribute("geometryShader.name", "");
		if(eAppearance.getAttribute(ShaderUtility.nameSpace(geomShaderName, CommonAttributes.FACE_DRAW),true)) {
//			ribHelper.attributeBegin();
//			setupShader(eAppearance,CommonAttributes.POLYGON_SHADER);
		   	DataList colors=i.getFaceAttributes( Attribute.COLORS );
	    	if (colors !=null && currentOpacity != 1.0) { //GeometryUtility.getVectorLength(colors) == 4){
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
				HashMap map = new HashMap();
				//boolean smooth = !((String)eAppearance.getAttribute(CommonAttributes.POLYGON_SHADER,"default")).startsWith("flat");
				boolean smooth = eAppearance.getAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.SMOOTH_SHADING,true);
				DataList coords = i.getVertexAttributes(Attribute.COORDINATES);
				DoubleArrayArray da = coords.toDoubleArrayArray();
				int pointlength = GeometryUtility.getVectorLength(coords);
				// We'd like to be able to use the "Pw" attribute which accepts 4-vectors for point coordinates, but 3Delight-5.0.1
				// does not support it ...  
				// TODO figure out how to allow Pw output if desired, for example, if you have Pixar renderman renderer.
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
		} 
		else { //face normals
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
				//ftex[2*j] =(float)d.getValueAt(j,0);
				//ftex[2*j+1] =(float)d.getValueAt(j,1);
			}
			map.put("uniform color Cs",vCol);
			//if(faceColorLength == 4 ) map.put("uniform color Os",vOp);
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
					ribHelper.opacity(f);
				}	
//					f[0]=thisOpacity*color[0];f[1]=thisOpacity*color[1];f[2]=thisOpacity*color[2];
				// TODO figure out work-around: this doesn't work
				// with PRMAN 12.5
				ribHelper.color(new float[]{color[0], color[1], color[2]});
			}
		ribHelper.pointsPolygons(npolys,nvertices,vertices,map);
		}
	}
    public void visit(ClippingPlane p) {
        ribHelper.clippingPlane(0,0,0,0,0,1);
    }
    /* (non-Javadoc)
     * @see de.jreality.scene.SceneGraphVisitor#visit(de.jreality.scene.UnitSphere)
     */
    public void visit(Sphere s) {
    	if (hasProxy(s)) return;
        setupShader(eAppearance,CommonAttributes.POLYGON_SHADER);
        ribHelper.sphere(1f,-1f,1f,360f,null);
    }
    
    public void visit(Cylinder c) {
        setupShader(eAppearance,CommonAttributes.POLYGON_SHADER);
        ribHelper.cylinder(1f,-1f,1f,360f,null);
        ribHelper.disk(-1f,1f,360f,null);
        ribHelper.disk(1f,1f,360f,null);
        
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
     */
    public void setMaximumEyeSplits(int maximumEyeSplits){
    	this.maximumEyeSplits[0]=maximumEyeSplits;
    }
    
    
    private static void dirToEuler(double r[]) {
        double d =Rn.euclideanNorm(r);
        double x = r[0]/d;
        double y = r[1]/d;
        double z = r[2]/d;
        
        double xrot = 0;
        double zrot = 0;
        double yrot = 0;
        
//  if(x*x+y*y -0.0001> 0.) {
//      xrot =  -Math.acos(z);
//      zrot =  Math.atan2(y,x);
//  }
        if(z*z +x*x -0.000000001> 0.) {
            xrot =  Math.acos(y);
            yrot =  Math.atan2(x,z);
        } else {
            xrot =  (y>0?0:Math.PI);
            yrot = 0;
        }    
        
        r[0] = xrot;
        r[1] = yrot;
        r[2] = zrot;
    }

    public int getRendererType() {
		return rendererType;
	}
}
