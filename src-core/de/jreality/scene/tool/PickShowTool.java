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

public class PickShowTool extends Tool {

  List activationSlots = new LinkedList();
  List currentSlots = new LinkedList();
  
  SceneGraphComponent c = new SceneGraphComponent();
  
  public PickShowTool(String activationAxis, double radius) {
    activationSlots.add(InputSlot.getDevice(activationAxis));
    currentSlots.add(InputSlot.getDevice("PointerTransformation"));
    c.addChild(Primitives.sphere(radius, 0, 0, 0));
    c.setAppearance(new Appearance());
    c.getAppearance().setAttribute("diffuseColor", Color.yellow);
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
    double[] worldCoordinates = pr.getWorldCoordinates();
    MatrixBuilder.euclidean().translate(worldCoordinates).assignTo(c);
  }

  public void deactivate(ToolContext tc) {
    tc.getViewer().getSceneRoot().removeChild(c);
  }

}
