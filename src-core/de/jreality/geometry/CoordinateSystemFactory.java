package de.jreality.geometry;

import java.awt.Color;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.Appearance;
import de.jreality.scene.PointSet;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.IntArrayArray;
import de.jreality.scene.data.StorageModel;
import de.jreality.geometry.Primitives;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.SceneGraphUtility;
import de.jreality.math.FactoredMatrix;
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
	
	private final String[] axesLabels = {
			"x-axis", "y-axis", "z-axis"};
	
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
		GeometryUtility.calculateAndSetVertexNormals(urCone);
	}
	
	int signature = Pn.EUCLIDEAN;
	
	private final double urStretch = 0.02; //stretch of arrows and marks of axes (octagonalCrossSection)
	private final double markScale = 0.5;  //the distance between two marks on an axis
	
	
	
	
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
		
		this.boxMin = new double[]{-extent, -extent, -extent};
		this.boxMax = new double[]{ extent,  extent,  extent};
		update();
	}
	
	
	/**
	 * coordinate system for an existing SceneGraphComponent
	 * @param component the SceneGraphComponent specifying the extent of the coordinate system
	 */
	public CoordinateSystemFactory(SceneGraphComponent component) {
	
		//get boundingbox from componment
		double[][] minMax = GeometryUtility.calculateBoundingBox(component).getBounds();
		this.boxMin = minMax[0];
		this.boxMax = minMax[1];
		update();
	}

	
	/**
	 * re-calculate the vertices of axes and bounding box
	 */
	private void update() {
		calculateBoxVertices();
		calculateAxesVertices();
	}
	
	
	/**
	 * get the bounding box of the coordinate system, which is specified by the choice of the constructor
	 * (either by an existing SceneGraphComponent or by a given extent)
	 * @return the bounding box
	 */
	public SceneGraphComponent getBox() {
		
		//create SceneGraphComponent which has each coordinate axis as its child
		SceneGraphComponent box = new SceneGraphComponent();
		box.setName("Box");
		
		for (int axis=0; axis<=2; axis++) {  //for each coordinate axis
			
			SceneGraphComponent singleAxis = new SceneGraphComponent();
			singleAxis.setName(axesLabels[axis]);
			
			for (int k=0; k<4; k++) {
				
				//create SceneGraphComponent with children line, arrow, marks
				SceneGraphComponent singleAxisK = new SceneGraphComponent();
				singleAxisK.setName(k+"");

				//create line with label
				SceneGraphComponent line = getAxisLine(axis, boxVertices[axis][2*k],boxVertices[axis][2*k+1], true);
				//create arrow
				//SceneGraphComponent arrow = getAxisArrow(axis, boxVertices[axis][2*k],boxVertices[axis][2*k+1], true);
				//create marks with labels
				SceneGraphComponent marks = getAxisMarks(axis, boxVertices[axis][2*k],boxVertices[axis][2*k+1], true);

				singleAxisK.addChild(line);
				//singleAxisK.addChild(arrow);
				singleAxisK.addChild(marks);

				singleAxis.addChild(singleAxisK);
			}
			box.addChild(singleAxis);
		}
		
		//set appearance of box node
		Appearance app = new Appearance();
	    app.setAttribute("tubeDraw", false);
	    app.setAttribute(CommonAttributes.VERTEX_DRAW, true);  //show labels
	    app.setAttribute("pointShader.diffuseColor",Color.BLACK);
	    app.setAttribute("pointShader.specularColor",Color.BLACK);
	    app.setAttribute("pointShader.scale", .01);  //label scale
	    app.setAttribute("lineShader.scale", .01);  //label scale
	    app.setAttribute("pointShader.offset", new double[]{0.04,-0.07,0});  //label offset of marks
	    app.setAttribute("lineShader.offset", new double[]{0,-.2,0});  //label offset of axes lines
	    //app.setAttribute("pointShader.diffuseColor",Color.RED);
		app.setAttribute("pointRadius",0.001);  //don't show label points
		app.setAttribute("lineShader.diffuseColor",Color.BLACK);
	    app.setAttribute("polygonShader.diffuseColor",Color.BLACK);
	    app.setAttribute("polygonShader.specularColor",Color.BLACK);
	    box.setAppearance(app);
		
		return box;
	}
	
	
	/**
	 * get the axes of the coordinate system, whose extent is specified by the choice of the constructor
	 * (either by an existing SceneGraphComponent or by a given extent)
	 * @return the axes
	 */
	public SceneGraphComponent getAxes() {

		//create SceneGraphComponent which has each coordinate axis as its child
		SceneGraphComponent axes = new SceneGraphComponent();
		axes.setName("Axes");
		
		for (int axis=0; axis<=2; axis++) {  //for each coordinate axis

			//create SceneGraphComponent with children line, arrow, marks
			SceneGraphComponent singleAxis = new SceneGraphComponent();
			singleAxis.setName(axesLabels[axis]);
			
			//create line with label
			SceneGraphComponent line = getAxisLine(axis, axesVertices[axis][0], axesVertices[axis][1], false);
			// create arrow
			SceneGraphComponent arrow = getAxisArrow(axis, axesVertices[axis][0], axesVertices[axis][1], false);
			// create marks with labels
			SceneGraphComponent marks = getAxisMarks(axis, axesVertices[axis][0], axesVertices[axis][1], false);
				
			singleAxis.addChild(line);
			singleAxis.addChild(arrow);
			singleAxis.addChild(marks);
			
			axes.addChild(singleAxis);
		}
		
		//set appearance of axes node
		Appearance app = new Appearance();
	    app.setAttribute("tubeDraw", false);
	    app.setAttribute(CommonAttributes.VERTEX_DRAW, true);  //show labels
	    app.setAttribute("pointShader.diffuseColor",Color.BLACK);
	    app.setAttribute("pointShader.specularColor",Color.BLACK);
	    app.setAttribute("pointShader.scale", .01);  //label scale
		app.setAttribute("pointShader.offset", new double[]{0.04,-0.07,0});  //label offset
	    //app.setAttribute("pointShader.diffuseColor",Color.RED);
		app.setAttribute("pointRadius",0.001);  //don't show label points
		app.setAttribute("lineShader.diffuseColor",Color.BLACK);
	    app.setAttribute("polygonShader.diffuseColor",Color.BLACK);
	    app.setAttribute("polygonShader.specularColor",Color.BLACK);
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
			{{Math.min(boxMin[0], 0)-1,0,0},
			 {Math.max(boxMax[0], 0)+1,0,0}},
			{{0,Math.min(boxMin[1]-1, 0),0},
			 {0,Math.max(boxMax[1]+1, 0),0}},
			{{0,0,Math.min(boxMin[2]-1, 0)},
			 {0,0,Math.max(boxMax[2]+1, 0)}}
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
			 {boxMin[0], boxMax[1], boxMin[2]}, {boxMax[0], boxMax[1], boxMin[2]},  //15
			 {boxMin[0], boxMax[1], boxMax[2]}, boxMax,  //26
			 {boxMin[0], boxMin[1], boxMax[2]}, {boxMax[0], boxMin[1], boxMax[2]}  //37
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
		};	

	//  0    boxMin
	//	1	{boxMin[0], boxMax[1], boxMin[2]}
	//	2	{boxMin[0], boxMax[1], boxMax[2]}    1   ---   5
	//	3	{boxMin[0], boxMin[1], boxMax[2]}  2   ---   6
	//	4	{boxMax[0], boxMin[1], boxMin[2]}    0   - -   4
	//	5	{boxMax[0], boxMax[1], boxMin[2]}  3   ---   7
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
		IndexedLineSet lineSet = new IndexedLineSet(2,1);
		lineSet.setVertexAttributes(Attribute.COORDINATES, StorageModel.DOUBLE_ARRAY.array(3).createReadOnly(new double[][]{min, max}));
		lineSet.setEdgeAttributes(Attribute.INDICES, new IntArrayArray.Array(new int[][]{{0,1}}));
		line.setGeometry(lineSet);
		//create line label
		if (forBox) lineSet.setEdgeAttributes(Attribute.LABELS, StorageModel.STRING_ARRAY.createReadOnly(new String[]{axesLabels[axis]}));
		else {
			PointSet labelPS = new PointSet(1);
			labelPS.setVertexAttributes(Attribute.COORDINATES, StorageModel.DOUBLE_ARRAY.array(3).createReadOnly(new double[][]{max}));
			labelPS.setVertexAttributes(Attribute.LABELS, StorageModel.STRING_ARRAY.createReadOnly(new String[]{axesLabels[axis]}));
			SceneGraphComponent label = SceneGraphUtility.createFullSceneGraphComponent("label");
			label.setGeometry(labelPS);
			Appearance a = new Appearance();  //for label offset
			a.setAttribute("pointShader.offset", new double[]{.15,0,0});
			line.setAppearance(a);
			line.addChild(label);
		}
		
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
		//get rotation from TubeUtility
		FactoredMatrix m = new FactoredMatrix(TubeUtility.tubeOneEdge(
				min, max, 0.025, null, signature).getTransformation());
		m.setStretch(2*urStretch); //stretch urCone
		m.setTranslation(max); //translate to axis tip
		m.assignTo(arrow);
		
		return arrow;
	}
		
	
	/**
	 * get the marks on the coordinate axis specified by <code>axis</code> 
	 * between min and max as a SceneGraphComponent (IndexedFaceSet)
	 * @param axis the coordinate axis (0,1,2)
	 * @param min the starting point of the line
	 * @param max the endpoint of the line
	 * @param forBox are the marks intended for a box ((0,0,0) is included in marks then)
	 * @return the marks
	 */
	private SceneGraphComponent getAxisMarks(int axis, double[] min, double[] max, boolean forBox) {
		
		//create the marks on a line in z-direction 
		final double minLevel = markScale*Math.ceil( min[axis]/markScale );
		final double maxLevel = markScale*Math.floor( (max[axis]-3*urStretch)/markScale );  //give space for axis arrow
		
		IndexedFaceSet marksIFS = Primitives.pyramid(octagonalCrossSection(minLevel), new double[]{0,0,minLevel});  //init
		for (double level=minLevel+markScale; level<=maxLevel; level+=markScale) {
			if (!forBox && Math.abs(level)<markScale/2) continue;  //no mark at origin (there level may not be exactly 0)
			marksIFS = IndexedFaceSetUtility.mergeIndexedFaceSets(
					new IndexedFaceSet[]{ marksIFS, 
					Primitives.pyramid(octagonalCrossSection(level), new double[]{0,0,level}) });
		}
		GeometryUtility.calculateAndSetVertexNormals(marksIFS);
		
		//create labels
		final int numOfMarks = marksIFS.getNumPoints()/10;  //each mark has 10 points
		PointSet labelsPS = new PointSet(numOfMarks);
		double[][] labelPoints = new double[numOfMarks][];
		String[] labelStr = new String[numOfMarks];
		double level = minLevel;
		for (int i=0; i<numOfMarks; i++, level+=markScale) {
			if (!forBox && level==0) level+=markScale;  //skip mark at origin (there level may not be exactly 0)
			labelPoints[i] = new double[]{0, 0, level};
			labelStr[i] = Math.round(level*1000)/1000. + "";  //3 decimal places
		}
		labelsPS.setVertexAttributes(Attribute.COORDINATES, StorageModel.DOUBLE_ARRAY.array(3).createReadOnly(labelPoints));
		labelsPS.setVertexAttributes(Attribute.LABELS, StorageModel.STRING_ARRAY.createReadOnly(labelStr));
		SceneGraphComponent labels = SceneGraphUtility.createFullSceneGraphComponent("labels");
		labels.setGeometry(labelsPS);
		
		//create the SceneGraphComponent and rotate the marks onto the corresponding coordinate axis
		SceneGraphComponent marks = SceneGraphUtility.createFullSceneGraphComponent("marks");
		marks.setGeometry(marksIFS);
		FactoredMatrix m = new FactoredMatrix(TubeUtility.tubeOneEdge(
				min, max, 0.025, null, signature).getTransformation());
		m.setStretch(urStretch, urStretch, 1); //stretch marks
		m.assignTo(marks);
		
		marks.addChild(labels);
			
		return marks;
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
	
	
	
	
	//---------------------------------------------------
	/**
	 * for testing
	 * (add axes or box as children of a given SceneGraphComponent)
	 */
	public static void main(String[] args) {
		
		//create a component
		SceneGraphComponent component = SphereUtility.tessellatedCubeSphere(2);
		component.setName("Sphere");
		
		//create coordinate system
		CoordinateSystemFactory coords = new CoordinateSystemFactory(component);
		
		//add box as a child
		component.addChild(coords.getBox());
		
		//add axes as a child
		component.addChild(coords.getAxes());
			
//		ViewerApp.display(coords.getBox());

// does not compile with ant - move this into a test/... file 
//		ViewerApp.display(component);
	}
	
}