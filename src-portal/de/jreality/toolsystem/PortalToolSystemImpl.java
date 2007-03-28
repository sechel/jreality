package de.jreality.toolsystem;

import java.awt.Toolkit;

import de.jreality.jogl.Viewer;
import de.jreality.portal.awt.SynchEventQueue;
import de.jreality.toolsystem.config.ToolSystemConfiguration;

public class PortalToolSystemImpl extends ToolSystem implements PortalToolSystem {

	private static final SynchEventQueue SYNCH_EVENT_QUEUE = new SynchEventQueue();
	private static final boolean DO_SYNCH=false;
	Viewer joglViewer;
	
	static {
		if (DO_SYNCH) Toolkit.getDefaultToolkit().getSystemEventQueue().push(SYNCH_EVENT_QUEUE);
	}
	
	  public PortalToolSystemImpl(Viewer joglViewer, ToolSystemConfiguration config) {
		  super(joglViewer, config, null);
		  this.joglViewer=joglViewer;
	  }

	  /* (non-Javadoc)
	 * @see de.jreality.toolsystem.PortalToolSystem#render()
	 */
	public void render() {
		if (DO_SYNCH) SYNCH_EVENT_QUEUE.unlock();
		joglViewer.render();
	  }
	  
	  /* (non-Javadoc)
	 * @see de.jreality.toolsystem.PortalToolSystem#swapBuffers()
	 */
	public void swapBuffers() {
		  joglViewer.swapBuffers();
	  }

	public void setAutoSwapBufferMode(boolean autoSwap) {
		joglViewer.setAutoSwapMode(autoSwap);
	}
	
	

}
