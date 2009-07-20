package de.jreality.tutorial.util;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Method;
import java.util.Collection;

import javax.swing.JButton;

import de.jreality.plugin.basic.View;
import de.jreality.util.LoggingSystem;
import de.jtem.beans.InspectorPanel;
import de.varylab.jrworkspace.plugin.PluginInfo;
import de.varylab.jrworkspace.plugin.sidecontainer.SideContainerPerspective;
import de.varylab.jrworkspace.plugin.sidecontainer.template.ShrinkPanelPlugin;

public class BeanInspectorPlugin extends ShrinkPanelPlugin {
	
	final private Object bean;
	final private String title;
	
	public BeanInspectorPlugin(String title, Object bean, Collection<String> excludedPropertyNames) {
		super();
		
		if (bean == null || title ==null)
			throw new NullPointerException();
		
		this.bean=bean;
		this.title=title;
		
		final InspectorPanel inspectorPanel=new InspectorPanel();
		inspectorPanel.setObject(bean, excludedPropertyNames);
		
		try {
			final Method updateMethod = bean.getClass().getMethod("update");
			JButton updateButton=new JButton("update");
			updateButton.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent e) {
					try {
						updateMethod.invoke(BeanInspectorPlugin.this.bean);
					} catch (Exception e1) {
						// should not happen, so print stack trace
						e1.printStackTrace();
					}
				}
			});
			inspectorPanel.add(updateButton,BorderLayout.SOUTH);
			inspectorPanel.validate();
		} catch (SecurityException e1) {
			LoggingSystem.getLogger(this).fine("Could not check whether " + bean.getClass() 
					+ " has an update method for security reasons.");
		} catch (NoSuchMethodException e1) {
			//thats fine
		}
		shrinkPanel.add(inspectorPanel);
		shrinkPanel.setTitle(this.title);		
	}
	
	@Override
	public Class<? extends SideContainerPerspective> getPerspectivePluginClass() {
		return View.class;
	}
	
	@Override
	public PluginInfo getPluginInfo() {
		return new PluginInfo(title);
	}	
}

