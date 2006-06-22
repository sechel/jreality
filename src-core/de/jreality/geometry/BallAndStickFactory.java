/*
 * Author	gunn
 * Created on Nov 14, 2005
 *
 */
package de.jreality.geometry;

import java.awt.Color;

import de.jreality.math.FactoredMatrix;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DataList;
import de.jreality.shader.CommonAttributes;

public class BallAndStickFactory {
	 IndexedLineSet ils;
	 double stickRadius=.025, ballRadius=.05;
	 Color stickColor = Color.YELLOW, ballColor=Color.GREEN, arrowColor = Color.RED;
	 int signature = Pn.EUCLIDEAN;
	 boolean showBalls = true, showSticks = true;
	 SceneGraphComponent theResult;
	 boolean drawArrows = false;
	 double arrowPosition = .5;		// where is tip of arrow placed?
	 double arrowScale = .1;			// scale=1:  height of cone is length of edge
	 double arrowSlope = 1.0;			// bigger scale: more pointy arrow profile
	 Appearance ballsAp, sticksAp, arrowsAp, topAp;
	 private static IndexedFaceSet urCone = null;
		public static double[][] octagonalCrossSection = {{1,0,-1}, 
			{.707, .707, -1}, 
			{0,1,-1},
			{-.707, .707, -1},
			{-1,0,-1},
			{-.707, -.707, -1},
			{0,-1,-1},
			{.707, -.707, -1},
			{1,0,-1}};
	 static {
		 urCone = Primitives.pyramid(octagonalCrossSection, new double[]{0,0,0});
		 GeometryUtility.calculateAndSetVertexNormals(urCone);
	 }

	public BallAndStickFactory(IndexedLineSet i)	{
		super();
		ils = i;
	 }

	 public void update()	{
		 	// create sticks on edges
			SceneGraphComponent sticks = new SceneGraphComponent();
			if (showSticks)	{
				if (sticksAp == null) sticksAp =  new Appearance();
				sticksAp.setAttribute(CommonAttributes.FACE_DRAW, true);
				sticksAp.setAttribute(CommonAttributes.EDGE_DRAW, false);
				sticksAp.setAttribute(CommonAttributes.VERTEX_DRAW, false);
				if (stickColor != null) sticksAp.setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, stickColor);
				sticks.setAppearance(sticksAp);
				if (arrowsAp == null) arrowsAp = new Appearance();
				arrowsAp.setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, arrowColor);
				DataList vertices = ils.getVertexAttributes(Attribute.COORDINATES);
				DataList edgeColors = ils.getEdgeAttributes(Attribute.COLORS);
				int n = ils.getNumEdges();
				for (int i = 0; i<n; ++i)	{
					int[] ed = ils.getEdgeAttributes(Attribute.INDICES).item(i).toIntArray(null);
					int m = ed.length;
					for (int j = 0; j<m-1; ++j)	{
						int k = ed[j];
						double[] p1 = vertices.item(k).toDoubleArray(null);	
						k = ed[j+1];
						double[] p2 = vertices.item(k).toDoubleArray(null);	
						SceneGraphComponent cc = TubeUtility.tubeOneEdge(p1, p2, stickRadius, null, signature);
						if (edgeColors != null) {
							Color ccc = null;
							double[] dcc = edgeColors.item(i).toDoubleArray(null);
							if (dcc.length == 4) ccc = new Color((float) dcc[0], (float) dcc[1], (float) dcc[2], (float) dcc[3]);
							else ccc = new Color((float) dcc[0], (float) dcc[1], (float) dcc[2]);
							Appearance ap = new Appearance();
							ap.setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, ccc);
							cc.setAppearance(ap);
						}
						if (cc != null) sticks.addChild(cc);
						if (drawArrows)		{
							FactoredMatrix arrowM = new FactoredMatrix(signature);
							double d;
							if (p1.length == 3) d = Rn.euclideanDistance(p1, p2);
							else d = Pn.distanceBetween(p1, p2, signature);
							double flatten = arrowSlope/(d);
							double stretch = arrowScale/stickRadius;
							arrowM.setStretch(stretch, stretch, arrowScale*flatten);
							arrowM.setTranslation(0,0,arrowPosition-.5);
							SceneGraphComponent arrow = new SceneGraphComponent();
							arrow.setAppearance(arrowsAp);
							arrowM.update();
							arrowM.assignTo(arrow);
							arrow.setGeometry(urCone);
							cc.addChild(arrow);
						}
					}
				}				
			}
			SceneGraphComponent balls = new SceneGraphComponent();
			if (showBalls)	{
				// we should allow the user to specify "real" balls, not via the appearance.
				balls.setGeometry(ils);
				if (ballsAp == null) ballsAp =  new Appearance();
				ballsAp.setAttribute(CommonAttributes.FACE_DRAW, false);
				ballsAp.setAttribute(CommonAttributes.EDGE_DRAW, false);
				ballsAp.setAttribute(CommonAttributes.VERTEX_DRAW, true);
				ballsAp.setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.SPHERES_DRAW, true);
				ballsAp.setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.POINT_RADIUS, ballRadius);
				if (ballColor != null) ballsAp.setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, ballColor);
				balls.setAppearance(ballsAp);				
			}
			if (topAp == null) topAp =  new Appearance();
			topAp.setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.SMOOTH_SHADING, true);
			theResult = new SceneGraphComponent();
			theResult.setAppearance(topAp);
			if (showSticks) theResult.addChild(sticks);
			if (showBalls) theResult.addChild(balls);
	 }
	 
	 public void setStickRadius(double r)	{
		 stickRadius = r;
	 }
	 
	public void setBallColor(Color ballColor) {
		this.ballColor = ballColor;
	}

	public void setBallRadius(double ballRadius) {
		this.ballRadius = ballRadius;
	}

	public void setStickColor(Color stickColor) {
		this.stickColor = stickColor;
	}

	public void setSignature(int signature) {
		this.signature = signature;
	}
	
	public SceneGraphComponent getSceneGraphComponent()	{
		return theResult;
	}

	protected static SceneGraphComponent sticks(IndexedLineSet ifs, double rad, int signature)	{
		SceneGraphComponent sgc = new SceneGraphComponent();
		DataList vertices = ifs.getVertexAttributes(Attribute.COORDINATES);
		int n = ifs.getNumEdges();
		for (int i = 0; i<n; ++i)	{
			int[] ed = ifs.getEdgeAttributes(Attribute.INDICES).item(i).toIntArray(null);
			int m = ed.length;
			for (int j = 0; j<m-1; ++j)	{
				int k = ed[j];
				double[] p1 = vertices.item(k).toDoubleArray(null);	
				k = ed[j+1];
				double[] p2 = vertices.item(k).toDoubleArray(null);	
				SceneGraphComponent cc = TubeUtility.tubeOneEdge(p1, p2, rad, null, signature);
				if (cc != null) sgc.addChild(cc);
			}
		}
		return sgc;
	}

	public double getArrowPosition() {
		return arrowPosition;
	}

	public void setArrowPosition(double arrowPosition) {
		this.arrowPosition = arrowPosition;
	}

	public double getArrowScale() {
		return arrowScale;
	}

	public void setArrowScale(double arrowScale) {
		this.arrowScale = arrowScale;
	}

	public double getArrowSlope() {
		return arrowSlope;
	}

	public void setArrowSlope(double arrowSlope) {
		this.arrowSlope = arrowSlope;
	}

	public boolean isDrawArrows() {
		return drawArrows;
	}

	public void setDrawArrows(boolean drawArrows) {
		this.drawArrows = drawArrows;
	}

	public IndexedLineSet getIls() {
		return ils;
	}

	public void setIls(IndexedLineSet ils) {
		this.ils = ils;
	}

	public boolean isShowBalls() {
		return showBalls;
	}

	public void setShowBalls(boolean showBalls) {
		this.showBalls = showBalls;
	}

	public boolean isShowSticks() {
		return showSticks;
	}

	public void setShowSticks(boolean showSticks) {
		this.showSticks = showSticks;
	}


}
