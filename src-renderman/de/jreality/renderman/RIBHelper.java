package de.jreality.renderman;

import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.awt.image.renderable.ParameterBlock;
import java.beans.Statement;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;

import de.jreality.backends.label.LabelUtility;
import de.jreality.geometry.Primitives;
import de.jreality.math.Matrix;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.renderman.shader.DefaultPolygonShader;
import de.jreality.renderman.shader.FreePolygonShader;
import de.jreality.renderman.shader.RendermanShader;
import de.jreality.renderman.shader.TwoSidePolygonShader;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.PointSet;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.AttributeCollection;
import de.jreality.scene.data.DoubleArrayArray;
import de.jreality.scene.data.IntArrayArray;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.CubeMap;
import de.jreality.shader.DefaultGeometryShader;
import de.jreality.shader.DefaultTextShader;
import de.jreality.shader.EffectiveAppearance;
import de.jreality.shader.ImageData;
import de.jreality.shader.LineShader;
import de.jreality.shader.PointShader;
import de.jreality.shader.PolygonShader;
import de.jreality.shader.ShaderUtility;
import de.jreality.shader.Texture2D;
import de.jreality.shader.TextureUtility;
import de.jreality.util.LoggingSystem;

public class RIBHelper {
	
	
	public static RendermanShader processShader(AttributeCollection shader, RIBVisitor ribv, String name){
		if(shader instanceof PolygonShader)
			return processPolygonShader((PolygonShader)shader, ribv, name);
		if(shader instanceof LineShader)
			return processLineShader((LineShader)shader, ribv, name);
		if(shader instanceof PointShader)
			return processPointShader((PointShader)shader, ribv, name);		
		else return null;
	}
	
	
	

	private static RendermanShader processPolygonShader(PolygonShader ps, RIBVisitor ribv, String name)	{
		RendermanShader rs = null;
		Color Cs = null;
		double transparency = 0.0;
		if (ps instanceof de.jreality.shader.DefaultPolygonShader)	{
//			System.err.println("processing defaultpolygonshader");
			de.jreality.shader.DefaultPolygonShader dps = (de.jreality.shader.DefaultPolygonShader) ps;
			DefaultPolygonShader rdps = new DefaultPolygonShader(dps);
			rdps.setFromEffectiveAppearance(ribv, ribv.eAppearance, name);
			rs = rdps;
			Cs = dps.getDiffuseColor();
			transparency = (float)dps.getTransparency().floatValue();
			ribv.cs=dps.getDiffuseColor();
			ribv.smooth = dps.getSmoothShading();
		} 
		else if (ps instanceof de.jreality.shader.TwoSidePolygonShader)	{
//			System.err.println("processing twosidepolygonshader");
			de.jreality.shader.TwoSidePolygonShader dps = (de.jreality.shader.TwoSidePolygonShader) ps;
			TwoSidePolygonShader rdps = new TwoSidePolygonShader(dps);
			rdps.setFromEffectiveAppearance(ribv, ribv.eAppearance, name);
			rs = rdps;
			de.jreality.shader.DefaultPolygonShader dpss = ((de.jreality.shader.DefaultPolygonShader)dps.getFront());
			Cs = dpss.getDiffuseColor();
			transparency = (float)dpss.getTransparency().floatValue();
			ribv.cs=dpss.getDiffuseColor();
			ribv.smooth = dpss.getSmoothShading();
			// TODO figure out how to read out a reasonable "smooth" and "cs" value from this shader
		}
		else {
			LoggingSystem.getLogger(ShaderUtility.class).warning("Unknown shader class "+ps.getClass());
		}
		float[] csos = extractCsOs(Cs, (!(ribv.handlingProxyGeometry && ribv.opaqueTubes) && ribv.transparencyEnabled) ? transparency : 0f);
		ribv.ri.color(csos);
		ribv.ri.shader(rs);
		
		return rs;
	}
	
	private static RendermanShader processLineShader(LineShader ls, RIBVisitor ribv, String name)	{
		RendermanShader rs = null;
		Color Cs = null;
		double transparency = 0.0;
		if (ls instanceof de.jreality.shader.DefaultLineShader)	{
			de.jreality.shader.DefaultLineShader dls=(de.jreality.shader.DefaultLineShader)ls;
			ribv.drawTubes=dls.getTubeDraw();
			ribv.tubeRadius=new Float(dls.getTubeRadius()).floatValue();
			ribv.cs=dls.getDiffuseColor();
			if(dls.getTubeDraw()){				
				return processPolygonShader(dls.getPolygonShader(), ribv, name+".polygonShader");
			}
			else{				
				Cs=dls.getDiffuseColor();
				//ribv.tubeRadius=new Float(dls.getLineWidth()).floatValue();
				
				Appearance slApp=new Appearance();
				SLShader sls=new SLShader("constant");				
				slApp.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.POLYGON_SHADER,"free");
				slApp.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.RMAN_SURFACE_SHADER, sls);
				EffectiveAppearance slEApp=EffectiveAppearance.create();
				slEApp=slEApp.create(slApp);
				rs=new FreePolygonShader();
				rs.setFromEffectiveAppearance(ribv, slEApp, "lineShader");
				
//				float[] csos = extractCsOs(Cs, (!(ribv.handlingProxyGeometry && ribv.opaqueTubes) && ribv.transparencyEnabled) ? transparency : 0f);
//				ribv.ri.color(csos);
//				ribv.ri.surface("constant", null);
			}
		}else {
			LoggingSystem.getLogger(ShaderUtility.class).warning("Unknown shader class "+ls.getClass());
		}
		float[] csos = extractCsOs(Cs, (!(ribv.handlingProxyGeometry && ribv.opaqueTubes) && ribv.transparencyEnabled) ? transparency : 0f);
		ribv.ri.color(csos);
		ribv.ri.shader(rs);
		
		return rs;
	}
	
	private static RendermanShader processPointShader(PointShader vs, RIBVisitor ribv, String name)	{
		RendermanShader rs = null;
		Color Cs = null;
		double transparency = 0.0;
		if (vs instanceof de.jreality.shader.DefaultPointShader)	{
			de.jreality.shader.DefaultPointShader dvs=(de.jreality.shader.DefaultPointShader)vs;			
			ribv.drawSpheres=dvs.getSpheresDraw();
			ribv.pointRadius=new Float(dvs.getPointRadius()).floatValue();
			ribv.cs=dvs.getDiffuseColor();
			if(dvs.getSpheresDraw()){			
				return processPolygonShader(dvs.getPolygonShader(), ribv, name+".polygonShader");
			}else{
				Cs=dvs.getDiffuseColor();
				//ribv.pointRadius=new Float(dvs.getPointSize()).floatValue();
				
				Appearance slApp=new Appearance();
				SLShader sls=new SLShader("constant");				
				slApp.setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.POLYGON_SHADER,"free");
				slApp.setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.RMAN_SURFACE_SHADER, sls);
				EffectiveAppearance slEApp=EffectiveAppearance.create();
				slEApp=slEApp.create(slApp);
				rs=new FreePolygonShader();
				rs.setFromEffectiveAppearance(ribv, slEApp, "pointShader");
			}
		}else {
			LoggingSystem.getLogger(ShaderUtility.class).warning("Unknown shader class "+vs.getClass());
		}
		float[] csos = extractCsOs(Cs, (!(ribv.handlingProxyGeometry && ribv.opaqueTubes) && ribv.transparencyEnabled) ? transparency : 0f);
		ribv.ri.color(csos);
		ribv.ri.shader(rs);
		
		return rs;
	}
	
	
	protected static float[] extractCsOs(Color color, double transparency)	{
		float[] csos = new float[4];
		float colorAlpha = 1.0f;
		if (color != Appearance.INHERITED) {
			float[] c = ((Color) color).getRGBComponents(null);
			if (c.length == 4)
				colorAlpha = c[3];
			csos[0] = c[0];
			csos[1] = c[1];
			csos[2] = c[2];
		}

		csos[3] = 1f - (float) transparency;
		// TODO remove this if we decide finally to not allow transparency control via alpha channel of Color
		csos[3] *= colorAlpha;
		return csos;
	}

	public static void writeShader(String name, String shaderName ) {
		try {
		    File file = new File(name);
		    LoggingSystem.getLogger(RIBHelper.class).fine("writing in  "+name);
		    file = new File(file.getParent(),shaderName);
		    LoggingSystem.getLogger(RIBHelper.class).fine("checking on "+file+" exists "+file.exists());
		    if(!file.exists()) {
		    	OutputStream os = new FileOutputStream(file);
		    	InputStream is = DefaultPolygonShader.class.getResourceAsStream(shaderName);
		    
		    	int c = 0;
		    	while((c =is.read())!=-1) {
		    		os.write(c);
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

	public static String str(String name) {
	    return "\""+name+"\"";
	}


	/**
	 * @param w2
	 * @param map
	 */
	public static void writeMap(PrintWriter w2, Map map) {
	    if(map!=null) {
	    Set keys = map.keySet();
	        for (Iterator key = keys.iterator(); key.hasNext();) {
	            String element = (String) key.next();
	            w2.print("\""+ element+"\" ");
	            RIBHelper.writeObject(w2,map.get(element));
	        }
	    }
	    w2.println("");
	}

	/**
	 * @param w2
	 * @param object
	 */
	public static void writeObject(PrintWriter w2, Object object) {
	    if(object instanceof double[]) {
	    	object = Rn.convertDoubleToFloatArray((double[]) object);
	    }
	    if(object instanceof float[]) {
	        float[] f = (float[]) object;
	        w2.print("[");
	        for (int i = 0; i < f.length; i++) {
	            w2.print(f[i]+" ");
	        }
	        w2.print("]");
	        return;
	    }
	    if(object instanceof int[]) {
	        int[] f = (int[]) object;
	        w2.print("[");
	        for (int i = 0; i < f.length; i++) {
	            w2.print(f[i]+" ");
	        }
	        w2.print("]");
	        return;
	    }
	    if(object instanceof Color) {
	        w2.print("[");
	        float[] rgb = ((Color)object).getRGBComponents(null);
	        for (int i = 0; i < 3; i++) {
	            w2.print(rgb[i]+" ");
	        }
	        w2.print("]");
	        return;
	    }
	
	    if(object instanceof String) {
	        w2.print("\""+object+"\"");
	        return;
	    }
	    w2.print(" "+object+" ");
	}
  
  
  public static void writeTexture(ImageData data, String noSuffix){
    
    BufferedImage img;
	  for (Iterator iter = ImageIO.getImageWritersByMIMEType("image/tiff"); iter.hasNext(); ) {
	   System.err.println("Writer: "+((ImageWriter) iter.next()).getClass().getName());
	  }
	        
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
             pix[0] = byteArray[ptr];
             pix[1] = byteArray[ptr + 1];
             pix[2] = byteArray[ptr + 2];
             pix[3] = byteArray[ptr + 3]; 
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
		  Class encParamClass = Class.forName("com.sun.media.jai.codec.TIFFEncodeParam");
		  
      Object encodeParam = encParamClass.newInstance();
      Object compField = encParamClass.getField("COMPRESSION_DEFLATE").get(null);
      
      new Statement(encodeParam, "setCompression", new Object[]{compField}).execute();
      //encodeParam.setCompression(TIFFEncodeParam.COMPRESSION_DEFLATE);
      new Statement(encodeParam, "setDeflateLevel", new Object[]{9}).execute();
      //encodeParam.setDeflateLevel(9);
      
      ParameterBlock pb = new ParameterBlock();
      pb.addSource(img);
      pb.add(new FileOutputStream(noSuffix+".tiff"));
      pb.add("tiff");
      pb.add(encodeParam);
      
	  new Statement(Class.forName("javax.media.jai.JAI"), "create", new Object[]{"encode", pb}).execute();
      //JAI.create("encode", pb);

		} catch(Throwable e) {
		  worked=false;
		  LoggingSystem.getLogger(RIBVisitor.class).log(Level.CONFIG, "could not write TIFF: "+noSuffix+".tiff", e);
		}
	  if (!worked) {
	    try {
			 worked =ImageIO.write(img, "PNG", new File(noSuffix+".png"));
	    } catch (IOException e) {
					// TODO Auto-generated catch block
	      e.printStackTrace();
			}
	    if (!worked) 
	     LoggingSystem.getLogger(RIBVisitor.class).log(Level.CONFIG, "could not write PNG: {0}.png", noSuffix);
	  }
	}
  
  
  	public static void createRIBLabel(PointSet ps, DefaultTextShader ts, RIBVisitor ribv){
		if (!ts.getShowLabels().booleanValue())
			return;
		Font font = ts.getFont();
		Color c = ts.getDiffuseColor();
		double scale = ts.getScale().doubleValue();
		double[] offset = ts.getOffset();
		int alignment = ts.getAlignment();
		ImageData[] img = LabelUtility.createPointImages(ps, font, c);
		DoubleArrayArray coords=ps.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray();
  		
  		writeLabel(ribv, img, coords, null, offset, alignment, scale);
  	}
  	
  	public static void createRIBLabel(IndexedLineSet ils, DefaultTextShader ts, RIBVisitor ribv){
		if (!ts.getShowLabels().booleanValue())
			return;
		Font font = ts.getFont();
		Color c = ts.getDiffuseColor();
		double scale = ts.getScale().doubleValue();
		double[] offset = ts.getOffset();
		int alignment = ts.getAlignment();
		ImageData[] img = LabelUtility.createEdgeImages(ils, font, c);
		DoubleArrayArray coords=ils.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray();
  		IntArrayArray inds=ils.getEdgeAttributes(Attribute.INDICES).toIntArrayArray();
  		writeLabel(ribv, img, coords, inds, offset, alignment, scale);
  	}

  	public static void createRIBLabel(IndexedFaceSet ifs, DefaultTextShader ts, RIBVisitor ribv){
		if (!ts.getShowLabels().booleanValue())
			return;
		Font font = ts.getFont();
		Color c = ts.getDiffuseColor();
		double scale = ts.getScale().doubleValue();
		double[] offset = ts.getOffset();
		int alignment = ts.getAlignment();
		ImageData[] img = LabelUtility.createFaceImages(ifs, font, c);
		DoubleArrayArray coords=ifs.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray();
  		IntArrayArray inds=ifs.getFaceAttributes(Attribute.INDICES).toIntArrayArray();
  		writeLabel(ribv, img, coords, inds, offset, alignment, scale);
  	}
  	
	private static IndexedFaceSet bb = Primitives.texturedQuadrilateral(new double[] { 0, 1,
			0, 1, 1, 0, 1, 0, 0, 0, 0, 0 });

	private static void writeLabel(RIBVisitor ribv, ImageData[] labels, DoubleArrayArray vertices, IntArrayArray indices, double[] offset, int alignment, double scale) {
		Matrix c2o = new Matrix(Rn.times(null,ribv.world2Camera, ribv.object2world.getMatrix(null))).getInverse();
		double[] bbm = new double[16];
		for (int i = 0, n = labels.length; i < n; i++) {
			ImageData img = labels[i];
			String labelName = new File(ribv.writeTexture(img,Texture2D.GL_CLAMP, Texture2D.GL_CLAMP)).getName();	
			LabelUtility.calculateBillboardMatrix(bbm, img.getWidth() * scale, img.getHeight()* scale, offset, alignment, c2o.getArray(), LabelUtility.positionFor(i, vertices,indices), Pn.EUCLIDEAN);	
			ribv.ri.transformBegin();
			ribv.ri.concatTransform(fTranspose(bbm));
			//ribv.ri.color(Color.WHITE);
			//ribv.ri.opacity(new Float(0.0));
			HashMap<String, Object> shaderMap = new HashMap<String, Object>();
			shaderMap.put("string texturename", labelName);
			ribv.ri.surface("constantTexture", shaderMap);
			ribv.pointPolygon(bb, null);
			ribv.ri.transformEnd();
		}
	}
	
	
	/**
	 * Shifts lineShader.polygonShader-Attributes to polygonShader-Attributes
	 * Is used when tubes are splitted in several geometries by the BallAndStickFactory
	 * in RIBVisitor._visit(IndexedLineSet)
	 * 
	 * @param DefaultGeometryShader dgs
	 * @return the shifted Appearance
	 */
	
	public static Appearance shiftTubesAppearance(DefaultGeometryShader dgs){
		Appearance ap = new Appearance();
		Texture2D tex2d = null;
		CubeMap cubeMap = null;					
		if (dgs.getLineShader() instanceof de.jreality.shader.DefaultLineShader)	{						
			de.jreality.shader.DefaultPolygonShader lsps=null;
			if(((de.jreality.shader.DefaultLineShader)dgs.getLineShader()).getPolygonShader() instanceof de.jreality.shader.DefaultPolygonShader){								
				ap.setAttribute("polygonShader", de.jreality.shader.DefaultPolygonShader.class);								
				lsps =(de.jreality.shader.DefaultPolygonShader)((de.jreality.shader.DefaultLineShader)dgs.getLineShader()).getPolygonShader();
			}
			else if(((de.jreality.shader.DefaultLineShader)dgs.getLineShader()).getPolygonShader() instanceof de.jreality.shader.TwoSidePolygonShader){
				ap.setAttribute("polygonShader", de.jreality.shader.TwoSidePolygonShader.class);
				lsps =(de.jreality.shader.DefaultPolygonShader)((de.jreality.shader.TwoSidePolygonShader)((de.jreality.shader.DefaultLineShader)dgs.getLineShader()).getPolygonShader()).getFront();
				de.jreality.shader.DefaultPolygonShader lspsb=(de.jreality.shader.DefaultPolygonShader)((de.jreality.shader.TwoSidePolygonShader)((de.jreality.shader.DefaultLineShader)dgs.getLineShader()).getPolygonShader()).getBack();
				de.jreality.shader.DefaultPolygonShader[] sideShaders={lsps,lspsb};				
				String side="front";
				for(int s=0;s<2;s++){
					if(s==1) side="back";										
					ap.setAttribute("polygonShader."+side+".diffuseColor",sideShaders[s].getDiffuseColor());									
					ap.setAttribute("polygonShader."+side+".ambientCoefficient",sideShaders[s].getAmbientCoefficient());									
					ap.setAttribute("polygonShader."+side+".ambientColor",sideShaders[s].getAmbientColor());									
					ap.setAttribute("polygonShader."+side+".diffuseCoefficient",sideShaders[s].getDiffuseCoefficient());									
					ap.setAttribute("polygonShader."+side+".specularCoefficient",sideShaders[s].getSpecularCoefficient());									
					ap.setAttribute("polygonShader."+side+".smoothShading",sideShaders[s].getSmoothShading());									
					ap.setAttribute("polygonShader."+side+".specularColor",sideShaders[s].getSpecularColor());									
					ap.setAttribute("polygonShader."+side+".specularExponent",sideShaders[s].getSpecularExponent());
					ap.setAttribute("polygonShader."+side+".transparency",sideShaders[s].getTransparency());
					if(sideShaders[s].getTexture2d()==null)
						ap.setAttribute("polygonShader."+side+".texture2d",  Appearance.DEFAULT);
					else
						TextureUtility.createTexture(ap, "polygonShader."+side, sideShaders[s].getTexture2d().getImage(), false);
					if(sideShaders[s].getReflectionMap()==null)
						ap.setAttribute("polygonShader."+side+"."+CommonAttributes.REFLECTION_MAP,  Appearance.DEFAULT);
					else{                      
						CubeMap lineCubeMap=TextureUtility.createReflectionMap(ap, "polygonShader."+side, TextureUtility.getCubeMapImages(sideShaders[s].getReflectionMap()));
						lineCubeMap.setBlendColor(sideShaders[s].getReflectionMap().getBlendColor());
					}
				}					
			}
			else System.err.println("the following shader-type of lineShader.polygonShader not supported in RIBHelper.shiftTubesAppearance(DefaultGeometryShader):\n"+((de.jreality.shader.DefaultLineShader)dgs.getLineShader()).getPolygonShader());
			
			tex2d = lsps.getTexture2d();
			cubeMap = lsps.getReflectionMap();
		}
		else System.err.println("the following shader-type of lineShader not supported in RIBHelper.shiftTubesAppearance(DefaultGeometryShader):\n"+dgs.getLineShader());
		
		if(tex2d==null)
			ap.setAttribute("polygonShader.texture2d",  Appearance.DEFAULT);
		else
			TextureUtility.createTexture(ap, "polygonShader", tex2d.getImage(), false);
		if(cubeMap==null)
			ap.setAttribute("polygonShader."+CommonAttributes.REFLECTION_MAP,  Appearance.DEFAULT);
		else{                      
			CubeMap lineCubeMap=TextureUtility.createReflectionMap(ap, "polygonShader", TextureUtility.getCubeMapImages(cubeMap));
			lineCubeMap.setBlendColor(cubeMap.getBlendColor());
		}
		
		return ap;
	}
}
