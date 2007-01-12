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
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.logging.Level;

import javax.imageio.stream.FileImageInputStream;

import de.jreality.math.Matrix;
import de.jreality.reader.vrml.VRMLParser;
import de.jreality.reader.vrml.VRMLV1Lexer;
import de.jreality.reader.vrml.VRMLV1Parser;
import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
import de.jreality.scene.DirectionalLight;
import de.jreality.scene.Geometry;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.Light;
import de.jreality.scene.PointLight;
import de.jreality.scene.PointSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SpotLight;
import de.jreality.scene.Transformation;
import de.jreality.scene.data.Attribute;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.EffectiveAppearance;
import de.jreality.ui.viewerapp.ViewerApp;
import de.jreality.util.LoggingSystem;

public class WriterVRML {
	private static final int PER_VERTEX=0,PER_FACE=1;
	
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
		writeComp(sgc,"",EffectiveAppearance.create());
		out.flush();
	}
	static int count=0; // counts component depth in the tree 
	
// ---------------------------- start writing --------------------
	private static void writeComp(SceneGraphComponent c,String hist,EffectiveAppearance parentEA){
		count++;
		int co=	count;
		System.out.println("WriterVRML."+co);
		System.out.println("WriterVRML.writeComp()");
		Geometry g = c.getGeometry();
		Camera cam = c.getCamera();
		Light li = c.getLight();
		EffectiveAppearance eApp=(c.getAppearance()!=null)?
				parentEA.create(c.getAppearance()):
				parentEA;
		Appearance app =c.getAppearance();
		Transformation t= c.getTransformation();
		out.print(""+hist+"Separator { ");
		out.println("# "+c.getName());
		String hist2= hist+spacing;
		if (t!=null)		writeTrafo(t,hist2);
		if (app!=null)		writeAppColorAndTrans(app,eApp,hist2);
		for (int i=0;i<c.getChildComponentCount();i++)
			writeComp(c.getChildComponent(i),hist2,eApp);
		if (g!=null)		writeGeo(g,eApp,hist2);	//TODO
		if (li!=null)		writeLight(li,eApp,hist2);//TODO
		if (cam!=null)		writeCam(cam,hist2);//TODO 3
		out.println(""+hist+"}");
		System.out.println("WriterVRML.Ende"+co);
		
	}
	private static void writeAppColorAndTrans(Appearance app,EffectiveAppearance eApp,String hist){
		// single color ,transparency
	if (app.getAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.AMBIENT_COLOR)!=null
		||app.getAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR)!=null
		||app.getAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.SPECULAR_COLOR)!=null
		||app.getAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.TRANSPARENCY)!=null){
		String hist2=hist+spacing;
		Color c;
		out.println(hist+"Material { ");
		// have to set all colors simultaniusly, otherwise not set colors return to defaultValue
		c=(Color)eApp.getAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.AMBIENT_COLOR, Color.BLUE);
		out.println(hist2+"ambientColor " + ColorToString(c) );
		
		c=(Color)eApp.getAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, Color.BLUE);
		out.println(hist2+"diffuseColor " + ColorToString(c) );
		
		c=(Color)eApp.getAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.SPECULAR_COLOR, Color.BLACK);
		out.println(hist2+"specularColor " + ColorToString(c) );
		
		if(eApp.getAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.TRANSPARENCY_ENABLED,false)){
			double tr =(double)eApp.getAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.TRANSPARENCY,0.0);
			out.println(hist2+"transparency " + tr );
		}
		
		out.println(hist+"}");}
	}
	private static void writeGeo(Geometry g,EffectiveAppearance eApp,String hist){
		if (g instanceof IndexedFaceSet)
			writeGeoFaces((IndexedFaceSet) g,eApp,hist);		
		else if (g instanceof IndexedLineSet)
			writeGeoLines((IndexedLineSet) g,hist);
		else if (g instanceof PointSet)
			writeGeoPoints((PointSet) g,hist);
		else System.err.println("WriterVRML.writeComp() Failure");
	}
	private static void writeGeoFaces(IndexedFaceSet f,EffectiveAppearance eApp,String hist){
		// writes an Indexed Faceset
		
		System.out.println("WriterVRML.writeGeoFaces()");
		// write the coordinates:
		writeCoordinates(f.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null),hist);
		// writes the Normals depending on smooth or flat shading:
		if(eApp.getAttribute(CommonAttributes.SMOOTH_SHADING,true)){
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
		// writes Face Colors if given:
		if(f.getFaceAttributes(Attribute.COLORS)!=null){
			writeMaterialBinding(PER_FACE,hist);
			writeColors(f.getFaceAttributes(Attribute.COLORS).toDoubleArrayArray(null),eApp,hist);
		}
		if(f.getVertexAttributes(Attribute.COLORS)!=null){
			writeMaterialBinding(PER_VERTEX,hist);
			writeColors(f.getVertexAttributes(Attribute.COLORS).toDoubleArrayArray(null),eApp,hist);
		}
		
		
//		   IndexedFaceSet {
//		          coordIndex         0  # MFLong indies
//		          materialIndex      -1 # MFLong egal
//		          normalIndex        -1 # MFLong egal
//		          textureCoordIndex  -1 # MFLong spaeter
//		     }
		out.print(hist+"IndexedFaceSet {");
		out.println(" # "+ f.getName());
		// writes the FaceIndices
		writeIndices(f.getFaceAttributes(Attribute.INDICES).toIntArrayArray(null), hist+spacing);
		out.println(hist+"}");
	}
	private static void writeGeoLines(IndexedLineSet l,String hist){
		System.out.println("WriterVRML.writeGeoLines()");
	}
	private static void writeGeoPoints(PointSet p,String hist){
		System.out.println("WriterVRML.writeGeoPoints()");
	}
	private static void writeLight(Light li,EffectiveAppearance app,String hist){
		if (li instanceof PointLight)
			writeLightPoint((PointLight) li,hist);				
		else if (li instanceof DirectionalLight)
			writeLightDir((DirectionalLight) li,hist);
		else if (li instanceof SpotLight)
			writeLightSpot((SpotLight) li,hist);
		else System.err.println("WriterVRML.writeLight() Failure");
	}
	private static void writeLightPoint(Light li,String hist){
		System.out.println("WriterVRML.writeLightPoint()");
	}
	private static void writeLightDir(Light li,String hist){
		System.out.println("WriterVRML.writeLightDir()");
	}
	private static void writeLightSpot(Light li,String hist){
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
		for(int i=0;i<points.length;i++)
			writeDoubleArray(points[i],hist2,",",3);
		out.println(hist+"]}");
	}	
	private static void writeNormals(double[][] normals,String hist){
		out.println(hist+"Normal { vector  [");
		String hist2=hist+spacing;
		for(int i=0;i<normals.length;i++)
			writeDoubleArray(normals[i],hist2,",",3);
		out.println(hist+"]}");	
	}
	private static void writeColors(double[][] Colors,EffectiveAppearance eApp,String hist){
		out.println(hist+"Material { ");
		out.println(hist+spacing+"diffuseColor  [");
		String hist2=hist+spacing;
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
	//	-----------------------------	
	private static double[] colorToDoubleArray(Color c){
		return new double[]{(double)c.getRed(),(double)c.getGreen(),(double)c.getBlue()};
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
	public static void writeIndices(int[][] in,String hist){
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
// -----------------------------
	public static void main(String[] args) {
		//String loadFile="/homes/geometer/gonska/VrmlFiles/lasertrk.wrl";
		//String loadFile="/homes/geometer/gonska/VrmlFiles/test.wrl";
		//String loadFile="/homes/geometer/gonska/VrmlFiles/BindingMTest.wrl";
		//String loadFile="/homes/geometer/gonska/VrmlFiles/geoTest.wrl";
		String loadFile="/homes/geometer/gonska/VrmlFiles/hangglider.wrl";
		
		String saveFile="/homes/geometer/gonska/VrmlFiles/ich.wrl";
		//String saveFile="/homes/geometer/gonska/VrmlFiles/hangglider.wrl";
		
		FileReader in=null;
		PrintWriter outs=null;
		VRMLV1Parser v=null;
		SceneGraphComponent comp=null;
		
		try {
			in=new FileReader(new File(loadFile));
			v=new VRMLV1Parser(new VRMLV1Lexer(in));
			comp=v.vrmlFile();

			outs= new PrintWriter(new FileWriter(saveFile));
			WriterVRML.write(comp, outs);

			in=new FileReader(new File(saveFile));
			v=new VRMLV1Parser(new VRMLV1Lexer(in));
			comp=v.vrmlFile();
			
			ViewerApp.display(comp);
		} catch (Exception e) {e.printStackTrace();}
	}
}
