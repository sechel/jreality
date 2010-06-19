package de.jreality.geometry;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.SimpleBeanInfo;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import de.jtem.beans.InspectorPanel;

/** Announces the customizer {@link ParametricSurfaceFactoryCustomizer} for JavaBeans introspection.
 * 
 * @author G. Paul Peters, 03.06.2010
 */
public class ParametricSurfaceFactoryBeanInfo extends SimpleBeanInfo {

	private final BeanDescriptor bd = new BeanDescriptor(ParametricSurfaceFactory.class, ParametricSurfaceFactoryCustomizer.class);
	
	public ParametricSurfaceFactoryBeanInfo() {
		bd.setDisplayName("Parametric Surface Factory Explorer");
	}

	@Override
	public BeanDescriptor getBeanDescriptor() {
		return bd;
	}
	

	public static void main(String[] args) throws IntrospectionException {
		initFrameWithInspectorPanel(new ParametricSurfaceFactory(), "update");
	}

	private static InspectorPanel initFrameWithInspectorPanel(Object o, String updateMethodName) {
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		InspectorPanel inspector = new InspectorPanel();
		inspector.setObject(o, updateMethodName);
		frame.add(inspector);
		frame.pack();
		frame.setVisible(true);
		return inspector;
	}

}
