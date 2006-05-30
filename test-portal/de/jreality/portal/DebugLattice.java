package de.jreality.portal;


import java.awt.Color;

import de.jreality.geometry.GeometryUtility;
import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.geometry.IndexedLineSetFactory;
import de.jreality.geometry.Primitives;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Sphere;
import de.jreality.scene.Transformation;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DataList;
import de.jreality.scene.data.DoubleArray;
import de.jreality.scene.data.StorageModel;
import de.jreality.scene.tool.PortalCoordinateSystem;
import de.jreality.shader.CommonAttributes;
import de.jreality.ui.viewerapp.ViewerApp;
import de.jreality.util.ConfigurationAttributes;
import de.jreality.util.Rectangle3D;
import de.jreality.util.SceneGraphUtility;

/**
 * @author weissman
 *
 */
public class DebugLattice  {

	public static void main(String[] args) {
		SceneGraphComponent grid = makeWorld();
		ViewerApp.display(grid);

	}

	public  static SceneGraphComponent makeWorld()	{
		SceneGraphComponent theWorld = new SceneGraphComponent();
		theWorld.setTransformation(new Transformation());
		theWorld.setName("navComp");
		double scale = PortalCoordinateSystem.xDimPORTAL/2; //4.068*0.3048;  // 2.034; //2*4.068/((dim-1));
		double yscale = PortalCoordinateSystem.yDimPORTAL;  //6.561*0.3048; // 1.64; //6.561/(dim-1);
		double yoffset = PortalCoordinateSystem.yOffsetPORTAL; //1.365*0.3048;
		double[][] bnds = {{-scale, yoffset, -scale},{scale, yoffset+yscale, scale}};
		Rectangle3D portalBox = new Rectangle3D(bnds);
		SceneGraphComponent lattice = makeLattice(portalBox, 5); //-scale*(dim-1)/2., -yscale*(dim-1)/2., -scale*(dim-1)/2., scale, yscale, scale, dim, dim, dim, 0.05, 0.01);
		theWorld.setAppearance(new Appearance());
		theWorld.addChild(lattice);
		return theWorld;
	}
	
    public static SceneGraphComponent makeLattice(Rectangle3D box, int segments) {
        SceneGraphComponent latticeComp = new SceneGraphComponent();
        Appearance ap = new Appearance();
        ap.setAttribute(CommonAttributes.TUBE_RADIUS, .02);
        ap.setAttribute(CommonAttributes.POINT_RADIUS, .04);
        ap.setAttribute(CommonAttributes.VERTEX_DRAW, true);
        ap.setAttribute(CommonAttributes.EDGE_DRAW, true);
        ap.setAttribute(CommonAttributes.SPHERES_DRAW, true);
        ap.setAttribute(CommonAttributes.TUBES_DRAW, true);
        ap.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.POLYGON_SHADER+".diffuseColor", new Color(.8f, .4f, 0f));
        ap.setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.POLYGON_SHADER+".diffuseColor", new Color(.3f, .8f, 0f));
        latticeComp.setAppearance(ap);
        int total = segments*segments*segments;
        int n = segments;
        int n2 = segments*segments;
        double factor = segments - 1.0;
        double[][] bnds = box.getBounds();
        double[] extent = box.getExtent();
        double[][] verts = new double[total][3];
        int[][] edges = new int[3*n2][2];
        for (int i = 0; i<segments; ++i)	{		// z loop
        	for (int j = 0; j<segments; ++j)	{	// y loop
        		for (int k = 0; k<segments; ++k)	{	// x loop
        			int[] lookup = {i,j,k};
        			for (int m = 0; m<3; ++m)	{
            			verts[i*n2+j*n+k][m] = bnds[0][m] + (lookup[m]/factor)*extent[m];	
        			}
        		}
        	}
        }
        for (int i = 0; i<segments; ++i)	{
        	for (int j = 0; j<segments; ++j)	{	
        		edges[i*n+j][0] = i*n+j;
        		edges[i*n+j][1] = i*n+j+n2*(n-1);
        		edges[i*n+j+n2][0] = i*n2+j;
        		edges[i*n+j+n2][1] = i*n2+j+n*(n-1);
        		edges[i*n+j+2*n2][0] = i*n2+j*n;
        		edges[i*n+j+2*n2][1] = i*n2+j*n+(n-1);
        	}
        }
        IndexedLineSetFactory ifsf = new IndexedLineSetFactory();
        ifsf.setVertexCount(total);
        ifsf.setVertexCoordinates(verts);
        ifsf.setLineCount(edges.length);
        ifsf.setEdgeIndices(edges);
        ifsf.update();
        IndexedLineSet ils = ifsf.getIndexedLineSet();
        latticeComp.setGeometry(ils);
        return latticeComp;
    }
    

}
