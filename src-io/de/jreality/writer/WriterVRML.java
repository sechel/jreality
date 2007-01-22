package de.jreality.writer;

/**
 * @author gonska
 * 
 */


/**TODO 
 * Texturen
 * Labels
 * 
 */
import java.awt.Color;
import java.io.FileWriter;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.LinkedList;

import de.jreality.math.FactoredMatrix;
import de.jreality.math.Matrix;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.renderman.RIBVisitor;
import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
import de.jreality.scene.Cylinder;
import de.jreality.scene.DirectionalLight;
import de.jreality.scene.Geometry;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.Light;
import de.jreality.scene.PointLight;
import de.jreality.scene.PointSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphNode;
import de.jreality.scene.SceneGraphVisitor;
import de.jreality.scene.Sphere;
import de.jreality.scene.SpotLight;
import de.jreality.scene.Transformation;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.AttributeEntityUtility;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.DefaultGeometryShader;
import de.jreality.shader.DefaultLineShader;
import de.jreality.shader.DefaultPointShader;
import de.jreality.shader.DefaultPolygonShader;
import de.jreality.shader.DefaultTextShader;
import de.jreality.shader.EffectiveAppearance;
import de.jreality.shader.ImageData;
import de.jreality.shader.RenderingHintsShader;
import de.jreality.shader.ShaderUtility;
import de.jreality.shader.Texture2D;



public class WriterVRML 
//extends SceneGraphVisitor 
{
	private static DefaultGeometryShader dgs;
	private static RenderingHintsShader rhs;
	private static DefaultPolygonShader dps;
	private static DefaultLineShader dls;
	private static DefaultPointShader dvs;
//	private static DefaultTextShader dts;
	private static final int PER_VERTEX=0,PER_PART=1,PER_FACE=2,OVERALL=3;

	private static PrintWriter out=null;
	private static final String spacing="  ";// for outlay

	public static void write( SceneGraphComponent sgc, OutputStream outS ) {
		write( sgc, new PrintWriter( outS ));
	}
	public static void write( SceneGraphComponent sgc, FileWriter outS ) {
		write( sgc, new PrintWriter( outS ));
	}
	public static void write( SceneGraphComponent sgc, PrintWriter outS ) {
		out=outS;
		out.println("#VRML V1.0 ascii");
		// init EffectiveAppearance
		Appearance app = sgc.getAppearance();
		if (app == null) app = new Appearance();
		EffectiveAppearance eApp= EffectiveAppearance.create();
		eApp= eApp.create(app);
		// init shaders
		updateShaders(eApp);
		// start writing scene
		writeComp(sgc,"",eApp);
		out.flush();
	}
//	------------------ 
	private static void updateShaders(EffectiveAppearance eap) {
		dgs = ShaderUtility.createDefaultGeometryShader(eap);
		rhs = ShaderUtility.createRenderingHintsShader(eap);

		if (dgs.getPointShader() instanceof DefaultPointShader)	dvs = (DefaultPointShader) dgs.getPointShader();
		else dvs = null;
		if (dgs.getLineShader() instanceof DefaultLineShader) dls = (DefaultLineShader) dgs.getLineShader();
		else dls = null;
		if (dgs.getPolygonShader() instanceof DefaultPolygonShader) dps = (DefaultPolygonShader) dgs.getPolygonShader();
		else dps = null;
//		if (dps.getTextShader() instanceof DefaultPolygonShader) dps = (DefaultPolygonShader) dgs.getPolygonShader();
//		else dts = null;

	}
//	---------------------------- start writing --------------------

	private static void writeComp(SceneGraphComponent c,String hist,EffectiveAppearance parentEA){
		if (!c.isVisible()) return;
		String hist2= hist+spacing;

		Geometry g = c.getGeometry();
		Camera cam = c.getCamera();
		Light li = c.getLight();
		Appearance app =c.getAppearance();
		EffectiveAppearance	eApp=parentEA;
		if (app!=null) eApp=parentEA.create(app);
		updateShaders(eApp);
		Transformation t= c.getTransformation();

		// write content
		out.print(""+hist+"Separator { ");
		out.println("# "+c.getName());
		if (t!=null)		writeTrafo(t,hist2);
		for (int i=0;i<c.getChildComponentCount();i++)
			writeComp(c.getChildComponent(i),hist2,eApp);
		if (g!=null)		writeGeo(g,hist2);// use Appearance
		if (li!=null)		writeLight(li,hist2);// use Appearance
		if (cam!=null)		writeCam(cam,hist2);
		out.println(""+hist+"}");
	}
	private static void writeGeo(Geometry g,String hist){
		if (g instanceof Sphere)
			writeSphere((Sphere)g,hist);
		else if (g instanceof Cylinder )
			writeCylinder((Cylinder)g,hist);
		else if (g instanceof IndexedFaceSet)
			writeGeoFaces((IndexedFaceSet) g,hist);		
		else if (g instanceof IndexedLineSet)
			writeGeoLines((IndexedLineSet) g,hist);
		else if (g instanceof PointSet)
			writeGeoPoints((PointSet) g,hist);
		else System.err.println("WriterVRML.writeComp() Failure");
	}
	private static void writeSphere(Sphere s,String hist){
		/**	Sphere {
		 *    radius  1     # SFFloat
		 *	}		*/
		out.println(hist+"Sphere { radius  1}");
	}
	private static void writeCylinder(Cylinder c,String hist){
	/**	PARTS
	*     SIDES   The cylindrical part
	*     TOP     The top circular face
	*     BOTTOM  The bottom circular face
	*     ALL     All parts
	*FILE FORMAT/DEFAULTS
	*     Cylinder {
	*          parts   ALL   # SFBitMask
	*          radius  1     # SFFloat
	*          height  2     # SFFloat
	*     }		*/
		out.print(hist+"Cylinder { ");
		out.print("radius  1 ");
		out.print("height  2 ");
		out.print("}");
		
		
		
		System.out.println("WriterVRML.writeCylinder()");
	}
	private static void writeGeoFaces(IndexedFaceSet f,String hist){
		// write the coordinates:
		writeCoordinates(f.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null),hist);
		// writes the Normals depending on smooth or flat shading:
		if(dps.getSmoothShading()){
			if(f.getVertexAttributes(Attribute.NORMALS)!=null){
				writeNormalBinding(PER_VERTEX,hist);
				writeNormals(f.getVertexAttributes(Attribute.NORMALS).toDoubleArrayArray(null),hist);
			}	
		}
		else{
			if(f.getFaceAttributes(Attribute.NORMALS)!=null){
				writeNormalBinding(PER_FACE,hist);
				writeNormals(f.getFaceAttributes(Attribute.NORMALS).toDoubleArrayArray(null),hist);
			}	
		}
		// writes Colors
		if(f.getFaceAttributes(Attribute.COLORS)!=null){
			writeMaterialBinding(PER_FACE,hist);
			writeColors(f.getFaceAttributes(Attribute.COLORS).toDoubleArrayArray(null),hist);
		}
		else if(f.getVertexAttributes(Attribute.COLORS)!=null){
			writeMaterialBinding(PER_VERTEX,hist);
			writeColors(f.getVertexAttributes(Attribute.COLORS).toDoubleArrayArray(null),hist);
		}
		else {
			writeMaterialBinding(OVERALL,hist);
			Color amb =dps.getAmbientColor();
			Color spec= dps.getSpecularColor();
			Color diff= dps.getDiffuseColor();
			double tra= dps.getTransparency();
			if (tra!=0|diff!=null|spec!=null|amb!=null){
				writeMaterial(amb,diff,spec,tra,hist);
			}
		}
		// write texturecoords
		writeTexCoords(f,hist);			
		// write face texture
		writeTexture(hist);
		/**	IndexedFaceSet {
		 * 	coordIndex         0  # MFLong indies
		 * 	materialIndex      -1 # MFLong egal
		 * 	normalIndex        -1 # MFLong egal
		 * 	textureCoordIndex  -1 # MFLong spaeter
		 * 	} */

		out.print(hist+"IndexedFaceSet {");
		out.println(" # "+ f.getName());
		// writes the FaceIndices
		writeIndices(f.getFaceAttributes(Attribute.INDICES).toIntArrayArray(null), hist+spacing);
		out.println(hist+"}");
	}
	private static void writeGeoLines(IndexedLineSet l,String hist){
		double[][] lcoords=l.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
		int[][] lindices=l.getEdgeAttributes(Attribute.INDICES).toIntArrayArray(null);
		
		// write coords
		writeCoordinates(lcoords,hist);
		// writes Edge Colors if given:

		if(l.getEdgeAttributes(Attribute.COLORS)!=null){
			writeMaterialBinding(PER_PART,hist);
			writeColors(l.getEdgeAttributes(Attribute.COLORS).toDoubleArrayArray(null),hist);
		}
		else if(l.getVertexAttributes(Attribute.COLORS)!=null){
			writeMaterialBinding(PER_VERTEX,hist);
			double[][] c=l.getVertexAttributes(Attribute.COLORS).toDoubleArrayArray(null);
			c=convertLineVertexColors(c,lindices);
			writeColors(c,hist);
		}
		else {
			writeMaterialBinding(OVERALL,hist);
			Color diff=dls.getDiffuseColor();
			if (diff!=null){
				writeMaterial(null,diff,null,0,hist);
			}
		}

		// write object
		/**		IndexedLineSet {
		 *		coordIndex         0  # ok
		 *		materialIndex      -1 # egal
		 *		normalIndex        -1 # egal
		 *		textureCoordIndex  -1 # egal
		 *		} */
		out.print(hist+"IndexedLineSet {");
		out.println(" # "+ l.getName());
		// writes the edgeIndices
		writeIndices(l.getEdgeAttributes(Attribute.INDICES).toIntArrayArray(null), hist+spacing);
		out.println(hist+"}");
	}
	private static void writeGeoPoints(PointSet p,String hist){
		// write coords
		writeCoordinates(p.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null),hist);
		// writes Vertex Colors
		if(p.getVertexAttributes(Attribute.COLORS)!=null){
			writeMaterialBinding(PER_VERTEX,hist);
			writeColors(p.getVertexAttributes(Attribute.COLORS).toDoubleArrayArray(null),hist);
		}
		else {
			writeMaterialBinding(OVERALL,hist);
			Color diff=dvs.getDiffuseColor();
			if (diff!=null){
				writeMaterial(null,diff,null,0,hist);
			}
		}
		// write object
		/**		PointSet {
		 *		startIndex  0 	# default ok
		 *		numPoints   -1    # ok
		 *		} */
		out.print(hist+"PointSet {");
		out.println(" # "+ p.getName());
		out.println(hist+ spacing + "numPoints "+ p.getNumPoints());
		out.println(hist+"}");
	}
	private static void writeLight(Light li,String hist){
		if (li instanceof PointLight)
			writePointLight((PointLight) li,hist);				
		else if (li instanceof DirectionalLight)
			writeDirLight((DirectionalLight) li,hist);
		else if (li instanceof SpotLight)
			writeSpotLight((SpotLight) li,hist);
		else System.err.println("WriterVRML.writeLight() Failure");
	}
	private static void writePointLight(Light li,String hist){
		System.out.println("WriterVRML.writeLightPoint()");
	}
	private static void writeDirLight(Light li,String hist){
		System.out.println("WriterVRML.writeLightDir()");
	}
	private static void writeSpotLight(Light li,String hist){
		System.out.println("WriterVRML.writeLightSpot()");
	}
	private static void writeCam(Camera c,String hist){
		if (c.isPerspective()){
			String hist2=hist+spacing;
			//TODO: orientation
			out.println(hist+"PerspectiveCamera { ");
			out.println(hist2+"focalDistance "+c.getFocalLength());
			double hAngle=c.getFieldOfView()*Math.PI /180;
			out.println(hist2+"heightAngle "+hAngle);			
			out.println(hist+"}");
		}
		else {System.out.println("WriterVRML.writeCam()");}
	}
	private static void writeTrafo(Transformation t,String hist){
		out.println(hist+"MatrixTransform { matrix");
		writeDoubleMatrix(t.getMatrix(),4,4,hist+spacing);
		out.println(hist+"}");
	}
	private static void writeCoordinates(double[][] points,String hist){
		out.println(hist+"Coordinate3 { point [");
		String hist2=hist+spacing;
		if (points[0].length == 4) points = Pn.dehomogenize(points, points);
		for(int i=0;i<points.length;i++)
			writeDoubleArray(points[i],hist2,",",3);
		out.println(hist+"]}");
	}	
	private static void writeNormals(double[][] normals,String hist){
		out.println(hist+"Normal { vector  [");
		String hist2=hist+spacing;
		if (normals[0].length == 4) normals = Pn.dehomogenize(normals, normals);
		for(int i=0;i<normals.length;i++)
			writeDoubleArray(normals[i],hist2,",",3);
		out.println(hist+"]}");	
	}
	private static void writeColors(double[][] Colors,String hist){
		out.println(hist+"Material { ");
		out.println(hist+spacing+"diffuseColor  [");
		String hist2=hist+spacing+spacing;
		for(int i=0;i<Colors.length;i++){
			writeDoubleArray(Colors[i],hist2,",",3);
		}
		out.println(hist+"]}");
		// siehe transparency
		// material nur diffuse
	}	
	private static void writeMaterialBinding(int binding,String hist ){
		out.print(hist+"MaterialBinding { value ");
		if(binding==PER_VERTEX)
			out.println("PER_VERTEX }");
		else if(binding==PER_FACE)
			out.println("PER_FACE }");
		else if(binding==PER_PART)
			out.println("PER_PART }");
		else if(binding==OVERALL)
			out.println("OVERALL }");
		else out.println("DEFAULT }");
	} 
	private static void writeNormalBinding(int binding,String hist){
		out.print(hist+"NormalBinding { value ");
		if(binding==PER_VERTEX)
			out.println("PER_VERTEX }");
		else if(binding==PER_FACE)
			out.println("PER_FACE }");
		else out.println("DEFAULT }");
	} 
	public static void writeTexture(String hist){
		/**		WRAP ENUM
		 *		REPEAT  Repeats texture outside 0-1 texture coordinate range
		 *		CLAMP   Clamps texture coordinates to lie within 0-1 range
		 *		FILE FORMAT/DEFAULTS
		 *		Texture2 {
		 *		filename    ""        # SFString egal
		 *		image       0 0 0     # SFImage
		 *		wrapS       REPEAT    # SFEnum later 
		 *		wrapT       REPEAT    # SFEnum later
		 *		}		*/
		String hist2=hist+spacing;
		Texture2D tex=dps.getTexture2d();

		if (tex!=null){
			out.println(hist+"Texture2 {");{
				writeImage(tex,hist2);
//				texture.getRepeatS()
//				texture.getRepeatT()
			}
			out.println(hist+"}");
			writeTexTrans(hist2,tex);
		}

	}
	//	-----------------------------	

	private static double[] colorToDoubleArray(Color c){
		return new double[]{(double)c.getRed(),(double)c.getGreen(),(double)c.getBlue()};
	}
	private static void writeImage(Texture2D tex,String hist){
		String hist2=hist+spacing;
		ImageData id=tex.getImage();
		byte[] data= id.getByteArray();
		int w=id.getWidth();
		int h=id.getHeight();
		int dim= data.length/(w*h);
		// write image
		out.print(hist+"image ");
		out.println(""+w+" "+h+" "+dim);
		for (int i = 0; i < w*h; i++) {
			int mergeVal=0;
			// calculate hexvalue from colors
			for (int k = 0; k < dim; k++) {
				mergeVal*=256;
				mergeVal+=data[i*4+k];
			}
			out.println(hist2+"0x"+ Integer.toHexString(mergeVal).toUpperCase());
		}
	}
	private static String ColorToString(Color c){
		return ""+((double)c.getRed())/255+" "+((double)c.getGreen())/255+" "+((double)c.getBlue())/255;
	}
	private static void writeDoubleArray(double[] d, String hist, String append,int size){
		out.print(""+hist);
		for (int i=0;i<size;i++)
			out.print(d[i]+" ");
		out.println(append);
	}
	private static void writeDoubleMatrix(double[] d,int width, int depth, String hist){
		double[] n=new double[width];
		for (int i=0;i<depth;i++){
			System.arraycopy(d,i*width,n,0,4);
			writeDoubleArray(n,hist,"",n.length);
		}
	}
	private static void writeIndices(int[][] in,String hist){
		out.println(hist+"coordIndex ["); 		
		for (int i=0;i<in.length;i++){
			int le=in[i].length;
			out.print(hist+spacing);
			for(int j=0;j<le;j++){
				out.print(""+in[i][j]+", ");
			}
			out.println(" -1, ");
		}
		out.println(hist+"]");
	}
	private static void writeMaterial(Color a,Color d,Color s,double t,String hist){
		String hist2=hist+spacing;
		out.println(hist+"Material { ");
		// have to set all colors at once, otherwise unset colors return to defaultValue
		if(a!=null)	out.println(hist2+"ambientColor " + ColorToString(a) );
		if(d!=null)	out.println(hist2+"diffuseColor " + ColorToString(d) );
		if(s!=null)	out.println(hist2+"specularColor " + ColorToString(s) );
		if(t!=0){ out.println(hist2+"transparency " + t );	}
		out.println(hist+"}");
	}
	private static void writeTexTrans(String hist,Texture2D tex){
		String hist2=hist+spacing;
		/**		Texture2Transform {
		 *		translation  0 0      # SFVec2f
		 *		rotation     0        # SFFloat
		 *		scaleFactor  1 1      # SFVec2f
		 *		center       0 0      # SFVec2f
		 *		}		*/
		Matrix mat= tex.getTextureMatrix();
		FactoredMatrix matrix= new FactoredMatrix(mat.getArray());
		double[] trans=matrix.getTranslation();
		double ang=matrix.getRotationAngle();
		double[] rotA=matrix.getRotationAxis();
		double[] scale = matrix.getStretch();

		out.println(hist+"Texture2Transform {");
		out.println(hist2+"translation  "+trans[0]/trans[3]+" "+trans[1]/trans[3]);
		out.println(hist2+"rotation  "+ang);
		out.println(hist2+"scaleFactor "+scale[0]+" "+scale[1]);
		out.println(hist2+"center 0 0");
		out.println(hist+"}");
	}
	private static void writeTexCoords(IndexedFaceSet f,String hist){
		/**		TextureCoordinate2 {
		 *		point  0 0    # MFVec2f
		 *		} */
		String hist2=hist+spacing;
		double [][] texcoords= null;
		if (f.getVertexAttributes(Attribute.TEXTURE_COORDINATES)!=null){
			texcoords=f.getVertexAttributes(Attribute.TEXTURE_COORDINATES).toDoubleArrayArray(null);
			out.println(hist+"TextureCoordinate2 { point [");
			for(int i=0;i<texcoords.length;i++)
				writeDoubleArray(texcoords[i],hist2,",",2);
			out.println(hist+"]}");
		}
	}	
	private static double[][] convertLineVertexColors(double[][] colors,int[][] lindis){
		LinkedList list= new LinkedList<double[]>();
		for (int i = 0; i < lindis.length; i++) 
			for (int j = 0; j < lindis[i].length; j++) 
				list.add(colors[lindis[i][j]]);
		double[][] newCol=new double[list.size()][];
		for (int i = 0; i < newCol.length; i++) 
			newCol[i]=(double[])list.get(i);
		return newCol;
	}
}
