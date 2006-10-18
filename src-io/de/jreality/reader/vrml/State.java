package de.jreality.reader.vrml;

import java.awt.Color;
import java.text.Format;
import java.util.Scanner;
import java.util.Vector;

import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.geometry.IndexedLineSetFactory;
import de.jreality.geometry.PointSetFactory;
import de.jreality.geometry.Primitives;
import de.jreality.geometry.QuadMeshFactory;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.DirectionalLight;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.Light;
import de.jreality.scene.PointLight;
import de.jreality.scene.PointSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.SpotLight;
import de.jreality.scene.Transformation;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DataList;
import de.jreality.scene.data.DoubleArrayArray;
import de.jreality.scene.data.IntArrayArray;
import de.jreality.scene.data.StorageModel;
import de.jreality.scene.data.StringArray;
import de.jreality.scene.data.StringArrayArray;
import de.jreality.shader.CommonAttributes;

public class State {
	// Info History String
	String history="";
	// color 
	public Color[] ambient;
	public Color[] diffuse;
	public Color[] specular;
	public Color[] emissive; 
	public double[] shininess;
	public double[] transparency;
	// Bindings
	public static final String[] BINDING= new String[]{
		"DEFAULT","OVERALL",
		"PER_PART","PER_PART_INDEXED",
		"PER_FACE","PER_FACE_INDEXED",
		"PER_VERTEX","PER_VERTEX_INDEXED"};
	public static int getBinding(String bind){
		int b=0;
		for(int i=0;i<8;i++){
			if (BINDING[i].equals(bind)) b=i;
		}	return b;}
	public int materialBinding=1;
	public int normalBinding=0;
	// Graph
	public SceneGraphPath camPath;
	public SceneGraphComponent currNode;
	// 3d Daten
	public double [][] coords;
	public double [][] normals;
	// TexturDaten
	String textureFile="";
	int[][][] textureData= new int[][][]{{{}}};
	boolean wrapS=true; // wiederhole Textur 
	boolean wrapT=true; 
	Matrix textureTrafo = MatrixBuilder.euclidean().getMatrix(); 
	double [][] textureCoords=new double[][]{{0,0}};
	// Transformationsmatrix
	public Transformation trafo; // 3d
	public Transformation extraGeoTrans; 
		// eine Trafo die nur fuer die Abmessungen der 
		// aktuellen Geometrie zustaendig ist
	// visibility
	public int vertexDraw=0;// 0= inherit, 1= true, 2= false; 
	public int edgeDraw=0;
	public int faceDraw=0; 
	// ShapeHints
	public static final String[] VERTORDER= new String[]
	      {"UNKNOWN_ORDERING","CLOCKWISE","COUNTERCLOCKWISE"};
	public int vertOrder=0;
	public static final String[] SHAPETYPE= new String[]
	      {"UNKNOWN_SHAPE_TYPE","SOLID"};
	public int shapeType=0;
	public static final String[] FACETYPE= new String[]
	      {"UNKNOWN_FACE_TYPE","CONVEX"};
	public int faceType=0;
	double creaseAngle = 0.5f;
	
	
	// TODO braucht das wer?
	public int colorLength(){
		int n=Math.max(ambient.length,diffuse.length);
		int m=Math.max(specular.length,emissive.length);
		return Math.max(n,m);
	}

	public State(){
		ambient = new Color[]{};
		diffuse = new Color[]{new Color(0,0,1)};
		specular = new Color[]{};
		emissive = new Color[]{};
		shininess = new double[]{};
		transparency = new double[]{};	
		trafo= null; 
		extraGeoTrans= null;
		coords= new double[0][3];
		normals= new double[0][3];
	}
	/**
	 * this is a deep-copyConstructor
	 * @param original
	 */
	public State(State orig){
		int n;
		// ambient
		n=orig.ambient.length;
		ambient= new Color[n];
		for (int i=0;i<n;i++)
			ambient[i]=new Color(orig.ambient[i].getRGB());
		// diffuse
		n=orig.diffuse.length;
		diffuse= new Color[n];
		for (int i=0;i<n;i++)
			diffuse[i]=new Color(orig.diffuse[i].getRGB());
		// emissive
		n=orig.emissive.length;
		emissive= new Color[n];
		for (int i=0;i<n;i++)
			emissive[i]=new Color(orig.emissive[i].getRGB());
		// specular
		n=orig.specular.length;
		specular= new Color[n];
		for (int i=0;i<n;i++)
			specular[i]=new Color(orig.specular[i].getRGB());
		// shininess
		n=orig.shininess.length;
		shininess= new double[n];
		for (int i=0;i<n;i++)
			shininess[i]=orig.shininess[i];
		// transparency
		n=orig.transparency.length;
		transparency= new double[n];
		for (int i=0;i<n;i++)
			transparency[i]=orig.transparency[i];
		// Trafo
		if (orig.trafo==null)
			trafo=null;
		else {
			double[] mOld =orig.trafo.getMatrix();
			double[] m= new double[16];
			for (int i=0;i<16;i++)	m[i]=mOld[i];
			trafo= new Transformation(m);}
		// coords
		n=orig.coords.length;
		coords= new double[n][];
		for (int i=0;i<n;i++)
			coords[i]= new double[]{orig.coords[i][0],orig.coords[i][1],orig.coords[i][2]};
		n=orig.normals.length;
		// normals
		normals= new double[n][];
		for (int i=0;i<n;i++)
			normals[i]= new double[]{orig.normals[i][0],orig.normals[i][1],orig.normals[i][2]};
		//curr Node
		currNode=orig.currNode;
		// camPath
		camPath=orig.camPath;
		// extraGeoTrans
		extraGeoTrans=orig.extraGeoTrans;
		// texture
		textureData= new int [][][]{{{}}};
		if (orig.textureData.length>0 && orig.textureData[0].length>0
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
		textureFile=orig.textureFile;
		wrapS=orig.wrapS;
		wrapT=orig.wrapT;
		double[] temp= new double[16];
		for (int i=0;i<16;i++)
			temp[i]=orig.textureTrafo.getArray()[i];
	
	}	
	
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

	public Appearance makeGeoApp(boolean useEmissive){
		Appearance a= new Appearance();
		//TODO: calculate shininess
		if (ambient.length>0)
			a.setAttribute(CommonAttributes.AMBIENT_COLOR,ambient[0]);
		if (specular.length>0)
			a.setAttribute(CommonAttributes.SPECULAR_COLOR,specular[0]);

		// TODO emissive Color; 
		if( useEmissive){
			if (emissive.length>0)
				a.setAttribute(CommonAttributes.DIFFUSE_COLOR,emissive[0]);
		}
		else{
			if (diffuse.length>0)
				a.setAttribute(CommonAttributes.DIFFUSE_COLOR,diffuse[0]);
		}
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
		return a;
	}
	public static void main(String[] args){
		
		int[] c=VRMLHelper.decodeColorFromString(4,"257");
		System.out.println(""+c[0]+" "+c[1]+" "+c[2]+" "+c[3]);
	}
	public static void codeTest(){
	State state= new State();
	
	IndexedFaceSetFactory ff=new IndexedFaceSetFactory();
	ff.setVertexTextureCoordinates(state.textureCoords);

	IndexedFaceSet self = new IndexedFaceSet();
	
	int l=self.getVertexAttributes(Attribute.COORDINATES).size();
	double [][] a= new double[l][3];
	double [][] b= new double[l][2];
	self.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(a);
	for(int i=0;i<l;i++){ b[i]=new double[]{a[i][0],a[i][1]};};
	
	self.setVertexAttributes(Attribute.TEXTURE_COORDINATES,StorageModel.DOUBLE_ARRAY.array(b[0].length).createReadOnly(b));	
	}
}
