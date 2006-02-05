package de.jreality.scene.event;

import de.jreality.scene.SceneGraphNode;
import de.jreality.scene.tool.Tool;

public class ToolEvent extends SceneEvent {
  
  public static final int TOOL_ADDED=0;
  public static final int TOOL_REMOVED=2;

  private final Tool tool;
  private final int eventType;
  
  public ToolEvent(SceneGraphNode source, Tool tool, int type) {
    super(source);
    this.tool = tool;
    if (type!=TOOL_ADDED && type!=TOOL_REMOVED) 
      throw new IllegalArgumentException("illegal type");
    this.eventType = type;
  }

  public Tool getTool() {
    return tool;
  }

  public int getEventType() {
    return eventType;
  }

}
