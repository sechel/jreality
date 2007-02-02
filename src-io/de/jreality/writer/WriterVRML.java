package de.jreality.writer;

/**
 * @author gonska
 * 
 */


/**TODO 
 * Labels
 */
import java.awt.Color;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedList;
import de.jreality.math.FactoredMatrix;
import de.jreality.math.Matrix;
import de.jreality.math.Pn;
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
import de.jreality.scene.Sphere;
import de.jreality.scene.SpotLight;
import de.jreality.scene.Transformation;
import de.jreality.scene.data.Attribute;
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
	// for  DEF & USE
	private static HashMap<Integer, SceneGraphComponent> cmpMap = new HashMap<Integer, SceneGraphComponent>();
	
	private static DefaultGeometryShader dgs;
	private static RenderingHintsShader rhs;
	private static DefaultPolygonShader dps;
	private static DefaultLineShader dls;
	private static DefaultPointShader dvs;
	private static DefaultTextShader dpts;
	private static DefaultTextShader dlts;
	private static DefaultTextShader dvts;

	private static final int PER_VERTEX=0,PER_PART=1,PER_FACE=2,OVERALL=3;

	private static PrintWriter out=null;
	private static final String spacing="  ";// for outlay

	public static void write( SceneGraphComponent sgc, OutputStream outS )throws IOException {
		if (sgc==null) throw new IOException("component is null");
		write( sgc, new PrintWriter( outS ));
	}
	public static void write( SceneGraphComponent sgc, FileWriter outS )throws IOException {
		if (sgc==null) throw new IOException("component is null");
		write( sgc, new PrintWriter( outS ));
	}
	public static void write( SceneGraphComponent sgc, PrintWriter outS )throws IOException {
		if (sgc==null) throw new IOException("component is null");
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
		writeFirstComp(sgc,"",eApp);
		out.flush();
	}
//	------------------ 
	private static void updateShaders(EffectiveAppearance eap) {
		dgs = ShaderUtility.createDefaultGeometryShader(eap);
		rhs = ShaderUtility.createRenderingHintsShader(eap);

		if (dgs.getPointShader() instanceof DefaultPointShader){	
			dvs = (DefaultPointShader) dgs.getPointShader();
			if (dvs.getTextShader() instanceof DefaultTextShader){
				dvts=(DefaultTextShader)dvs.getTextShader();
			}
			else dvts=null;
		}
		else dvs = null;
		if (dgs.getLineShader() instanceof DefaultLineShader){ 
			dls = (DefaultLineShader) dgs.getLineShader();
			if (dls.getTextShader() instanceof DefaultTextShader){
				dlts=(DefaultTextShader)dls.getTextShader();
			}
			else dlts=null;
		}
		else dls = null;
		if (dgs.getPolygonShader() instanceof DefaultPolygonShader){
			dps = (DefaultPolygonShader) dgs.getPolygonShader();
			if (dps.getTextShader() instanceof DefaultTextShader)
				dpts=(DefaultTextShader) dps.getTextShader();
			else dpts=null;
		}
		else dps = null;
		
		
	}
// ---------------------
//	---------------------------- start writing --------------------

	private static void writeFirstComp(SceneGraphComponent c,String hist,
			EffectiveAppearance parentEA)throws IOException{
		if (c==null)throw new IOException("A SceneGraphComponent is null");
		if (!c.isVisible()) return;
		// write 
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
		// defaults:
		/*		ShapeHints {
	          vertexOrdering  UNKNOWN_ORDERING      # SFEnum
	          shapeType       UNKNOWN_SHAPE_TYPE    # SFEnum
	          faceType        CONVEX                # SFEnum
	          creaseAngle     0.5                   # SFFloat
	     }*/
		out.println(""+hist+"ShapeHints { ");
		out.println(""+hist2+"vertexOrdering  UNKNOWN_ORDERING");
		out.println(""+hist2+"shapeType       UNKNOWN_SHAPE_TYPE");
		out.println(""+hist2+"faceType        CONVEX");
		out.println(""+hist+"}");
		//
		
		
		out.println("# "+c.getName());
		if (t!=null)		writeTrafo(t,hist2);
		for (int i=0;i<c.getChildComponentCount();i++)
			writeComp(c.getChildComponent(i),hist2,eApp);
		if (g!=null)		writeGeo(g,hist2);// use Appearance
		if (li!=null)		writeLight(li,hist2);// use Appearance
		if (cam!=null)		writeCam(cam,hist2);
		out.println(""+hist+"}");
	}

	private static void writeComp(SceneGraphComponent c,String hist,
			EffectiveAppearance parentEA)throws IOException{
		if (c==null)throw new IOException("A SceneGraphComponent is null");
		if (!c.isVisible()) return;
		// check if allready defined
		if (cmpMap.containsKey(c.toString())){
			out.println(""+hist+"USE \""+c.hashCode()+"\"");
			return;
		}
		cmpMap.put(c.hashCode(), c);
		
		// write 
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
		out.println(""+hist+"DEF \""+c.hashCode()+"\"");
		out.println(""+hist+"Separator { # "+c.getName());
		if (t!=null)		writeTrafo(t,hist2);
		for (int i=0;i<c.getChildComponentCount();i++)
			writeComp(c.getChildComponent(i),hist2,eApp);
		if (g!=null)		writeGeo(g,hist2);// use Appearance
		if (li!=null)		writeLight(li,hist2);// use Appearance
		if (cam!=null)		writeCam(cam,hist2);
		out.println(""+hist+"}");
	}
	private static void writeGeo(Geometry g,String hist)throws IOException{
		if (g instanceof Sphere){
			if (dgs.getShowFaces())	writeSphere((Sphere)g,hist);return;}
		if (g instanceof Cylinder ){
			if (dgs.getShowFaces())	writeCylinder((Cylinder)g,hist);return;}
		if (g instanceof IndexedFaceSet)
			if (dgs.getShowFaces())	writeGeoFaces((IndexedFaceSet) g,hist);		
		if (g instanceof IndexedLineSet)
			if(dls.getTubeDraw()&& dgs.getShowLines())
				writeGeoLines((IndexedLineSet) g,hist);
		if (g instanceof PointSet){
			if(dvs.getSpheresDraw()&& dgs.getShowPoints())
			    writeGeoPoints((PointSet) g,hist);
			return;
		}
		else System.err.println("WriterVRML.writeComp() unknown geometrytype");
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
		out.print("parts SIDES ");
		out.print("radius  1 ");
		out.print("height  1 ");
		out.println("}");
	}
	private static void writeGeoFaces(IndexedFaceSet f,String hist)throws IOException{
		// write the coordinates:
		if (f.getVertexAttributes(Attribute.COORDINATES)==null)throw new IOException("no Coordinates");
		double[][] coords=f.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
		writeCoordinates(coords,hist);
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
		try {
			// check texture
			if (f.getVertexAttributes(Attribute.TEXTURE_COORDINATES)==null)
				throw new IOException("missing texture component");			
			double[][]texcords=f.getVertexAttributes(Attribute.TEXTURE_COORDINATES).toDoubleArrayArray(null);
			Texture2D tex=dps.getTexture2d();
			if (tex==null) throw new IOException("missing texture component");
			Matrix mat= tex.getTextureMatrix();
			if (mat==null) throw new IOException("missing texture component");
			ImageData id=tex.getImage();
			if (id==null) throw new IOException("missing texture component");
			
			// write texture
			writeTexCoords(texcords,hist);	
			writeTexture(tex,hist);
		} catch (IOException e) {
			// falls es nur nicht genug componenten fuer die Textur gibt 
			// werfe keinen Fehler
			if (!e.getMessage().equals("missing texture component"))
			throw e;
		}
			
		/**	IndexedFaceSet {
		 * 	coordIndex         0  # MFLong indies
		 * 	materialIndex      -1 # MFLong egal
		 * 	normalIndex        -1 # MFLong egal
		 * 	textureCoordIndex  -1 # MFLong spaeter
		 * 	} */	
		
		out.println(hist+"IndexedFaceSet { # "+ f.getName());
		// writes the FaceIndices
		if (f.getFaceAttributes(Attribute.INDICES)==null) throw new IOException("no FaceIndices");
		int[][] indices=f.getFaceAttributes(Attribute.INDICES).toIntArrayArray(null);
		writeIndices(indices, hist+spacing);
		out.println(hist+"}");
		
		// write Labels
		if(dpts.getShowLabels())
			if (f.getFaceAttributes(Attribute.LABELS)!=null){
				String[] labels=f.getFaceAttributes(Attribute.LABELS).toStringArray(null);
				writeFaceLabels(coords,indices,labels,hist);	
			}
			
	}
	private static void writeGeoLines(IndexedLineSet l,String hist)throws IOException{
		if (l.getVertexAttributes(Attribute.COORDINATES)==null)
			throw new IOException("missing coordinates");
		if (l.getEdgeAttributes(Attribute.INDICES)==null) 
			return;
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
		out.println(hist+"IndexedLineSet { # "+ l.getName());
		// writes the edgeIndices
		writeIndices(lindices, hist+spacing);
		out.println(hist+"}");
		// write labels
		if(dlts.getShowLabels())
			if (l.getEdgeAttributes(Attribute.LABELS)!=null){
				String[] labels=l.getEdgeAttributes(Attribute.LABELS).toStringArray(null);
				writeFaceLabels(lcoords,lindices,labels,hist);	
			}
	}
	private static void writeGeoPoints(PointSet p,String hist)throws IOException{
		// write coords
		if(p.getVertexAttributes(Attribute.COORDINATES)==null)throw new IOException("missing coordinates");
		double[][] coords=p.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
		writeCoordinates(coords,hist);
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
		out.println(hist+"PointSet { # "+ p.getName());
		out.println(hist+ spacing + "numPoints "+ p.getNumPoints());
		out.println(hist+"}");
		// write labels
		if(dvts.getShowLabels())
			if (p.getVertexAttributes(Attribute.LABELS)!=null){
				String[] labels=p.getVertexAttributes(Attribute.LABELS).toStringArray(null);
				writeLabelsAtPoints(coords, labels, hist);
			}
	}
	private static void writeLight(Light li,String hist)throws IOException{
		
		if (li instanceof SpotLight)
			writeSpotLight((SpotLight) li,hist);
		else if (li instanceof PointLight)
			writePointLight((PointLight) li,hist);				
		else if (li instanceof DirectionalLight)
			writeDirLight((DirectionalLight) li,hist);
		else System.err.println("WriterVRML.writeLight() unknown Lighttype");
	}
	private static void writePointLight(PointLight li,String hist)throws IOException{
		String hist2= hist+spacing;
		/* PointLight {
	          on         TRUE       # SFBool
	          intensity  1          # SFFloat
	          color      1 1 1      # SFColor
	          location   0 0 1      # SFVec3f
	     }  */
		double di=li.getIntensity();
		double[] dc=colorToDoubleArray(li.getColor());
		out.println(hist+"PointLight { # "+ li.getName());
		out.println(hist2 + "intensity " +di);
		out.print(hist2 + "color " );
		writeDoubleArray(dc, "", "", 3);
		out.println(hist2 + "location   0 0 0");
		out.println(hist+"}");
	}
	private static void writeDirLight(DirectionalLight li,String hist)throws IOException{
		String hist2= hist+spacing;
		/*	DirectionalLight {
	          on         TRUE       # SFBool
	          intensity  1          # SFFloat
	          color      1 1 1      # SFColor
	          direction  0 0 -1     # SFVec3f
	     } */
		double di=li.getIntensity();
		double[] dc=colorToDoubleArray(li.getColor());
		out.println(hist+"DirectionalLight { # "+ li.getName());
		out.println(hist2 + "intensity " +di);
		out.print(hist2 + "color " );
		writeDoubleArray(dc, "", "", 3);
		out.println(hist2 + "direction  0 0 1");
		out.println(hist+"}");
	}
	private static void writeSpotLight(SpotLight li,String hist)throws IOException{
		String hist2= hist+spacing;
		/*  SpotLight {
	          on           TRUE     # SFBool
	          intensity    1        # SFFloat
	          color        1 1 1    # SFVec3f
	          location     0 0 1    # SFVec3f
	          direction    0 0 -1   # SFVec3f
	          dropOffRate  0        # SFFloat
	          cutOffAngle  0.785398 # SFFloat
	     }  */
		double di=li.getIntensity();
		double[] dc=colorToDoubleArray(li.getColor());
		
		out.println(hist+"SpotLight { # "+ li.getName());
		out.println(hist2 + "intensity " +di);
		out.print(hist2 + "color " );
		writeDoubleArray(dc, "", "", 3);
		out.println(hist2 + "location 0 0 0 ");
		out.println(hist2 + "dropOffRate 0" );
		out.println(hist2 + "direction  0 0 1");
		out.println(hist2 + "cutOffAngle "+li.getConeAngle() );
		out.println(hist+"}");
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
		else {System.out.println("WriterVRML.writeCam(not completely implemented)");}
	}

	private static void writeTrafo(Transformation t,String hist)throws IOException{
		out.println(hist+"MatrixTransform { matrix");
		writeDoubleMatrix(t.getMatrix(),4,4,hist+spacing);
		out.println(hist+"}");
	}
	private static void writeCoordinates(double[][] points,String hist)throws IOException{
		out.println(hist+"Coordinate3 { point [");
		String hist2=hist+spacing;
		if (points[0].length == 4) points = Pn.dehomogenize(points, points);
		for(int i=0;i<points.length;i++){
			if (points[i].length<3)throw new IOException("invalid Coordinates");
			writeDoubleArray(points[i],hist2,",",3);
			}
		out.println(hist+"]}");
	}	
	private static void writeNormals(double[][] normals,String hist)throws IOException{
		out.println(hist+"Normal { vector  [");
		String hist2=hist+spacing;
		if (normals[0].length == 4) normals = Pn.dehomogenize(normals, normals);
		for(int i=0;i<normals.length;i++){
			if (normals[i].length<3)throw new IOException("invalid Normals");
			writeDoubleArray(normals[i],hist2,",",3);
		}
		out.println(hist+"]}");	
	}
	private static void writeColors(double[][] Colors,String hist)throws IOException{
		out.println(hist+"Material { ");
		out.println(hist+spacing+"diffuseColor  [");
		String hist2=hist+spacing+spacing;
		for(int i=0;i<Colors.length;i++){
			if (Colors[i].length<3)throw new IOException("invalid Colors");
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
	public static void writeTexture(Texture2D tex,String hist)throws IOException{
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
		out.println(hist+"Texture2 {");
		writeImage(tex,hist2);
		out.print(hist+"wrapS ");
		writeTexWrap(tex.getRepeatS());
		out.print(hist+"wrapT ");
		writeTexWrap(tex.getRepeatT());
		out.println(hist+"}");
		writeTexTrans(hist2,tex);

	}
	private static void writeTexWrap(int wrap)throws IOException{
		switch (wrap) {
		case Texture2D.GL_CLAMP:
		case Texture2D.GL_CLAMP_TO_EDGE:
			System.out.println("texture wrap:only clamp & repeat are supported");
		case Texture2D.CLAMP:
			out.println("CLAMP");				
			break;
		case Texture2D.GL_REPEAT:
		case Texture2D.GL_MIRRORED_REPEAT:
			System.out.println("texture wrap:only clamp & repeat are supported");
		case Texture2D.REPEAT:
			out.println("REPEAT");				
			break;
		default:
			throw new IOException("unknown Texture wrapping");
		}
	}
	
	private static void writeFaceLabels(double[][]coords,int[][]indis,String[]labs,String hist)throws IOException{
		if (labs==null||indis==null||coords==null)throw new IOException("Labels impossible");
		double[][] centers= new double[indis.length][3];// coords for the labels
		double[] midpoint;
		int[] face;
		for (int i = 0; i < indis.length; i++) {// all faces
			face=indis[i];
			midpoint=new double[]{0,0,0};
			int j;
			for (j = 0; j < face.length; j++)// add vertices
				for (int k = 0; k < 3; k++)
					midpoint[k]+=coords[face[j]][k];
			for (int k = 0; k < 3; k++) // calc the mean
				centers[i][k]=midpoint[k]/j;
		}
		writeLabelsAtPoints(centers,labs,hist);
	}
	
	private static void writeEdgeLabels(double[][]coords,int[][]indis,String[]labs,String hist)throws IOException{
		if (labs==null||indis==null||coords==null)throw new IOException("Labels impossible");
		double[][] centers= new double[indis.length][3];// coords for the labels
		int[] firstEdge;
		for (int i = 0; i < indis.length; i++) {// all line-Objects
			firstEdge=indis[i];
			if (firstEdge.length<2)throw new IOException("bad Lines");
			for (int k = 0; k < 3; k++) // calc the mean of the first segment
				centers[i][k]=coords[indis[i][0]][k]/2+coords[indis[i][1]][k]/2;
		}
		writeLabelsAtPoints(centers,labs,hist);
	}
	private static void writeLabelsAtPoints(double[][]centers,String[] labs,String hist)throws IOException{
		 /*AsciiText {
        	string         ""    # MFString
        	spacing        1     # SFFloat
        	justification  LEFT  # SFEnum
        	width          0     # MFFloat
   		}*/
		/*Translation {
        	translation  0 0 0    # SFVec3f
   		} */
		for (int i = 0; i < centers.length; i++) {
			out.println(hist+"Translation { translation ");
			writeDoubleArray(centers[i], "", " }", 3);
			out.println(hist+"AsciiText { width 1 string "+labs[i]+"}");
			out.println(hist+"Translation { translation ");
			writeDoubleArray(new double[]{-centers[i][0],-centers[i][1],-centers[i][2]}, "", " }", 3);
		}
		
	}
	//	-----------------------------	
	private static double[] colorToDoubleArray(Color c){
		double[] d=new double[]{(double)c.getRed()/255,(double)c.getGreen()/255,(double)c.getBlue()/255};
		return d;
	}
	private static void writeImage(Texture2D tex,String hist)throws IOException{
		String hist2=hist+spacing;
		ImageData id=tex.getImage();
		byte[] data= id.getByteArray();
		int w=id.getWidth();
		int h=id.getHeight();
		int dim= data.length/(w*h);
		if (data.length!=dim*h*w) throw new IOException("invalid image");
		if (dim <0|dim>4) throw new IOException("invalid image Color-Dimension");
		// write image
		out.print(hist+"image ");
		out.println(""+w+" "+h+" "+dim);
		for (int i = 0; i < w*h; i++) {
			int mergeVal=0;
			// calculate hexvalue from colors
			for (int k = 0; k < dim; k++) {
				int val=data[i*4+k];
				if (val<0)val=val+256;
				mergeVal*=256;
				mergeVal+=val;
				}
			out.println(hist2+"0x"+ Integer.toHexString(mergeVal).toUpperCase());
		}
	}
	private static String ColorToString(Color c){
		return ""+((double)c.getRed())/255+" "+((double)c.getGreen())/255+" "+((double)c.getBlue())/255;
	}
	private static void writeDoubleArray(double[] d, String hist, String append,int size)throws IOException{
		out.print(""+hist);
		if (d.length<size)throw new IOException("Invalid Data");
		for (int i=0;i<size;i++)
			out.print(String.format(" %13.7g",d[i]));
		out.println(append);
	}
	private static void writeDoubleMatrix(double[] d,int width, int depth, String hist)throws IOException{
		if (d.length<width*depth)throw new IOException("Matrix is to short");
		double[] n=new double[width];
		for (int i=0;i<depth;i++){
			System.arraycopy(d,i*width,n,0,4);
			writeDoubleArray(n,hist,"",n.length);
		}
	}
	private static void writeIndices(int[][] in,String hist)throws IOException{
		if (in==null)throw new IOException("no coordinate Indices");
		out.println(hist+"coordIndex ["); 		
		for (int i=0;i<in.length;i++){
			if (in==null)throw new IOException("missing coordinate Indice");
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
	private static void writeTexCoords(double[][] texCoords,String hist)throws IOException{
		/**		TextureCoordinate2 {
		 *		point  0 0    # MFVec2f
		 *		} */
		String hist2=hist+spacing;
		out.println(hist+"TextureCoordinate2 { point [");
		for(int i=0;i<texCoords.length;i++)
			writeDoubleArray(texCoords[i],hist2,",",2);
		out.println(hist+"]}");
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
