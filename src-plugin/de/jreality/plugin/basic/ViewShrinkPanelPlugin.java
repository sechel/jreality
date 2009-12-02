package de.jreality.plugin.basic;

import de.jreality.plugin.JRViewer;
import de.jreality.plugin.scene.SceneShrinkPanel;
import de.jtem.jrworkspace.plugin.Plugin;
import de.jtem.jrworkspace.plugin.sidecontainer.SideContainerPerspective;
import de.jtem.jrworkspace.plugin.sidecontainer.template.ShrinkPanelPlugin;

/** Use this class to get a  {@link ShrinkPanelPlugin} that belongs to the 
 * main {@link Plugin}, i.e., {@link View}, of {@link JRViewer}. Extend this class
 * and add it to the JRViewer with {@link JRViewer#registerPlugin()};
 * 
 * <p>Attach online help: If a file "<code>clazz.getSimpleName()</code>.html" is found as a resource of <code>clazz</code>, then this is
 * attached as the help file of this plugin, where <code>clazz</code> is the top level 
 * enclosing class of the runtime class of this object. Note that in Eclipse you most likely need to 
 * remove *.html from "Filtered resources" under Window &rarr; Preferences &rarr; Java &rarr; Compiler &rarr; Building
 * 
 * @author G. Paul Peters, 22.07.2009
 * 
 * @see SceneShrinkPanel
 *
 */
abstract public class ViewShrinkPanelPlugin extends ShrinkPanelPlugin {

	private String helpDocument;
	private String helpPath;
	private Class<?> helpHandle;
	private boolean helpResourceChecked=false;
	
	public ViewShrinkPanelPlugin() {
		super();
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
	
	public Class<? extends SideContainerPerspective> getPerspectivePluginClass() {
		return View.class;
	}
	

}
