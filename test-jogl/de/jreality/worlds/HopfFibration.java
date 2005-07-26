/*
 * Created on Nov 11, 2004
 *
 */
package de.jreality.worlds;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;

import de.jreality.geometry.GeometryUtility;
import de.jreality.geometry.IndexedLineSetUtility;
import de.jreality.geometry.QuadMeshShape;
import de.jreality.geometry.SphereHelper;
import de.jreality.scene.Appearance;
import de.jreality.scene.CommonAttributes;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Viewer;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.StorageModel;
import de.jreality.util.Pn;
import de.jreality.util.Quaternion;
import de.jreality.util.SceneGraphUtilities;

/**
 * @author gunn
 *
 */
public class HopfFibration extends AbstractJOGLLoadableScene {
		SceneGraphComponent icokit;
		SceneGraphComponent[][] levels = new SceneGraphComponent[4][2];
		protected boolean showTubes = true, showLevel[] = {true, true, false, false};
		
		public SceneGraphComponent makeWorld()	{
			
			SceneGraphComponent theWorld = SceneGraphUtilities.createFullSceneGraphComponent("world");
			int numSegs = 11;
			double[] radii = {.02, .01, .005, .0025};
			for (int i =0 ; i< levels.length; ++i)	{
				SceneGraphComponent tmp = hopfFibration(i, numSegs, radii[i]);
				theWorld.addChild(tmp);
				levels[i][0] = tmp.getChildComponent(0);
				System.out.println("Adding "+tmp.getName()+levels[i][0].getName());
				levels[i][1] = tmp.getChildComponent(1);				
				System.out.println("Adding "+tmp.getName()+levels[i][1].getName());
			}
			updateSceneGraph();
			//SceneGraphComponent icokit = SceneGraphUtilities.createFullSceneGraphComponent("ico");
			//icokit.setGeometry(ico);
			//icokit.getTransformation().setStretch(.2);
			//theWorld.addChild(icokit);
			return theWorld;
		}

		public void customize(JMenuBar theMenuBar, final Viewer viewer)	{
			//theMenuBar = super.createMenuBar(viewer);
			//theMenuBar = new JMenuBar(); //super.createMenuBar();
			JMenu testM = new JMenu("View");
			final JCheckBoxMenuItem jcm = new JCheckBoxMenuItem("Show tubes");
			jcm.setSelected(showTubes);
			testM.add(jcm);
			jcm.addActionListener( new ActionListener() {
				public void actionPerformed(ActionEvent e)	{
					showTubes = jcm.isSelected();
					System.out.println("Show tubes is "+showTubes);
					updateSceneGraph();
					viewer.render();
					viewer.getViewingComponent().requestFocus();
				}
			});
			for (int i = 0; i<showLevel.length; ++i)	{
				final JCheckBoxMenuItem jcx = new JCheckBoxMenuItem("Show level "+i);
				jcx.setSelected(showLevel[i]);
				testM.add(jcx);
				final int j = i;
				jcx.addActionListener( new ActionListener() {
					public void actionPerformed(ActionEvent e)	{
						showLevel[j] = jcx.isSelected();
						updateSceneGraph();
						viewer.render();
						viewer.getViewingComponent().requestFocus();
					}
				});				
			}
			theMenuBar.add(testM);
		}
		
		/**
		 * 
		 */
		protected void updateSceneGraph() {
			for (int i = 0; i < levels.length; ++i)	{
				if (showLevel[i]) 	{
					levels[i][0].setVisible(true);
					if (showTubes)  levels[i][1].setVisible(true);
					else levels[i][1].setVisible(false);
				} else	{
					levels[i][0].setVisible(false);
					levels[i][1].setVisible(false);
				}
			}
		}

		/**
		 * @param theWorld
		 * @return
		 */
		private SceneGraphComponent hopfFibration(int level, int numSegs, double radius) {
			if (level < 0) level = 0; 
			if (level > 3) level = 3;
			IndexedFaceSet ico = SphereHelper.tessellatedIcosahedra[level];
			int profileSize = 7;
			double[][] verts = ico.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
			int numCurves = verts.length;
			Quaternion[] baseCircle = new Quaternion[numSegs];
			Quaternion[][] baseTube = new Quaternion[numSegs][profileSize];
			double totalAngle = Math.PI * 2;
			double da = totalAngle/(numSegs-1.0);
			double scale = radius;
			for (int j = 0; j<numSegs; ++j)	{
				double angle = (j) * da;
				baseCircle[j] = new Quaternion(  Math.cos(angle), 0.0, 0.0,  Math.sin(angle));
				for (int k = 0; k < profileSize; ++k)	{
					double a = k * Math.PI/3.0;
					Quaternion smallCircleYZ = new Quaternion(Math.sqrt(1-scale*scale), Math.cos(a)*scale, Math.sin(a)*scale, 0.0);
					baseTube[j][k] = Quaternion.times(null, smallCircleYZ, baseCircle[j]);
				}
			}
			SceneGraphComponent node = new SceneGraphComponent();
			node.setName("Hopf fibration level "+level);
			SceneGraphComponent curves = new SceneGraphComponent();
			curves.setName("Curves");
			SceneGraphComponent tubes = new SceneGraphComponent();
			tubes.setName("Tubes");
			node.addChild(curves);
			node.addChild(tubes);
			Appearance ap = new Appearance();
			node.setAppearance( ap);
			ap.setAttribute(CommonAttributes.LIGHTING_ENABLED, false);
			ap.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.LINE_WIDTH, 0.5d);
			
			for (int i = 0; i<numCurves; ++i)  	{
				///if (verts[i][2] > 0.0) continue;
				double[][] oneCircle = new double[numSegs][4];
				double[][] oneTube = new double[numSegs*profileSize][4];
				// find phi and then halve it
				double xy = Math.sqrt(verts[i][0]*verts[i][0] + verts[i][1]*verts[i][1]);
				double phi = Math.atan2(verts[i][2], xy);
				// measure the angle from the north pole
				phi = Math.PI/2.0 - phi;
				phi = phi/2.0;
				// and convert back to traditional latitude
				phi = Math.PI/2.0 - phi;
				double x = Math.cos(phi) * verts[i][0]/ xy;
				double y = Math.cos(phi) * verts[i][1] / xy;
				double z = Math.sin(phi);
				Quaternion q = new Quaternion(0.0,x,y,z);
				//System.out.println("Vertex "+i+" is "+q.toString());
				Color c = new Color((float) (.5 + .5 *verts[i][0]), (float) (.5 + .5 *verts[i][1]), (float) (.5 + .5 *verts[i][2]),1.0f);
				for (int j = 0; j<numSegs; ++j)	{
					Quaternion product = Quaternion.times(null, q, baseCircle[j]);
					product.asDouble(oneCircle[j]);
					for (int k = 0; k<profileSize; ++k)	{
						product = Quaternion.times(null, q, baseTube[j][k]);
						product.asDouble(oneTube[j*profileSize+k]);
					}
				}
				IndexedLineSet dsc = IndexedLineSetUtility.createCurveFromPoints(oneCircle, false);
				SceneGraphComponent coreSGC = new SceneGraphComponent();
				coreSGC.setName("core curve"+i);
				coreSGC.setAppearance(new Appearance());
				coreSGC.getAppearance().setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, c );
				coreSGC.setGeometry(dsc);
				curves.addChild(coreSGC);
				QuadMeshShape qms = new QuadMeshShape(profileSize, numSegs, false, false);
				qms.setVertexAttributes(Attribute.COORDINATES, StorageModel.DOUBLE_ARRAY.array(4).createReadOnly(oneTube));
				GeometryUtility.calculateAndSetNormals(qms);
				SceneGraphComponent tubeSGC = new SceneGraphComponent();
				tubeSGC.setName("tube"+i);
				tubeSGC.setAppearance(new Appearance());
				tubeSGC.getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, c );
				tubeSGC.setGeometry(qms);
				tubes.addChild(tubeSGC);
				//theWorld.addChild(tubeSGC);
			}
			return node;
		}

		public int getSignature()	{
			return Pn.ELLIPTIC;
		}
		public boolean addBackPlane()	{
			return false;
		}
		public boolean isEncompass()	{
			return false;
		}

	}
