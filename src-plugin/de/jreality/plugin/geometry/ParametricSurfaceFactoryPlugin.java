package de.jreality.plugin.geometry;

import java.awt.GridLayout;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;

import de.jreality.geometry.ParametricSurfaceFactory;
import de.jreality.geometry.ParametricSurfaceFactoryCustomizer;
import de.jreality.plugin.JRViewer;
import de.jreality.plugin.scene.SceneShrinkPanel;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.Plugin;
import de.jtem.jrworkspace.plugin.PluginInfo;

/** Wraps a {@link ParametricSurfaceFactoryCustomizer} into a plugin. This takes care of showing up somewhere 
 * (see {@link SceneShrinkPanel}) in {@link JRViewer} and storing its properties by overriding the corresponding methods of {@link Plugin}.
 * 
 * @author G. Paul Peters, 16.06.2010
 */
public class ParametricSurfaceFactoryPlugin extends SceneShrinkPanel {
	
	
	private final ParametricSurfaceFactoryCustomizer psfCustomizer;
	private final ParametricSurfaceFactory psf;
	private final ArrayList<PropertyDescriptor> properties = new ArrayList<PropertyDescriptor>();

	public ParametricSurfaceFactoryPlugin(ParametricSurfaceFactory psf) {
		this.psf = psf;
		psfCustomizer = new ParametricSurfaceFactoryCustomizer(psf);
		psfCustomizer.updateGuiFromFactory();
		initPanel();
		readProperties();
	}

	private void initPanel() {
		getShrinkPanel().setLayout(new GridLayout());
		getShrinkPanel().add(psfCustomizer);
	}
	
	private void readProperties() {
		properties.addAll(psfCustomizer.getIntegerSliders().keySet());
		properties.addAll(psfCustomizer.getDoubleSliders().keySet());
		properties.addAll(psfCustomizer.getJToggleButtons().keySet());
	}

	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo("Parameter Domain Settings");
		info.vendorName = "G. Paul Peters";
		info.email = "peters@math.tu-berlin.de";
		return info;
	}

	@Override
	public void restoreStates(Controller c) throws Exception {
		super.restoreStates(c);

		for (PropertyDescriptor property : properties) {
			String name = property.getName();
			Method readMethod = property.getReadMethod();
			Method writeMethod = property.getWriteMethod();
			writeMethod.invoke(psf, c.getProperty(this.getClass(), name, readMethod.invoke(psf)));
		}
		psfCustomizer.updateGuiFromFactory();
	}

	@Override
	public void storeStates(Controller c) throws Exception {
		super.storeStates(c);
		
		for (PropertyDescriptor property : properties) {
			String name = property.getName();
			Method readMethod = property.getReadMethod();
			c.storeProperty(this.getClass(), name, readMethod.invoke(psf));
		}
	}
	
	
	
	
	

}
