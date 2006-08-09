package de.jreality.examples.tooldemo;

import de.jreality.geometry.GeometryUtility;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.util.Rectangle3D;

public class ToolDemoContent {

  private static final double[][] DEF_BOUNDS={{-10,1,-10},{10,21,10}};
  private SceneGraphComponent content;
  private Rectangle3D placementBounds;
  private Rectangle3D contentBounds;
  private boolean keepAspectRatio=true;
  
  public ToolDemoContent(SceneGraphComponent cmp, Rectangle3D placementBds, Rectangle3D contentBds) {
    content=cmp;
    placementBounds=placementBds;
    contentBounds=contentBds;
  }

  public ToolDemoContent(SceneGraphComponent cmp, Rectangle3D placementBds) {
    this(cmp, placementBds, GeometryUtility.calculateBoundingBox(cmp));
  }
  
  public ToolDemoContent(SceneGraphComponent cmp) {
    this(cmp, new Rectangle3D(DEF_BOUNDS));
  }
  
  public boolean getKeepAspectRatio() {
    return keepAspectRatio;
  }

  public void setKeepAspectRatio(boolean keepAspectRatio) {
    this.keepAspectRatio = keepAspectRatio;
  }

  public Rectangle3D getPlacementBounds() {
    return placementBounds;
  }

  public Rectangle3D getContentBounds() {
    return contentBounds;
  }  
  
  public SceneGraphComponent getContent() {
    return content;
  }
}
