
/*
 * Created on Jul 14, 2004
 *
 */
package de.jreality.worlds;

import de.jreality.geometry.GeometryUtility;
import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.geometry.Primitives;
import de.jreality.geometry.QuadMeshShape;
import de.jreality.scene.Appearance;
import de.jreality.scene.CommonAttributes;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Sphere;
import de.jreality.scene.Transformation;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DataList;
import de.jreality.scene.data.DoubleArray;
import de.jreality.scene.data.StorageModel;
import de.jreality.util.ConfigurationAttributes;
import de.jreality.util.Pn;
import de.jreality.util.Rn;
import de.jreality.util.SceneGraphUtilities;

/**
 * @author weissman
 *
 */
public class DebugLattice extends AbstractJOGLLoadableScene {

	public int getSignature() {
		// TODO Auto-generated method stub
		return Pn.EUCLIDEAN;
	}
	public  SceneGraphComponent makeWorld()	{
		SceneGraphComponent theRow;
		//Cube ico = new Cube();
		
		SceneGraphComponent theWorld = new SceneGraphComponent();
		theWorld.setTransformation(new Transformation());
		theWorld.setName("navComp");
		theRow = new SceneGraphComponent();
		theRow.setTransformation(new Transformation());
		SceneGraphComponent theRowI = new SceneGraphComponent();
		theRowI.setTransformation(new Transformation());
		SceneGraphComponent newRow;
		double[] axis2 = {0.0, 0.0, 1.0};
		newRow = new SceneGraphComponent();
		newRow.setTransformation(new Transformation());
		newRow.getTransformation().setRotation(Math.PI/2.0, axis2);
		newRow.getTransformation().setTranslation(0.0, 0.0, 1.0);
		newRow.addChild(theRow);
		//theWorld.addChild(newRow);
		
		IndexedFaceSet[] spheres = new IndexedFaceSet[5];
		for (int i = 0; i<4; ++i)	{
			if (i == 0)		spheres[0] = Primitives.icosahedron();
			else {
				spheres[i] = IndexedFaceSetUtility.binaryRefine(spheres[i-1]);
				double[][] verts = spheres[i].getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
				int vlength = GeometryUtility.getVectorLength(spheres[i]);
				Rn.normalize(verts, verts);
				spheres[i].setVertexAttributes(Attribute.COORDINATES, StorageModel.DOUBLE_ARRAY.array(vlength).createReadOnly(verts));
			}
			spheres[i].setVertexAttributes(Attribute.NORMALS, spheres[i].getVertexAttributes(Attribute.COORDINATES)); 
			GeometryUtility.calculateAndSetFaceNormals(spheres[i]);
			spheres[i].buildEdgesFromFaces();
			
			DataList vv = spheres[i].getVertexAttributes(Attribute.COORDINATES);
			int ll = spheres[i].getNumPoints();
			double[][] vc = new double[ll][4];
			for (int j=0; j<ll; ++j)	{
				DoubleArray v = vv.item(j).toDoubleArray();
				vc[j][0] = .3+.7*v.getValueAt(0);
				vc[j][1] = .3+.7*v.getValueAt(1);
				vc[j][2] = .3+.7*v.getValueAt(2);
				vc[j][3] = 0.5d;		// alpha component
			}
			spheres[i].setVertexAttributes(Attribute.COLORS, StorageModel.DOUBLE_ARRAY.array(4).createReadOnly(vc));

			SceneGraphComponent icokit = SceneGraphUtilities.createFullSceneGraphComponent();
			icokit.setTransformation(new Transformation());
			icokit.setGeometry(spheres[i]);
			icokit.getTransformation().setTranslation(-1.5 + i, 0, 0);
			icokit.getTransformation().setStretch(.5);
			if (i == 0) icokit.getAppearance().setAttribute(CommonAttributes.LIGHTING_ENABLED, false);
			theRow.addChild(icokit);

		}
			
		//theWorld.addChild(theRow);
		theWorld.getTransformation().setTranslation(0., 3.28, 0.);
		// create a 5x5x5 lattice which fills the Portal space
		int dim = 5;
		double scale = 2*4.068/((dim-1));
		double yscale = 6.561/(dim-1);
		SceneGraphComponent lattice = makeLattice(-scale*(dim-1)/2., -yscale*(dim-1)/2., -scale*(dim-1)/2., scale, yscale, scale, dim, dim, dim, 0.05, 0.01);
		lattice.setAppearance(new Appearance());
		theWorld.setAppearance(new Appearance());
		theWorld.addChild(lattice);
		return theWorld;
	}
	
    public static SceneGraphComponent makeSimpleUnitLattice(int dim) {
        return makeLattice(-((double)(dim-1))/2., -((double)(dim-1))/2., -((double)(dim-1))/2., 1., 1., 1., dim, dim, dim, 0.05, 0.01);
    }

    
    public static SceneGraphComponent makeLattice(double xmin, double ymin,
            double zmin, double dx, double dy, double dz, int cx, int cy,
            int cz, double sphereRadius, double latticeRad) {
        SceneGraphComponent latticeComp = new SceneGraphComponent();
        if (latticeRad > 0) {
            SceneGraphComponent lineX = new SceneGraphComponent();
            double[][] profileX = { { xmin, latticeRad, 0},
                    { xmin + (cx-1) * dx, latticeRad, 0}};
            QuadMeshShape lX = Primitives.surfaceOfRevolutionAsIFS(
                    profileX, 5, 2.0 * Math.PI);
            lineX.setGeometry(lX);
            SceneGraphComponent lineY = new SceneGraphComponent();
            double[][] profileY = { { ymin, latticeRad, 0},
                    { ymin + (cy-1) * dy, latticeRad, 0}};
            QuadMeshShape lY = Primitives.surfaceOfRevolutionAsIFS(
                    profileY, 5, 2.0 * Math.PI);
            lineY.setGeometry(lY);
            lineY.setTransformation(new Transformation());
            lineY.getTransformation().setRotation(Math.PI / 2.,
                    new double[] { 0., 0., 1.});
            SceneGraphComponent lineZ = new SceneGraphComponent();
            double[][] profileZ = { { zmin, latticeRad, 0},
                    { zmin + (cz-1) * dz, latticeRad, 0}};
            QuadMeshShape lZ = Primitives.surfaceOfRevolutionAsIFS(
                    profileZ, 5, 2.0 * Math.PI);
            lineZ.setGeometry(lZ);
            lineZ.setTransformation(new Transformation());
            lineZ.getTransformation().setRotation(-Math.PI / 2.,
                    new double[] { 0., 1., 0.});

            for (int i = 0; i < cy; i++) {
                for (int j = 0; j < cz; j++) {
                    SceneGraphComponent c = new SceneGraphComponent();
                    c.setTransformation(new Transformation());
                    c.getTransformation().setTranslation(0, ymin + i * dy,
                            zmin + j * dz);
                    c.addChild(lineX);
                    latticeComp.addChild(c);
                }
            }
            for (int i = 0; i < cx; i++) {
                for (int j = 0; j < cz; j++) {
                    SceneGraphComponent c = new SceneGraphComponent();
                    c.setTransformation(new Transformation());
                    c.getTransformation().setTranslation(xmin + i * dx, 0, zmin + j * dz);
                    c.addChild(lineY);
                    latticeComp.addChild(c);
                }
            }
            for (int i = 0; i < cx; i++) {
                for (int j = 0; j < cy; j++) {
                    SceneGraphComponent c = new SceneGraphComponent();
                    c.setTransformation(new Transformation());
                    c.getTransformation().setTranslation(xmin + i * dx, ymin + j * dy, 0);
                    c.addChild(lineZ);
                    latticeComp.addChild(c);
                }
            }
        }
        if (sphereRadius > 0) {
            SceneGraphComponent sphereComp = new SceneGraphComponent();
            sphereComp.setGeometry(new Sphere());
            sphereComp.setTransformation(new Transformation());
            sphereComp.getTransformation().setStretch(sphereRadius);

            SceneGraphComponent line = new SceneGraphComponent();
            for (int i = 0; i < cx; i++) {
                SceneGraphComponent c = new SceneGraphComponent();
                c.setTransformation(new Transformation());
                c.getTransformation().setTranslation(xmin + i * dx, 0, 0);
                c.addChild(sphereComp);
                line.addChild(c);
            }
            SceneGraphComponent plane = new SceneGraphComponent();
            for (int j = 0; j < cy; j++) {
                SceneGraphComponent c = new SceneGraphComponent();
                c.setTransformation(new Transformation());
                c.getTransformation().setTranslation(0, ymin + j * dy, 0);
                c.addChild(line);
                plane.addChild(c);
            }
            for (int k = 0; k < cz; k++) {
                SceneGraphComponent c = new SceneGraphComponent();
                c.setTransformation(new Transformation());
                c.getTransformation().setTranslation(0, 0, zmin + k * dz);
                c.addChild(plane);
                latticeComp.addChild(c);
            }
        }
        return latticeComp;
    }

	ConfigurationAttributes config = null;

	/* (non-Javadoc)
	 * @see de.jreality.portal.WorldMaker#setConfiguration(de.jreality.portal.util.Configuration)
	 */
	public void setConfiguration(ConfigurationAttributes config) {
		this.config = config;
	}


	

}
