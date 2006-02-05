package de.jreality.scene.tool;

import java.util.List;

public abstract class Tool
{
	/**
	 * 
	 * If the result is empty, then the tool is always active.
	 * If the result is not empty, then the tool is active if and only if
	 * the axis of an activation slot is pressed.
	 * 
	 * The result must remain constant.
	 * 
	 * @return list of slots for activating the tool
	 */
	public abstract List getActivationSlots();
	
	/**
	 * TODO: what happens when a tool is activated "twice"?
	 *
	 * @return list of relevant input slots, may depend on how the tool was activated
	 */
    public abstract List getCurrentSlots();
    
    /** 
     * @return list of output slots of the tool, usually empty, should remain constant
     * TODO: make this more precise
     */
    public abstract List getOutputSlots();
	
    public abstract void activate(ToolContext tc);
    public abstract void perform(ToolContext tc);
    public abstract void deactivate(ToolContext tc);
    
}