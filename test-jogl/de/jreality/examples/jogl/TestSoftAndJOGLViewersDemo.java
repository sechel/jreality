/*
 * Created on Jul 5, 2004
 *
 */
package de.jreality.examples.jogl;

import de.jreality.geometry.GeometryUtility;
import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.geometry.Primitives;
import de.jreality.scene.Appearance;
import de.jreality.scene.CommonAttributes;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Transformation;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DataList;
import de.jreality.scene.data.DoubleArray;
import de.jreality.scene.data.StorageModel;
import de.jreality.util.Rn;
import de.jreality.util.SceneGraphUtilities;


/**
 * @author gunn
 *
 */
public class TestSoftAndJOGLViewersDemo extends InteractiveViewerDemo {
	SceneGraphComponent icokit;
	boolean tryFlatten = true;
	/**
	 * 
	 */
	public TestSoftAndJOGLViewersDemo() {
		super();
	}

	
		/*	
		*/
	
		/**
	 * @param tabbed_pane
	 * @param b
	 */
	public TestSoftAndJOGLViewersDemo(int tabbed_pane, boolean b) {
		
		super(tabbed_pane, b);
	}


		public SceneGraphComponent makeWorld()	{
			SceneGraphComponent theRow;
			//Cube ico = new Cube();
			
			SceneGraphComponent theWorld = new SceneGraphComponent();
			theWorld.setTransformation(new Transformation());
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
			theWorld.addChild(newRow);
			
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

				icokit = SceneGraphUtilities.createFullSceneGraphComponent();
				icokit.setTransformation(new Transformation());
				icokit.setGeometry(spheres[i]);
				icokit.getTransformation().setTranslation(-1.5 + i, 0, 0);
				icokit.getTransformation().setStretch(.5);
				if (i == 0) icokit.getAppearance().setAttribute(CommonAttributes.LIGHTING_ENABLED, false);
				theRow.addChild(icokit);

			}
				
			theWorld.addChild(theRow);
			theWorld.setAppearance(new Appearance());
			theWorld.getAppearance().setAttribute(CommonAttributes.DEPTH_FUDGE_FACTOR, .99999d);
			theWorld.getAppearance().setAttribute(CommonAttributes.SMOOTH_SHADING, true);
			return theWorld;
		}
	
	
		public boolean showSoft()	{return true; }
		
		public boolean addBackPlane()	{return false; }
		
		public static void main(String argv[])	{
			TestSoftAndJOGLViewersDemo test = new TestSoftAndJOGLViewersDemo(InteractiveViewerDemo.TABBED_PANE, false);
			test.begin();
			test = new TestSoftAndJOGLViewersDemo(InteractiveViewerDemo.TABBED_PANE, false);
			test.begin();
		}
	
}
