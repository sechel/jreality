/*
 * Created on 09.02.2004
 *
 * This file is part of the jReality package.
 * 
 * This program is free software; you can redistribute and/or modify 
 * it under the terms of the GNU General Public License as published 
 * by the Free Software Foundation; either version 2 of the license, or
 * any later version.
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITTNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License 
 * along with this program; if not, write to the 
 * Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307
 * USA 
 */
package de.jreality.renderman;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
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
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import javax.imageio.ImageIO;

import com.sun.opengl.util.FileUtil;

import de.jreality.geometry.BallAndStickFactory;
import de.jreality.geometry.GeometryUtility;
import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.geometry.IndexedLineSetUtility;
import de.jreality.geometry.PolygonalTubeFactory;
import de.jreality.geometry.TubeUtility;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.renderman.shader.RendermanShader;
import de.jreality.renderman.shader.ShaderLookup;
import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
import de.jreality.scene.ClippingPlane;
import de.jreality.scene.Cylinder;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.PointSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.SceneGraphVisitor;
import de.jreality.scene.Sphere;
import de.jreality.scene.Transformation;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.AttributeEntityUtility;
import de.jreality.scene.data.DataList;
import de.jreality.scene.data.DoubleArray;
import de.jreality.scene.data.DoubleArrayArray;
import de.jreality.scene.data.IntArray;
import de.jreality.scene.data.IntArrayArray;
import de.jreality.scene.data.StorageModel;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.EffectiveAppearance;
import de.jreality.shader.ImageData;
import de.jreality.shader.ShaderUtility;
import de.jreality.shader.Texture2D;
import de.jreality.util.CameraUtility;


/**
 * A Visitor for writing renderman<sup>TM</sup> rib files. At the moment the following 
 * things do not work as expected:
 * <ul>
 * <li>twoside shading is not supported</li>
 * <li>object transparency is not multiplied with vertex alpha. the latter will override
 * if present</li>
 * <li>clipping planes are written but Icould not test them since neither 
 * 3delight<sup>TM</sup>  nor aqsis do support them at the moment</li>
 * <li>lots of other stuff I just did not check...</li>
 * <li>...</li>
 * </ul>
 * Other TODO's (more on the software engineering side)
 *   put constant strings into CommonAttributes so people can write 
 *     into Appearances and "be heard" (or make an interface with AttributeEntity)
 *   add control over global options using "renderingHints" shader
 *   Reduce use of static fields here and in Ri.java (not thread safe!)
 *   minimize use of SLShader class as an Appearance attribute:
 *   	as in other backends, the RIB backend shaders should set themselves by reading
 *   	from an effective appearance.  The attributes should be set there.
 *  * @version 1.0
 * @author <a href="mailto:hoffmann@math.tu-berlin.de">Tim Hoffmann</a>
 *
 */
public class RIBVisitor extends SceneGraphVisitor {
    private int width =640;
    private int height = 480;
    private String name;
    protected EffectiveAppearance eAppearance;
    private int textureCount = 0;
    private Map textures =new HashMap();
    private Hashtable pointsets = new Hashtable();
    int pointsetCount = 0;
    private String proj = "perspective";
    private int[] maximumEyeSplits={10};
    boolean insidePointset = false;
    // setting this true doesn't seem to work with the Renderman renderer.
    public static boolean fullSpotLight = false;
    public static boolean retainGeometry = false;		// use "ObjectBegin/End"?
    public static String shaderPath = null;
    private int rendererType = RIBViewer.TYPE_PIXAR;
    // features related to renderer type
    private boolean hasPw;
    
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
    
	public void visit(SceneGraphComponent root, SceneGraphPath path, String name) {
        //SceneGraphPath path =SceneGraphPath.getFirstPathBetween(root,camera);
    	
          Camera camera =(Camera) path.getLastElement();
        this.name =name;
        double[] world2Camera =path.getInverseMatrix(null);
        try {
            File file = new File(name);
            System.out.println("writing in  "+file);
            file = new File(file.getParent(),"transformedpaintedplastic.sl");
            System.out.println("checking on "+file+" exists "+file.exists());
            if(!file.exists()) {
               // file.createNewFile();
            OutputStream os = new FileOutputStream(file);
            InputStream is = getClass().getResourceAsStream("transformedpaintedplastic.sl");
            
            int c = 0;
            while((c =is.read())!=-1) {
                os.write(c);
////                System.out.print((char)c);
            }
            os.close();
            is.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(!name.endsWith(".rib"))
            name = name+".rib";
        Ri.begin(name);
      Appearance ap = root.getAppearance();
      if (ap != null) {
      		Object global = ap.getAttribute(CommonAttributes.RMAN_SEARCHPATH_SHADER);
      		if (global instanceof String)	{
      			shaderPath = (String) global;
      		}
      }
      
        HashMap map = new HashMap();
        map.put("shader", (shaderPath!=null?(shaderPath+":"):"")+".:&");
        //map.put("shader", (fullSpotLight!=null?(fullSpotLight+":"):"")+".:&");
        Ri.option( "searchpath", map);
        
        HashMap map2 = new HashMap();
        map2.put("eyesplits",maximumEyeSplits);
        Ri.option("limits",map2);
        //We ensured that name ends with .rib so :
        String outputName = name.substring(0,name.length()-3)+"tif";
        String outputDisplayFormat = "rgb";
        Object foo = ap.getAttribute(CommonAttributes.RMAN_OUTPUT_DISPLAY_FORMAT);
        if (foo != null && foo instanceof String)		{
        	outputDisplayFormat = (String) foo;
        }
        Ri.display(outputName, "tiff", outputDisplayFormat,null);
        
//        Appearance ap = root.getAppearance();
//        if (ap != null) {
//        	Object global = ap.getAttribute(CommonAttributes.RMAN_GLOBAL_INCLUDE_FILE);
//        	if (global instanceof String)	{
//        		Ri.archiveRecord((String) global);
//        	}
//        }
        
        
        Ri.format(width,height,1);
        System.out.println("ww "+width+" hh "+height);
        Ri.shadingRate(1f);        	
        foo = ap.getAttribute(CommonAttributes.RMAN_PREAMBLE);
        if (foo != null && foo instanceof String)	Ri.verbatim((String) foo);
     
        map = new HashMap();
        float fov = (float) camera.getFieldOfView();
        float a = 1.0f;
        Ri.clipping(camera.getNear(), camera.getFar());
        if(proj.equals("perspective")) {
            map.put("fov", new Float(fov));
            Ri.projection(proj,map);
        } else {
            Ri.projection(proj,map);    
            a =(float) (1/((Math.tan((Math.PI/180.0)*camera.getFieldOfView()/2.0)*camera.getFocus())));
        }
        Ri.transform(new float[] {a,0,0,0,0,a,0,0,0,0,1,0,0,0,0,1});
        // flip the z-direction
        Ri.concatTransform(fTranspose(MatrixBuilder.euclidean().scale(1,1,-1).getArray()));
       
        Ri.comment("world to camera");
        //       world2Camera = MatrixBuilder.euclidean().scale(1,1,-1).times(world2Camera).getArray();
        Ri.concatTransform(fTranspose(world2Camera));
        Ri.worldBegin();
       // alpha in the output format means skip over any background settings
        if (outputDisplayFormat != "rgba")	{
            map = new HashMap();
            Color col = Color.WHITE;
           if(ap!=null) { 
        	         Object o = ap.getAttribute(CommonAttributes.BACKGROUND_COLORS);
	      	          if (o != null && o instanceof Color[])	
	      	        	  // insert a polygon at the back of the viewing frustrum
  	      	        	  handleBackgroundColors((Color[]) o, camera, path.getMatrix(null));
	      	          else {
	         	         o = ap.getAttribute(CommonAttributes.BACKGROUND_COLOR,Color.class);
	      	        	  if(o instanceof Color) {
	        	            	col = (Color) o;   		
	      	        	  }
	           	         float[] f = col.getRGBColorComponents(null);     
	           	         map.put("color background", f);
	           	         Ri.imager("background",map);        
      	            } 
       		}
         }
         new LightCollector(root);
 //       new GeometryCollector(root,  this);
        root.accept(this);
        Ri.worldEnd();
        Ri.end();
    }
	
    private void handleBackgroundColors(Color[] colors, Camera camera, double[] w2c) {
		Rectangle2D vp = CameraUtility.getViewport(camera, ((double) width)/height);      	
		double z = camera.getFar() - 10E-4;
		double xmin = vp.getMinX();
		double xmax = vp.getMaxX();
		double ymin = vp.getMinY();
		double ymax = vp.getMaxY();
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
		Ri.attributeBegin();
		Ri.concatTransform(fTranspose(w2c));
		Ri.surface("constant",null);
		pointPolygon(bkgd, null);
		Ri.attributeEnd();
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
        Ri.comment(c.getName());        
        EffectiveAppearance tmp =eAppearance;
        Appearance a = c.getAppearance();
        boolean attrblock = false;
        Ri.attributeBegin();
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
        	Ri.attributeEnd();
        
        eAppearance= tmp;
    }
     public void visit(Transformation t) {
         double[] mat = t.getMatrix();
          Ri.concatTransform(fTranspose(mat));
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
                Ri.attribute(key,(Map)m.get(key));
            }
        }
        
        Object color = eap.getAttribute(type+"."+CommonAttributes.DIFFUSE_COLOR,CommonAttributes.DIFFUSE_COLOR_DEFAULT);
        float colorAlpha = 1.0f;
        if(color!=Appearance.INHERITED) {
            float[] c =((Color)color).getRGBComponents(null);
            Ri.color(new float[] {c[0],c[1],c[2]});
            if (c.length == 4) colorAlpha = c[3];
        }
 
        // interpret rendering hints to decide whether to do object instancing
        // currently there's a problem with instancing combined with face colors 
        // due to a bug in the renderman proserver renderer (I believe so anyway)
        // so I've disabled this feature until I figure that out. -gunn
        boolean anyDisplayLists = eap.getAttribute(CommonAttributes.ANY_DISPLAY_LISTS,true);
        boolean manyDisplayLists = eap.getAttribute(CommonAttributes.MANY_DISPLAY_LISTS,false);
        retainGeometry = false; //anyDisplayLists && !manyDisplayLists;
        
       double transparency = eap.getAttribute(type+"."+CommonAttributes.TRANSPARENCY,CommonAttributes.TRANSPARENCY_DEFAULT);
        currentOpacity = 1f - (float)transparency;
		//currentOpacity *= colorAlpha;
        Ri.opacity(new float[] 
           {currentOpacity*colorAlpha,currentOpacity*colorAlpha,currentOpacity*colorAlpha});
        //System.out.println("transparency is "+type+" is "+transparency);
        SLShader slShader = (SLShader) eap.getAttribute(type+CommonAttributes.RMAN_DISPLACEMENT,null,SLShader.class);
        if(slShader != null) {
            Ri.displacement(slShader.getName(),slShader.getParameters());
        }
        boolean testNewShaderStuff = true;
    	if (testNewShaderStuff)	{
            RendermanShader polygonShader =(RendermanShader) ShaderLookup.getShaderAttr(this,eap, "", CommonAttributes.POLYGON_SHADER);        		
            Ri.shader(polygonShader);
    	} else {
            Object shader = eap.getAttribute(type,"default");
            slShader = (SLShader) eap.getAttribute(type+"."+CommonAttributes.RMAN_SURFACE,null,SLShader.class);
            if(slShader == null) {
                if(shader.equals("default")) {
                    float phongSize =(float) eap.getAttribute(type+"."+CommonAttributes.SPECULAR_EXPONENT,CommonAttributes.SPECULAR_EXPONENT_DEFAULT);
                    float phong =(float) eap.getAttribute(type+"."+CommonAttributes.SPECULAR_COEFFICIENT,CommonAttributes.SPECULAR_COEFFICIENT_DEFAULT);
                    float Kd =(float) eap.getAttribute(type+"."+CommonAttributes.DIFFUSE_COEFFICIENT,CommonAttributes.DIFFUSE_COEFFICIENT_DEFAULT);
                    float Ka =(float) eap.getAttribute(type+"."+CommonAttributes.AMBIENT_COEFFICIENT,CommonAttributes.AMBIENT_COEFFICIENT_DEFAULT);
                  HashMap map =new HashMap(); 
                    map.put("roughness",new Float(1/phongSize));
                    map.put("Ks",new Float(phong));
                    map.put("Kd",new Float(Kd));
                    map.put("Ka",new Float(Ka));
                   
                    //System.out.println("has texture "+AttributeEntityUtility.hasAttributeEntity(Texture2D.class, ShaderUtility.nameSpace("polygonShader","texture2d"), a));
                    if (AttributeEntityUtility.hasAttributeEntity(Texture2D.class, ShaderUtility.nameSpace("polygonShader","texture2d"), eap)) {
                        Texture2D tex = (Texture2D) AttributeEntityUtility.createAttributeEntity(Texture2D.class, ShaderUtility.nameSpace("polygonShader","texture2d"), eap);
                   //System.out.println("texture is "+tex);
//                        Texture2D tex = (Texture2D) a.getAttribute(type+".texture",null,Texture2D.class);
//                        if(tex != null) {
                  
                        String fname = null;
                        if (rendererType == RIBViewer.TYPE_PIXAR)	{
                        		fname = (String) eap.getAttribute(CommonAttributes.RMAN_TEXTURE_FILE,"");
                        		if (fname == "")	{
                        			fname = null;
                        		} 
                        } 
                        if (fname == null) {
                        	fname = writeTexture(tex);
                        }
                        map.put("string texturename",fname);
                        double[] mat = tex.getTextureMatrix().getArray();
                        if(mat != null) {
                        	map.put("matrix textureMatrix",fTranspose(mat));
                        }
                        Ri.surface("transformedpaintedplastic",map);
                    } else {
                        Ri.surface("plastic",map);
                    }
                }
             } else 
            	 Ri.surface(slShader.getName(),slShader.getParameters());
    	}
    }
    /**
     * @param tex
     * @return
     */
    public String writeTexture(Texture2D tex) {
            Image img;
            ImageData data = tex.getImage();
        String fname = (String) textures.get(data);
//        Iterator iter = ImageIO.getImageWritersByMIMEType("image/tiff");
//        while (iter.hasNext())	{
//        	System.err.println("Writer: "+((ImageWriter) iter.next()).getClass().getName());
//        }
        if(fname == null) {
            RenderedImage rImage =null;
            BufferedImage bImage = null;
            fname = name+"_texture"+(textureCount++)+".tiff"; //".png"; //
            File f = new File(fname);
            if (true)	{
                //Image img = tex.getImage().getImage();
                // TODO temporary as long as ImageData does not return a propper BufferedImage
                byte[] byteArray = data.getByteArray();
                int dataHeight = data.getHeight();
                int dataWidth = data.getWidth();
                BufferedImage bi = new BufferedImage(dataWidth, dataHeight,
                        BufferedImage.TYPE_INT_ARGB);
                WritableRaster raster = bi.getRaster();
                int[] pix = new int[4];
               
                for (int y = 0, ptr = 0; y < dataHeight; y++)
                    for (int x = 0; x < dataWidth; x++, ptr += 4) {
                        pix[3] = byteArray[ptr + 3];
                        pix[0] = byteArray[ptr];
                        pix[1] = byteArray[ptr + 1];
                        pix[2] = byteArray[ptr + 2];
//                    	pix[3] = byteArray[ptr + 0];
//                        pix[0] = byteArray[ptr + 3];
//                        pix[1] = byteArray[ptr + 2];
//                        pix[2] = byteArray[ptr + 1];
                        raster.setPixel(x, y, pix);
                    }
                img = bi;
                //END of temp code...
                
                if( img instanceof RenderedImage )
                    rImage = (RenderedImage) img;
                else {
                    bImage =new BufferedImage(img.getWidth(null),img.getHeight(null),BufferedImage.TYPE_INT_ARGB);
                    Graphics g =bImage.getGraphics();
                    g.drawImage(img,0,0,null);
                    rImage =bImage;
                    img = (Image) rImage;
                }
           	
            } else {
            	bImage = (BufferedImage) data.getImage();
            	rImage = bImage;
            	img = (Image) bImage;
            }
             //System.out.println( Arrays.asList(ImageIO.getWriterFormatNames()));
//            RenderedImage image = tex.getImage();
//            String format = "tiff";
//
//            RenderedOp op = JAI.create("filestore", image,
//                    fname, format);
            try {
                //OutputStream os = new FileOutputStream(f);
                boolean worked =ImageIO.write(rImage,FileUtil.getFileSuffix(f),f);
                if(!worked) System.err.println("writing of "+fname+" did not work!");
                //os.close();
                textures.put(data,fname);
            } catch (IOException e) {
                e.printStackTrace();
                fname = null;
            }
        }
        return fname;
    }
    boolean testBallStick = true;
	private float currentOpacity;
    /* (non-Javadoc)
     * @see de.jreality.scene.SceneGraphVisitor#visit(de.jreality.scene.IndexedLineSet)
     */
    public void visit(IndexedLineSet g) {
		Ri.comment("IndexedLineSet "+g.getName());
     	if (!insidePointset)	{
      		insidePointset = true;
    		// p is not a proper subclass of IndexedLineSet
    		if (retainGeometry) {
   	    		Object which = pointsets.get(g);
   	  			if (which != null)	{
     	    		Ri.ObjectInstance(((Integer) which).intValue());
    			} else {
    				Ri.comment("Retained geometry "+g.getName());
    				Ri.ObjectBegin(pointsetCount);
    				_visit(g);
    				Ri.ObjectEnd();
    	    		Ri.ObjectInstance(pointsetCount);
    				pointsets.put(g, new Integer(pointsetCount));
    				pointsetCount++;
    			} 
   		}
       		else
    			_visit(g);
   		}
    	else
    			_visit(g);
    }
    
    private void _visit(IndexedLineSet g)	{
       String geomShaderName = (String)eAppearance.getAttribute("geometryShader.name", "");
         if(eAppearance.getAttribute(ShaderUtility.nameSpace(geomShaderName, CommonAttributes.EDGE_DRAW),true)) {
        
        DataList dl = g.getEdgeAttributes(Attribute.INDICES);
        if(dl!=null){
            boolean tubesDraw = eAppearance.getAttribute(ShaderUtility.nameSpace(CommonAttributes.LINE_SHADER, CommonAttributes.TUBES_DRAW),CommonAttributes.TUBES_DRAW_DEFAULT);
            if (tubesDraw)  {
           Ri.attributeBegin();
            setupShader(eAppearance,CommonAttributes.LINE_SHADER);
               float r = (float) eAppearance.getAttribute(ShaderUtility.nameSpace(CommonAttributes.LINE_SHADER,CommonAttributes.TUBE_RADIUS),CommonAttributes.TUBE_RADIUS_DEFAULT);
               // A test to get tubes drawn correctly for non-euclidean case (also fixes some problems I've noticed with
               // the euclidean case too.  -gunn 20.04.06
               // can't use the Renderman Cylinder primitive since it doesn't correspond to equidistant surfaces in non-euclidean cases.
               if (testBallStick)		{
             	    int sig = eAppearance.getAttribute("signature", Pn.EUCLIDEAN);
                    Color cc = (Color) eAppearance.getAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR,  CommonAttributes.DIFFUSE_COLOR_DEFAULT );
             	    if (g instanceof IndexedFaceSet)	{
                    BallAndStickFactory bsf = new BallAndStickFactory(g);
               	  	bsf.setSignature(sig);
               	  	bsf.setStickRadius(r);
                	  	bsf.setShowBalls(false);	// need to actually omit the balls
               	  	bsf.setStickColor(cc);
               	  	bsf.update();
               	  	visit(bsf.getSceneGraphComponent());
            	   } else {
           				DataList edgec =  g.getEdgeAttributes(Attribute.COLORS);
            		   int n = g.getNumEdges();
            		   Ri.color(cc);
            		   double[][] crossSection = TubeUtility.octagonalCrossSection;
            			Object foo = eAppearance.getAttribute("lineShader.crossSection", crossSection);
            			if (foo != crossSection)	{
            				crossSection = (double[][]) foo;
            			}
           		   for (int i = 0; i<n; ++i)	{
             				if (edgec != null) {
               					double[] edgecolor = edgec.item(i).toDoubleArray(null);
               					Ri.comment("Edge color");
               					Ri.color(edgecolor);
               				}
          				double[][] oneCurve = null;
            				oneCurve = IndexedLineSetUtility.extractCurve(oneCurve, g, i);
            				PolygonalTubeFactory ptf = new PolygonalTubeFactory(oneCurve);
//            				ptf.setClosed(true);
            				ptf.setCrossSection(crossSection);
//            				ptf.setFrameFieldType(tubeStyle);
            				ptf.setSignature(sig);
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
                           Ri.color(f);
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
               Ri.attributeEnd();
           }
 
         }
        }
 //       super.visit(g);
         _visit((PointSet) g);
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
		
        Ri.transformBegin();
        Ri.concatTransform(fTranspose(mat));
        Ri.cylinder(r,0,l,360,null);
        Ri.transformEnd();
    }
        
    
    
     public void visit(IndexedFaceSet g) {
			Ri.comment("IndexedFaceSet "+g.getName());
     	if (!insidePointset)	{
      		insidePointset = true;
    		// p is not a subclass of PointSet
    		if (retainGeometry) {
   	    		Object which = pointsets.get(g);
   	  			if (which != null)	{
     				Ri.ObjectInstance(((Integer) which).intValue());
    	    	} else {
    				Ri.comment("Retained geometry "+g.getName());
    				Ri.ObjectBegin(pointsetCount);
    				_visit(g, null);
    				Ri.ObjectEnd();
       	    		Ri.ObjectInstance(pointsetCount);
       	    		pointsets.put(g, new Integer(pointsetCount));
    				pointsetCount++;
    	      	} 
   		}
       		else
    			_visit(g, null);
   		}
    	else
    			_visit(g, null);
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
			Ri.attributeBegin();
			setupShader(eAppearance,CommonAttributes.POLYGON_SHADER);
 		   	DataList colors=i.getFaceAttributes( Attribute.COLORS );
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
    	    		pointPolygon(i, color);
    		}
    	    	Ri.attributeEnd();
    	    	 
    	}
   		if (color == null) _visit((IndexedLineSet) i);
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
		    fcoords[3*j+0] =(float)da.getValueAt(j,0);
		    fcoords[3*j+1] =(float)da.getValueAt(j,1);
		    fcoords[3*j+2] =(float)da.getValueAt(j,2);
		    fcoords[3*j+3] =(float)da.getValueAt(j,3);
		}
		map.put("Pw",fcoords);       	
      }
				DataList normals = i.getVertexAttributes(Attribute.NORMALS);
				if(smooth && normals!=null) {
					da = normals.toDoubleArrayArray();
					float[] fnormals =new float[3*da.getLength()];
					for (int j = 0; j < da.getLength(); j++) {
						fnormals[3*j+0] =(float)da.getValueAt(j,0);
						fnormals[3*j+1] =(float)da.getValueAt(j,1);
						fnormals[3*j+2] =(float)da.getValueAt(j,2);
					}
					map.put("N",fnormals);
				} 
				else { //face normals
					normals = i.getFaceAttributes(Attribute.NORMALS);
					if (normals != null)	{
						da = normals.toDoubleArrayArray();
						float[] fnormals =new float[3*da.getLength()];
						for (int j = 0; j < da.getLength(); j++) {
							fnormals[3*j+0] =(float)da.getValueAt(j,0);
							fnormals[3*j+1] =(float)da.getValueAt(j,1);
							fnormals[3*j+2] =(float)da.getValueAt(j,2);
						}
						map.put("uniform normal N",fnormals);            	
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
					f[0]=color[0];f[1]=color[1];f[2]=color[2];
					Ri.color(f);
					if (color.length==4){
						f[0]=color[3]*currentOpacity;f[1]=color[3]*currentOpacity;f[2]=color[3]*currentOpacity;
						Ri.opacity(f);
						}	
				}
			Ri.pointsPolygons(npolys,nvertices,vertices,map);
			}
	}
    
    /* (non-Javadoc)
     * @see de.jreality.scene.SceneGraphVisitor#visit(de.jreality.scene.PointSet)
     */
    public void visit(PointSet g) {
		Ri.comment("PointSet "+g.getName());
     	if (!insidePointset)	{
    		// p is not a subclass of PointSet
     		insidePointset = true;
    		if (retainGeometry) {
   	    		Object which = pointsets.get(g);
   	  			if (which != null)	{
     	    		Ri.ObjectInstance(((Integer) which).intValue());
    			} else {
    				Ri.comment("Retained geometry "+g.getName());
    				Ri.ObjectBegin(pointsetCount);
    				_visit(g);
    				Ri.ObjectEnd();
       	    		Ri.ObjectInstance(pointsetCount);
       	    		pointsets.put(g, new Integer(pointsetCount));
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
            Ri.attributeBegin();
            float r = (float) eAppearance.getAttribute(ShaderUtility.nameSpace(CommonAttributes.POINT_SHADER,CommonAttributes.POINT_RADIUS),CommonAttributes.POINT_RADIUS_DEFAULT);
            //System.out.println("point radius is "+r);
            setupShader(eAppearance,CommonAttributes.POINT_SHADER);
            boolean drawSpheres = eAppearance.getAttribute(CommonAttributes.SPHERES_DRAW,CommonAttributes.SPHERES_DRAW_DEFAULT);
            if(drawSpheres) {
            	    int sig = eAppearance.getAttribute("signature", Pn.EUCLIDEAN);
                 Color cc = (Color) eAppearance.getAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR,  CommonAttributes.DIFFUSE_COLOR_DEFAULT );
                 //DoubleArrayArray a=coord.toDoubleArrayArray();
                 double[][] a=coord.toDoubleArrayArray(null);
                 Ri.color(cc.getRGBColorComponents(null));
               double[] trns = new double[16];
                for (int i= 0; i < n; i++) { 
                    trns = MatrixBuilder.init(null, sig).translate(a[i]).getArray();
                    Ri.transformBegin();
                    Ri.concatTransform(fTranspose(trns));
                    HashMap map =new HashMap();
                    Ri.sphere(r,-r,r,360f,map);
                    Ri.transformEnd();
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
                Ri.points(n,map);
            }
                Ri.attributeEnd();       	
        }
    }
    
    public void visit(ClippingPlane p) {
        Ri.clippingPlane(0,0,0,0,0,1);
    }
    /* (non-Javadoc)
     * @see de.jreality.scene.SceneGraphVisitor#visit(de.jreality.scene.UnitSphere)
     */
    public void visit(Sphere s) {
        setupShader(eAppearance,CommonAttributes.POLYGON_SHADER);
        Ri.sphere(1f,-1f,1f,360f,null);
    }
    
    public void visit(Cylinder c) {
        setupShader(eAppearance,CommonAttributes.POLYGON_SHADER);
        Ri.cylinder(1f,-1f,1f,360f,null);
        Ri.disk(-1f,1f,360f,null);
        Ri.disk(1f,1f,360f,null);
        
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
     * @param proj The style of Projection.
     */
    public void projection(String proj){
    	this.proj=proj;
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

    private class GeometryCollector extends SceneGraphVisitor	{
    	SceneGraphComponent root = null;
    	RIBVisitor rib = null;
    	GeometryCollector(SceneGraphComponent r, RIBVisitor b)	{
    		super();
    		root  = r;
    		rib = b;
    		Ri.comment("'\nBeginning of retained geometry\n");
    		visit(root);
       		Ri.comment("'\nEnd of retained geometry\n\n");
       	    	}
        public void visit(SceneGraphComponent c) {
            c.childrenAccept(this);
        }
        
		public void visit(IndexedFaceSet i) {
			if (pointsets.get(i) != null) return;
			Ri.comment("Retained geometry "+i.getName());
			Ri.ObjectBegin(pointsetCount);
			rib.visit(i);
			Ri.ObjectEnd();
			pointsets.put(i, new Integer(pointsetCount));
			pointsetCount++;
		}

		public void visit(IndexedLineSet i) {
			if (pointsets.get(i) != null) return;
			Ri.comment("Retained geometry "+i.getName());
			Ri.ObjectBegin(pointsetCount);
			rib.visit(i);
			Ri.ObjectEnd();
			pointsets.put(i, new Integer(pointsetCount));
			pointsetCount++;
		}

		public void visit(PointSet i) {
			if (pointsets.get(i) != null) return;
			Ri.comment("Retained geometry "+i.getName());
			Ri.ObjectBegin(pointsetCount);
			rib.visit(i);
			Ri.ObjectEnd();
			pointsets.put(i, new Integer(pointsetCount));
			pointsetCount++;
		}
    	
    }

	public int getRendererType() {
		return rendererType;
	}
}
