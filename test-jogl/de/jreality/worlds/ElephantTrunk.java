/*
 * Created on Feb 23, 2004
 *
 */
package de.jreality.worlds;
import de.jreality.geometry.Primitives;
import de.jreality.scene.Appearance;
import de.jreality.scene.Geometry;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphNode;
import de.jreality.scene.Transformation;
import de.jreality.scene.event.TransformationEvent;
import de.jreality.scene.event.TransformationListener;
import de.jreality.util.ConfigurationAttributes;
import de.jreality.util.Pn;



/**
 * @author gunn
 *
 */
public class ElephantTrunk extends AbstractJOGLLoadableScene {
	SceneGraphComponent icokit;
	/**
	 * 
	 */
	public ElephantTrunk() {
		super();
		// TODO Auto-generated constructor stub
	}

		public SceneGraphComponent makeWorld()	{
			IndexedFaceSet ico = Primitives.coloredCube();
			icokit = new SceneGraphComponent();
			icokit.setName("theLeaf");
			icokit.setGeometry(ico);
			double a = 0.3;
			double[] tlate = {-a, 0.0, 0.0, 1.0};
			//double[] stretchV = {.5d, .5d, .5d};
			SceneGraphComponent theWorld = new SceneGraphComponent();
			theWorld.setName("theWorld");
			Transformation t = new Transformation();
			t.setRotation(0.1, 1d, 0d, 0d);
			t.setTranslation(tlate);
			t.setStretch(.9,.9,.9);
			IteratedTransform it = new IteratedTransform(t, 50, icokit);
			it.setName("iteratedTransform");
			//it.setIterationCount(40);
			//it.addChild(icokit);
			theWorld.addChild(it);
			theWorld.setAppearance(new Appearance());
			return theWorld;
		}
	
		/* (non-Javadoc)
		 * @see de.jreality.util.LoadableScene#isEncompass()
		 */
		public boolean isEncompass() {
			return true;
		}
		protected class IteratedTransform extends SceneGraphComponent implements TransformationListener {
			int iterationCount = 0;
			boolean countChanged = true;
			Transformation theT;
			SceneGraphNode geometry;
			/**
			 * 
			 */
			public IteratedTransform(Transformation t, int itcount, SceneGraphNode g) {
				super();
				theT = t;
				setTransformation(theT);
				theT.addTransformationListener(this);
		 		geometry = g;
				setIterationCount(itcount);
			}
			
			public IteratedTransform()	{
				this(new Transformation(), 1, Primitives.coloredCube());
			}

			/**
			 * @return
			 */
			public int getIterationCount() {
				return iterationCount;
			}

			/**
			 * @param i
			 */
			public void setIterationCount(int i) {
				if ( i == iterationCount) return;
				iterationCount = i;
				countChanged = true;
				update();
			}
			
			public void update()	{
				//Transformation theT = (Transformation) transforms.get(0);
				//if (theT == null)	theT = new Transformation();
				double[] theM = theT.getMatrix();
				if (countChanged) {
					SceneGraphComponent parent = this, child = null;
					for (int i = 0 ; i<iterationCount; ++i)	{
						child = new SceneGraphComponent();
						child.setName("itT"+i);
						child.setTransformation(theT);
						if (geometry instanceof Geometry) child.setGeometry((Geometry) geometry);
						else if (geometry instanceof SceneGraphComponent) child.addChild((SceneGraphComponent) geometry);
						//parent.removeChildren();
						parent.addChild(child);
						parent = child;
					}
					countChanged = false;
				} 
			}

				

			/* (non-Javadoc)
			 * @see de.jreality.scene.event.TransformationListener#transformationMatrixChanged(de.jreality.scene.event.TransformationEvent)
			 */
			public void transformationMatrixChanged(TransformationEvent ev) {
				update();

			}

		}
	
}
