package de.jreality.reader.mathematica;
import java.util.Vector;
import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.data.Attribute;



/**
 * @author gonska
 * 
 * vorgehen: nehme der Reihe nach Indizees aus iOld
 *			->damit rufe den Punkt aus pOld auf	
 *			-> ist dieser in pNew	-> nimm dessen Index und schmeiss ihn in iNew 
 *			-> falls nicht 			-> fuege ihn in pNew ein, schmeiss curr in iNew, curr++
 * setze pNew & iNew in die factory ein
 */


public class FaceMelt {
	public static double eps=0.000000001;
	//	eps  : Tolleranz bei der noch Gleichheit der Punkte angesehen wird 	(double)
	
	private static boolean compare(double[] p1,double[] p2){
		// vergleicht Punkte bis auf eps Tolleranz
		boolean res=true;
		if((p1[0]>p2[0]+eps)|(p2[0]>p1[0]+eps)) res=false;
		if((p1[1]>p2[1]+eps)|(p2[1]>p1[1]+eps)) res=false;
		if((p1[2]>p2[2]+eps)|(p2[2]>p1[2]+eps)) res=false;
		return res;
	}
	private static int searchIndex(Vector pNew, double[] p,int alt){
		//	 returnes the index of the first match of p in pNew
		//	 returnes alt for no match of the first alt-1 points in pNew
		//   alt should be at most the size of pNew
		int index= alt;
		for (int i= 0; i<alt;i++){
			if ( compare((double[])pNew.elementAt(i),p)){
				index=i;
				break;
			}			
		}
		return index;
	} 
	/**
	 * reduces the points:
	 * makes corresponding Lists to the params
	 *  with no point twice set in the new coords-List 
	 * @param coordsOld
	 * @param indicesOld
	 * @return a Vector of two elements:
	 *	 double[][3]  : points
	 *	 int[][]      : indicesVector of  
	 */
	public static Vector meltCoords( double [][] coordsOld,int [][] indicesOld){
		// returns a Vector of two elements:
		// double[][3]  : points
		// int[][]      : indices
		
		int iOldSize=indicesOld.length;
		//	 die neuen Typen Daten erstellen
		int [][] indicesNew = new int[iOldSize][];
		Vector coordsNew = new Vector();
	
		// hilfsVariablen
		int curr=0; // : aktuell einzufuegender Index(= length(pNew) )
		int n=0;
		int index;
		double[] currPoint;
	
		for (int i=0;i<iOldSize;i++){	
			n=indicesOld[i].length;
			indicesNew[i]= new int[n];
			for (int j=0;j<n;j++){
				currPoint=coordsOld[indicesOld[i][j]];
				index= searchIndex(coordsNew, currPoint,curr);
				indicesNew[i][j]=index;
				if (index==curr){
					curr++;
					coordsNew.add(currPoint);
				} 
			}
		}
		
		//konvertieren
		double [][] points= new double[curr][];
		for (int i=0;i<curr;i++)
			points[i]=(double[])coordsNew.elementAt(i);

		Vector res=new Vector();
		res.add(points);
		res.add(indicesNew);
		return res;
	}
	/**
	 * reduces the points:
	 * makes a simple IndexedFaceSet which has no Point
	 *  twice set in the CoordinateList
	 *  this IFS has no Attributes set
	 * @param indexedFaceSet to smaler
	 * @return indexedFaceSet with less Points but no Attributes  
	 */
	public static IndexedFaceSet meltFace (IndexedFaceSet ifs){
		// die alten Daten auslesen
		int [][] indicesOld = ifs.getFaceAttributes( Attribute.INDICES ).toIntArrayArray(null);
		double [][] coordsOld = ifs.getFaceAttributes( Attribute.COORDINATES ).toDoubleArrayArray(null);
		int iOldSize=indicesOld.length;
		//	 die neuen Typen Daten erstellen
		int [][] indicesNew = new int[iOldSize][];
		Vector coordsNew = new Vector();
	
		// hilfsVariablen
		int curr=0; // : aktuell einzufuegender Index(= length(pNew) )
		int n=0;
		int index;
		double[] currPoint;
	
		for (int i=0;i<iOldSize;i++){	
			n=indicesOld[i].length;
			indicesNew[i]= new int[n];
			for (int j=0;j<n;j++){
				currPoint=coordsOld[indicesOld[i][j]];
				index= searchIndex(coordsNew, currPoint,curr);
				indicesNew[i][j]=index;
				if (index==curr){
					curr++;
					coordsNew.add(currPoint);
				} 
			}
		}
		
		//konvertieren
		double [][] Points= new double[curr][];
		for (int i=0;i<curr;i++)
			Points[i]=(double[])coordsNew.elementAt(i);
		// einfuegen der neuen Punkte & Indizees
		IndexedFaceSetFactory ifsf=new IndexedFaceSetFactory();
		
		//	 uebernehmen der Face Atribute:
		ifsf.setFaceAttributes(ifs.getFaceAttributes());	
		ifsf.setFaceCount(iOldSize);
		ifsf.setVertexCount(curr);
		ifsf.setFaceIndices(indicesNew);
		ifsf.setVertexCoordinates(Points);
		
		ifsf.setGenerateFaceNormals(true);
		ifsf.update();
		return ifsf.getIndexedFaceSet();
	}
}