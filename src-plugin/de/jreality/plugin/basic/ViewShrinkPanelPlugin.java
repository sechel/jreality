package de.jreality.plugin.basic;

import de.jreality.plugin.JRViewer;
import de.varylab.jrworkspace.plugin.Plugin;
import de.varylab.jrworkspace.plugin.PluginInfo;
import de.varylab.jrworkspace.plugin.sidecontainer.SideContainerPerspective;
import de.varylab.jrworkspace.plugin.sidecontainer.template.ShrinkPanelPlugin;

/** Use this class to get a  {@link ShrinkPanelPlugin} that belongs to the 
 * main {@link Plugin}, i.e., {@link View}, of {@link JRViewer}. Add this
 * to the JRViewer with {@link JRViewer#registerPlugin()};
 * 
 * @author G. Paul Peters, 22.07.2009
 *
 */
public class ViewShrinkPanelPlugin extends ShrinkPanelPlugin {
	//TODO: pinfoDummy hack because ShrinkPanelPlugin needs a PluginInfo with non-null name!
	static private PluginInfo pinfoDummy=new PluginInfo("");
	
	private PluginInfo pinfo;
	
	public ViewShrinkPanelPlugin(String name) {
		this(new PluginInfo(name));
	}

	public ViewShrinkPanelPlugin(PluginInfo pinfo) {
		super();
		shrinkPanel.setTitle(pinfo.name);
		this.pinfo=pinfo;
	}
	
	@Override
	public Class<? extends SideContainerPerspective> getPerspectivePluginClass() {
		return View.class;
	}

	@Override
	public PluginInfo getPluginInfo() {
		//TODO: pinfoDummy hack because ShrinkPanelPlugin needs a PluginInfo with non-null name!
		return pinfo==null?pinfoDummy:pinfo;
	}

}
