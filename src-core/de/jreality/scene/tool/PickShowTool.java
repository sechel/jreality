package de.jreality.scene.tool;

import java.awt.Color;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import de.jreality.geometry.Primitives;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.pick.PickResult;
import de.jreality.shader.CommonAttributes;

public class PickShowTool extends Tool {

  List activationSlots = new LinkedList();
  List currentSlots = new LinkedList();
  
  SceneGraphComponent c = new SceneGraphComponent();
  Appearance a = new Appearance();
  
  public PickShowTool(String activationAxis, double radius) {
    activationSlots.add(InputSlot.getDevice(activationAxis));
    currentSlots.add(InputSlot.getDevice("PointerTransformation"));
    c.addChild(Primitives.sphere(radius, 0, 0, 0));
    c.setAppearance(a);
    a.setAttribute("pickable", false);
    a.setAttribute(CommonAttributes.FACE_DRAW, true);
  }
  public PickShowTool(String activationAxis) {
    this(activationAxis, 0.05);
  }
  
  public List getActivationSlots() {
    return activationSlots;
  }

  public List getCurrentSlots() {
    return currentSlots;
  }

  public List getOutputSlots() {
    return Collections.EMPTY_LIST;
  }

  public void activate(ToolContext tc) {
    tc.getViewer().getSceneRoot().addChild(c);
    perform(tc);
  }

  public void perform(ToolContext tc) {
    PickResult pr = tc.getCurrentPick();
    if (pr == null) {
      System.out.println("pick==null ??");
      return;
    }
    switch (tc.getCurrentPick().getPickType()) {
    case PickResult.PICK_TYPE_FACE:
      c.getAppearance().setAttribute("diffuseColor", Color.yellow);
      break;
    case PickResult.PICK_TYPE_LINE:
      c.getAppearance().setAttribute("diffuseColor", Color.green);
      break;
    case PickResult.PICK_TYPE_POINT:
      //System.out.println(pr);
      c.getAppearance().setAttribute("diffuseColor", Color.magenta);
      break;
    case PickResult.PICK_TYPE_OBJECT:
      c.getAppearance().setAttribute("diffuseColor", Color.red);
      break;
    default:
      c.getAppearance().setAttribute("diffuseColor", Color.black);
    }
    double[] worldCoordinates = pr.getWorldCoordinates();
    MatrixBuilder.euclidean().translate(worldCoordinates).assignTo(c);
  }

  public void deactivate(ToolContext tc) {
    tc.getViewer().getSceneRoot().removeChild(c);
  }

}
