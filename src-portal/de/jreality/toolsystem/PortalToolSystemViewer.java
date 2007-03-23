package de.jreality.toolsystem;

import de.jreality.jogl.Viewer;
import de.jreality.scene.pick.AABBPickSystem;
import de.jreality.toolsystem.config.ToolSystemConfiguration;
import de.jreality.ui.viewerapp.ViewerSwitch;

public class PortalToolSystemViewer extends ToolSystemViewer {

	public PortalToolSystemViewer(ViewerSwitch viewerSwitch, ToolSystemConfiguration config) {
	    this.viewer = viewerSwitch;
	    toolSystem = new PortalToolSystemImpl((Viewer) viewerSwitch.getCurrentViewer(), config);
	    setPickSystem(new AABBPickSystem());
	}
	
}
