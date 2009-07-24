package de.jreality.plugin.basic;

import de.jreality.plugin.JRViewer;
import de.varylab.jrworkspace.plugin.Plugin;
import de.varylab.jrworkspace.plugin.sidecontainer.SideContainerPerspective;
import de.varylab.jrworkspace.plugin.sidecontainer.template.ShrinkPanelPlugin;

/** Use this class to get a  {@link ShrinkPanelPlugin} that belongs to the 
 * main {@link Plugin}, i.e., {@link View}, of {@link JRViewer}. Extend this class#
 * and add it to the JRViewer with {@link JRViewer#registerPlugin()};
 * 
 * Extension of the class is necessary, because only one plugin of each class is allowed.
 * 
 * @author G. Paul Peters, 22.07.2009
 *
 */
abstract public class ViewShrinkPanelPlugin extends ShrinkPanelPlugin {

	public ViewShrinkPanelPlugin() {
		super();
	}
	
	@Override
	public Class<? extends SideContainerPerspective> getPerspectivePluginClass() {
		return View.class;
	}

}
