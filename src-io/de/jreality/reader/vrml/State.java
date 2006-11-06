package de.jreality.reader.vrml;

import java.awt.Color;

//import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.Appearance;
//import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Transformation;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DoubleArrayArray;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.ImageData;
import de.jreality.shader.Texture2D;
import de.jreality.shader.TextureUtility;
//import de.jreality.ui.viewerapp.ViewerApp;
import de.jreality.util.Input;

public class State {
	// konstanten
	public static final String[] BINDING= new String[]{	
		"DEFAULT","OVERALL",
		"PER_PART","PER_PART_INDEXED",
		"PER_FACE","PER_FACE_INDEXED",
		"PER_VERTEX","PER_VERTEX_INDEXED"};
	public static final String[] VERTORDER= new String[]{
		"UNKNOWN_ORDERING","CLOCKWISE","COUNTERCLOCKWISE"};
	public static final String[] SHAPETYPE= new String[]{
		"UNKNOWN_SHAPE_TYPE","SOLID"};
	public static final String[] FACETYPE= new String[]{
		"UNKNOWN_FACE_TYPE","CONVEX"};
	// data
	String history="";			// Info History String
	public Color[] ambient=new Color[]{};	// color 
	public Color[] diffuse= new Color[]{new Color(0,0,1)};
	public Color[] specular= new Color[]{};
	public Color[] emissive= new Color[]{}; 
	public double[] shininess= new double[]{};
	public double[] transparency= new double[]{};
	public int materialBinding=1;// Bindings
	public int normalBinding=0;
	public SceneGraphPath camPath=new SceneGraphPath();// Graph
	public SceneGraphComponent currNode=null;
	public double [][] coords= new double[0][3];	// 3d Daten
	public double [][] normals= new double[0][3];
	public Transformation trafo=null;
	public Transformation extraGeoTrans=null;// fuer die Abmessungen der Geometrie
	String textureFile="";				// TexturDaten
	int[][][] textureData= new int[][][]{{{}}};
	boolean wrapS=true; 
	boolean wrapT=true; 
	Matrix textureTrafo = MatrixBuilder.euclidean().getMatrix(); 
	double [][] textureCoords=new double[][]{{0,0}};
	public int vertexDraw=0;// visibility // 0= inherit, 1= true, 2= false; 
	public int edgeDraw=0;
	public int faceDraw=0; 
	public int vertOrder=0;	// ShapeHints
	public int shapeType=0;
	public int faceType=0;
	double creaseAngle = 0.5f;
	
//	-------------- construct --------------
	public State(){	}
	/**
	 * this is a deep-copyConstructor
	 * @param original
	 */
	public State(State orig){
		history= orig.history;
		int n;// color
		n=orig.ambient.length;	ambient= new Color[n];
		for (int i=0;i<n;i++)ambient[i]=new Color(orig.ambient[i].getRGB());
		n=orig.diffuse.length;	diffuse= new Color[n];
		for (int i=0;i<n;i++)diffuse[i]=new Color(orig.diffuse[i].getRGB());
		n=orig.emissive.length;	emissive= new Color[n];
		for (int i=0;i<n;i++)emissive[i]=new Color(orig.emissive[i].getRGB());
		n=orig.specular.length;	specular= new Color[n];
		for (int i=0;i<n;i++)specular[i]=new Color(orig.specular[i].getRGB());
		n=orig.shininess.length;shininess= new double[n];
		for (int i=0;i<n;i++)	shininess[i]=orig.shininess[i];
		n=orig.transparency.length;	transparency= new double[n];
		for (int i=0;i<n;i++)	transparency[i]=orig.transparency[i];
		materialBinding=orig.materialBinding;// bindings
		normalBinding=orig.normalBinding;
		camPath=orig.camPath;		// camPath
		currNode=orig.currNode;		//curr Node
		n=orig.coords.length;	coords= new double[n][];// coords
		for (int i=0;i<n;i++)
			coords[i]= new double[]{orig.coords[i][0],orig.coords[i][1],orig.coords[i][2]};
		n=orig.normals.length;	normals= new double[n][]; // normals
		for (int i=0;i<n;i++)
			normals[i]= new double[]{orig.normals[i][0],orig.normals[i][1],orig.normals[i][2]};
		if (orig.trafo==null)	trafo=null;				// transfo
		else {
			double[] mOld =orig.trafo.getMatrix();
			double[] m= new double[16];
			for (int i=0;i<16;i++)	m[i]=mOld[i];
			trafo= new Transformation(m);}
		if (orig.extraGeoTrans==null)	extraGeoTrans=null; // extraGeoTrans
		else {
			double[] mOld =orig.extraGeoTrans.getMatrix();
			double[] m= new double[16];
			for (int i=0;i<16;i++)	m[i]=mOld[i];
			extraGeoTrans= new Transformation(m);}
		textureFile=orig.textureFile;		// texture -file
		textureData= new int [][][]{{{}}};  // texture -Data
		if (orig.textureData.length>0 
				&& orig.textureData[0].length>0
				&& orig.textureData[0][0].length>0){
			int a=orig.textureData.length;
			int b=orig.textureData[0].length;
			int c=orig.textureData[0][0].length;
			textureData= new int[a][b][c];
			for (int i=0;i<a;i++)
				for(int j=0;j<b;j++)
					for(int k=0;k<c;k++)
						textureData[i][j][k]=orig.textureData[i][j][k];
		}
		wrapS=orig.wrapS;				// texture -wrap
		wrapT=orig.wrapT;
		double[] temp= new double[16];	// texture -trafo
		for (int i=0;i<16;i++)
			temp[i]=orig.textureTrafo.getArray()[i];
		if (orig.textureCoords.length>0 	// texture -coords
				&& orig.textureCoords[0].length>0){
			int a=orig.textureCoords.length;
			int b=orig.textureCoords[0].length;
			textureCoords= new double[a][b];
			for (int i=0;i<a;i++)
				for(int j=0;j<b;j++)
					textureCoords[i][j]=orig.textureCoords[i][j];
		}
		vertexDraw=orig.vertexDraw;	// visibility  
		edgeDraw=orig.edgeDraw;
		faceDraw=orig.faceDraw; 
		vertOrder=orig.vertOrder;	// ShapeHints
		shapeType=orig.shapeType;
		faceType=orig.faceType;
		creaseAngle = orig.creaseAngle;
	}
		
//	 -------------- get ---------------------	
	// TODO braucht das wer?
	public int colorLength(){
		int n=Math.max(ambient.length,diffuse.length);
		int m=Math.max(specular.length,emissive.length);
		return Math.max(n,m);
	}

	public static int getBinding(String bind){
		int b=0;
		for(int i=0;i<8;i++){
			if (BINDING[i].equals(bind)) b=i;
		}
		return b;}
	
//   -------------- set ----------------------
	/**
	 * setzt Farben und Transparenz und visibility
	 * in eine Appearance  
	 * @param useEmissive 
	 *   Emissive ist nicht implementiert.
	 *   Fuer Linien und Punkte wird die Farbe von emmissive
	 *   als diffuseColor gesetzt.
	 *   So werden Flaechen und Linien trotzdem getrennt gefaerbt.
	 * @return o.g. Appearance 
	 */

	public void setColorApp(Appearance a,boolean useEmissive){
		//TODO: calculate shininess
		if (ambient.length>0)
			a.setAttribute(CommonAttributes.AMBIENT_COLOR,ambient[0]);
		if (specular.length>0)
			a.setAttribute(CommonAttributes.SPECULAR_COLOR,specular[0]);

		// TODO emissive Color vs diffuse Color
//		if( useEmissive){
//		if (emissive.length>0)
//			a.setAttribute(CommonAttributes.DIFFUSE_COLOR,emissive[0]);
//	}
//	else{
//		if (diffuse.length>0)
//			a.setAttribute(CommonAttributes.DIFFUSE_COLOR,diffuse[0]);
//	}
		
		// VRML diffuse & emissive definition, if emissive is not supported:
		// take diffuse as base Color, take emissive if all others colors are empty
		if (diffuse.length>0)
				a.setAttribute(CommonAttributes.DIFFUSE_COLOR,diffuse[0]);
		else
			if (emissive.length>0)
				a.setAttribute(CommonAttributes.DIFFUSE_COLOR,emissive[0]);
		// 
		if (transparency.length>0)
			if (transparency[0]!=1)
				a.setAttribute(CommonAttributes.TRANSPARENCY,transparency[0]);
		if (vertexDraw==1)
			a.setAttribute(CommonAttributes.VERTEX_DRAW,true);
		if (vertexDraw==2)
			a.setAttribute(CommonAttributes.VERTEX_DRAW,false);
		if (edgeDraw==1)
			a.setAttribute(CommonAttributes.EDGE_DRAW,true);
		if (edgeDraw==2)
			a.setAttribute(CommonAttributes.EDGE_DRAW,false);
		if (faceDraw==1)
			a.setAttribute(CommonAttributes.FACE_DRAW,true);
		if (faceDraw==2)
			a.setAttribute(CommonAttributes.FACE_DRAW,false);
	}
	
	/** sets the transformation resulting of trafo and 
	 * extraGeoTrans to the given SceneGraphComponent 
	 * @param sgc
	 */
	public void setTrafo(SceneGraphComponent sgc){
		if ((trafo!=null)|(extraGeoTrans!=null)){
			Transformation t= new Transformation();
			if (trafo!=null){
				t=trafo;
			}
			else{MatrixBuilder.euclidean().assignTo(t);}
			if(extraGeoTrans!=null)
				t.multiplyOnRight(extraGeoTrans.getMatrix());
			sgc.setTransformation(t);
		}
	}
	/** 
	 * sets the texture
	 * @param app
	 */ 
	public void assignTexture(Appearance app, IndexedFaceSet f){
		if (textureFile.equals(""))return ;
		double[][] texCoord = new double [f.getNumPoints()][];
		System.arraycopy(textureCoords,0,texCoord,0,f.getNumPoints());
		f.setVertexAttributes( Attribute.TEXTURE_COORDINATES,
				new DoubleArrayArray.Array( texCoord));
		app.setAttribute(CommonAttributes.DIFFUSE_COLOR,new Color(1f,1f,1f));
		app.setAttribute(CommonAttributes.TRANSPARENCY_ENABLED,false);
		ImageData id=null;
		try {id = ImageData.load(Input.getInput(textureFile));}
		catch (Exception e) {}
	    Texture2D tex = TextureUtility.createTexture(app, CommonAttributes.POLYGON_SHADER,id);
	    tex.setTextureMatrix(textureTrafo);
	    tex.setApplyMode(Texture2D.GL_MODULATE);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public static void main(String[] args){
		IndexedFaceSetFactory i=new IndexedFaceSetFactory();
		codeTest();
		
	}
	
	public static void codeTest(){
	State state= new State();
	Appearance app= new Appearance();
	SceneGraphComponent cmp =new SceneGraphComponent();
	IndexedFaceSetFactory fac=new IndexedFaceSetFactory();
	IndexedFaceSet f;
	
	state.textureFile="wall.png";
	MatrixBuilder.euclidean().assignTo(state.textureTrafo);
	state.textureCoords=new double[][]{{1,0},{0,1},{1,1}};
	
	
	fac.setVertexCount(3);	fac.setFaceCount(1);
	fac.setFaceIndices(new int[][]{{0,1,2,0}});
	fac.setVertexCoordinates(new double[][]{{1,0,0},{0,1,0},{1,1,0}});
	fac.update();
	f= fac.getIndexedFaceSet();
	
	state.assignTexture(app, f);
	cmp.setGeometry(f);
	cmp.setAppearance(app);
	//ViewerApp.display(cmp);
	fac.setGenerateVertexNormals(true);
	fac.update();
	}
}
