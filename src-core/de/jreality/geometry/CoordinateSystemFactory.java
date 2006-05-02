package de.jreality.geometry;

import java.awt.Color;
import java.util.HashMap;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Transformation;
import de.jreality.geometry.Primitives;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.SceneGraphUtility;
import de.jreality.math.FactoredMatrix;
import de.jreality.math.Matrix;
import de.jreality.math.Quaternion;
import de.jreality.math.Rn;
import de.jreality.math.Pn;


/**
 * Represents a coordinate system in Euclidean space and is
 * either created for an existing SceneGraphComponent or for a given extent. 
 *  
 * @author msommer
 */
public class CoordinateSystemFactory {

	
	
	private double[] boxMin, boxMax;
	
	private double[][][] axesVertices, boxVertices;
	private SceneGraphComponent box, axes;
	
	private final String[] axesNames = {"x", "y", "z"};
	
	private double[][] octagonalCrossSection = {
			{1,0,0}, 
			{.707, .707, 0}, 
			{0,1,0},
			{-.707, .707, 0},
			{-1,0,0},
			{-.707, -.707, 0},
			{0,-1,0},
			{.707, -.707, 0},
			{1,0,0}};
	
	private IndexedFaceSet urCone = null;
	{
		urCone = Primitives.pyramid(octagonalCrossSection, new double[]{0,0,3});
		//GeometryUtility.calculateAndSetVertexNormals(urCone);
	}
	
	int signature = Pn.EUCLIDEAN;
	
	private final double urStretch = 0.02; //stretch of arrows and ticks of axes (octagonalCrossSection)
	
	private HashMap nodes = new HashMap();
	private HashMap attributes = new HashMap();
	
	//attributes
	private double axisScale;  //the distance between two ticks on an axis
	private boolean axesArrows, boxArrows;  //show or hide arrows on axes and box
	
	
	
	/**
	 * coordinate system with extent 4
	 */
	public CoordinateSystemFactory() {
		this(4);
	}
	
	
	/**
	 * coordinate system with given extent
	 * @param extent the extent of the coordinate system
	 */
	public CoordinateSystemFactory(int extent) {
		//To DO: validate extent
		
		initAttributes();
		
		boxMin = new double[]{-extent, -extent, -extent};
		boxMax = new double[]{ extent,  extent,  extent};
		
		box = calculateBox();
		box.setVisible(false);
		axes = calculateAxes();
		axes.setVisible(false);
	}
	
	
	/**
	 * coordinate system for an existing SceneGraphComponent
	 * @param component the SceneGraphComponent specifying the extent of the coordinate system
	 */
	public CoordinateSystemFactory(SceneGraphComponent component) {
		
		initAttributes();
		
		//need to calculate bounding box without transformation of component
		Transformation tmp = component.getTransformation();
		component.setTransformation(new Transformation());
				
		//get boundingbox from componment
		double[][] minMax = GeometryUtility.calculateBoundingBox(component).getBounds();
		this.boxMin = minMax[0];
		this.boxMax = minMax[1];
		
		//enlarge box if graphic is 2d
		for (int axis=0; axis<=2; axis++) {
			if (boxMin[axis] == boxMax[axis]) {
				boxMin[axis] -= 0.5;
				boxMax[axis] += 0.5;
			}
		}
		
		box = calculateBox();
		box.setVisible(false);
		axes = calculateAxes();
		axes.setVisible(false);
		component.addChild(box);
		component.addChild(axes);
		component.setTransformation(tmp);
	}

		
	/**
	 * show the bounding box of the coordinate system, which is specified by the choice of the constructor
	 * (either by an existing SceneGraphComponent or by a given extent)
	 */
	public void displayBox() {
		box.setVisible(true);
	}
	
		
	private SceneGraphComponent calculateBox() {
		
		calculateBoxVertices();
		
		//create SceneGraphComponent which has each coordinate axis as its child
		SceneGraphComponent box = new SceneGraphComponent();
		box.setName("Box");
		//hashMap.put("box", box);
		
		for (int axis=0; axis<=2; axis++) {  //for each coordinate axis
			
			SceneGraphComponent singleAxis = new SceneGraphComponent();
			singleAxis.setName(axesNames[axis] +"-axis");
			nodes.put(axesNames[axis] +"Box", singleAxis);  //e.g. xBox
			
			for (int k=0; k<4; k++) {
				
				//create SceneGraphComponent with children line, arrow, ticks
				SceneGraphComponent singleAxisK = new SceneGraphComponent();
				//assign binary value of k to the name of the SGC
				if (k<2) singleAxisK.setName("0"+k);
				else singleAxisK.setName(Integer.toBinaryString(k));

				nodes.put(axesNames[axis]+singleAxisK.getName(), singleAxisK);  //e.g. x00
				
				//create line with label
				SceneGraphComponent line = getAxisLine(axis, boxVertices[axis][2*k],boxVertices[axis][2*k+1], true);
				//create arrow
				//SceneGraphComponent arrow = getAxisArrow(axis, boxVertices[axis][2*k],boxVertices[axis][2*k+1], true);
				//create ticks with labels
				SceneGraphComponent ticks = getAxisTicks(axis, boxVertices[axis][2*k],boxVertices[axis][2*k+1], true);
				nodes.put(axesNames[axis]+singleAxisK.getName()+"label", ticks);  //e.g. x00label
				
				singleAxisK.addChild(line);
				//singleAxisK.addChild(arrow);
				singleAxisK.addChild(ticks);

				singleAxis.addChild(singleAxisK);
			}
			box.addChild(singleAxis);
		}

		
		//set appearance of box node
		Appearance app = new Appearance();
	    app.setAttribute("tubeDraw", false);
	    app.setAttribute(CommonAttributes.VERTEX_DRAW, true);  //show labels
	    //app.setAttribute("pointShader.diffuseColor",Color.BLACK);
	    //app.setAttribute("pointShader.specularColor",Color.BLACK);
	    app.setAttribute("pointShader.scale", .01);  //label scale
	    app.setAttribute("lineShader.scale", .01);  //label scale
	    app.setAttribute("pointShader.offset", new double[]{0.04,-0.07,0});  //label offset of ticks
	    app.setAttribute("lineShader.offset", new double[]{0,-.2,0});  //label offset of axes lines
		app.setAttribute("pointRadius",0.001);  //don't show label points
		//app.setAttribute("lineShader.diffuseColor",Color.BLACK);
		//app.setAttribute("polygonShader.diffuseColor",Color.BLACK);
		//app.setAttribute("polygonShader.specularColor",Color.BLACK);
		app.setAttribute("diffuseColor",Color.BLACK);
		app.setAttribute("specularColor",Color.BLACK);
	    app.setAttribute("depthFudgeFactor", 1.0);
	    box.setAppearance(app);
		
		return box;
	}
	
	
	/**
	 * display the axes of the coordinate system, whose extent is specified by the choice of the constructor
	 * (either by an existing SceneGraphComponent or by a given extent)
	 */
	public void displayAxes() {
		axes.setVisible(true);
	}
	
	
	private SceneGraphComponent calculateAxes() {

		calculateAxesVertices();
		
		//create SceneGraphComponent which has each coordinate axis as its child
		SceneGraphComponent axes = new SceneGraphComponent();
		axes.setName("Axes");
		
		for (int axis=0; axis<=2; axis++) {  //for each coordinate axis

			//create SceneGraphComponent with children line, arrow, ticks
			SceneGraphComponent singleAxis = new SceneGraphComponent();
			singleAxis.setName(axesNames[axis] +"-axis");
			
			//create line with label
			SceneGraphComponent line = getAxisLine(axis, axesVertices[axis][0], axesVertices[axis][1], false);
			// create arrow
			SceneGraphComponent arrow = getAxisArrow(axis, axesVertices[axis][0], axesVertices[axis][1], false);
			// create ticks with labels
			SceneGraphComponent ticks = getAxisTicks(axis, axesVertices[axis][0], axesVertices[axis][1], false);
				
			singleAxis.addChild(line);
			singleAxis.addChild(arrow);
			singleAxis.addChild(ticks);
			
			axes.addChild(singleAxis);
		}
		
		//set appearance of axes node
		Appearance app = new Appearance();
	    app.setAttribute("tubeDraw", false);
	    app.setAttribute(CommonAttributes.VERTEX_DRAW, true);  //show labels
	    //app.setAttribute("pointShader.diffuseColor",Color.BLACK);
	    //app.setAttribute("pointShader.specularColor",Color.BLACK);
	    app.setAttribute("pointShader.scale", .01);  //label scale
		app.setAttribute("pointShader.offset", new double[]{0.04,-0.07,0});  //label offset
		app.setAttribute("pointRadius",0.001);  //don't show label points
		//app.setAttribute("lineShader.diffuseColor",Color.BLACK);
	    //app.setAttribute("polygonShader.diffuseColor",Color.BLACK);
	    //app.setAttribute("polygonShader.specularColor",Color.BLACK);
		app.setAttribute("diffuseColor",Color.BLACK);
		app.setAttribute("specularColor",Color.BLACK);
	    app.setAttribute("depthFudgeFactor", 1.0);
		axes.setAppearance(app);
		
		return axes;
	}
	

	/**
	 * calculate the vertices of the axes (specified by the choice of the constructor)
 	 * first index specifies the coordinate axis (0,1,2)
	 * second index specifies starting and endpoint of each coordinate axis {start, end}
	 */
	private void calculateAxesVertices(){
		
		this.axesVertices = new double[][][] {
			{{Math.min(boxMin[0]-0.5, 0),0,0},
			 {Math.max(boxMax[0]+0.5, 0),0,0}},
			{{0,Math.min(boxMin[1]-0.5, 0),0},
			 {0,Math.max(boxMax[1]+0.5, 0),0}},
			{{0,0,Math.min(boxMin[2]-0.5, 0)},
			 {0,0,Math.max(boxMax[2]+0.5, 0)}}
		};
	}
	
		
	/**
	 * calculate the vertices of the bounding box (specified by the choice of the constructor)
	 * first index specifies the coordinate axis (0,1,2)
	 * second index specifies starting and endpoint for each of the 4 copies of each coordinate axis {start1, end1, start2, end2,...}
	 */
	private void calculateBoxVertices(){
		
		this.boxVertices = new double[][][] {
			{boxMin, {boxMax[0], boxMin[1], boxMin[2]},  //04
			 {boxMin[0], boxMin[1], boxMax[2]}, {boxMax[0], boxMin[1], boxMax[2]},  //37
			 {boxMin[0], boxMax[1], boxMin[2]}, {boxMax[0], boxMax[1], boxMin[2]},  //15
			 {boxMin[0], boxMax[1], boxMax[2]}, boxMax  //26
			},
			{boxMin, {boxMin[0], boxMax[1], boxMin[2]},  //01
			 {boxMin[0], boxMin[1], boxMax[2]}, {boxMin[0], boxMax[1], boxMax[2]},  //32
			 {boxMax[0], boxMin[1], boxMin[2]}, {boxMax[0], boxMax[1], boxMin[2]},  //45
			 {boxMax[0], boxMin[1], boxMax[2]}, boxMax  //76
			},
			{boxMin, {boxMin[0], boxMin[1], boxMax[2]},  //03
			 {boxMin[0], boxMax[1], boxMin[2]}, {boxMin[0], boxMax[1], boxMax[2]},  //12
			 {boxMax[0], boxMin[1], boxMin[2]}, {boxMax[0], boxMin[1], boxMax[2]},  //47
			 {boxMax[0], boxMax[1], boxMin[2]}, boxMax  //56			 
			}
		}; //note that the ordering of the copies of each coordinate axis is significant

	//  0    boxMin
	//	1	{boxMin[0], boxMax[1], boxMin[2]}
	//	2	{boxMin[0], boxMax[1], boxMax[2]}    1   ---   5     y
	//	3	{boxMin[0], boxMin[1], boxMax[2]}  2   ---   6       |_ x
	//	4	{boxMax[0], boxMin[1], boxMin[2]}    0   - -   4     /
	//	5	{boxMax[0], boxMax[1], boxMin[2]}  3   ---   7      z
	//	6	 boxMax
	//	7	{boxMax[0], boxMin[1], boxMax[2]}
	}	
	

	/**
	 * get the line for the coordinate axis specified by <code>axis</code> 
	 * between min and max as a SceneGraphComponent (IndexedLineSet)
	 * (the line thru min and max has to be parallel to the coordinate axis)
	 * @param axis the coordinate axis (0,1,2)
	 * @param min the starting point of the line
	 * @param max the endpoint of the line
	 * @param forBox is the line intended for a box (line is labelled differently then)
	 * @return the line
	 */
	private SceneGraphComponent getAxisLine(int axis, double[] min, double[] max, boolean forBox) {
		//line through min and max has to be parallel to the coordinate axis specified by axis 
	
		SceneGraphComponent line = SceneGraphUtility.createFullSceneGraphComponent("line");
		IndexedLineSetFactory lineLSF = new IndexedLineSetFactory();
		lineLSF.setVertexCount(2);
		lineLSF.setLineCount(1);
		lineLSF.setVertexCoordinates(new double[][]{min, max});
		lineLSF.setEdgeIndices(new int[]{0,1});
		
		//create line label
		if (forBox) lineLSF.setEdgeLabels(new String[]{axesNames[axis]});
		else {
			PointSetFactory labelPSF = new PointSetFactory();
			labelPSF.setVertexCount(1);
			labelPSF.setVertexCoordinates(max);
			labelPSF.setVertexLabels(new String[]{axesNames[axis]});
			labelPSF.update();
			SceneGraphComponent label = SceneGraphUtility.createFullSceneGraphComponent("label");
			label.setGeometry(labelPSF.getPointSet());
			Appearance a = new Appearance();  //for label offset
			a.setAttribute("pointShader.offset", new double[]{.15,0,0});
			line.setAppearance(a);
			line.addChild(label);
		}
		lineLSF.update();
		line.setGeometry(lineLSF.getIndexedLineSet());
		return line;
	}
		
		
	/**
	 * get the arrow for the coordinate axis specified by <code>axis</code> 
	 * between min and max as a SceneGraphComponent (IndexedFaceSet)
	 * @param axis the coordinate axis (0,1,2)
	 * @param min the starting point of the line
	 * @param max the endpoint of the line
	 * @param forBox is the arrow intended for a box (no difference as of yet)
	 * @return the arrow
	 */
	private SceneGraphComponent getAxisArrow(int axis, double[] min, double[] max, boolean forBox) {
	
		SceneGraphComponent arrow = SceneGraphUtility.createFullSceneGraphComponent("arrow");
		arrow.setGeometry(urCone);
		//get rotation for axis
		//FactoredMatrix m = new FactoredMatrix(TubeUtility.tubeOneEdge(min, max, 0.025, null, signature).getTransformation());
		//above method results in incorrect translation
		FactoredMatrix m = new FactoredMatrix();
		m.setRotation(getRotation(axis));
		m.setStretch(2*urStretch); //stretch urCone
		m.setTranslation(max); //translate to axis tip
		m.assignTo(arrow);
		
		return arrow;
	}
		
	
	/**
	 * get the ticks on the coordinate axis specified by <code>axis</code> 
	 * between min and max as a SceneGraphComponent (IndexedFaceSet)
	 * @param axis the coordinate axis (0,1,2)
	 * @param min the starting point of the line
	 * @param max the endpoint of the line
	 * @param forBox are the ticks intended for a box ((0,0,0) is included in ticks then)
	 * @return the ticks
	 */
	private SceneGraphComponent getAxisTicks(int axis, double[] min, double[] max, boolean forBox) {
		
		//create the ticks on a line in z-direction
		//determine minimum and maximum value of the tick level
		final double minLevel = axisScale*Math.ceil( min[axis]/axisScale + 0.5);  //give space for box corner
		final double maxLevel = axisScale*Math.floor( (max[axis]-3*urStretch)/axisScale -0.5);  //give space for axis arrow and box corner
		
		//if (minLevel>maxLevel) return SceneGraphUtility.createFullSceneGraphComponent("ticks");
		
		IndexedFaceSet ticksIFS = Primitives.pyramid(octagonalCrossSection(minLevel), new double[]{0,0,minLevel});  //init
		for (double level=minLevel+axisScale; level<=maxLevel; level+=axisScale) {
			if (!forBox && Math.abs(level)<axisScale/2) continue;  //no tick at origin (there level may not be exactly 0)
			ticksIFS = IndexedFaceSetUtility.mergeIndexedFaceSets(
					new IndexedFaceSet[]{ ticksIFS, 
					Primitives.pyramid(octagonalCrossSection(level), new double[]{0,0,level}) });
		}
		//GeometryUtility.calculateAndSetVertexNormals(ticksIFS);
		
		//create labels
		final int numOfTicks = ticksIFS.getNumPoints()/10;  //each tick has 10 points
		PointSetFactory labelPSF = new PointSetFactory();
		labelPSF.setVertexCount(numOfTicks);
		double[][] labelPoints = new double[numOfTicks][];
		String[] labelStr = new String[numOfTicks];
		double level = minLevel;
		for (int i=0; i<numOfTicks; i++, level+=axisScale) {
			if (!forBox && Math.abs(level)<axisScale/2) level+=axisScale;  //skip tick at origin (there level may not be exactly 0)
			labelPoints[i] = new double[]{0, 0, level};
			labelStr[i] = Math.round(level*1000)/1000. + "";  //3 decimal places
		}
		labelPSF.setVertexCoordinates(labelPoints);
		labelPSF.setVertexLabels(labelStr);
		labelPSF.update();
		SceneGraphComponent labels = SceneGraphUtility.createFullSceneGraphComponent("labels");
		labels.setGeometry(labelPSF.getPointSet());
		
		//create the SceneGraphComponent and rotate the ticks onto the corresponding coordinate axis
		SceneGraphComponent ticks = SceneGraphUtility.createFullSceneGraphComponent("ticks");
		ticks.setGeometry(ticksIFS);
		//FactoredMatrix m = new FactoredMatrix(TubeUtility.tubeOneEdge(min, max, 0.025, null, signature).getTransformation());
		//above method results in incorrect translation
		FactoredMatrix m = new FactoredMatrix();
		m.setRotation(getRotation(axis));
		double[] translation = (double[])min.clone();
		translation[axis] = 0;
		m.setTranslation(translation);
		m.setStretch(urStretch, urStretch, 1); //stretch ticks
		m.assignTo(ticks);
		
		ticks.addChild(labels);
		return ticks;
	}
	
	
	/**
	 * get the octagonalCrossSection on a different level than 0
	 * @param level the level of the octagonalCrossSection (z-value) 
	 */
	private double[][] octagonalCrossSection(double level) {
		
		double[][] octagonalCrossSection = this.octagonalCrossSection; 
		for (int i=0; i<octagonalCrossSection.length; i++)
			octagonalCrossSection[i][2] = level;
		return octagonalCrossSection;
	}
	
	
	private Quaternion getRotation(int axis) {
	
		FactoredMatrix m = new FactoredMatrix();
		switch(axis) {
		case 0 : m.setRotation(Math.PI/2,0,1,0); break;
		case 1 : m.setRotation(-Math.PI/2,1,0,0);
		}
		return Quaternion.rotationMatrixToQuaternion(new Quaternion(), m.getArray());
	}
	
	
	/**
	 * returns the SGC to which the specified key is mapped in hashMap
	 * @param key the key specifying the SGC
	 * @return the SGC 
	 */
	private SceneGraphComponent getSGC(Object key) {
		return (SceneGraphComponent)nodes.get(key);
	}


	
//-----------------------------------------------------------------------------------
//the following methods are intended to be used 
//to hide specific box vertices, axes or labels
//-----------------------------------------------------------------------------------
	
	/**
	 * get a box vertex which is "closest to the screen" when looking in a specified direction
	 * @param direction the direction
	 * @return a closest box vertex
	 */
	private double[] getClosestBoxVertex(double[] dir) {
		double[] direction;
		if (dir.length==3) direction=dir;
		else {
			direction=new double[3];
			direction[0]=dir[0]/dir[3];
			direction[1]=dir[1]/dir[3];
			direction[2]=dir[2]/dir[3];
		}
		//closest box vertex has minimal inner product with direction
		int closest = 0;
		
		double tmp = Rn.innerProduct(boxVertices[0][closest], direction);
		
		for (int k=1; k<8; k++) {
			if ( Rn.innerProduct(boxVertices[0][k], direction) < tmp) {
				closest = k;
				tmp = Rn.innerProduct(boxVertices[0][k], direction);
			}
		}
		return boxVertices[0][closest];
	}
	
	
	/**
	 * specify a direction to look at in a viewer
	 * => hide closest box vertex (or edges if there are more than one closest box vertices)
	 */
	public void updateBox(double[] direction) {
		
		//set all vertices to visible
		for (int i=0; i<=1; i++)
			for (int j=0; j<=1; j++) {
				getSGC("x"+i+j).setVisible(true);
				getSGC("y"+i+j).setVisible(true);
				getSGC("z"+i+j).setVisible(true);
			}
				
		double[] closest = getClosestBoxVertex(direction);
		int[] edgeCriteria = new int[3];
		
		//get the 3 edges belonging to a closest box vertex
		for (int i=0; i<=2; i++) {
			if (closest[i] == boxMin[i]) edgeCriteria[i] = 0; 
			else edgeCriteria[i] = 1; 
		}
		//set those edges invisible which don't have copies of same "distance to the screen"
		if (direction[1]!=0 && direction[2]!=0)
			getSGC("x" + edgeCriteria[1] + edgeCriteria[2]).setVisible(false);
		if (direction[0]!=0 && direction[2]!=0)
			getSGC("y" + edgeCriteria[0] + edgeCriteria[2]).setVisible(false);
		if (direction[0]!=0 && direction[1]!=0)
			getSGC("z" + edgeCriteria[0] + edgeCriteria[1]).setVisible(false);
	}
	

	/**
	 * calculate direction of view via the paths of camera and the component (object)
	 * uses method updateBox(direction)
	 */
	public void updateBox(SceneGraphPath objectPath, SceneGraphPath cameraPath) {
		
		//direction of view in camera coordinates is (0,0,-1)
		//transform camera coordinates to local coordinates
		Matrix cameraTrans = new Matrix( cameraPath.getMatrix(null) );
		Matrix objectTrans = new Matrix( objectPath.getMatrix(null) );
		Matrix c2oTrans = new Matrix( Matrix.product( cameraTrans.getInverse(), objectTrans )); 
		double[] direction = c2oTrans.multiplyVector(new double[]{0,0,-1, 0});
		direction[3]=1;		
		//for (int i=0; i<=2; i++) System.out.println(direction[i]);
		
		updateBox(direction);
	}
	
	
	
//-----------------------------------------------------------------------------------
//for setting attributes
//-----------------------------------------------------------------------------------
		
	
	public void setAttribute(String key, Object value) {
		
		attributes.put(key, value);
		axisScale = ((Double)attributes.get("axisScale")).doubleValue();
		axesArrows = ((Boolean)attributes.get("axesArrows")).booleanValue();
		boxArrows = ((Boolean)attributes.get("boxArrows")).booleanValue();
	}

	public void setAttribute(String key, double value) {
		setAttribute(key, new Double(value));
	}

	public void setAttribute(String key, float value) {
		setAttribute(key, new Float(value));
	}

	public void setAttribute(String key, int value) {
		setAttribute(key, new Integer(value));
	}

	public void setAttribute(String key, long value) {
		setAttribute(key, new Long(value));
	}

	public void setAttribute(String key, boolean value) {
		setAttribute(key, Boolean.valueOf(value));
	}

	public void setAttribute(String key, char value) {
		setAttribute(key, new Character(value));
	}
	
	private void initAttributes() {
		attributes.put("axesArrows", new Boolean(true));
		attributes.put("boxArrows", new Boolean(false));
		
		setAttribute("axisScale", 0.5);  //global variables are set in this method
	}
}