package de.jreality.writer;

/**
 * @author gonska
 * 
 */


/**TODO 
 * Labels
 */
import java.awt.Color;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.WeakHashMap;

import de.jreality.math.FactoredMatrix;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
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
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.SceneGraphVisitor;
import de.jreality.scene.Sphere;
import de.jreality.scene.SpotLight;
import de.jreality.scene.Transformation;
import de.jreality.scene.data.Attribute;
import de.jreality.shader.DefaultGeometryShader;
import de.jreality.shader.DefaultLineShader;
import de.jreality.shader.DefaultPointShader;
import de.jreality.shader.DefaultPolygonShader;
import de.jreality.shader.DefaultTextShader;
import de.jreality.shader.EffectiveAppearance;
import de.jreality.shader.ImageData;
import de.jreality.shader.ShaderUtility;
import de.jreality.shader.Texture2D;

public class WriterVRML2{
	private boolean useDefs = true;
	private boolean drawTubes = false;
	private boolean drawSpheres = false;
	private boolean moveLightsToSceneRoot=true;
//	 moveCamToSceneRoot has to be true if scale is used in 
//	  Transformations which effect the camera
	private Matrix camMatrix;
	
	private VRMLWriterHelper wHelp= new VRMLWriterHelper();
	private DefaultGeometryShader dgs;
	private DefaultPolygonShader dps;
	private DefaultLineShader dls;
	private DefaultPointShader dvs;
	private DefaultTextShader dpts;
	private DefaultTextShader dlts;
	private DefaultTextShader dvts;
	private DefaultPolygonShader dlps;
	private DefaultPolygonShader dvps;
	private String fileStem = String.format("texture-%10d-", System.currentTimeMillis());
	private PrintWriter out=null;
	private static final String spacing="  ";// for outlay
	private static String hist="";// for outlay
	private Color amb;
	private Color spec;
	private Color diff;
	private double tra;

	
	private static enum GeoTyp{FACE,TUBE,LINE,SPHERE,POINT}
	// -----------------constructor----------------------
	public WriterVRML2(OutputStream outS) {	
		out=new PrintWriter( outS );
	}
	public WriterVRML2(FileWriter outS) {
		out=new PrintWriter( outS );
	}
	public WriterVRML2(PrintWriter outS) {
		out=outS;
	}
	public static void write(SceneGraphComponent sceneRoot, FileOutputStream stream)   {
		WriterVRML writer = new WriterVRML(stream);
		writer.write(sceneRoot);
	}
	String writePath = "";
	WeakHashMap<ImageData, String> textureMaps = new WeakHashMap<ImageData, String>();
	public void setWritePath(String path)	{
		writePath = path;
		if (!writePath.endsWith("/")) writePath = writePath + "/";
	}

	public void setWriteTextureFiles(boolean writeTextureFiles2) {
		writeTextureFiles = writeTextureFiles2;
	}
//	---------------------------------------

	public void write( SceneGraphNode sgn ){
		out.println("#VRML V2.0 utf8");
		// what should be defined
		if(useDefs){
			wHelp.inspect(sgn);
		}
		if(moveLightsToSceneRoot){
			// collect Lights
			out.println(""+hist+"Group { # collected lights and sceneRoot" );
			String oldhist= hist;
			hist=hist+spacing;
			out.println(hist+"children [");
			hist=hist+spacing;
			
			sgn.accept(new MyLightVisitor());
			sgn.accept(new MyVisitor());
			hist=oldhist;
			out.println(""+hist+spacing+"]");			
			out.println(""+hist+"}");
		}
		else
			sgn.accept(new MyVisitor());
		out.flush();
	}
//	------------------ 
	private void updateShaders(EffectiveAppearance eap) {
		dgs = ShaderUtility.createDefaultGeometryShader(eap);

		if (dgs.getPointShader() instanceof DefaultPointShader){	
			dvs = (DefaultPointShader) dgs.getPointShader();
			if (dvs.getTextShader() instanceof DefaultTextShader){
				dvts=(DefaultTextShader)dvs.getTextShader();
			}
			else dvts=null;
			if (dvs.getPolygonShader() instanceof DefaultPolygonShader){
				dvps=(DefaultPolygonShader)dvs.getPolygonShader();
			}
			else dvps=null;
		}
		else dvs = null;
		if (dgs.getLineShader() instanceof DefaultLineShader){ 
			dls = (DefaultLineShader) dgs.getLineShader();
			if (dls.getTextShader() instanceof DefaultTextShader){
				dlts=(DefaultTextShader)dls.getTextShader();
			}
			else dlts=null;
			if (dls.getPolygonShader() instanceof DefaultPolygonShader){
				dlps=(DefaultPolygonShader)dvs.getPolygonShader();
			}
			else dlps=null;
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
//	---------------------
//	---------------------------- start writing --------------------
	// --------------- helper Classes ---------------------
	
	static boolean writeTextureFiles = false;
	int textureCount = 0;
	private  void writeImage(Texture2D tex,String hist) {//TODO
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
				int val=data[i*4+k];
				if (val<0)val=val+256;
				mergeVal*=256;
				mergeVal+=val;
			}
			out.println(hist2+"0x"+ Integer.toHexString(mergeVal).toUpperCase());
		}
	}
	private   void writeCoords(double[][] coords,String hist) {// done
		/*Coordinate {
		  exposedField MFVec3f point  []
		}*/
		out.println(hist+"Coordinate { point [");
		String hist2=hist+spacing;
		for(int i=0;i<coords.length;i++){
			VRMLWriterHelper.writeDoubleArray(coords[i],hist2,",",3,out);
		}
		out.println(hist+"]}");
	}
	
	
	private  void writeColors(double[][] colors,String hist) {//done
		/*	Color {
		  exposedField MFColor color  []
		}*/
		out.println(hist+"Color { color [");
		String hist2=hist+spacing;
		for(int i=0;i<colors.length;i++){
			VRMLWriterHelper.writeDoubleArray(colors[i],hist2,",",3,out);
		}
		out.println(hist+"]}");
	}
	private   void writeNormals(double[][] normals,String hist) {//done
		/*Normal {
  			exposedField MFVec3f vector  []
		}*/
		out.println(hist+"Normal { vector [");
		String hist2=hist+spacing;
		for(int i=0;i<normals.length;i++){
			VRMLWriterHelper.writeDoubleArray(normals[i],hist2,",",3,out);
		}
		out.println(hist+"]}");
	}
	
	private  void writeDoubleMatrix(double[] d,int width, int depth, String hist) {//TODO
		double[] n=new double[width];
		for (int i=0;i<depth;i++){
			System.arraycopy(d,i*width,n,0,4);
			VRMLWriterHelper.writeDoubleArray(n,hist,"",n.length,out);
		}
	}
	private  void writeInfoString(String info,String hist) {
		/*Info {
	          string  "<Undefined info>"      # SFString
	     }	*/
		out.println(hist+"Info { string \"" +info+ "\" }");	
	}
	private  void writeIndices(int[][] in,String hist) {
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
	private  void writeTexTrans(String hist,Texture2D tex){//TODO
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
	private  void writeTexCoords(double[][] texCoords,String hist) {//done
		/*TextureCoordinate {
			  exposedField MFVec2f point  []
			}*/
		String hist2=hist+spacing;
		out.println(hist+"TextureCoordinate { point [");
		for(int i=0;i<texCoords.length;i++)
			VRMLWriterHelper.writeDoubleArray(texCoords[i],hist2,",",2,out);
		out.println(hist+"]}");
	}	
	private  void writeMaterial(GeoTyp typ){
		/*Material {
		  exposedField SFFloat ambientIntensity  0.2
		  exposedField SFColor diffuseColor      0.8 0.8 0.8
		  exposedField SFColor emissiveColor     0 0 0
		  exposedField SFFloat shininess         0.2
		  exposedField SFColor specularColor     0 0 0
		  exposedField SFFloat transparency      0
		}*/
		String histold= hist;
		hist=hist+spacing;
		out.println(hist+"Material { ");
		hist=hist+spacing;
		// have to set all colors at once, otherwise unset colors return to defaultValue
		switch (typ){
		case FACE: 
		case SPHERE: 
		case TUBE:{
			if(spec!=null)	out.println(hist+"specularColor " + VRMLWriterHelper.ColorToString(spec) );
			if(tra!=0){ out.println(hist+"transparency " + tra );	}
			if(diff!=null)	out.println(hist+"diffuseColor " + VRMLWriterHelper.ColorToString(diff) );
			break;
		}
		case LINE:
		case POINT:
			if(diff!=null)	out.println(hist+"emissiveColor " + VRMLWriterHelper.ColorToString(diff) );
		}
		hist= histold;
		out.println(hist+spacing+"}");
	}
	private void writeApp(GeoTyp typ){// TODO texture stuff unhandled
		String histOld=hist;
		String hist2=hist+spacing;
		String hist3=hist2+spacing;
		out.println(hist+"Appearance { ");
		hist=hist2;
		out.println(hist+"material ");
		hist=hist3;
		writeMaterial(typ);
		hist=hist2;
//		out.println(hist+"texture ");
		hist=hist3;
		//writeTexture(typ);
		hist=hist2;
//		out.println(hist+"textureTransform ");
		hist=hist3;
		//writeTextureTranfsform(typ);
		hist=histOld;
		out.println(hist+"} ");		
	}
	/** wraps the geometry in a shapeNode which supports an appearance
	 *  iff an appearance is given or forcedDiffuseColor is not null.
	 *  forcedDiffuseColor will be used (if not null) instead of diffusecolor from 
	 *  EffectiveAppearance    
	 * @param typ
	 * @param forcedDiffuseColor
	 * @return
	 */
	private boolean tryWriteShapeNode(GeoTyp typ,double[] forcedDiffuseColor){
		switch (typ){
		case FACE:{
			amb = dps.getAmbientColor();
			spec= dps.getSpecularColor();
			diff= dps.getDiffuseColor();
			tra= dps.getTransparency();			
			if(tra==0&&diff==null&&forcedDiffuseColor==null&&spec==null&&amb==null) return false;
		}
		case TUBE:{
			amb = dlps.getAmbientColor();
			spec= dlps.getSpecularColor();
			diff= dlps.getDiffuseColor();
			tra= dlps.getTransparency();
			if(tra==0&&diff==null&&forcedDiffuseColor==null&&spec==null&&amb==null) return false;
		}
		case LINE:{
			diff= dls.getDiffuseColor();
			if(diff==null) return false;
		}
		case SPHERE:{
			amb = dvps.getAmbientColor();
			spec= dvps.getSpecularColor();
			diff= dvps.getDiffuseColor();
			tra= dvps.getTransparency();
			if(tra==0&&diff==null&&forcedDiffuseColor==null&&spec==null&&amb==null) return false;
		}
		case POINT:{
			diff= dvs.getDiffuseColor();
			if(diff==null) return false;
		}
		}
		if(forcedDiffuseColor!=null)
			diff=VRMLWriterHelper.DoublesToColor(forcedDiffuseColor);
		writeShapeNode(typ);
		return true;
	}
	private void closeShapeNode(String oldHist){
		hist=oldHist;
		out.println(hist+"}");// close shapeNode
	}
	private void writeShapeNode(GeoTyp typ){
		String hist2=hist+spacing;
		String hist3=hist2+spacing;
		out.println(hist+"Shape { ");
		hist=hist2;
		out.println(hist+"appearance ");
		hist=hist3;
		writeApp(typ);
		hist=hist2;
		out.println(hist+"geometry ");
		hist=hist3;// will be closed with closeShapeNode()
	}
	private boolean needShapeNode(){
		Color amb = dps.getAmbientColor();
		Color spec= dps.getSpecularColor();
		Color diff= dps.getDiffuseColor();
		double tra= dps.getTransparency();
		return(tra!=0|diff!=null|spec!=null|amb!=null);
	}
	private void writeTrafo(Transformation trafo){
		FactoredMatrix fm= new FactoredMatrix(trafo.getMatrix());
		fm.update();
		double[] cen=fm.getCenter();
		if(cen!=null){
			out.println(hist+spacing+"center ");
			VRMLWriterHelper.writeDoubleArray(cen, hist, "", 3, out);
		}
		out.println(hist+spacing+"rotation ");
		VRMLWriterHelper.writeDoubleArray(fm.getRotationAxis(), hist, "  "+fm.getRotationAngle(), 3, out);
		out.println(hist+spacing+"scale ");
		VRMLWriterHelper.writeDoubleArray(fm.getStretch(), hist, "", 3, out);
		out.println(hist+spacing+"translation ");
		VRMLWriterHelper.writeDoubleArray(fm.getTranslation(), hist, "", 3, out);
	}
	private void writeDirLight(DirectionalLight l,String hist,PrintWriter out){
		/*DirectionalLight {
//		exposedField SFFloat ambientIntensity  0 
		exposedField SFColor color             1 1 1
		exposedField SFVec3f direction         0 0 -1
		exposedField SFFloat intensity         1 
//		exposedField SFBool  on                TRUE 
	}*/

	double di=l.getIntensity();
	double[] dc=VRMLWriterHelper.colorToDoubleArray(l.getColor());
	out.println(hist+"DirectionalLight { # "+ l.getName());
	String oldHist= hist;
	hist=hist+spacing;
	out.println(hist + "intensity " +di);
	out.print(hist + "color " );
	VRMLWriterHelper.writeDoubleArray(dc, "", "", 3,out);
	out.println(hist + "direction  0 0 1");
	hist=oldHist;
	out.println(hist+"}");
	}
	private void writePointLight(PointLight l,String hist,PrintWriter out,double[] location){
		/*PointLight {
//		  exposedField SFFloat ambientIntensity  0 
//		  exposedField SFVec3f attenuation       1 0 0
		  exposedField SFColor color             1 1 1 
		  exposedField SFFloat intensity         1
//		  exposedField SFVec3f location          0 0 0
//		  exposedField SFBool  on                TRUE 
//		  exposedField SFFloat radius            100
		}*/
	double di=l.getIntensity();
	double[] dc=VRMLWriterHelper.colorToDoubleArray(l.getColor());
	out.println(hist+"PointLight { # "+ l.getName());
	String oldHist= hist;
	hist=hist+spacing;
	out.println(hist + "intensity " +di);
	if(location!=null){
		out.print(hist + "location ");
		VRMLWriterHelper.writeDoubleArray(location, "", "", 3,out);
	}
	out.print(hist + "color " );
	VRMLWriterHelper.writeDoubleArray(dc, "", "", 3,out);
	hist=oldHist;
	out.println(hist+"}");			
	}
	private void writeSpotLight(SpotLight l,String hist,PrintWriter out,double[] location, double[] dir){
		
		/*SpotLight {
//			  exposedField SFFloat ambientIntensity  0 
//			  exposedField SFVec3f attenuation       1 0 0
			  exposedField SFFloat beamWidth         1.570796
			  exposedField SFColor color             1 1 1 
			  exposedField SFFloat cutOffAngle       0.785398
			  exposedField SFVec3f direction         0 0 -1
			  exposedField SFFloat intensity         1  
//			  exposedField SFVec3f location          0 0 0  
//			  exposedField SFBool  on                TRUE
//			  exposedField SFFloat radius            100 
			}*/
		double di=l.getIntensity();
		double[] dc=VRMLWriterHelper.colorToDoubleArray(l.getColor());
		out.println(hist+"SpotLight { # "+ l.getName());
		String oldHist= hist;		hist=hist+spacing;
		out.println(hist + "intensity " +di);
		out.print(hist + "color " );
		VRMLWriterHelper.writeDoubleArray(dc, "", "", 3,out);
		if(dir!=null){
			out.print(hist + "direction ");
			VRMLWriterHelper.writeDoubleArray(dir, "", "", 3,out);
		}
		if(location!=null){
			out.print(hist + "location ");
			VRMLWriterHelper.writeDoubleArray(location, "", "", 3,out);
		}
		out.println(hist + "beamWidth "+(l.getConeAngle()-l.getConeDeltaAngle()) );
		out.println(hist + "cutOffAngle "+l.getConeAngle() );
		hist=oldHist;
		out.println(hist+"}");

	}
	private void writeCamera(Camera c,String hist,PrintWriter out,double[] location, double[] rotAx,double ang){
	}
	
	// -------------- Visitor -------------------
	private class MyVisitor extends SceneGraphVisitor{
		protected EffectiveAppearance effApp= EffectiveAppearance.create();
		
		public MyVisitor() {}
		public MyVisitor(MyVisitor mv) {
			effApp=mv.effApp;
		}		
		public void visit(SceneGraphComponent c) {// fin
			if(!c.isVisible())return;
			Transformation trafo= c.getTransformation();
			if (trafo!=null){
				out.println(""+hist+"Transform { # "+c.getName());
				writeTrafo(trafo);
			}
			else{
				out.println(""+hist+"Group { # "+c.getName());
			}
			String oldhist= hist;
			hist=hist+spacing;
			out.println(hist+"children [");
			hist=hist+spacing;
			c.childrenAccept(new MyVisitor(this));
			super.visit(c);
			hist=oldhist;
			out.println(""+hist+spacing+"]");			
			out.println(""+hist+"}");
		}
		public void visit(Appearance a) {// fin
			effApp=effApp.create(a);
			super.visit(a);
		}
		// ----- geometrys -----
		public void visit(Sphere s) {//done
			super.visit(s);
			if ( !dgs.getShowFaces())	return;
			String histOld= hist;
			boolean hasShapeNode=tryWriteShapeNode(GeoTyp.FACE,null);
			out.println(hist+"Sphere { radius  1}");
			if(hasShapeNode) closeShapeNode(histOld);
		}
		public void visit(Cylinder c) {//done
			super.visit(c);
			if ( !dgs.getShowFaces())	return;
			String histOld= hist;
			boolean hasShapeNode=tryWriteShapeNode(GeoTyp.FACE,null);
			out.print(hist+"Cylinder { ");
			out.print("bottom    FALSE ");
			out.print("top    FALSE ");
//			out.print("radius  1 ");
//			out.print("height  2 ");
			out.println("}");
			if(hasShapeNode) closeShapeNode(histOld);
		}
		public void visit(Geometry g) {//done
			updateShaders(effApp);
			super.visit(g);
		}

		public void visit(PointSet p) {
			super.visit(p);
			if ( !dgs.getShowPoints()|| p.getNumPoints()==0) return;
			String histOld= hist;
			GeoTyp typ=(dvs.getSpheresDraw()&&drawSpheres)?(GeoTyp.SPHERE):(GeoTyp.POINT);
			if(typ==GeoTyp.SPHERE){
				double[][] coords=VRMLWriterHelper.getDoubleDoubleVertexAttr(p, Attribute.COORDINATES);
				double[][] colors=VRMLWriterHelper.getDoubleDoubleVertexAttr(p, Attribute.COLORS);
				double radius=dvs.getPointRadius();
				double [] forcedDiffuseColor=null;
				String hist2=histOld+spacing;
				String hist3=hist2+spacing;
				hist=hist3;
				for(int i=0;i<coords.length;i++){
					out.println(""+histOld+"Transform { # sphere of PointSet ");
					out.println(hist2+"translation ");
					VRMLWriterHelper.writeDoubleArray(coords[i], hist2, "", 3, out);
					out.println(hist2+"children [ ");
					if(colors!=null)
						forcedDiffuseColor=colors[i];
					boolean hasShapeNode=tryWriteShapeNode(typ,forcedDiffuseColor);
					out.println(hist+"Sphere { radius "+radius+" }");
					if(hasShapeNode) closeShapeNode(hist3);
					out.println(hist2+"]");// children
					out.println(histOld+"}");// transform
				}
				return;
			}
			boolean hasShapeNode=tryWriteShapeNode(typ,null);
			//-------------------------------
			//	check if allready defined
			if(useDefs){
				if (wHelp.isDefinedPointSet(p)){
					out.println(""+hist+"USE "+ VRMLWriterHelper.str( p.hashCode()+"POINT")+" ");
					if(hasShapeNode) closeShapeNode(histOld);
					return;
				}
			}
			if (useDefs && wHelp.isMultipleUsedPointSet(p)){
				out.print(""+hist+"DEF "+VRMLWriterHelper.str(p.hashCode()+"POINT")+" ");
				wHelp.setDefinedPointSet(p);
				}
			else out.print(""+hist);
			// write object:
			/*PointSet {
				  exposedField  SFNode  color      NULL
				  exposedField  SFNode  coord     NULL
				}*/
			String hist2=hist;
			hist=hist+spacing;
			out.println("PointSet { # "+ p.getName());
			double[][] coords=VRMLWriterHelper.getDoubleDoubleVertexAttr(p, Attribute.COORDINATES);
			if(coords!=null){
				out.println(hist+"coord ");
				writeCoords(coords, hist+spacing);
			}
			// writes Vertex Colors
			double[][] colors=VRMLWriterHelper.getDoubleDoubleVertexAttr(p, Attribute.COLORS);
			if(colors!=null)
				if(colors.length>0){
					out.println(hist+"color ");
					writeColors(colors,hist+spacing);
				}
			hist=hist2;
			out.println(hist+"}");
			///old end -------------------
			if(hasShapeNode) closeShapeNode(histOld);
			/// --------------- Labels -------------------
//			if(dvts.getShowLabels())
//				if (p.getVertexAttributes(Attribute.LABELS)!=null){
//					String[] labels=p.getVertexAttributes(Attribute.LABELS).toStringArray(null);
//					writeLabelsAtPoints(coords, labels, hist);
//				}
		}
		public void visit(IndexedLineSet g) {
			super.visit(g);
			if ( !dls.getTubeDraw() || !dgs.getShowLines() || g.getNumEdges()==0) return;
			String histOld= hist;
			GeoTyp typ=(dls.getTubeDraw()&&drawTubes)?(GeoTyp.TUBE):(GeoTyp.LINE);
			if(typ==GeoTyp.TUBE){
				double[][] coords=VRMLWriterHelper.getDoubleDoubleVertexAttr(g, Attribute.COORDINATES);
				double[][] colors=VRMLWriterHelper.getDoubleDoubleVertexAttr(g, Attribute.COLORS);
				int[][] indices=VRMLWriterHelper.getIntIntEdgeAttr(g, Attribute.INDICES);
				if(indices==null)return;
				double radius=dls.getTubeRadius();
				double [] forcedDiffuseColor=null;
				String hist2=histOld+spacing;
				String hist3=hist2+spacing;
				hist=hist3;
				for(int[] line :indices){
					for(int i=1;i<line.length;i++){
						double[] v=coords[line[i-1]];
						double[] w=coords[line[i]];
						out.println(""+histOld+"Transform { # tubes of LineSet ");
						Transformation cylTrafo= new Transformation(VRMLWriterHelper.calcCylinderMatrix(v, w, radius));
						writeTrafo(cylTrafo);
						out.println(hist2+"children [ ");
						if(colors!=null)
							forcedDiffuseColor=colors[i];
						boolean hasShapeNode=tryWriteShapeNode(typ,forcedDiffuseColor);
						out.print(hist+"Cylinder { ");
						out.print("bottom    FALSE ");
						out.print("top    FALSE ");
//						out.print("radius  1 ");// ist default
//						out.print("height  2 ");// ist default
						out.println(" } ");
						if(hasShapeNode) closeShapeNode(hist3);
						out.println(hist2+"]");// children
						out.println(histOld+"}");// transform
					}
				}
				return;
			}
			boolean hasShapeNode=tryWriteShapeNode(typ,null);
			//-------------------------------
			//	check if allready defined
			if(useDefs){
				if (wHelp.isDefinedLineSet(g)){
					out.println(""+hist+"USE "+ VRMLWriterHelper.str( g.hashCode()+"LINE")+" ");
					if(hasShapeNode) closeShapeNode(histOld);
					return;
				}
			}
			if (useDefs && wHelp.isMultipleUsedLineSet(g)){
				out.print(""+hist+"DEF "+VRMLWriterHelper.str(g.hashCode()+"LINE")+" ");
				wHelp.setDefinedLineSet(g);
				}
			else out.print(""+hist);
			// write object:
			/*IndexedLineSet {
 //				eventIn       MFInt32 set_colorIndex
 //				eventIn       MFInt32 set_coordIndex
  				exposedField  SFNode  color             NULL
  				exposedField  SFNode  coord             NULL
// 				field         MFInt32 colorIndex        []
 //				field         SFBool  colorPerVertex    TRUE
 //				field         MFInt32 coordIndex        []
			}*/
			String hist2=hist;
			hist=hist+spacing;
			out.println("IndexedLineSet { # "+ g.getName());
			double[][] coords=VRMLWriterHelper.getDoubleDoubleVertexAttr(g, Attribute.COORDINATES);
			if(coords!=null){
				out.println(hist+"coord ");
				writeCoords(coords, hist+spacing);
			}
			int[][] indis=VRMLWriterHelper.getIntIntEdgeAttr(g, Attribute.INDICES);
			if(indis!=null)	writeIndices(indis, hist+spacing);
			double[][] colors=VRMLWriterHelper.getDoubleDoubleEdgeAttr(g, Attribute.COLORS);
			if(colors!=null)
				if(colors.length>0){
					out.println(hist+"color ");
					writeColors(colors,hist+spacing);
				}
			hist=hist2;
			out.println(hist+"}");
			///old end -------------------
			if(hasShapeNode) closeShapeNode(histOld);
			/// --------------- Labels -------------------
//			if(dvts.getShowLabels())
//				if (p.getVertexAttributes(Attribute.LABELS)!=null){
//					String[] labels=p.getVertexAttributes(Attribute.LABELS).toStringArray(null);
//					writeLabelsAtPoints(coords, labels, hist);
//				}
		}
		public void visit(IndexedFaceSet g) {//done
			super.visit(g);
			if ( !dgs.getShowFaces()|| g.getNumFaces()==0)	return;
			String histOld= hist;
			boolean hasShapeNode=tryWriteShapeNode(GeoTyp.FACE,null);
			//			check if allready defined
			if(useDefs){
				if (wHelp.isDefinedFaceSet(g)){
					out.println(""+hist+"USE "+ VRMLWriterHelper.str( g.hashCode()+"POLYGON")+" ");
					if(hasShapeNode) closeShapeNode(histOld);
					return;
				}
			}
			if (useDefs && wHelp.isMultipleUsedFaceSet(g)){
				out.print(""+hist+"DEF "+VRMLWriterHelper.str(g.hashCode()+"POLYGON")+" ");
				wHelp.setDefinedFaceSet(g);
				}
			else out.print(""+hist);
			String hist2=hist;
			hist=hist+spacing;
			/*IndexedFaceSet {
// 				eventIn       MFInt32 set_colorIndex
// 				eventIn       MFInt32 set_coordIndex
// 				eventIn       MFInt32 set_normalIndex
// 				eventIn       MFInt32 set_texCoordIndex
  				exposedField  SFNode  color             NULL
  				exposedField  SFNode  coord             NULL
  				exposedField  SFNode  normal            NULL
  				exposedField  SFNode  texCoord          NULL
//  			field         SFBool  ccw               TRUE
//  			field         MFInt32 colorIndex        []
  				field         SFBool  colorPerVertex    TRUE
 				field         SFBool  convex            TRUE
  				field         MFInt32 coordIndex        []
// 				field         SFFloat creaseAngle       0
// 				field         MFInt32 normalIndex       []
  				field         SFBool  normalPerVertex   TRUE
  				field         SFBool  solid             TRUE
// 				field         MFInt32 texCoordIndex     []
			}*/
			out.println("IndexedFaceSet { # "+ g.getName());
			// write coordinates
			double[][] coords=VRMLWriterHelper.getDoubleDoubleVertexAttr(g, Attribute.COORDINATES);
			if(coords!=null){
				out.println(hist+"coord ");
				writeCoords(coords, hist+spacing);
			}
			// write Texture coordinates
			double[][] textCoords=VRMLWriterHelper.getDoubleDoubleVertexAttr(g, Attribute.TEXTURE_COORDINATES);
			if(textCoords!=null){
				out.println(hist+"texCoord ");
				writeTexCoords(textCoords, hist+spacing);
			}
			// writes Indices			
			int[][] indis=VRMLWriterHelper.getIntIntFaceAttr(g, Attribute.INDICES);
			if(indis!=null) writeIndices(indis, hist);
			// writes Colors
			double[][] vertColors=VRMLWriterHelper.getDoubleDoubleVertexAttr(g, Attribute.COLORS);
			double[][] faceColors=VRMLWriterHelper.getDoubleDoubleFaceAttr(g, Attribute.COLORS);
			if(dps.getSmoothShading()&&vertColors!=null){
				out.println(hist+"color ");
				writeColors(vertColors,hist+spacing);
				out.println(hist+"colorPerVertex TRUE");
			}
			else if(faceColors!=null){
				out.println(hist+"color ");
				writeColors(faceColors,hist+spacing);
				out.println(hist+"colorPerVertex FALSE");
			}
			// writes normals
			double[][] vertNormals=VRMLWriterHelper.getDoubleDoubleVertexAttr(g, Attribute.NORMALS);
			double[][] faceNormals=VRMLWriterHelper.getDoubleDoubleFaceAttr(g,Attribute.NORMALS);
			if(dps.getSmoothShading()&&vertNormals!=null){
				out.println(hist+"normal ");
				writeNormals(vertNormals, hist+spacing);
				out.println(hist+"normalPerVertex TRUE");
			}
			else if(faceNormals!=null){
				out.println(hist+"normal ");
				writeNormals(faceNormals, hist+spacing);
				out.println(hist+"normalPerVertex FALSE");
			}
			out.println(hist+"convex FALSE solid FALSE ");
			hist=hist2;
			out.println(hist+"}");
			///old end -------------------
			if(hasShapeNode) closeShapeNode(histOld);
			/// --------------- Labels? -------------------
//			if(dvts.getShowLabels())
//				if (p.getVertexAttributes(Attribute.LABELS)!=null){
//					String[] labels=p.getVertexAttributes(Attribute.LABELS).toStringArray(null);
//					writeLabelsAtPoints(coords, labels, hist);
//				}
		}
		// ---- Lights ----
		public void visit(Light l) {
			super.visit(l);
		}
		public void visit(DirectionalLight l) {
			if(moveLightsToSceneRoot)return;
			writeDirLight(l,hist,out);
			super.visit(l);
		}
		public void visit(PointLight l) {
			if(moveLightsToSceneRoot)return;
			if(!(l instanceof SpotLight)){
				writePointLight(l, hist, out, null);
			}
			super.visit(l);
		}
		public void visit(SpotLight l) {
			if(moveLightsToSceneRoot)return;
			writeSpotLight(l, hist, out, null,null);
			super.visit(l);
		}
		// ----------- cam ------------
		public void visit(Camera c) {
			/*Viewpoint {
//			  eventIn      SFBool     set_bind
			  exposedField SFFloat    fieldOfView    0.785398
//			  exposedField SFBool     jump           TRUE
			  exposedField SFRotation orientation    0 0 1  0
			  exposedField SFVec3f    position       0 0 10
//			  field        SFString   description    ""
//			  eventOut     SFTime     bindTime
//			  eventOut     SFBool     isBound
			}*/
			out.println(hist+"Viewpoint { ");
			String oldHist= hist;		hist=hist+spacing;
			out.println(hist+"fieldOfView "+c.getFieldOfView()*Math.PI/180);

			// ---------------------
			
			double[] m=c.getOrientationMatrix();
			FactoredMatrix fm= new FactoredMatrix(m);
			fm.update();
			double[] rotAx=fm.getRotationAxis();
			double ang=fm.getRotationAngle();
			double[] pos=fm.getTranslation();
			out.println(hist + "position 0 0 0");
				// ---------------------
			hist=oldHist;
			out.println(hist+"}");
			super.visit(c);
		}
		// ---------- trafo ---------
		public void visit(Transformation t) {//fin
			// handled in SceneGraphComponent
			super.visit(t);
		}
	}
	private class MyLightVisitor extends SceneGraphVisitor{
		SceneGraphPath p= new SceneGraphPath();
		public MyLightVisitor() {}
		public void visit(SceneGraphComponent c) {// fin
			if(!c.isVisible())return;
			p.push(c);
			c.childrenAccept(this);
			super.visit(c);
			p.pop();
		}
		public void visit(DirectionalLight l) {
			writeDirLight(l,hist,out);
			super.visit(l);
		}
		public void visit(PointLight l) {
			if(!(l instanceof SpotLight)){
				FactoredMatrix fm= new FactoredMatrix(p.getMatrix(null));
				fm.update();
				double[] c=fm.getTranslation();
				if(c!=null)
					c=new double[]{c[0],c[1],c[2]};
				writePointLight(l, hist, out, c);
			}
			super.visit(l);
		}
		public void visit(SpotLight l) {
			if(moveLightsToSceneRoot){
				FactoredMatrix fm= new FactoredMatrix(p.getMatrix(null));
				fm.update();
				double[] c=fm.getTranslation();
				double[] dir=fm.getRotation().multiplyVector(new double[]{0,0,1,0});
				writeSpotLight(l, hist, out, c,dir);
			}
			super.visit(l);
		}
	}
	//--------------------getter&setter-------------
	public boolean isDrawSpheres() {
		return drawSpheres;
	}
	public boolean isDrawTubes() {
		return drawTubes;
	}
	public boolean isMoveLightsToSceneRoot() {
		return moveLightsToSceneRoot;
	}
	public boolean isUseDefs() {
		return useDefs;
	}
	public void setDrawSpheres(boolean drawSpheres) {
		this.drawSpheres = drawSpheres;
	}
	public void setDrawTubes(boolean drawTubes) {
		this.drawTubes = drawTubes;
	}
	public void setUseDefs(boolean useDefs) {
		this.useDefs = useDefs;
	}
	public void setMoveLightsToSceneRoot(boolean moveLightsToSceneRoot) {
		this.moveLightsToSceneRoot = moveLightsToSceneRoot;
	}
}
