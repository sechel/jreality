package de.jreality.reader.vrml;

import java.awt.Color;
import java.util.Vector;
import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.geometry.PointSetFactory;
import de.jreality.geometry.Primitives;
import de.jreality.geometry.QuadMeshFactory;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.DirectionalLight;
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
	
	// 
	
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
	 * this is a copyConstructor
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
		
	}
	public static void checkFlag(String [] code,boolean[] mask,String key){
		boolean hit=false;
		for (int i= 0;i<code.length;i++){
			if (key.equals(code[i])){
				mask[i]=true;
				hit=true;
				break;
			}
		}
		if (!hit)System.out.println("unknown AttributValue:"+key);
	}
	public static int getEnum(String[] code, String key){
		int n=0;
		for (int i=0;i<code.length;i++){
			if (key.equals(code[i]))
				n=i;
		}
		return n;
	}
	public static int[][] convertIndexList (int[] list){
		// -1 trennt die Teile
		int n=0;// # parts
		for (int i=0;i<list.length;i++)// zaehlt die '-1'-en
			if (list[i]==-1) n++;
		if (list.length==0)
			return new int[][]{};
		if (list[list.length-1]!=-1) n++;// -1 am ende ist optional
		int[][] indices = new int[n][];
		Vector v;
		int k=0;// current index 
		for(int i=0;i<n;i++){// fuer alle Listen
			v= new Vector();
			while ((k<list.length)&&(list[k]!=-1)){
				v.add(new Integer(list[k]));
				k++;
			}
			indices[i]= new int[v.size()];
			for (int j=0;j<v.size();j++)
				indices[i][j]=((Integer)v.get(j)).intValue();
			k++;// -1 ueberspringen
		}
		return indices;
	}
	
	
	public static IndexedFaceSet cylinder(boolean side,boolean top,boolean bottom,int n) {
		int rn = n+1;
		double[] verts = new double[2*3*rn];
		double angle = 0, delta = Math.PI*2/(n);
		for (int i = 0 ;i<rn; ++i)	{
			angle = i*delta;
			verts[3*(i+rn)]   = verts[3*i]   = Math.cos(angle);
			verts[3*(i+rn)+1] = verts[3*i+1] = Math.sin(angle);
			verts[3*i+2] = 0.5;
			verts[3*(i+rn)+2] = -0.5;
		}
		QuadMeshFactory qmf = new QuadMeshFactory();//Pn.EUCLIDEAN, n+1, 2, true, false);
		qmf.setULineCount(n+1);
		qmf.setVLineCount(2);
		qmf.setClosedInUDirection(true);
		qmf.setVertexCoordinates(verts);
		qmf.setGenerateEdgesFromFaces(true);
		qmf.setGenerateFaceNormals(true);
		qmf.setGenerateVertexNormals(true);
		qmf.update();
		IndexedFaceSet geo1= qmf.getIndexedFaceSet();
		double[][] verts2 = new double[n][3];
		double[][] verts3 = new double[n][3];
		for (int  i =0; i<n; ++i)	{
			angle = 2 * (i+.5) * Math.PI/n;
			verts2[i][0] = verts3[i][0] = Math.cos(angle);
			verts2[i][1] = verts3[i][1] = Math.sin(angle);
			verts2[i][2] = 0.5;
			verts3[i][2] = -0.5;
		}
		IndexedFaceSet geo2= IndexedFaceSetUtility.constructPolygon(verts2);
		IndexedFaceSet geo3= IndexedFaceSetUtility.constructPolygon(verts3);
		// GeometrieTeile auswaehlen
		Vector v=new Vector();
		if (side)  v.add(geo1);
		if (top)   v.add(geo2);
		if (bottom)v.add(geo3);
		IndexedFaceSet[] geos= new IndexedFaceSet[v.size()];
		for (int i=0;i<v.size();i++){
			geos[i]=(IndexedFaceSet)v.get(i);
			}
		if(geos.length==0)
			return null; 
		IndexedFaceSet resul=IndexedFaceSetUtility.mergeIndexedFaceSets(geos);
		return resul;
		}
	public static IndexedFaceSet cone(boolean sidesdraw, boolean bottomdraw,int n) {
		// Points
		double[] tip= new double[]{0,1,0}; 
		double[][] vertsBottom = new double[n][];
		double angle = 0;
		for (int i=0;i<n;i++){
			angle = 2 * (i+.5) * Math.PI/n;
			vertsBottom[i] = new double[]{
					Math.cos(angle),0,Math.sin(angle) };
		}
		IndexedFaceSet sides=null;
		IndexedFaceSet bottom=null;
		IndexedFaceSetFactory ifsf=null;
		if (bottomdraw){
			int[][] indicesB = new int[1][];
			indicesB[0] = new int[n];
			for (int i = 0; i<n; ++i)	indicesB[0][i] = i;
			ifsf = new IndexedFaceSetFactory();
			ifsf.setVertexCount(n);
			ifsf.setFaceCount(1);
			ifsf.setVertexCoordinates(vertsBottom);
			ifsf.setFaceIndices(indicesB);
			ifsf.setGenerateEdgesFromFaces(true);
			ifsf.setGenerateFaceNormals(true);
			ifsf.update();
			bottom= ifsf.getIndexedFaceSet();
		}
		if (sidesdraw){
			double[][] vertsSides = new double[n+1][];
			System.arraycopy(vertsBottom,0,vertsSides,0,n);
			vertsSides[n]=tip;
			int[][] indicesS = new int[n][];
			for (int i = 0; i<n; ++i)	{
				indicesS[i] = new int[3];
				indicesS[i][0] = i;
				indicesS[i][1] = (i+1)%n;
				indicesS[i][2] = n;
			}
			ifsf = new IndexedFaceSetFactory();
			ifsf.setVertexCount(n+1);
			ifsf.setFaceCount(n);
			ifsf.setVertexCoordinates(vertsSides);
			ifsf.setFaceIndices(indicesS);
			ifsf.setGenerateEdgesFromFaces(true);
			ifsf.setGenerateFaceNormals(true);
			ifsf.update();
			sides= ifsf.getIndexedFaceSet();
		}
		if (sidesdraw && bottomdraw)
			return IndexedFaceSetUtility.mergeIndexedFaceSets(
					new	IndexedFaceSet[]{bottom,sides});
		if (bottomdraw)
			return bottom;
		if (sidesdraw)
			return sides;
		return null;
	}
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
	public static String mergeStrings(String [] ss){
		String s=ss[0];
		for (int i=1;i<ss.length;i++){
			s=s+"   "+ss[i];
		}
		if(s.equals("")) return " ";
		return s;
	}  
	public static void setNormals(IndexedFaceSetFactory ifsf,
			int [][] cIndex,int[][] nIndex,State state){
	int faceCount=state.coords.length;
	int VertexCount= state.coords.length;
	double[][] fNormals=new double[faceCount][3];
	double[][] vNormals=new double[VertexCount][3];
	switch (state.normalBinding) {
	case 1:// overall
	{	for (int i=0;i<faceCount;i++){
			fNormals[i][0]=state.normals[0][0];
			fNormals[i][1]=state.normals[0][1];
			fNormals[i][2]=state.normals[0][2];
		}
		ifsf.setFaceNormals(fNormals);
	}
	break;
	case 2:// per part
	case 4:// per face
	{	System.arraycopy(state.normals,0,fNormals,0,faceCount);
		ifsf.setFaceNormals(fNormals);
	}
	break;
	case 3:// per part indexed
	case 5:// per face indexed
	{	for (int i=0;i<faceCount;i++){
			fNormals[i][0]=state.normals[(nIndex[0][i])][0];
			fNormals[i][1]=state.normals[(nIndex[0][i])][1];
			fNormals[i][2]=state.normals[(nIndex[0][i])][2];
		}
		ifsf.setFaceNormals(fNormals);
	}
	break;
	case 6:// per Vertex
	{
		int m=0;
		for (int i=0;i<faceCount;i++){
			int k=faceCount-i-1;
			int faceLength=cIndex[k].length;
			for (int j=0;j<faceLength;j++){
				int l=faceLength-1-j;
				double [] n=state.normals[m];
				vNormals[cIndex[k][l]][0]=n[0];
				vNormals[cIndex[k][l]][0]=n[0];
				vNormals[cIndex[k][l]][0]=n[0];
				m++;
			}
		}
		ifsf.setVertexNormals(vNormals);
	}
		break;
	case 0:// default
	case 7:// per Vertex indexed 
	{
		if (nIndex == null || nIndex.length != faceCount){
			ifsf.setGenerateVertexNormals(true);
			break;
		}
		for (int i=0;i<faceCount;i++){
			int k=faceCount-i-1;
			int faceLength=cIndex[k].length;
			for (int j=0;j<faceLength;j++){
				int l=faceLength-1-j;
				double [] n=state.normals[nIndex[k][l]];
				vNormals[cIndex[k][l]][0]=n[0];
				vNormals[cIndex[k][l]][0]=n[0];
				vNormals[cIndex[k][l]][0]=n[0];
			}
		}
		ifsf.setVertexNormals(vNormals);
	}
		break;
		default:
		break;
	}
}
	public static void setColors(IndexedFaceSetFactory ifsf,
			int [][] coordIndex,int[][] colorIndex,State state){
	int faceCount=state.coords.length;
	int VertexCount= state.coords.length;
	Color[] fColors=new Color[faceCount];
	Color[] vColors=new Color[VertexCount];
	switch (state.materialBinding) {
	case 0:// default
	case 1:// overall
	break;
	case 2:// per part
	case 4:// per face
	{	System.arraycopy(state.diffuse,0,fColors,0,faceCount);
		ifsf.setFaceColors(fColors);
	}
	break;
	case 3:// per part indexed
	case 5:// per face indexed
	{	for (int i=0;i<faceCount;i++){
			fColors[i]=state.diffuse[(colorIndex[0][i])];
			}
		ifsf.setFaceColors(fColors);
	}
	break;
	case 6:// per Vertex
	{
		int m=0;
		for (int i=0;i<faceCount;i++){
			int k=faceCount-i-1;
			int faceLength=coordIndex[k].length;
			for (int j=0;j<faceLength;j++){
				int l=faceLength-1-j;
				vColors[coordIndex[k][l]]=state.diffuse[m];
				m++;
			}
		}
		ifsf.setVertexColors(vColors);
	}
		break;
	case 7:// per Vertex indexed 
	{
		for (int i=0;i<faceCount;i++){
			int k=faceCount-i-1;
			int faceLength=coordIndex[k].length;
			for (int j=0;j<faceLength;j++){
				int l=faceLength-1-j;
				vColors[coordIndex[k][l]]=state.diffuse[colorIndex[k][l]];
			}
		}
		ifsf.setVertexColors(vColors);
	}
		break;
		default:
		break;
	}
}
	
	public static void codeTest(){
	
	
	}
}
