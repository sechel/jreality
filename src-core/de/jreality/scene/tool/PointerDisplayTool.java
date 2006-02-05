package de.jreality.scene.tool;

import java.awt.Color;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import de.jreality.geometry.GeometryUtility;
import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.geometry.IndexedLineSetUtility;
import de.jreality.geometry.Primitives;
import de.jreality.geometry.QuadMeshFactory;
import de.jreality.math.FactoredMatrix;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Pn;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Transformation;
import de.jreality.scene.pick.PickResult;

public class PointerDisplayTool extends Tool {

  List currentSlots = new LinkedList();
  InputSlot pointer = InputSlot.getDevice("PointerShipTransformation");
  SceneGraphComponent c = new SceneGraphComponent();
  
  public PointerDisplayTool(double radius) {
    currentSlots.add(pointer);
    SceneGraphComponent stick=new SceneGraphComponent();
//    QuadMeshFactory qmf = new QuadMeshFactory(Pn.EUCLIDEAN, 16, 2, false, false);
//    double[][] points = Primitives.surfaceOfRevolution(
//      new double[][]{{0,0,radius,1}, {1,0,radius,1}}, 16, Math.PI 		
//    );
//    qmf.setVertexCoordinates(points);
//    qmf.refactor();
//    MatrixBuilder.euclidean().rotateFromTo(new double[]{1,0,0}, new double[]{0,0,1}).assignTo(stick);
    MatrixBuilder.euclidean().translate(0,0,-1).scale(radius, radius, 1).assignTo(stick);
    IndexedFaceSet cube = new IndexedFaceSet();
    GeometryUtility.calculateAndSetFaceNormals(cube);
	stick.setGeometry(cube);
    c.setAppearance(new Appearance());
    c.getAppearance().setAttribute("diffuseColor", Color.yellow);
    c.setTransformation(new Transformation());
    c.addChild(stick);
  }
  public PointerDisplayTool() {
    this(0.05);
  }
  
  public List getActivationSlots() {
	    return Collections.EMPTY_LIST;
  }

  public List getCurrentSlots() {
    return currentSlots;
  }

  public List getOutputSlots() {
    return Collections.EMPTY_LIST;
  }

  public void activate(ToolContext tc) {
  }

  boolean isAssigned;
  
  Matrix m = new Matrix();
  public void perform(ToolContext tc) {
	if (!isAssigned) {
		tc.getToolSystem().getAvatarPath().getLastComponent().addChild(c);
		isAssigned=true;
	}
    m.assignFrom(tc.getTransformationMatrix(pointer));
    m.assignTo(c.getTransformation());
  }

  public void deactivate(ToolContext tc) {
  }

}
