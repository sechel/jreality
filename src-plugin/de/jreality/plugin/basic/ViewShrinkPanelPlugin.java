package de.jreality.plugin.basic;

import de.jreality.plugin.JRViewer;
import de.jtem.jrworkspace.plugin.Plugin;
import de.jtem.jrworkspace.plugin.sidecontainer.SideContainerPerspective;
import de.jtem.jrworkspace.plugin.sidecontainer.template.ShrinkPanelPlugin;

/** Use this class to get a  {@link ShrinkPanelPlugin} that belongs to the 
 * main {@link Plugin}, i.e., {@link View}, of {@link JRViewer}. Extend this class
 * and add it to the JRViewer with {@link JRViewer#registerPlugin()};
 * 
 * <p>If a file <code>clazz.getSimpleName</code>.html is found as a resource of <code>clazz</code>, then this is
 * attached as the help file of this plugin, where <code>clazz</code> is the top level 
 * enclosing class of the runtime class of this object.
 * 
 * @author G. Paul Peters, 22.07.2009
 *
 */
abstract public class ViewShrinkPanelPlugin extends ShrinkPanelPlugin {

	protected String helpDocument;
	protected String helpPath;
	protected Class<?> helpHandle;
	private boolean helpResourceChecked=false;
	
	public ViewShrinkPanelPlugin() {
		super();
	}
	
	@Override
	public Class<? extends SideContainerPerspective> getPerspectivePluginClass() {
		return View.class;
	}
	
	@Override
	public String getHelpDocument() {
		checkHelpResource();
		return helpDocument==null ? super.getHelpDocument() : helpDocument;
	}

	@Override
	public String getHelpPath() {
		checkHelpResource();
		return helpPath==null ? super.getHelpPath() : helpPath;
	}

	@Override
	public Class<?> getHelpHandle() {
		checkHelpResource();
		return helpHandle==null ? super.getHelpHandle() : helpHandle;
	}
	
	
	private void checkHelpResource() {
		if (helpResourceChecked) return;
		Class<?> clazz = getClass();
		while (null != clazz.getEnclosingClass()) {
			clazz = clazz.getEnclosingClass();
		}
		String filename = clazz.getSimpleName()+".html";
		if (null!=clazz.getResource(filename)) {
			helpDocument=filename;
			helpPath="";
			helpHandle=clazz;
		}
		helpResourceChecked=true;
	}
	

}
