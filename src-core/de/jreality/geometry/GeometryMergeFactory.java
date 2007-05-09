package de.jreality.geometry;

import java.awt.Color;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.P3;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.Geometry;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.PointSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DataItem;
import de.jreality.scene.data.DataList;
import de.jreality.scene.data.DataListSet;
import de.jreality.scene.data.DoubleArrayArray;
import de.jreality.scene.data.IntArrayArray;
import de.jreality.scene.data.StorageModel;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.DefaultGeometryShader;
import de.jreality.shader.DefaultLineShader;
import de.jreality.shader.DefaultPointShader;
import de.jreality.shader.DefaultPolygonShader;
import de.jreality.shader.EffectiveAppearance;
import de.jreality.shader.ShaderUtility;
import de.jreality.ui.viewerapp.ViewerApp;

public class GeometryMergeFactory {
	private boolean respectFaces=true;//	[] more dificult 'cause of coordinates 
	private boolean respectEdges=true;//	[v]
	private boolean respectVertices=true;// [v]

	private boolean generateFaceNormals=true;// [v]
	private boolean generateVertexNormals=true;// [v]

	// attribute-values which are set if the named 
	// attribute is not supported in a geometry. 
	// will be ignored if no geometry supports them.
	private List<Attribute> defaultFaceAttributes=new LinkedList<Attribute>();//	[v]
	private List<List<double[]>>defaultFaceAttributeValues=new LinkedList<List<double[]>>();// [v]
	private List<Attribute> defaultEdgeAttributes=new LinkedList<Attribute>();//	[v]
	private List<List<double[]>>defaultEdgeAttributeValues=new LinkedList<List<double[]>>();// [v]
	private List<Attribute> defaultVertexAttributes=new LinkedList<Attribute>();//	[v]
	private List<List<double[]>>defaultVertexAttributeValues=new LinkedList<List<double[]>>();// [v]
	// defaults which have to be set even if no geometry supports them.
	// must be listed above.
	private List<Attribute> importantFaceDefaultAttributes=null;// []
	private List<Attribute> importantEdgeDefaultAttributes=null;// []
	private List<Attribute> importantVertexDefaultAttributes=null;// []
	// will be used intern if only this attributes have to be respected
	// wil be ignored if they are null
	private List<Attribute> onlyThisFaceAttributes=null;// []
	private List<Attribute> onlyThisEdgeAttributes=null;// []
	private List<Attribute> onlyThisVertexAttributes=null;// []

	private final int FACE_ATTR=2,EDGE_ATTR=1,VERT_ATTR=0;
	// TODO:

	private boolean SpheresAsFacesSets;//	[]
	private boolean tubesAsFaceSets;// 		[]
	// END TODO

	public GeometryMergeFactory() {

	}


	private static List<Attribute> collectAttributes(List<List<Attribute>> atLists, List<Attribute> defAtt){
//		unwichtige Defs merken(badDefaults)
		List<Attribute> badDefaults= new LinkedList<Attribute>();
		if(defAtt!=null)
			for(Attribute defa :defAtt){
				boolean hit=false;
				for(List<Attribute> list : atLists){
					for(Attribute at : list){
						if (defa.getName().equals(at.getName())){
							hit=true; break;
						}	
					}
					if(hit) break;
				}
				if(!hit) badDefaults.add(defa);
			}
		// nur voll definierte Attr merken(goodAttr)
		List<Attribute> goodAttr= new LinkedList<Attribute>();

		// ersatz :
//		for (int i = 0; i < atLists[0].length; i++)	goodAttr.add(atLists[0][i]);
		// fuer:
		List<Attribute> firstList=atLists.get(0);
		for(Attribute firstA : firstList){
			boolean hitAll=true;// everywhere defined attrib. ?
			if(atLists!=null)
				for(List<Attribute> list : atLists){	 
					boolean hit=false; // in this list defined attr. ?
					if(list!=null)
						for(Attribute at: list){	
							if(at.getName().equals(firstA.getName())){
								hit=true;
								break;
							}
						}
					else hitAll=false;
					if(!hit) {
						hitAll=false;
						break;
					}
				}
			else hitAll=false;
			//	 remember only everywhere defined attr.:
			if(hitAll) goodAttr.add((Attribute)firstA);
		}
		// gather:
		if (defAtt!= null)
			for(Attribute dAt : defAtt){
				boolean isIn=false;
				for (Attribute goodAt : goodAttr ) {
					if (goodAt.getName().equals(dAt.getName())){
						isIn=true;
						break;
					}
				}
				if (!isIn)	goodAttr.add(dAt);
			}

		// unwichtige defaults (kein attribut da) loeschen:
		//	<<=>-------- Achtung Farben !muessen! gesetzt werden da sie durch Appearances gesetzt sind! ----------<<

		// genuegt das?
		for (Attribute bad : badDefaults)
			goodAttr.remove(bad);

		// return:
		return  goodAttr;
	} 

	private void mergeDoubleArrayArrayAttributes(PointSet result,
			List<Attribute> defaultAttributes,List<List<double[]>>defaultAttributeValues, DataListSet[] dls ,int typ) {
		// attr liste erstellen :
		List<List<Attribute>> Atts= new LinkedList<List<Attribute>>();
		for (int i = 0; i < dls.length; i++) {
			Object[] o= dls[i].storedAttributes().toArray();
			LinkedList<Attribute> list=new LinkedList<Attribute>();
			Atts.add(list);
			if(o!=null)
				for ( Object oo : o)
					list.add((Attribute)oo);
		}
		List<Attribute> Attr= collectAttributes(Atts, defaultAttributes);
		for ( Attribute at : Attr){
			try {
				int k=-1;
				if( defaultAttributes!=null)
					for (int j = 0; j < defaultAttributes.size(); j++) {
						if (at.getName().equals(defaultAttributes.get(j).getName()))	k=j;
					}
				DataList dataList;
				// default supportet:
				if(k>=0)  dataList= new DoubleArrayArray.Array(mergeDoubleArrayArrayAttribute(dls, at, defaultAttributeValues.get(k)));
				// no default supportet:
				else     dataList= new DoubleArrayArray.Array(mergeDoubleArrayArrayAttribute(dls, at, null));
				if (dataList!=null){
					// create Datalist
					if(typ==VERT_ATTR){
						if(result.getVertexAttributes().getListLength()==0){
							result.setVertexCountAndAttributes(at,dataList);
						}
						else result.setVertexAttributes( at, dataList );
					}
					if(typ==EDGE_ATTR)
						((IndexedLineSet)result).setEdgeAttributes( at, dataList );
					if(typ==FACE_ATTR)
						((IndexedFaceSet)result).setFaceAttributes( at, dataList );
				}
			} catch (Exception e) {}				
		}
	}

	/**
	 * Merges the double attributes into a single trivial type array.
	 * If a single entry of the array fails to have the prescribed attribute(and no default is given) 
	 * null is returned
	 * @param Datalists
	 * @param the attribute which is to merge
	 * @param the list of defaults
	 * 			size==ifs.size : possible default is given for every single ifs    
	 * 			size==1 : all ifs have same defaultValue  
	 * 			null: no default is supported 
	 * @return array containing all data of given attribute  
	 */
	private static double [][] mergeDoubleArrayArrayAttribute( 
			DataListSet [] list , Attribute attr, List<double[]> defaults) {
		// total len of list (all elements)
		int totalLen=0; 
		for (int i = 0; i < list.length; i++)
			totalLen+=list[i].getListLength();	
		// result
		double [][] result = new double[totalLen][];

		int n=0;// current position 
		for( int i=0; i < list.length; i++ ) {
			double[][] values; // part of result
			// if we have to less Data  
			if (list[i].getList( attr )==null && (defaults==null||defaults.size()==0)){
				return null; 
			}
			// if default values are nescesarry
			DataList l=list[i].getList( attr );
			if (l==null){
				values= new double[list[i].getListLength()][];
				double[] d;
				if(defaults.size()>i){
					d=defaults.get(i);
				}
				else d= defaults.get(0);
				for (int j = 0; j < values.length; j++){
					values[j]=d;
				}
			}
			else{
				// if datas are given
				if (!(l instanceof DoubleArrayArray)){
					return null; 
				}
				values = l.toDoubleArrayArray(null);
			}
			System.arraycopy(values, 0, result, n, values.length ); 
			n += list[i].getListLength();
		}
		return result;  
	}
	/**
	 * Merges the data for a face attribute for an array of indexed face set into a single trivial type array.
	 * If a single entry of the array fails to have the prescribed attribute a NullPointerException is
	 * thrown.
	 * @param ifs array of indexed face sets.
	 * @param attr a face attribute, e.g., @link de.jreality.scene.data.Attribute.NORMALS
	 * @return array containing all data of face attribute of an array of indexed face set.
	 */

	private static int [][] mergeIntArrayArrayAttribute( DataListSet [] dls , Attribute attr) {
		//  total len of list (all elements)
		int totalLen=0;
		for (int i=0;i<dls.length;i++)
			totalLen+=dls[i].getListLength();
		// result
		int [][] result = new int[totalLen][];
		int n=0;// current position 
		for( int i=0; i < dls.length; i++ ) {
			if (dls[i].getList( attr )!=null){
				int[][] values = dls[i].getList( attr ).toIntArrayArray(null);
				System.arraycopy(values, 0, result, n, values.length );
				n += dls[i].getListLength();
			}
			else return null;
		}
		return result;
	}

//	-----------------new Merge--------------------
	private static DefaultGeometryShader dgs;
	private static DefaultPolygonShader dps;
	private static DefaultLineShader dls;
	private static DefaultPointShader dvs;

	private void assignTransformation(IndexedFaceSet ifs, double[] matrix){
		double[] flipit = P3.makeStretchMatrix(null, new double[] {-1,0, -1,0, -1.0});
		if (ifs.getVertexAttributes(Attribute.COORDINATES) == null) return;
		double[][] v = ifs.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
		double[][] nv = Rn.matrixTimesVector(null, matrix, v);
		ifs.setVertexAttributes(Attribute.COORDINATES, StorageModel.DOUBLE_ARRAY.array(nv[0].length).createWritableDataList(nv));
		double[] mat = Rn.transpose(null, matrix);          	
		mat[12] = mat[13] = mat[14] = 0.0;
		Rn.inverse(mat, mat);
		if (respectFaces){
			if (ifs.getFaceAttributes(Attribute.NORMALS) != null)	{
				v = ifs.getFaceAttributes(Attribute.NORMALS).toDoubleArrayArray(null);
				nv = Rn.matrixTimesVector(null, mat, v);
				if (Rn.determinant(matrix) < 0.0)
					nv = Rn.matrixTimesVector(null, flipit, nv);
				ifs.setFaceAttributes(Attribute.NORMALS, StorageModel.DOUBLE_ARRAY.array(nv[0].length).createWritableDataList(nv));
			} 
			else{
				if(generateFaceNormals)
					GeometryUtility.calculateAndSetFaceNormals(ifs);
			}
		}
		if(respectVertices){
			if (ifs.getVertexAttributes(Attribute.NORMALS) != null)	{
				v = ifs.getVertexAttributes(Attribute.NORMALS).toDoubleArrayArray(null);
				nv = Rn.matrixTimesVector(null, mat, v);
				if (Rn.determinant(matrix) < 0.0)	
					nv = Rn.matrixTimesVector(null, flipit, nv);
				ifs.setVertexAttributes(Attribute.NORMALS, StorageModel.DOUBLE_ARRAY.array(nv[0].length).createWritableDataList(nv));
			} 
			else{
				if(generateVertexNormals)
					GeometryUtility.calculateAndSetVertexNormals(ifs);
			}
		}
	}

	private static void collectMergeData(
			List<IndexedFaceSet> g,
			List<double[]> tra,
			List<double[]> fCol,
			List<double[]> eCol,
			List<double[]> vCol,
			SceneGraphComponent cmp,// sgc to handle
			SceneGraphPath p,
			EffectiveAppearance parentEA){

		// manage EApp and SGPath
		Appearance app =cmp.getAppearance();
		EffectiveAppearance	eApp=parentEA;
		if (app!=null){
			eApp=parentEA.create(app);
		};
		p.push(cmp);
		updateShaders(eApp);
		// insert data
		Geometry myG=cmp.getGeometry();
		if(myG!= null && myG instanceof PointSet) {

			// geometry 
			IndexedFaceSet myface;
			PointSet myPs = (PointSet) myG;
			if (myPs instanceof IndexedLineSet) {
				IndexedLineSet myLSet = (IndexedLineSet) myPs;	
				if (myPs instanceof IndexedFaceSet) {
					myface = (IndexedFaceSet) myPs;
				}else myface=indexedLineSetToIndexedFaceSet(myLSet);
			}else myface=pointSetToIndexedFaceSet(myPs); 
			g.add(myface);
			// color
			Color Mycol;
			Mycol=dps.getDiffuseColor();
			fCol.add(new double[]{Mycol.getRed(),Mycol.getGreen(),Mycol.getBlue(),Mycol.getAlpha()});
			Mycol=dls.getDiffuseColor();
			eCol.add(new double[]{Mycol.getRed(),Mycol.getGreen(),Mycol.getBlue(),Mycol.getAlpha()});
			Mycol=dvs.getDiffuseColor();
			vCol.add(new double[]{Mycol.getRed(),Mycol.getGreen(),Mycol.getBlue(),Mycol.getAlpha()});

			// transformation
			double[] mat=p.getMatrix(new Matrix().getArray());
			tra.add(mat);
		}
		// append subnode Data
		List<SceneGraphComponent> childs =cmp.getChildNodes();
		for (Iterator iter = childs.iterator(); iter.hasNext();) {
			Object o=iter.next();
			if (o instanceof SceneGraphComponent) {
				SceneGraphComponent child = (SceneGraphComponent) o;
				collectMergeData(g,tra,fCol,eCol,vCol,child,p,eApp);
			}
		}
		// undo scenegraphpath extention
		p.pop();
	}
	private static void updateShaders(EffectiveAppearance eap) {
		dgs =ShaderUtility.createDefaultGeometryShader(eap);
		if (dgs.getPointShader() instanceof DefaultPointShader){	
			dvs = (DefaultPointShader) dgs.getPointShader();
		}
		else dvs = null;
		if (dgs.getLineShader() instanceof DefaultLineShader){ 
			dls = (DefaultLineShader) dgs.getLineShader();
		}
		else dls = null;
		if (dgs.getPolygonShader() instanceof DefaultPolygonShader){
			dps = (DefaultPolygonShader) dgs.getPolygonShader();
		}
		else dps = null;
	}

	public static PointSet indexedLineSetToPointSet(IndexedLineSet l){
		PointSet p= new PointSet(l.getNumPoints());
		p.setGeometryAttributes(l.getGeometryAttributes());
		p.setVertexAttributes(l.getVertexAttributes());
		return p;
	}
	public static PointSet IndexedFaceSetToPointSet(IndexedFaceSet f){
		PointSet p= new PointSet(f.getNumPoints());
		p.setGeometryAttributes(f.getGeometryAttributes());
		p.setVertexAttributes(f.getVertexAttributes());
		return p;
	}
	public static IndexedLineSet pointSetToIndexedLineSet(PointSet p){
		IndexedLineSet l= new IndexedLineSet(p.getNumPoints(),0);
		l.setGeometryAttributes(p.getGeometryAttributes());
		l.setVertexAttributes(p.getVertexAttributes());
		return l;
	}
	public static IndexedLineSet indexedFaceSetToIndexedLineSet(IndexedFaceSet f){
		IndexedLineSet l= new IndexedLineSet(f.getNumPoints(),f.getNumEdges());
		l.setGeometryAttributes(f.getGeometryAttributes());
		l.setVertexAttributes(f.getVertexAttributes());
		l.setVertexAttributes(f.getEdgeAttributes());
		return l;
	}
	public static IndexedFaceSet indexedLineSetToIndexedFaceSet(IndexedLineSet l){
		IndexedFaceSet f= new IndexedFaceSet(l.getNumPoints(),0);
		f.setGeometryAttributes(l.getGeometryAttributes());
		f.setVertexAttributes(l.getVertexAttributes());
		f.setEdgeAttributes(l.getEdgeAttributes());
		return f;
	}
	public static IndexedFaceSet pointSetToIndexedFaceSet(PointSet p){
		IndexedFaceSet f= new IndexedFaceSet(p.getNumPoints(),0);
		f.setGeometryAttributes(p.getGeometryAttributes());
		f.setVertexAttributes(p.getVertexAttributes());
		return f;
	}

	// ------------ Start Method ------------------
	public IndexedFaceSet mergeGeometrySets(SceneGraphComponent cmp){
		// init Eap
		Appearance app = cmp.getAppearance();
		if (app == null) app = new Appearance();
		EffectiveAppearance eApp= EffectiveAppearance.create();
		eApp= eApp.create(app);

		// collect Data
		List<IndexedFaceSet> geos= new LinkedList<IndexedFaceSet>();
		List<double[]> fCol=new LinkedList<double[]>();
		List<double[]> eCol=new LinkedList<double[]>();
		List<double[]> vCol=new LinkedList<double[]>();
		List<double[]> trafos= new LinkedList<double[]>();
		collectMergeData(geos, trafos,fCol,eCol,vCol, cmp,new SceneGraphPath(),eApp);

		///// geather and compute:
		// strukturieren
		int num=geos.size();
		IndexedFaceSet[] faces= new IndexedFaceSet[num];
		int i=0;
		for (Iterator iter = geos.iterator(); iter.hasNext();i++) {
			faces[i]= (IndexedFaceSet) iter.next();
		}
		//  anpassen (trafo)
		i=0;
		for (Iterator iter = trafos.iterator(); iter.hasNext();i++) {
			double[] m = (double[]) iter.next();
			assignTransformation(faces[i], m);
		}		
		//  mergen
		IndexedFaceSet f= new IndexedFaceSet();
		if(respectFaces)
			if(!defaultFaceAttributes.contains(Attribute.COLORS)){
				defaultFaceAttributes.add(Attribute.COLORS);
				defaultFaceAttributeValues.add(fCol);
			}
		if(!defaultVertexAttributes.contains(Attribute.COLORS)){
			defaultVertexAttributes.add(Attribute.COLORS);
			defaultVertexAttributeValues.add(vCol);
		}
		f=mergeIndexedFaceSets(faces);
		return f;
	}  
	public IndexedFaceSet mergeIndexedFaceSets( PointSet[] geo) {
		IndexedFaceSet[] ifs=new IndexedFaceSet[geo.length];
		// convert entrys to IndexedFaceSets
		for (int i = 0; i < geo.length; i++) {
			if(geo[i] instanceof IndexedFaceSet)
				ifs[i]=(IndexedFaceSet)geo[i];
			else if (geo[i] instanceof IndexedLineSet)
				ifs[i]=indexedLineSetToIndexedFaceSet((IndexedLineSet)geo[i]);
			else ifs[i]=pointSetToIndexedFaceSet(geo[i]);
		}
		//
		IndexedFaceSet result = new IndexedFaceSet();
		DataListSet[] faceDls= new DataListSet [ifs.length];
		DataListSet[] edgeDls= new DataListSet [ifs.length];
		DataListSet[] vertDls= new DataListSet [ifs.length];
		if (respectFaces){
			for (int j = 0; j < faceDls.length; j++)
				faceDls[j]=ifs[j].getFaceAttributes();
			final int [][] faceIndices = mergeIntArrayArrayAttribute( faceDls, Attribute.INDICES );
			for( int i=1, n=ifs[0].getNumPoints(), k=ifs[0].getNumFaces(); i<ifs.length; n += ifs[i].getNumPoints(), i++ ) {
				final int nof = ifs[i].getNumFaces();
				for( int f=0; f<nof; f++, k++ ) {
					final int [] face = faceIndices[k];
					for( int j=0; j<face.length; j++)	face[j] += n;
				}	
			}
			result.setFaceCountAndAttributes(
					Attribute.INDICES,
					new IntArrayArray.Array( faceIndices )
			);
		}
		if(respectEdges){
			for (int j = 0; j < edgeDls.length; j++) 
				edgeDls[j]=ifs[j].getEdgeAttributes();
			final int [][] edgeIndices = mergeIntArrayArrayAttribute(edgeDls, Attribute.INDICES );
			for( int i=1, n=ifs[0].getNumPoints(), k=ifs[0].getNumEdges(); i<ifs.length; n += ifs[i].getNumPoints(), i++ ) {
				final int nof = ifs[i].getNumEdges();
				for( int f=0; f<nof; f++, k++ ) {
					final int [] edge = edgeIndices[k];
					for( int j=0; j<edge.length; j++) edge[j] += n;
				}	
			}
			result.setEdgeCountAndAttributes(
					Attribute.INDICES,
					new IntArrayArray.Array( edgeIndices )
			);
		}
		for (int j = 0; j < vertDls.length; j++) 
			vertDls[j]=ifs[j].getVertexAttributes();
		
		mergeDoubleArrayArrayAttributes(result,defaultVertexAttributes,defaultVertexAttributeValues,vertDls,VERT_ATTR);
		if(respectEdges)mergeDoubleArrayArrayAttributes(result,defaultEdgeAttributes,defaultEdgeAttributeValues,edgeDls,EDGE_ATTR);
		if(respectFaces)mergeDoubleArrayArrayAttributes(result,defaultFaceAttributes,defaultFaceAttributeValues,faceDls,FACE_ATTR);
		return result;
	}

	public IndexedLineSet mergeIndexedLineSets( IndexedLineSet [] ils){
		boolean temp=respectFaces;
		respectFaces=false;
		IndexedFaceSet f=mergeIndexedFaceSets(ils);
		IndexedLineSet l=indexedFaceSetToIndexedLineSet(f);
		respectFaces=temp;
		return l;
	}

	public PointSet mergePointSets( PointSet [] ps){
		boolean temp=respectFaces;
		respectFaces=false;
		IndexedFaceSet f=mergeIndexedFaceSets(ps);
		PointSet p=IndexedFaceSetToPointSet(f);
		respectFaces=temp;
		return p;
	}

	public IndexedFaceSet mergeFaceSets(SceneGraphComponent cmp){
		return mergeGeometrySets(cmp);
	}
	public IndexedLineSet mergeLineSets(SceneGraphComponent cmp){
		boolean temp=respectFaces;
		respectFaces= false;
		IndexedLineSet l=indexedFaceSetToIndexedLineSet(mergeGeometrySets(cmp));
		respectFaces=temp;
		return l;
	}
	public PointSet mergePointSets(SceneGraphComponent cmp){
		boolean temp=respectFaces;	boolean temp2=respectEdges;
		respectFaces= false;		respectEdges= false;
		PointSet p=IndexedFaceSetToPointSet(mergeGeometrySets(cmp));
		respectFaces=temp;			respectEdges=temp2;
		return p;
	}

	// -------------- setter --------------
	/**Attributes which will be set to the given default, 
	* if not yet supported in the geometry.
	* must be <code>doubleArrayArray</code> Attributes !!
	* @param defaultFaceAttributes  list of default Attributes    
	* @param defaultAttributeValues 
	* 		must have the same length. 
	* 		Each entry can individualy have
	* 			- just a single entry (default for all is the same)
	* 		    - multiple entrys (one for each geometry)
	* 			- null or empty (works like no default is given)
	*/
	public void setDefaultFaceAttributes(
	List<Attribute> defaultAttributes
	,List<List <double[]>> defaultAttributeValues) {
	this.defaultFaceAttributeValues= defaultAttributeValues;
	this.defaultFaceAttributes= defaultAttributes;
	}
	/** see <code>setDefaultFaceAttributes</code> */
	public void setDefaultEdgeAttributes(
	List<Attribute> defaultAttributes
	,List<List <double[]>> defaultAttributeValues) {
	this.defaultFaceAttributeValues= defaultAttributeValues;
	this.defaultFaceAttributes= defaultAttributes;
	}
	/** see <code>setDefaultFaceAttributes</code> */
	public void setDefaultVertexAttributes(
	List<Attribute> defaultAttributes
	,List<List <double[]>> defaultAttributeValues) {
	this.defaultFaceAttributeValues= defaultAttributeValues;
	this.defaultFaceAttributes= defaultAttributes;
	}
	/**default Attributes wich are defined will normaly not be used if 
	* no Geometry supports them. 
	* Attributes which are also listed here will although be used.
	* 
	* @param importantDefaultAttributes
	*  
	*/
	public void setImportantFaceDefaultAttributes(
	List<Attribute> importantFaceDefaultAttributes) {
	this.importantFaceDefaultAttributes = importantFaceDefaultAttributes;
	}
	public void setImportantEdgeDefaultAttributes(
	List<Attribute> importantEdgeDefaultAttributes) {
	this.importantEdgeDefaultAttributes = importantEdgeDefaultAttributes;
	}
	public void setImportantVertexDefaultAttributes(
	List<Attribute> importantVertexDefaultAttributes) {
	this.importantVertexDefaultAttributes = importantVertexDefaultAttributes;
	}
	/** generates FaceNormals if not already given 
	* @param generateVertexNormals
	*/
	public void setGenerateFaceNormals(boolean generateFaceNormals) {
	this.generateFaceNormals = generateFaceNormals;
	}
	/** generates VertexNormals if not already given 
	* @param generateVertexNormals
	*/
	public void setGenerateVertexNormals(boolean generateVertexNormals) {
	this.generateVertexNormals = generateVertexNormals;
	}
	/**Edge Attributes will not be ignored  
	* @param respectEdges
	*/
	public void setRespectEdges(boolean respectEdges) {
	this.respectEdges = respectEdges;
	}
	/**Face Attributes will not be ignored  
	* @param respectEdges
	*/
	public void setRespectFaces(boolean respectFaces) {
	this.respectFaces = respectFaces;
	}
	/**Verrtex Attributes will not be ignored  
	* @param respectEdges
	*/
	public void setRespectVertices(boolean respectVertices) {
	this.respectVertices = respectVertices;
	}
	// -------- getter --------------
	public List<Attribute> getDefaultEdgeAttributes() {
	return defaultEdgeAttributes;
	}
	public List<List<double[]>> getDefaultEdgeAttributeValues() {
	return defaultEdgeAttributeValues;
	}
	public List<Attribute> getDefaultFaceAttributes() {
	return defaultFaceAttributes;
	}
	public List<List<double[]>> getDefaultFaceAttributeValues() {
	return defaultFaceAttributeValues;
	}
	public List<Attribute> getDefaultVertexAttributes() {
	return defaultVertexAttributes;
	}
	public List<List<double[]>> getDefaultVertexAttributeValues() {
	return defaultVertexAttributeValues;
	}
	public boolean isGenerateFaceNormals() {
	return generateFaceNormals;
	}
	public boolean isGenerateVertexNormals() {
	return generateVertexNormals;
	}
	public boolean isRespectEdges() {
	return respectEdges;
	}
	public boolean isRespectFaces() {
	return respectFaces;
	}
	public boolean isRespectVertices() {
	return respectVertices;
	}
	//----------------Test - Main ---------------------

	static ViewerApp vApp ;
	static SceneGraphComponent root= new SceneGraphComponent();

	public static void main(String[] args) {
		IndexedFaceSet ico= Primitives.sharedIcosahedron;
		IndexedFaceSet ico2= Primitives.sharedIcosahedron;
		IndexedFaceSetUtility.assignSmoothVertexNormals(ico2, 20);
		IndexedFaceSet box= Primitives.box(10, .5, .5, true);
		IndexedFaceSet box2= Primitives.box(10, .6, 0.4, true);
		IndexedFaceSet zyl= Primitives.cylinder(20,1,0,.5,5);

		SceneGraphComponent root= new SceneGraphComponent();
		Appearance app=new Appearance();
		app.setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, new Color(255,255,0));
		app.setAttribute(CommonAttributes.VERTEX_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, new Color(0,255,255));
		root.setAppearance(app);

		SceneGraphComponent A= new SceneGraphComponent();
		SceneGraphComponent B= new SceneGraphComponent();

		SceneGraphComponent C= new SceneGraphComponent();
		SceneGraphComponent A1= new SceneGraphComponent();
		MatrixBuilder.euclidean().translate(0,1,0).assignTo(A1);
		SceneGraphComponent A11= new SceneGraphComponent();
		MatrixBuilder.euclidean().rotate(Math.PI/2,0,0,1 ).assignTo(A11);
		SceneGraphComponent B1= new SceneGraphComponent();
		SceneGraphComponent B2= new SceneGraphComponent();
		Appearance app2=new Appearance();
		app2.setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, new Color(255,0,255));
		B2.setAppearance(app2);

		root.addChild(A); 	A.addChild(A1); A1.addChild(A11);
		root.addChild(B); 	B.addChild(B1);
		B.addChild(B2);
		root.addChild(C);

		A1.setGeometry(box);
		A11.setGeometry(box2);
		B1.setGeometry(zyl);
		B2.setGeometry(ico);
		//C.setGeometry(ico2);

		IndexedFaceSet[] list= new IndexedFaceSet[]{ico};
		//PointSet[] list= new PointSet[]{box,box2,zyl,ico2};
		//IndexedLineSet[] list= new IndexedLineSet[]{box,box2,zyl,ico2};
		//IndexedFaceSet i=mergeIndexedFaceSets(list,new Attribute[]{Attribute.COLORS},new double[][][]{{{0,1,1}}},null,null,null,null );

		GeometryMergeFactory t= new GeometryMergeFactory();
		//t.respectFaces=false;
		//t.generateFaceNormals=false;
		//t.generateVertexNormals=false;
		//t.respectEdges=false;
		
		IndexedFaceSet i=t.mergeGeometrySets(root);
		//PointSet i=t.mergeIndexedLineSets(list);
		//PointSet i=t.mergeIndexedFaceSets(list);
		//System.out.println("Report:"+i);
		vApp.display(i);
	}

}
