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
 * 
 * TO DO:
 * - determine default value of labelScale via bounding box of the component
 * - more factory attributes
 * - new tick shape
 * - grid
 * 
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
	
	private final double arrowHeight = 3;	
	private IndexedFaceSet urCone = null;
	{
		urCone = Primitives.pyramid(octagonalCrossSection(-arrowHeight), new double[]{0,0,0});
		//GeometryUtility.calculateAndSetVertexNormals(urCone);
	}
	
	int signature = Pn.EUCLIDEAN;
	
	private int currentClosestBoxVertex = -1;  //index of a currently closest box vertex in boxVertices[0] 
	
	private HashMap nodes = new HashMap();
	
	//attributes
	private double axisScale = 0.5;  //the distance between two ticks on an axis
	private double labelScale = 0.01;  //size of labels
	private double arrowStretch = 4*labelScale; //stretch of arrows of axes (octagonalCrossSection)
	private double tickStretch = 2*labelScale; //stretch of ticks of axes (octagonalCrossSection)
	private boolean showAxesArrows = true;  //show or hide arrows on axes
	private boolean showBoxArrows = false;  //show or hide arrows on box
	
	
	
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
	
	public void hideBox() {
		box.setVisible(false);
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
			
			for (int k=0; k<=3; k++) {
				
				//create SceneGraphComponent with children line, arrow, ticks
				SceneGraphComponent singleAxisK = new SceneGraphComponent();
				//assign binary value of k to the name of the SGC
				singleAxisK.setName(toBinaryString(k));

				nodes.put(axesNames[axis]+singleAxisK.getName(), singleAxisK);  //e.g. x00
				
				//create line with label
				SceneGraphComponent line = getAxisLine(axis, boxVertices[axis][2*k],boxVertices[axis][2*k+1], true);
				//create arrow
				SceneGraphComponent arrow = getAxisArrow(axis, boxVertices[axis][2*k],boxVertices[axis][2*k+1]);
				arrow.setVisible(showBoxArrows);
				nodes.put(axesNames[axis]+singleAxisK.getName()+"arrow", arrow);  //e.g. x00arrow
				//create ticks with labels
				SceneGraphComponent ticks = getAxisTicks(axis, boxVertices[axis][2*k],boxVertices[axis][2*k+1], true);
				nodes.put(axesNames[axis]+singleAxisK.getName()+"label", ticks);  //e.g. x00label
				
				singleAxisK.addChild(line);
				singleAxisK.addChild(arrow);
				singleAxisK.addChild(ticks);

				singleAxis.addChild(singleAxisK);
			}
			box.addChild(singleAxis);
		}

		
		//set appearance of box node
		Appearance app = new Appearance();
	    app.setAttribute(CommonAttributes.TUBES_DRAW, false);
	    app.setAttribute(CommonAttributes.EDGE_DRAW, true);
	    app.setAttribute(CommonAttributes.VERTEX_DRAW, true);  //show labels
	    app.setAttribute("pointShader.scale", labelScale);  //label scale
	    app.setAttribute("lineShader.scale", labelScale);  //label scale
	    app.setAttribute("pointShader.offset", new double[]{0.04,-0.07,0});  //label offset of ticks
	    app.setAttribute("lineShader.offset", new double[]{0,-.2,0});  //label offset of axes lines
		app.setAttribute(CommonAttributes.POINT_RADIUS, 0.001);  //don't show label points
		app.setAttribute(CommonAttributes.DIFFUSE_COLOR,Color.BLACK);
		app.setAttribute(CommonAttributes.SPECULAR_COLOR,Color.BLACK);
	    app.setAttribute(CommonAttributes.DEPTH_FUDGE_FACTOR, 1.0);
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
	
	public void hideAxes() {
		axes.setVisible(false);
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
			nodes.put(axesNames[axis] +"Axis", singleAxis);  //e.g. xAxis
			
			//create line with label
			SceneGraphComponent line = getAxisLine(axis, axesVertices[axis][0], axesVertices[axis][1], false);
			// create arrow
			SceneGraphComponent arrow = getAxisArrow(axis, axesVertices[axis][0], axesVertices[axis][1]);
			arrow.setVisible(showAxesArrows);
			nodes.put(axesNames[axis]+"Arrow", arrow);  //e.g. xArrow
			// create ticks with labels
			SceneGraphComponent ticks = getAxisTicks(axis, axesVertices[axis][0], axesVertices[axis][1], false);
			nodes.put(axesNames[axis]+"Label", ticks);  //e.g. xLabel
			
			singleAxis.addChild(line);
			singleAxis.addChild(arrow);
			singleAxis.addChild(ticks);
			
			axes.addChild(singleAxis);
		}
		
		//set appearance of axes node
		Appearance app = new Appearance();
	    app.setAttribute(CommonAttributes.TUBES_DRAW, false);
	    app.setAttribute(CommonAttributes.EDGE_DRAW, true);
	    app.setAttribute(CommonAttributes.VERTEX_DRAW, true);  //show labels
	    app.setAttribute("pointShader.scale", labelScale);  //label scale
		app.setAttribute("pointShader.offset", new double[]{0.04,-0.07,0});  //label offset
		app.setAttribute(CommonAttributes.POINT_RADIUS, 0.001);  //don't show label points
		app.setAttribute(CommonAttributes.DIFFUSE_COLOR,Color.BLACK);
		app.setAttribute(CommonAttributes.SPECULAR_COLOR,Color.BLACK);
	    app.setAttribute(CommonAttributes.DEPTH_FUDGE_FACTOR, 1.0);
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
	 * @return the arrow
	 */
	private SceneGraphComponent getAxisArrow(int axis, double[] min, double[] max) {
	
		SceneGraphComponent arrow = SceneGraphUtility.createFullSceneGraphComponent("arrow");
		arrow.setGeometry(urCone);
		//get rotation for axis
		//FactoredMatrix m = new FactoredMatrix(TubeUtility.tubeOneEdge(min, max, 0.025, null, signature).getTransformation());
		//above method results in incorrect translation
		FactoredMatrix m = new FactoredMatrix();
		m.setRotation(getRotation(axis));
		m.setStretch(arrowStretch); //stretch urCone
		//translate to axis tip
		m.setTranslation(max);
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
		final double maxLevel = axisScale*Math.floor( (max[axis]-arrowHeight*arrowStretch)/axisScale -0.5);  //give space for axis arrow and box corner
		
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
		m.setStretch(tickStretch, tickStretch, 1); //stretch ticks
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


	private String toBinaryString(int k) {
		if (k<2) return ("0"+k);
		else return Integer.toBinaryString(k);
	}
	
//-----------------------------------------------------------------------------------
//the following methods are intended to be used 
//to hide specific box vertices, axes or labels
//-----------------------------------------------------------------------------------
	
	/**
	 * get index of a box vertex which is "closest to the screen" when looking in a specified direction
	 * @param direction the direction
	 * @return the index of a closest box vertex in boxVertices[0]
	 */
	private int getClosestBoxVertex(double[] dir) {
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
		return closest;
	}
	
	
	/**
	 * specify a direction to look at in a viewer
	 * => hide closest box vertex (resp. only edges if there are more than one closest box vertices)
	 */
	public void updateBox(double[] direction) {
		
		//only do something if closest box vertex changed
		final int index = getClosestBoxVertex(direction);
		if (currentClosestBoxVertex == index) return;
		
		//set all vertices to visible
		for (int k=0; k<=3; k++) {
			getSGC("x"+toBinaryString(k)).setVisible(true);
			getSGC("y"+toBinaryString(k)).setVisible(true);
			getSGC("z"+toBinaryString(k)).setVisible(true);
		}
		
		currentClosestBoxVertex = index;
		double[] closest = boxVertices[0][currentClosestBoxVertex];
		int[] edgeCriteria = new int[3];
		
		//get the 3 edges belonging to a closest box vertex
		for (int i=0; i<=2; i++) {
			if (closest[i] == boxMin[i]) edgeCriteria[i] = 0;  //0 corresponds to vertex with minimum value on axis i
			else edgeCriteria[i] = 1;  //1 corresponds to vertex with maximum value on axis i 
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
//set and get attributes
//-----------------------------------------------------------------------------------
		
	public void setAxisScale(double axisScale) {
		if (this.axisScale == axisScale) return;
		//else
		this.axisScale = axisScale;

		//update ticks and labels
		for (int axis = 0; axis <= 2; axis++) { // for each coordinate axis

			//for box:
			for (int k = 0; k <= 3; k++) {
				//remove old ticks and labels
				SceneGraphComponent singleAxisK = getSGC(axesNames[axis]+ toBinaryString(k));  //e.g. x00
				singleAxisK.removeChild(getSGC(axesNames[axis]+ toBinaryString(k) + "label"));  //e.g. x00label
				//create new ticks with labels
				SceneGraphComponent ticks = getAxisTicks(axis, 
						boxVertices[axis][2 * k], boxVertices[axis][2 * k + 1], true);
				//update hash table
				nodes.put(axesNames[axis] + toBinaryString(k) + "label", ticks);  //e.g. x00label
				//add new ticks and labels to SceneGraph
				singleAxisK.addChild(ticks);
			}

			//for axes:

			//remove old ticks and labels
			SceneGraphComponent singleAxis = getSGC(axesNames[axis] +"Axis");  //e.g. xAxis
			singleAxis.removeChild(getSGC(axesNames[axis]+"Label"));  //e.g. xLabel
			//create new ticks with labels
			SceneGraphComponent ticks = getAxisTicks(axis,
					axesVertices[axis][0], axesVertices[axis][1], false);
			//update hash table
			nodes.put(axesNames[axis] + "Label", ticks);  //e.g. xLabel
			//add new ticks and labels to SceneGraph
			singleAxis.addChild(ticks);
		}
	}
	
	public double getAxisScale() {
		return axisScale;
	}
	
	
	public void setLabelScale(double labelScale) {
		if (this.labelScale == labelScale) return;
		//else
		this.labelScale = labelScale;
		
		box.getAppearance().setAttribute("pointShader.scale", labelScale);
		box.getAppearance().setAttribute("lineShader.scale", labelScale);
		axes.getAppearance().setAttribute("pointShader.scale", labelScale);
		
		//update size of arrows and ticks
		arrowStretch = 4*labelScale;
		tickStretch = 2*labelScale;
		for (int axis=0; axis<=2; axis++) {
			SceneGraphComponent arrow, ticks;
			FactoredMatrix m;

			//for box:
			for (int k=0; k<=3; k++) {
				arrow = getSGC(axesNames[axis]+toBinaryString(k)+"arrow");  //e.g. x00arrow
				m = new FactoredMatrix(arrow.getTransformation());
				m.setStretch(arrowStretch); //stretch urCone
				m.assignTo(arrow);
				ticks = getSGC(axesNames[axis]+toBinaryString(k)+"label");  //e.g. x00label
				m = new FactoredMatrix(ticks.getTransformation());
				m.setStretch(tickStretch, tickStretch, 1); //stretch ticks
				m.assignTo(ticks);
			}
			//for axes:
			arrow = getSGC(axesNames[axis]+"Arrow");  //e.g. xArrow
			m = new FactoredMatrix(arrow.getTransformation());
			m.setStretch(arrowStretch); //stretch urCone
			m.assignTo(arrow);
			ticks = getSGC(axesNames[axis]+"Label");  //e.g. xLabel
			m = new FactoredMatrix(ticks.getTransformation());
			m.setStretch(tickStretch, tickStretch, 1); //stretch ticks
			m.assignTo(ticks);
		}
		// recalculate tick levels since arrow size changed ???
	}
	
	public double getLabelScale() {
		return labelScale;
	}
	
	
	public void showAxesArrows() {
		if (showAxesArrows) return;
		showAxesArrows = true;
		//set visiblity of all arrows
		for (int axis=0; axis<=2; axis++)
			getSGC(axesNames[axis]+"Arrow").setVisible(true);
	}
	
	public void hideAxesArrows() {
		if (!showAxesArrows) return;
		showAxesArrows = false;
		//set visiblity of all arrows
		for (int axis=0; axis<=2; axis++)
			getSGC(axesNames[axis]+"Arrow").setVisible(false);
	}
	

	public void showBoxArrows() {
		if (showBoxArrows) return;
		showBoxArrows = true;
		//set visiblity of all arrows
		for (int axis=0; axis<=2; axis++) {
			for (int k=0; k<=3; k++) {
				getSGC(axesNames[axis]+toBinaryString(k)+"arrow").setVisible(true);
			}
		}
	}
	
	public void hideBoxArrows() {
		if (!showBoxArrows) return;
		showBoxArrows = false;
		//set visiblity of all arrows
		for (int axis=0; axis<=2; axis++) {
			for (int k=0; k<=3; k++) {
				getSGC(axesNames[axis]+toBinaryString(k)+"arrow").setVisible(false);
			}
		}
	}

}