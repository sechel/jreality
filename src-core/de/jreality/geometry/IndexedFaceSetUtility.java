/*
 * Author	gunn
 * Created on Apr 25, 2005
 *
 */
package de.jreality.geometry;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Level;

import de.jreality.math.Matrix;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.Geometry;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.PointSet;
import de.jreality.scene.Scene;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphVisitor;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DataList;
import de.jreality.scene.data.DataListSet;
import de.jreality.scene.data.DoubleArray;
import de.jreality.scene.data.DoubleArrayArray;
import de.jreality.scene.data.IntArray;
import de.jreality.scene.data.IntArrayArray;
import de.jreality.scene.data.StorageModel;
import de.jreality.shader.EffectiveAppearance;
import de.jreality.util.LoggingSystem;
import de.jreality.util.Rectangle3D;

/**
 * @author gunn
 *
 */
public class IndexedFaceSetUtility {

	static private int[][] boxIndices = {
	{0,1,3,2},
	{1,0,4,5},
	{3,1,5,7},
	{5,4,6,7},
	{2,3,7,6},
	{0,2,6,4}};

	/**
	 * 
	 */
	private IndexedFaceSetUtility() {
		super();
	}

	public static double[][] extractEdge(double[][] curve, IndexedFaceSet ifs, int which)	{
		DataList verts = ifs.getVertexAttributes(Attribute.COORDINATES);
		int[] indices = ifs.getEdgeAttributes(Attribute.INDICES).item(which).toIntArray(null);
		int n = indices.length;
		int m = GeometryUtility.getVectorLength(verts);
		if (curve == null || curve.length != n || curve[0].length != m) curve = new double[n][m];
		for (int i = 0; i<n; ++i)	{
			DoubleArray da = verts.item(i).toDoubleArray();
			for (int j = 0; j < n; ++j)	{
				curve[i][j] = da.getValueAt(j);				
			}
		}
		return curve;
	}
		
	/**
	 * @param points
	 * @param closed
	 * @return
	 * @deprecated  Use the version in IndexedLineSetUtility.  I'm leaving this here now since some 
	 * oorange networks may depend on it and I don't want to upset that applecart.
	 */
	public static IndexedLineSet createCurveFromPoints(double[][] points, boolean closed)	{
		int n = points.length;
		int size = (closed) ? n+1 : n;
		IndexedLineSet g = new IndexedLineSet(size,1);
		// TODO replace this with different call if IndexedLineSet exists.
		int[][] ind = new int[1][size];
		for (int i = 0; i<n ; ++i)	{
			ind[0][i] = i;
		}
		if (closed) ind[0][n] = 0;
		g.setEdgeCountAndAttributes(Attribute.INDICES, new IntArrayArray.Array(ind));

		int vectorLength = points[0].length;
		g.setVertexAttributes(Attribute.COORDINATES, StorageModel.DOUBLE_ARRAY.array(vectorLength).createWritableDataList(points));
		return g;
	}


	public static IndexedFaceSet constructPolygon(double[][] points)	{
		return constructPolygon(null, points);
	}

	public static IndexedFaceSet constructPolygon(IndexedFaceSet ifs, double[][] points)	{
		int[][] ind = new int[1][points.length];
		for (int i = 0; i<points.length; ++i)	ind[0][i] = i;
		if (ifs == null) ifs = new IndexedFaceSet();
		// TODO replace this code when it's fixed.
		IndexedFaceSetFactory ifsf = new IndexedFaceSetFactory();// Pn.EUCLIDEAN, true, false, true);
//		IndexedFaceSetFactory ifsf = new IndexedFaceSetFactory(ifs, Pn.EUCLIDEAN, true, false, true);
		ifsf.setGenerateEdgesFromFaces(true);
		ifsf.setGenerateFaceNormals(true);
		ifsf.setVertexCount(points.length);
		ifsf.setFaceCount(1);
		ifsf.setVertexCoordinates(points);
		ifsf.setFaceIndices(ind);
		ifsf.update();
		return ifsf.getIndexedFaceSet();
	}

	public static IndexedFaceSet representAsSceneGraph(final IndexedFaceSet exists, Rectangle3D box)	{
		if (exists == null) return representAsSceneGraph(box);
		final double[][] verts = new double[8][3];
		double[][] bnds = box.getBounds();
		for (int i = 0; i<2; ++i)	
			for (int j = 0; j<2; ++j)	
				for (int k = 0; k<2; ++k)	{
					verts[4*i + 2 * j + k][0] = bnds[i][0];
					verts[4*i + 2 * j + k][1] = bnds[j][1];
					verts[4*i + 2 * j + k][2] = bnds[k][2];
				}
		Scene.executeWriter(exists, new Runnable() {

			public void run() {
				exists.setVertexAttributes(Attribute.COORDINATES,StorageModel.DOUBLE_ARRAY.array(3).createReadOnly(verts));	
			}
			
		});
		return exists;
	}
	public static IndexedFaceSet representAsSceneGraph(Rectangle3D box)	{
		double[][] verts = new double[8][3];
		double[][] bnds = box.getBounds();
		for (int i = 0; i<2; ++i)	
			for (int j = 0; j<2; ++j)	
				for (int k = 0; k<2; ++k)	{
					verts[4*i + 2 * j + k][0] = bnds[i][0];
					verts[4*i + 2 * j + k][1] = bnds[j][1];
					verts[4*i + 2 * j + k][2] = bnds[k][2];
				}
		IndexedFaceSetFactory ifsf = new IndexedFaceSetFactory();
		ifsf.setVertexCount(8);
		ifsf.setFaceCount(6);
		ifsf.setVertexCoordinates(verts);
		ifsf.setFaceIndices(boxIndices);
		ifsf.setGenerateEdgesFromFaces(true);
		ifsf.update();
		return ifsf.getIndexedFaceSet();
	}

	/**
	 * @deprecated	Use IndexedFaceSetFactory
	 * @return
	 */
	public static IndexedFaceSet createIndexedFaceSetFrom(int[][] indices, 
	double[][] verts, 
	double[][] vnormals, 
	double[][] vcolors, 
	double[][] fnormals, 
	double[][] fcolors)  
	{
		return createIndexedFaceSetFrom(indices, verts, vnormals, vcolors, null, fnormals, fcolors);
	}

	/**
	 * @deprecated	Use IndexedFaceSetFactory
	 * @return
	 */
	public static IndexedFaceSet createIndexedFaceSetFrom(int[][] indices, 
		double[][] verts, 
		double[][] vnormals, 
		double[][] vcolors, 
		double[][] vtexcoords,
		double[][] fnormals, 
		double[][] fcolors)  
	{
		if (verts == null || indices == null) return null;		// TODO and signal error!
		IndexedFaceSet ifs = new IndexedFaceSet(verts.length, indices.length);
		setIndexedFaceSetFrom(ifs, indices, verts, vnormals, vcolors, vtexcoords, fnormals, fcolors);
		return ifs;
	}

	/**
	 * @deprecated	Use IndexedFaceSetFactory
	 * @return
	 */
	public static IndexedFaceSet setIndexedFaceSetFrom(IndexedFaceSet ifs, int[][] indices, 
		double[][] verts, 
		double[][] vnormals, 
		double[][] vcolors, 
		double[][] vtexcoords,
		double[][] fnormals, 
		double[][] fcolors)  
	{
		if (indices != null)	{
			ifs.setFaceCountAndAttributes(Attribute.INDICES, new IntArrayArray.Array(indices));
		}
		if (verts != null)	{
			int vectorLength = verts[0].length;
			ifs.setVertexCountAndAttributes(Attribute.COORDINATES, StorageModel.DOUBLE_ARRAY.array(vectorLength).createReadOnly(verts));
		}
		if (vnormals != null)	{
			int vectorLength = vnormals[0].length;
			ifs.setVertexAttributes(Attribute.NORMALS, StorageModel.DOUBLE_ARRAY.array(vectorLength).createReadOnly(vnormals));
		}
		if (vcolors != null)	{
			int vectorLength = vcolors[0].length;
			ifs.setVertexAttributes(Attribute.COLORS, StorageModel.DOUBLE_ARRAY.array(vectorLength).createReadOnly(vcolors));
		}
		if (vtexcoords != null)	{
			int vectorLength = vtexcoords[0].length;
			ifs.setVertexAttributes(Attribute.TEXTURE_COORDINATES, StorageModel.DOUBLE_ARRAY.array(vectorLength).createReadOnly(vtexcoords));
		}
		if (fnormals != null)	{
			int vectorLength = fnormals[0].length;
			ifs.setFaceAttributes(Attribute.NORMALS, StorageModel.DOUBLE_ARRAY.array(vectorLength).createReadOnly(fnormals));
		}
		if (fcolors != null)	{
			int vectorLength = fcolors[0].length;
			ifs.setFaceAttributes(Attribute.COLORS, StorageModel.DOUBLE_ARRAY.array(vectorLength).createReadOnly(fcolors));
		}
		return ifs;
	}

	/**
	 * @deprecated  Use the version in {@link IndexedLineSetUtility};
	 */
	public static IndexedLineSet setIndexedLineSetFrom(IndexedLineSet ifs, int[][] indices, 
		double[][] verts, 
		double[][] vcolors, 
		double[][] ecolors)  
	{
		if (indices != null)	{
			ifs.setEdgeCountAndAttributes(Attribute.INDICES, new IntArrayArray.Array(indices));
		}
		if (verts != null)	{
			int vectorLength = verts[0].length;
			ifs.setVertexCountAndAttributes(Attribute.COORDINATES, StorageModel.DOUBLE_ARRAY.array(vectorLength).createReadOnly(verts));
		}
		if (vcolors != null)	{
			int vectorLength = vcolors[0].length;
			ifs.setVertexAttributes(Attribute.COLORS, StorageModel.DOUBLE_ARRAY.array(vectorLength).createReadOnly(vcolors));
		}
		if (ecolors != null)	{
			int vectorLength = ecolors[0].length;
			ifs.setEdgeAttributes(Attribute.COLORS, StorageModel.DOUBLE_ARRAY.array(vectorLength).createReadOnly(ecolors));
		}
		return ifs;
	}

	/**
	 * TODO parametrize this, like implode.
	 * @param ifs
	 * @return
	 */public static IndexedFaceSet truncate(IndexedFaceSet ifs)	{
		int vertcount = 0;
		int[][] ind = ifs.getFaceAttributes(Attribute.INDICES).toIntArrayArray().toIntArrayArray(null);
		for (int i = 0; i<ind.length; ++i)	{
			vertcount += ind[i].length;
		}
		
		double[][] oldverts = ifs.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
		//double[][] oldverts = ifs.getVertices().getData();
		int vectorLength = oldverts[0].length; //ifs.getVertices().getVectorLength();
		if (vectorLength != 3 && vectorLength != 4)	{
			throw new IllegalArgumentException("Vector length must be 3 or 4");
		}
		double[][] oldverts3;
		if (vectorLength == 4)	{
			oldverts3 = Pn.dehomogenize(null, oldverts);
		} else oldverts3 = oldverts;
		vectorLength = 3;
		int[][] newind;
		double[][] newverts;
//		IndexedFaceSet truncated;
		newind = new int[ind.length][];
		newverts = new double[vertcount][vectorLength];
		for (int i = 0, count = 0; i<ind.length; ++i)	{
			int[] thisf = ind[i];
			newind[i] = new int[thisf.length];
			for (int j = 0; j<thisf.length; ++j)	{
				int k  = (j+1)%thisf.length;
				Rn.add(newverts[count+j], oldverts3[ind[i][j]], oldverts3[ind[i][k]]);
				Rn.times(newverts[count+j], .5, newverts[count+j]);
				newind[i][j] = count+j;
			} 
			count += thisf.length;
		}
		double[][] fn = null, fc = null;
		//imploded = new IndexedFaceSet(newind, new DataGrid(newverts, false), null, null, ifs.getFaceNormals(), ifs.getFaceColors());
		if (ifs.getFaceAttributes(Attribute.NORMALS) != null) fn = ifs.getFaceAttributes(Attribute.NORMALS).toDoubleArrayArray(null);
		if (ifs.getFaceAttributes(Attribute.COLORS) != null) fc = ifs.getFaceAttributes(Attribute.COLORS).toDoubleArrayArray(null);
		IndexedFaceSetFactory ifsf = new IndexedFaceSetFactory();
		ifsf.setVertexCount(vertcount);
		ifsf.setFaceCount(ind.length);
		ifsf.setVertexCoordinates(newverts);
		ifsf.setFaceIndices(newind);
		if (fn != null)   ifsf.setFaceNormals(fn);
		else ifsf.setGenerateFaceNormals(true);
		if (fc != null)   ifsf.setFaceColors(fc);
		ifsf.setGenerateEdgesFromFaces(true);
		ifsf.update();
		return ifsf.getIndexedFaceSet();

//		truncated = createIndexedFaceSetFrom(newind, newverts, null, null, fn, fc);
//		truncated.buildEdgesFromFaces();
//		return truncated;
	}

	public static IndexedFaceSet implode(IndexedFaceSet ifs, double factor)	{
		int vertcount = 0;
		int[][] ind = ifs.getFaceAttributes(Attribute.INDICES).toIntArrayArray(null);
		//int[][] ind = ifs.getIndices();
		for (int i = 0; i<ind.length; ++i)	{
			vertcount += ind[i].length;
		}
		
		double[][] oldverts = ifs.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
		//double[][] oldverts = ifs.getVertices().getData();
		int vectorLength = oldverts[0].length; //ifs.getVertices().getVectorLength();
		if (vectorLength != 3)	{
			double[][] oldverts2 = new double[oldverts.length][3];
			Pn.dehomogenize(oldverts2, oldverts);
			oldverts = oldverts2;
			vectorLength = 3;
			//return null;
		}
		
		int[][] newind;
		double[][] newverts;
		IndexedFaceSet imploded;
		double implode = -factor; //(factor > 0) ? (-(1.0-factor)) : ((1.0+factor));
		if (implode > 0.0)	{
			newind = new int[ind.length][];
			newverts = new double[vertcount][vectorLength];
			for (int i = 0, count = 0; i<ind.length; ++i)	{
				int[] thisf = ind[i];
				newind[i] = new int[thisf.length];
				double[] center = new double[3];
				for (int j = 0; j<thisf.length; ++j)	{
					Rn.add(center, oldverts[ind[i][j]], center);
					newind[i][j] = count+j;
				} 
				Rn.times(center, 1.0/thisf.length, center);
				double[] diff = new double[vectorLength];
				for (int j = 0; j<thisf.length; ++j)	{
					Rn.subtract(diff, oldverts[ind[i][j]], center);
					Rn.times(diff, implode, diff);
					Rn.add(newverts[count+j], center, diff);
				}
				count += thisf.length;
			}
			double[][] fn = null, fc = null;
			//imploded = new IndexedFaceSet(newind, new DataGrid(newverts, false), null, null, ifs.getFaceNormals(), ifs.getFaceColors());
			if (ifs.getFaceAttributes(Attribute.NORMALS) != null) fn = ifs.getFaceAttributes(Attribute.NORMALS).toDoubleArrayArray(null);
			else fn = GeometryUtility.calculateFaceNormals(ifs);
			if (ifs.getFaceAttributes(Attribute.COLORS) != null) fc = ifs.getFaceAttributes(Attribute.COLORS).toDoubleArrayArray(null);

			IndexedFaceSetFactory ifsf = new IndexedFaceSetFactory();
			ifsf.setVertexCount(vertcount);
			ifsf.setFaceCount(ind.length);
			ifsf.setVertexCoordinates(newverts);
			ifsf.setFaceIndices(newind);
			if (fn != null)   ifsf.setFaceNormals(fn);
			else ifsf.setGenerateFaceNormals(true);
			if (fc != null)   ifsf.setFaceColors(fc);
			ifsf.setGenerateEdgesFromFaces(true);
			ifsf.update();
			imploded = ifsf.getIndexedFaceSet();

//			imploded = createIndexedFaceSetFrom(newind,newverts, null, null, fn, fc);
//			imploded.buildEdgesFromFaces();
		} else {
			int oldcount = oldverts.length;
			newind = new int[vertcount][4];
			newverts = new double[vertcount + oldcount][vectorLength];
			for (int i = 0; i<oldcount; ++i)	Rn.copy(newverts[i], oldverts[i]);
			for (int i = 0, count = 0; i<ind.length; ++i)	{
				int[] thisf = ind[i];
				double[] center = new double[3];
				for (int j = 0; j<thisf.length; ++j)	{
					Rn.add(center, oldverts[ind[i][j]], center);
					newind[count+j][0] = ind[i][j];
					newind[count+j][1] = ind[i][(j+1)%thisf.length];
					newind[count+j][2] = oldcount+count+((j+1)%thisf.length);
					newind[count+j][3] = oldcount+count+j;
				} 
				Rn.times(center, 1.0/thisf.length, center);
				double[] diff = new double[vectorLength];
				for (int j = 0; j<thisf.length; ++j)	{
					Rn.subtract(diff, center, oldverts[ind[i][j]]);
					Rn.times(diff, -implode, diff);
					Rn.add(newverts[oldcount + count+j], oldverts[ind[i][j]], diff);
				}
				count += thisf.length;
			}
//			//imploded = new IndexedFaceSet(newind, new DataGrid(newverts, false), null, null, null, null);
//			imploded = createIndexedFaceSetFrom(newind,newverts, null, null, null, null);
//			GeometryUtility.calculateAndSetFaceNormals(imploded);
//			//imploded.setEdgeAttributes(Attribute.INDICES, ifs.getEdgeAttributes(Attribute.INDICES));
//			imploded.setEdgeCountAndAttributes(Attribute.INDICES, ifs.getEdgeAttributes(Attribute.INDICES));
			IndexedFaceSetFactory ifsf = new IndexedFaceSetFactory();
			ifsf.setVertexCount(vertcount+oldcount);
			ifsf.setFaceCount(vertcount);
			ifsf.setVertexCoordinates(newverts);
			ifsf.setFaceIndices(newind);
			ifsf.setGenerateFaceNormals(true);
			ifsf.setGenerateEdgesFromFaces(true);
			ifsf.update();
			imploded = ifsf.getIndexedFaceSet();

		}
		return imploded;
	}
	/*
	 * A special purpose code for the Buddy-Baer project which might be useful for other situations
	 * where texture coordinates wrap around
	 * 
	 */
	public static IndexedFaceSet removeTextureCoordinateJumps(IndexedFaceSet src, double jumpSize)	{
		int np = src.getNumPoints();
		int nf = src.getNumFaces();
		double[][] textureCoords = src.getVertexAttributes(Attribute.TEXTURE_COORDINATES).toDoubleArrayArray(null);
		int[][] indices = src.getFaceAttributes(Attribute.INDICES).toIntArrayArray(null);
		int newVerts = 0;
		double[][][] minmax = new double[nf][][];
		boolean[][] textureJumps = new boolean[nf][2];
		for (int i = 0; i<nf; ++i)		{
			minmax[i] = getMinMax(indices[i], textureCoords );
			if ( minmax[i][1][0] - minmax[i][0][0] > jumpSize) textureJumps[i][0] = true;
			if ( minmax[i][1][1] - minmax[i][0][1] > jumpSize) textureJumps[i][1] = true;
			if (textureJumps[i][0] || textureJumps[i][1]) newVerts += indices[i].length;
		}
		LoggingSystem.getLogger(GeometryUtility.class).log(Level.INFO, "Adding "+newVerts+" vertices");
		IndexedFaceSet dst = new IndexedFaceSet(newVerts+np, nf);
		double[][] oldVerts = src.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
		double[][] on =null, nn = null;
		int nl = 0;
		if (src.getVertexAttributes(Attribute.NORMALS) != null)		{
			on = src.getVertexAttributes(Attribute.NORMALS).toDoubleArrayArray(null);
			nl = on[0].length;
			nn = new double[newVerts+np][nl];
		}
		double[][] nverts = new double[newVerts+np][oldVerts[0].length];
		double[][] ntex = new double[newVerts+np][textureCoords[0].length];
		int l = oldVerts[0].length;
		for (int i = 0; i<np; ++i)	{
			System.arraycopy(oldVerts[i], 0, nverts[i],0,l );
			System.arraycopy(textureCoords[i], 0, ntex[i], 0, 2);
			if (on != null) System.arraycopy(on[i], 0, nn[i], 0 ,nl);
		}
		int outcount = np;
		for (int i = 0; i<nf; ++i)	{
			if ( textureJumps[i][0] || textureJumps[i][1])		{
				LoggingSystem.getLogger(GeometryUtility.class).log(Level.INFO,"Face "+i);
				for (int j = 0; j<indices[i].length; ++j)	{
					int which = indices[i][j];
					System.arraycopy(oldVerts[which],0,nverts[outcount], 0, l);
					System.arraycopy(textureCoords[which], 0, ntex[outcount], 0 ,2);
					if (on != null) System.arraycopy(on[which], 0, nn[outcount], 0 ,nl);
					for (int k=0;k<2;++k)		{
						if (textureJumps[i][k])	{
							if (ntex[outcount][k] < .5) ntex[outcount][k] += 1.0;//Math.ceil(ntex[outcount][k] - minmax[i][0][k]); 
							LoggingSystem.getLogger(GeometryUtility.class).log(Level.INFO, "Setting texture coordinate to "+ntex[outcount][k]+"from "+textureCoords[which][k]);
												}
					}
					indices[i][j] = outcount;
					outcount++;
				}
			}
			
		}
		setIndexedFaceSetFrom(dst, indices, nverts,  nn, null, ntex,null,null);
		if( src.getFaceAttributes(Attribute.NORMALS) != null)dst.setFaceAttributes(Attribute.NORMALS, src.getFaceAttributes(Attribute.NORMALS));
		if( src.getFaceAttributes(Attribute.COLORS) != null) dst.setFaceAttributes(Attribute.COLORS, src.getFaceAttributes(Attribute.COLORS));
		//if (src.getVertexAttributes(Attribute.NORMALS) != null)  calculateAndSetVertexNormals(dst);
		return dst;
	}

	private static double[][] getMinMax(int[] indices, double[][] array2d)	{
		int f = array2d[0].length;
		double[][] minmax = new double[2][f];
		System.arraycopy(array2d[indices[0]], 0, minmax[0], 0, f);
		System.arraycopy(array2d[indices[0]], 0, minmax[1], 0, f);
		for (int i = 1; i<indices.length; ++i)	{
			Rn.min(minmax[0], minmax[0], array2d[indices[i]]);
			Rn.max(minmax[1], minmax[1], array2d[indices[i]]);
		}
		return minmax;
	}

	public static IndexedFaceSet binaryRefine(IndexedFaceSet ifs)	{
		int[][] indices = ifs.getFaceAttributes(Attribute.INDICES).toIntArrayArray().toIntArrayArray(null);
		for (int i=0; i<indices.length; ++i)	{
			if (indices[i].length != 3) 
				throw new IllegalArgumentException("Indexed face set must consist of triangles");
		}
	
		Hashtable edgeVals = new Hashtable();	
	
		// the new triangulation will have:
		// 		vertices:	V + E
		//		edges:		2E + 3F
		//		faces:		4F
		//	we also know 	 V - E + F = 2 so E = V + F - 2
		int numVerts = ifs.getNumPoints();
		int numFaces = ifs.getNumFaces();
		int numEdges = ifs.getNumEdges();
		int vLength = GeometryUtility.getVectorLength(ifs);
		int newSize = numVerts + numEdges; //numFaces - 2;
		double[][] nvd = new double[newSize][vLength];
		double[][] newpptr = nvd;
		double[][] oldpptr = ifs.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
		System.arraycopy(oldpptr, 0, newpptr, 0, numVerts);		// copy over the existing vertices
	
		int[][] newIndices = new int[4*numFaces][3];
		int[] index = new int[3];
		int nV = numVerts;
		int nF = 0;
		for ( int i=0; i<numFaces; ++i)	{
			// create the new points
			for (int j=0; j<3; ++j)	{
				int v1 = indices[i][(j+1)%3];
				int v2 = indices[i][(j+2)%3];
				int kk = (v1>v2) ? (v1<<15) + v2 : (v2<<15) + v1;	// same for either order!
				Integer key = new Integer(kk);
				Integer value = (Integer) edgeVals.get(key);
				if (value != null )       {	// reuse old vertex
					index[j] = value.intValue();
				} else {		// new vertex creation
					index[j] = nV;	
					Rn.add(newpptr[nV],oldpptr[v1], oldpptr[v2]);
					Rn.times(newpptr[nV], .5,newpptr[nV]);
					edgeVals.put(key, new Integer(nV));
					nV++;
					if ( nV > newSize)	{
						//TODO indicate error!
						return null;
					}
				}
			}
			// the internal new face
			for (int k = 0 ; k<3; ++k)	newIndices[nF][k] = index[k];
			// the other three new faces
			nF++;
			newIndices[nF][0] = indices[i][0];
			newIndices[nF][1] = index[2];
			newIndices[nF][2] = index[1];
			nF++;
			newIndices[nF][0] = indices[i][1];
			newIndices[nF][1] = index[0];
			newIndices[nF][2] = index[2];
			nF++;
			newIndices[nF][0] = indices[i][2];
			newIndices[nF][1] = index[1];
			newIndices[nF][2] = index[0];
			nF++;
		}
	
		return createIndexedFaceSetFrom(newIndices, newpptr, null, null, null, null );
	
	}

	public static IndexedFaceSet clipToBox(IndexedFaceSet ifs, Rectangle3D box)	{
//		IndexedFaceSet clipped = new IndexedFaceSet();
		int n = ifs.getNumFaces();
//		clipped.setNumPoints(ifs.getNumPoints());
//		clipped.setNumEdges(ifs.getNumEdges());
//		clipped.setNumFaces(ifs.getNumFaces());
//		clipped.setVertexAttributes(ifs.getVertexAttributes());
//		clipped.setFaceAttributes(ifs.getFaceAttributes());
//		clipped.setEdgeAttributes(ifs.getEdgeAttributes());
		DataList verts = ifs.getVertexAttributes(Attribute.COORDINATES);
		ArrayList inBounds = new ArrayList();
		double[][] bnds = box.getBounds();
		for (int i = 0; i<n; ++i)	{
			int[] tf = ifs.getFaceAttributes(Attribute.INDICES).item(i).toIntArray(null);
			boolean outside = true;
			for (int j = 0; (j<tf.length) && outside; ++j)	{
				int k = tf[j];
				double[] vec = verts.item(k).toDoubleArray(null);
				if (vec.length == 4) Pn.dehomogenize(vec,vec);
				if (bnds[0][0] < vec[0] && bnds[1][0] > vec[0] 
				&& bnds[0][1] < vec[1] && bnds[1][1] > vec[1] 
				&& bnds[0][2] < vec[2] && bnds[1][2] > vec[2]	 ) 
						outside = false;
			}
			if (!outside)	{
				inBounds.add(tf);
			}
		}
		int m = inBounds.size();
		int[][] newIndices = new int[m][];
		for (int i =0; i<m; ++i)	{
			newIndices[i] = (int[] ) inBounds.get(i);
		}
		LoggingSystem.getLogger(GeometryUtility.class).log(Level.FINE,"In, out face count: "+n+"  "+m);
		//TODO rescue the other face attributes
		IndexedFaceSetFactory ifsf = new IndexedFaceSetFactory();
		ifsf.setVertexCount(ifs.getNumPoints());
		ifsf.setVertexAttributes(ifs.getVertexAttributes());
		ifsf.setFaceCount(m);
		ifsf.setFaceIndices(newIndices);		
		ifsf.setGenerateEdgesFromFaces(true);

		ifsf.update();
		return ifsf.getIndexedFaceSet();
	}

	public static double[] calculateVertexCurvature(IndexedFaceSet ifs)	{
		int n = ifs.getNumPoints();
		double[] curvature = new double[n];
		int[][] indices = ifs.getFaceAttributes(Attribute.INDICES).toIntArrayArray(null);
		double[][] verts = ifs.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
		int m = indices.length;
		for (int i = 0; i<m; ++i)		{
			int k = indices[i].length;
			for (int j = 0; j<k; ++j)	{
				int v0 = indices[i][j];
				int vp = indices[i][(j+k-1)%k];
				int vn = indices[i][(j+1)%k];
				double angle = Rn.euclideanAngle(Rn.subtract(null,verts[vn], verts[v0]), Rn.subtract(null, verts[vp],verts[v0]));
				curvature[v0] += angle;
			}
		}
		for (int i = 0; i<n; ++i)		curvature[i] = Math.PI - curvature[i];
		return curvature;
	}

	
	/**
	 * Returns the total number of points in array of point sets.
	 * @param ps array of point sets
	 * @return total number of points in array of point set.
	 */
	public static int getTotalNumPoints( PointSet [] ps ) {
		int N =0;
		for( int i=0; i<ps.length; i++ ) {
			N += ps[i].getNumPoints();
		}
		return N;
	}
	
	/**
	 * Merges the data for a vertex attribute for an array of point set into a single trivial type array.
	 * If a single entry of the array fails to have the prescribed attribute a NullPointerException is
	 * thrown.
	 * @param ps array of point sets.
	 * @param attr a vertex attribute, e.g., @link de.jreality.scene.data.Attribute.COORDINATES
	 * @return array containing all data of vertex attribute of an array of point sets.
	 */
	public static double [][] mergeDoubleArrayArrayVertexAttribute( PointSet [] ps , Attribute attr) {
		
		double [][] result = new double[getTotalNumPoints(ps)][];
		
		for( int i=0, n=0; i<ps.length; n += ps[i].getNumPoints(), i++ ) {
			double[][] values = ps[i].getVertexAttributes( attr ).toDoubleArrayArray(null);
			System.arraycopy(values, 0, result, n, values.length );
		}
		
		return result;  
	}
	
	/**
	 * Returns the total nubmer of faces in array of indexed face sets.
	 * @param ifs array of indexed faces sets
	 * @return total number of faces in array of indexed face sets.
	 */
	public static int getTotalNumFaces( IndexedFaceSet [] ifs ) {
		int N =0;
		for( int i=0; i<ifs.length; i++ ) {
			N += ifs[i].getNumFaces();
		}
		return N;
	}
	
	/**
	 * Merges the data for a face attribute for an array of indexed face set into a single trivial type array.
	 * If a single entry of the array fails to have the prescribed attribute a NullPointerException is
	 * thrown.
	 * @param ifs array of indexed face sets.
	 * @param attr a face attribute, e.g., @link de.jreality.scene.data.Attribute.NORMALS
	 * @return array containing all data of face attribute of an array of indexed face set.
	 */
	public static double [][] mergeDoubleArrayArrayFaceAttribute( IndexedFaceSet [] ifs , Attribute attr) {
		
		double [][] result = new double[getTotalNumFaces(ifs)][];
		
		for( int i=0, n=0; i<ifs.length; n += ifs[i].getNumFaces(), i++ ) {
			double[][] values = ifs[i].getFaceAttributes( attr ).toDoubleArrayArray(null);
			System.arraycopy(values, 0, result, n, values.length );
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
	public static int [][] mergeIntArrayArrayFaceAttribute( IndexedFaceSet [] ifs , Attribute attr) {
		
		int [][] result = new int[getTotalNumFaces(ifs)][];
		
		for( int i=0, n=0; i<ifs.length; n += ifs[i].getNumFaces(), i++ ) {
			int[][] values = ifs[i].getFaceAttributes( attr ).toIntArrayArray(null);
			System.arraycopy(values, 0, result, n, values.length );
		}
		
		return result;  
	}
	
	/**
	 * Merges an array of indexed face sets into a single.
	 * Currently only vertex and face attributes associated to
	 * {@link de.jreality.scene.data.DataList DataList} which are
	 * instance of {@link de.jreality.scene.data.DoubleArrayArray DoubleArrayArray}
	 * are taken into account. e.g. NORMALS, COLORS, TEXTRUE_COORDINATES, ...
	 * @param ifs array of indexed face sets.
	 * @return merger of all indexed face sets
	 */
	public static IndexedFaceSet mergeIndexedFaceSets( IndexedFaceSet [] ifs ) {
				
		IndexedFaceSet result = new IndexedFaceSet();
		
		final int [][] faceIndices = mergeIntArrayArrayFaceAttribute( ifs, Attribute.INDICES );
		
		for( int i=1, n=ifs[0].getNumPoints(), k=ifs[0].getNumFaces(); i<ifs.length; n += ifs[i].getNumPoints(), i++ ) {
			final int nof = ifs[i].getNumFaces();
			for( int f=0; f<nof; f++, k++ ) {
				final int [] face = faceIndices[k];
				for( int j=0; j<face.length; j++) {
					face[j] += n;
				}
			}	
		}
			
		result.setFaceCountAndAttributes(
				Attribute.INDICES,
				new IntArrayArray.Array( faceIndices )
		);
		
		mergeDoubleArrayArrayVertexAttributes(ifs, result, true );
		mergeDoubleArrayArrayFaceAttributes(ifs, result, false );
		
		return result;
	}

	private static void mergeDoubleArrayArrayVertexAttributes( PointSet[] ps, PointSet result, boolean firstTime ) {
		Object [] vertexAttr = ps[0].getVertexAttributes().storedAttributes().toArray();
		
		for( int i=0; i<vertexAttr.length; i++ ) {
			Attribute attr = (Attribute)vertexAttr[i];
			if( ps[0].getVertexAttributes(attr) instanceof DoubleArrayArray ) {
				try {
					DataList dataList = new DoubleArrayArray.Array(mergeDoubleArrayArrayVertexAttribute(ps, attr ));
					if( firstTime ) {
						result.setVertexCountAndAttributes( attr, dataList );
						firstTime = false;
					} else {
						result.setVertexAttributes( attr, dataList );
					}
					
				} catch (Exception e) {}
				
			}
		}
	}
	
	private static void mergeDoubleArrayArrayFaceAttributes(IndexedFaceSet[] ifs, IndexedFaceSet result, boolean firstTime ) {
		Object [] faceAttr = ifs[0].getFaceAttributes().storedAttributes().toArray();
		for( int i=0; i<faceAttr.length; i++ ) {
			Attribute attr = (Attribute)faceAttr[i];
			if( ifs[0].getFaceAttributes(attr) instanceof DoubleArrayArray  ) {
				try {
					DataList dataList = new DoubleArrayArray.Array(mergeDoubleArrayArrayFaceAttribute(ifs, attr ));
					if( firstTime ) {
						result.setFaceCountAndAttributes( attr, dataList );
						firstTime = false;
					} else {
						result.setFaceAttributes( attr, dataList );
					}
			} catch (Exception e) {}
				
			}
		}
	}

    /**
         * This mehtod assumes that the faces in the given IndexedFaceSet are
         * planar embedded polygons. They need not be convex. 
         * Moreover it assumes that the facenormals are set.
         * This method is not R4 save at the moment. 
         * @param fs
         * @return
         */
        public static IndexedFaceSet triangulate(IndexedFaceSet fs) {
         IndexedFaceSet ts = new IndexedFaceSet();
         DataListSet vertexData = fs.getVertexAttributes();
         ts.setVertexCountAndAttributes(vertexData);
         DataListSet edgeData = fs.getEdgeAttributes();
         ts.setEdgeCountAndAttributes(edgeData);
         
         final int n = fs.getNumFaces(); 
         DataList faceDL = fs.getFaceAttributes(Attribute.INDICES);
         DataList pointDL = fs.getVertexAttributes(Attribute.COORDINATES);
         DataList fNormalDL = fs.getFaceAttributes(Attribute.NORMALS);
         if (fNormalDL == null) {
           double[][] fn = GeometryUtility.calculateFaceNormals(fs);
           fNormalDL = StorageModel.DOUBLE_ARRAY_ARRAY.createReadOnly(fn);
         }
         
    //     System.out.println(" points are "+pointDL);
         
         // here we place the new triangles:
         ArrayList triangles = new ArrayList();
         
         // iterate over all polygons:
         for(int i = 0;i<n;i++) {
             int[] faceIndices = faceDL.item(i).toIntArray(null);
    //         System.out.println(" face "+i+" is "+faceDL.item(i));
             // pack the points for this polygon into a list:
             DoubleArray[] pts = new DoubleArray[faceIndices.length];
             for(int j = 0;j<faceIndices.length;j++)
                 pts[j] = (pointDL.item(faceIndices[j]).toDoubleArray());
             
             double[] normal = fNormalDL.item(i).toDoubleArray(null);
             normal = Rn.normalize(null,normal);
             int rotationIndex = rotationIndex(normal,pts);
             if(rotationIndex<0) Rn.times(normal,-1,normal);
             // iterate over triplets of succesive points
             final int numPts = faceIndices.length;
             int remainingPts = numPts;
             int first =0, second = 0, third = 0;
             while(remainingPts>3) {
                 first = first%numPts;
                 //find three successive points:
                 while(pts[first]== null) first= (first+1)%numPts;
                 second = (first+1)%numPts;
                 while(pts[second]== null) second = (second+1)%numPts;
                 third = (second+1)%numPts;
                 while(pts[third]== null) third = (third+1)%numPts;
                  // check triangle for degeneracy and test
                 // wether any other point is inside the triangle:
                 double[] p1 = pts[first].toDoubleArray(null);
                 double[] p2 = pts[second].toDoubleArray(null);
                 double[] p3 = pts[third].toDoubleArray(null);
                 double[] e1 = Rn.subtract(null,p2,p1);
                 double[] e2 = Rn.subtract(null,p3,p2);
                 double[] e3 = Rn.subtract(null,p1,p3);
    
                 double[] cnormal = Rn.crossProduct(null,e2,e1);
                 double d = Rn.innerProduct(normal, cnormal);
                 if(Math.abs(d) < EPS) {
                     System.out.println("Warning degenerate triangle in triangulate... dropping "+second);
                     System.out.println(" ->"+first+" "+second+" "+third);
                     pts[second] = null;
                     remainingPts--;
                     first = second;
                     continue;
                 }
                 if(d < 0) {
                   first++;
                   continue;
                 }
                 boolean allOutside = true;
                 for(int k = 0;k<numPts;k++) {
                     if(pts[k] ==null||k == first|| k == second|| k == third) continue;
                     double[] p4 = pts[k].toDoubleArray(null);
                     double[] dir = Rn.subtract(null,p4,p1);
                     double s1 = Rn.innerProduct(normal,Rn.crossProduct(null,e1,dir));
                     dir = Rn.subtract(dir,p4,p2);
                     double s2 = Rn.innerProduct(normal,Rn.crossProduct(null,e2,dir));
                     dir = Rn.subtract(dir,p4,p3);
                     double s3 = Rn.innerProduct(normal,Rn.crossProduct(null,e3,dir));
                     //if(!(s1 <=0 || s2 <=0 || s3<=0)) {
                     if((s1 <0 && s2 <0 && s3<0)) {
                         allOutside = false;
    //                     System.out.println("pnt no "+k+" is inside "+s1+"  "+s2+"  "+s3);
    //                     System.out.println("  ->"+pts[k]+" is inside");
    //                     System.out.println("  ->"+pts[first]);
    //                     System.out.println("  ->"+pts[second]);
    //                     System.out.println("  ->"+pts[third]);
                         break;
                     } 
                 }  
                 if(!allOutside) {
                     //if yes continue:
                     first++;
                     continue;
                 }
				// if not add the triangle to the list of new polygons and
				 // remove the middle point from pts list:
				 triangles.add(new int[] {faceIndices[first],faceIndices[second],faceIndices[third]});
				 pts[second] = null;
				 remainingPts--;
   //                 System.out.println("adding "+ first+" "+second+" "+third+" ... "+remainingPts+" to go");
				 first++;
             }
             while(pts[first]== null) first= (first+1)%numPts;
             second = (first+1)%numPts;
             while(pts[second]== null) second = (second+1)%numPts;
             third = (second+1)%numPts;
             while(pts[third]== null) third = (third+1)%numPts;
             triangles.add(new int[] {faceIndices[first],faceIndices[second],faceIndices[third]});
    //         System.out.println("finally adding "+ first+" "+second+" "+third);
         }
         
         int[][] faces = new int[triangles.size()][];
         faces = (int[][]) triangles.toArray(faces);
         
         ts.setFaceCountAndAttributes(Attribute.INDICES,StorageModel.INT_ARRAY_ARRAY.createReadOnly(faces));
         GeometryUtility.calculateAndSetNormals(ts);
         return ts;
        }

        private static double EPS = 0.000000000001; 

        /**
         * @param normal
         * @param pts
         * @return
         */
        private static int rotationIndex(double[] normal, DoubleArray[] pts) {
            final int numPts = pts.length;
            double[] pt0 = pts[0].toDoubleArray(null); 
            double[] pt1 = pts[1].toDoubleArray(null);
            double dir1[] = Rn.subtract(null,pt1,pt0);
            Rn.normalize(dir1,dir1);
            double dir0[] = new double[dir1.length];
            double angle = 0;
            for(int i = 0;i<numPts;i++) {
                double[] tmp = dir0;
                dir0 = dir1;
                pt0 = pts[(i+1)%numPts].toDoubleArray(null); 
                pt1 = pts[(i+2)%numPts].toDoubleArray(null);
                dir1 = Rn.subtract(tmp,pt1,pt0);
                Rn.normalize(dir1,dir1);
                angle += Math.asin(Rn.innerProduct(normal,Rn.crossProduct(null,dir1,dir0)));
            }
            angle /= 2*Math.PI;
            int i = (int) Math.round(angle);
            return i;
        }

        
        // needed for the new implementation of buildEdgesFromFaces:
        private static final class Pair {
          final int l, h;
          Pair(int a, int b) {
            if(a<=b) { l=a; h=b; }
            else     { h=a; l=b; }
          }
          public boolean equals(Object obj) {
            if(this==obj) return true;
            try {
              final Pair p=(Pair)obj;
              return l == p.l && h == p.h;
            } catch(ClassCastException ex) {
              return false;
            }
          }
          public int hashCode() {
            return (l<<16)^h;
          }
        }
       
        public static IntArrayArray edgesFromFaces( int [][] faces ) {
        	return edgesFromFaces( new IntArrayArray.Array(faces));
        }
        
        /**
         * TODO optimize in case its a QuadMesh (u+v curves instead of u*v segments)
         * @param faces
         * @return
         */
        public static IntArrayArray edgesFromFaces( IntArrayArray faces )
        {
            HashSet set =new HashSet();
           
            for (int i= 0; i < faces.getLength(); i++)
            {
                IntArray f= faces.getValueAt(i);
                for (int j= 0; j < f.getLength() - 1; j++)
                {
                    set.add(new Pair(f.getValueAt(j), f.getValueAt(j + 1)));
                    
                }
                set.add(new Pair(f.getValueAt(f.getLength()-1), f.getValueAt(0)));
            }
            final int num= set.size();
            int[] edge=new int[num*2];
            int count=0;
            for (Iterator iter = set.iterator(); iter.hasNext(); count+=2) {
              Pair p = (Pair) iter.next();
              edge[count]=p.l;
              edge[count+1]=p.h;
            }
            return new IntArrayArray.Inlined(edge, 2);
        }
        
        public static void calculateAndSetEdgesFromFaces( IndexedFaceSet ifs ) {
        	IntArrayArray faces=
                ifs.getFaceAttributes(Attribute.INDICES).toIntArrayArray();
        	 ifs.setEdgeCountAndAttributes(Attribute.INDICES, edgesFromFaces( faces ) );
        }
        
        // merge all IndexedFaceSet's which occur as the geometry field of 
        // direct children of sgc which
        // do not have a transformation
        // and which do not themselves have children
        public static SceneGraphComponent mergeIndexedFaceSets(SceneGraphComponent sgc)	{
        
        	// collects sgc's which themselves have children
        	class CollectComponents extends SceneGraphVisitor {
        		Vector sgclist;
        		SceneGraphComponent root;
        		CollectComponents(SceneGraphComponent r)	{
        			root = r;
        		}
				public void visit(SceneGraphComponent c) {
					if (c.getChildComponentCount() > 0) sgclist.add(c);
					c.childrenAccept(this);
				}
        		
				Object visit()	{
					sgclist = new Vector();
					visit(root);
					return sgclist;
				}
	        }
        	
        	CollectComponents cc = new CollectComponents(sgc);
        	Vector sgclist = (Vector) cc.visit();
        	Iterator iter = sgclist.iterator();
        	while (iter.hasNext())	{
        		SceneGraphComponent next = (SceneGraphComponent) iter.next();
        		_mergeIndexedFaceSets(next);
        	}
        	return sgc;
        }
        
       private static SceneGraphComponent _mergeIndexedFaceSets(SceneGraphComponent sgc)	{
        	Vector ifslist = new Vector();
        	Vector colorList = new Vector();
        	Vector lengths = new Vector();
        	Vector toRemove = new Vector();
        	int n = sgc.getChildComponentCount();
        	Appearance ap = sgc.getAppearance();
        	EffectiveAppearance eap = EffectiveAppearance.create();
        	if (ap != null) eap = eap.create(ap);
        	IndexedFaceSet ifs;
        	int vcount = 0;
        	if (sgc.getGeometry()!= null && sgc.getGeometry() instanceof IndexedFaceSet) {
        		ifs = (IndexedFaceSet) sgc.getGeometry(); 
        		ifslist.add(ifs);
        		lengths.add(new Integer(ifs.getNumPoints()));
        		vcount += ifs.getNumPoints();
            	Object dc =  eap.getAttribute("polygonShader.diffuseColor",Color.WHITE, Color.class);
            	if (dc instanceof Color)		{
            		colorList.add(dc);
            	}  else 
            		colorList.add(Color.WHITE);
            	 
        	}
        	for (int i = 0; i<n; ++i)	{
        		SceneGraphComponent child = sgc.getChildComponent(i);
        		if (child.getTransformation() != null) continue;
        		if (child.getChildComponentCount() != 0) continue;
        		if (child.getGeometry() == null) continue;
        		Geometry geom = child.getGeometry();
         		if (geom instanceof IndexedFaceSet)	{
        			ifslist.add(geom);
            		toRemove.add(child);
                	ifs = (IndexedFaceSet) geom;
               		lengths.add(new Integer(ifs.getNumPoints()));
            		vcount += ifs.getNumPoints();
              		ap = child.getAppearance();
            		if (ap != null)	{
            			EffectiveAppearance ceap = eap.create(ap);
                    	Object dc =  ceap.getAttribute("polygonShader.diffuseColor",Color.WHITE, Color.class);
                    	if (dc instanceof Color)		{
                    		colorList.add(dc);
                    	}  else 
                    		colorList.add(Color.WHITE);	
            		}
        		}
        	}
        	Iterator iter = toRemove.iterator();
        	while (iter.hasNext())	{ sgc.removeChild( (SceneGraphComponent) iter.next()); }
        	
        	n = ifslist.size();
        	if (n <= 1) return sgc;
        	IndexedFaceSet[] list = new IndexedFaceSet[ifslist.size()];
        	list = (IndexedFaceSet[]) ifslist.toArray(list);
        	ifs = mergeIndexedFaceSets(list);
        	// construct vertex color list
        	double[] carray = new double[vcount*4];
        	iter = colorList.iterator();
        	int i = 0;
        	int cptr = 0;
        	while (iter.hasNext())	{
        		Color c = (Color) iter.next();
        		int howMany = ((Integer) lengths.get(i++)).intValue();
        		float[] rgba = c.getRGBComponents(null);
        		for (int j= 0; j<howMany; ++j)	{
        			for (int k=0; k<4; ++k)		carray[cptr++] = rgba[k];
        		}
        	}
        	ifs.setVertexAttributes(Attribute.COLORS, StorageModel.DOUBLE_ARRAY.inlined(4).createReadOnly(carray));
        	sgc.setGeometry(ifs);
        	return sgc;
        }
       
       /**
        * applies the given matrix to each vector in <code>data</code>.
        * <br> If the columns of <code>data</code> are not 4 vectors, then the
        * vectors are assumed to have w=1, and the result is dehomogenized before
        * writing back.
        * @param data array of dimension [n][1-4]
        * @param matrix the matrix to transform all the vectors
        */
       public static void transformVertexData(double[][] data, Matrix matrix) {
         final double[] p = new double[4];
         int j;
         for (int i = 0; i < data.length; i++) {
           boolean dehomogenize = data[i].length < 4;
           for (j = 0; j<data[i].length; j++) p[j] = data[i][j];
           for (; j<4; j++) p[j] = j == 3 ? 1 : 0;
           double[] transformed = matrix.multiplyVector(p);
           double w = dehomogenize ? transformed[3] : 1;
           for (j = 0; j<data[i].length; j++) data[i][j] = transformed[j]/w;           
         }
       }
       
}
