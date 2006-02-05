
package de.jreality.scene.tool;

import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Viewer;
import de.jreality.scene.data.DoubleArray;
import de.jreality.scene.pick.PickResult;

/**
 */
public interface ToolContext
{
    Viewer getViewer();
    
    InputSlot getSource();
    
    DoubleArray getTransformationMatrix(InputSlot slot);
    AxisState getAxisState(InputSlot slot);
    
    /**
     * @return the time stamp of the event that's currently
     * being processed
     */
    long getTime();
    
    /**
     * @return Returns the path to the current tool if tool is not
     * activated by picking, path to pick otherwise
     */
    SceneGraphPath getRootToLocal();
    
    /**
     * @return Returns the path to the component where the
     * current tool is attatched
     */
    SceneGraphPath getRootToToolComponent();
    
    PickResult getCurrentPick();
    
    /**
     * Posts a new event to the tool event queue
     * @param event
     */
    void send(ToolEvent event);
    
    
    public void schedule(Object key, AnimatorTask task);
    
    public void deschedule(Object key);

    public ToolSystem getToolSystem();
    
    /**
     * a tool calls this method during activation
     * if the context is insufficient for activation.
     * That means the tool is not in activated state after
     * the activate call. calling this method at any other
     * time than activation, it has absolutely no effect.
     * 
     * PENDING: possibly we should put this as a boolean return
     * value for activate(TC);
     *
     */
    public void reject();
}
