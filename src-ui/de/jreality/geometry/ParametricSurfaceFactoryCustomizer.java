package de.jreality.geometry;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.Customizer;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.Box;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.border.EmptyBorder;

import de.jreality.ui.widgets.TextSlider;


/** A user interface for a {@link ParametricSurfaceFactory}.
 * 
 * @author G. Paul Peters, 03.06.2010
 */
public class ParametricSurfaceFactoryCustomizer extends JPanel implements Customizer {
	private static final long serialVersionUID = 1L;
	private Object object;
	private final Method updateMethod = getUpdateMethod();
	private final List<SliderProperty<?>> sliderProperties = new LinkedList<SliderProperty<?>>();
	private final List<ToggleProperty> toggleProperties = new LinkedList<ToggleProperty>();
	
	private boolean
		alwaysCallUpdateMethdod = true;

	public ParametricSurfaceFactoryCustomizer() {
		this(null);
	}

	public ParametricSurfaceFactoryCustomizer(ParametricSurfaceFactory factory) {
		if (factory != null) setObject(factory);
	}
	
	public Map<PropertyDescriptor, JToggleButton> getJToggleButtons() {
		Map<PropertyDescriptor, JToggleButton> toggleButtons = new HashMap<PropertyDescriptor, JToggleButton>();
		for (ToggleProperty toggleProperty : toggleProperties) {
			toggleButtons.put(toggleProperty.getPropertyDescriptor(), toggleProperty.getButton());
		}
		return toggleButtons ;
	}

	public Map<PropertyDescriptor, TextSlider<Double>> getDoubleSliders() {
		Map<PropertyDescriptor, TextSlider<Double>> integerSliders = new HashMap<PropertyDescriptor, TextSlider<Double>>();
		for (SliderProperty<? extends Number> sliderProperty : sliderProperties) {
			if (sliderProperty instanceof DoubleSliderProperty) {
				integerSliders.put(sliderProperty.getPropertyDescriptor(), ((DoubleSliderProperty) sliderProperty).getSlider());
			}
		}
		return integerSliders ;
	}
	
	public Map<PropertyDescriptor, TextSlider<Integer>> getIntegerSliders() {
		Map<PropertyDescriptor, TextSlider<Integer>> integerSliders = new HashMap<PropertyDescriptor, TextSlider<Integer>>();
		for (SliderProperty<? extends Number> sliderProperty : sliderProperties) {
			if (sliderProperty instanceof IntegerSliderProperty) {
				integerSliders.put(sliderProperty.getPropertyDescriptor(), ((IntegerSliderProperty) sliderProperty).getSlider());
			}
		}
		return integerSliders ;
	}

	public void setObject(Object object) {
		checkObject(object);
		if (this.object == object) return;
		this.object = object;
		initSliderProperties();
		initToggleProperties();
		initPanel();
		revalidate();
	}

	public boolean isAlwaysCallUpdateMethdod() {
		return this.alwaysCallUpdateMethdod;
	}

	public void setAlwaysCallUpdateMethdod(boolean alwaysCallUpdateMethdod) {
		this.alwaysCallUpdateMethdod = alwaysCallUpdateMethdod;
	}
	
	public void updateGuiFromFactory() {
		List<Property> properties = new LinkedList<Property>();
		properties.addAll(sliderProperties);
		properties.addAll(toggleProperties);
		for (Property property : properties) {
			property.updateGuiFromFactory();
		}
	}

	private void checkObject(Object object) {
		if (!getAcceptableClass().isInstance(object)) {
			throw new IllegalArgumentException(this.getClass() + " can only inspect objects of type "
				+ getAcceptableClass().getCanonicalName());
		}
	}

	protected static Class<?> getAcceptableClass() {
		return ParametricSurfaceFactory.class;
	}
	
	protected static Method getUpdateMethod() {
		try {
			return getAcceptableClass().getMethod("update");
		} catch (Exception e) {
			throw new IllegalStateException(e);
		} 
	}

	protected void initSliderProperties() {
		sliderProperties.clear();
		sliderProperties.add(new DoubleSliderProperty("uMin", -10., 10.));
		sliderProperties.add(new DoubleSliderProperty("uMax", -10., 10.));
		sliderProperties.add(new DoubleSliderProperty("vMin", -10., 10.));
		sliderProperties.add(new DoubleSliderProperty("vMax", -10., 10.));
		sliderProperties.add(new IntegerSliderProperty("uLineCount", "uLines", 2, 100));
		sliderProperties.add(new IntegerSliderProperty("vLineCount", "vLines", 2, 100));
		sliderProperties.add(new DoubleSliderProperty("uTextureScale", 0., 10.));
		sliderProperties.add(new DoubleSliderProperty("vTextureScale", 0., 10.));
		sliderProperties.add(new DoubleSliderProperty("uTextureShift", 0., 1.));
		sliderProperties.add(new DoubleSliderProperty("vTextureShift", 0., 1.));
	}
	
	protected void initToggleProperties() {
		toggleProperties.clear();
		toggleProperties.add(new ToggleProperty("generateVertexNormals", "vrtxNrmls"));
		toggleProperties.add(new ToggleProperty("generateFaceNormals", "fcNrmls"));
		toggleProperties.add(new ToggleProperty("generateTextureCoordinates", "txCrds"));
		toggleProperties.add(new ToggleProperty("generateEdgesFromFaces", "edges"));
		toggleProperties.add(new ToggleProperty("edgeFromQuadMesh", "cnctEdgs"));
		toggleProperties.add(new ToggleProperty("closedInUDirection", "uClosed"));
		toggleProperties.add(new ToggleProperty("closedInVDirection", "vClosed"));
		toggleProperties.add(new ToggleProperty("generateVertexLabels", "vrtxLbls"));
		toggleProperties.add(new ToggleProperty("generateEdgeLabels", "edgeLbls"));
		toggleProperties.add(new ToggleProperty("generateFaceLabels", "faceLbls"));
		toggleProperties.add(new ToggleProperty("generateAABBTree", "AABBTree"));
	}

	private void initPanel() {
		removeAll();
		setLayout(new GridBagLayout());
		addToggleButtons();
		addSlider();
	}

	private void addSlider() {
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(0, 5, 0, 5);
		c.gridwidth = GridBagConstraints.REMAINDER;
		for (SliderProperty<?> sliderProperty : sliderProperties) {
			add(sliderProperty.getSlider(), c);
		}
	}

	private void addToggleButtons() {
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2, 2, 2, 2);
		c.fill = GridBagConstraints.BOTH;
		c.gridwidth = 1;
		c.weightx = .3;
		int toggleButtons = 1;
		for (ToggleProperty toggleProperty : toggleProperties) {
			add(toggleProperty.getButton(), c);
			toggleButtons++;
			if (0 == toggleButtons % 3) {
				c.gridwidth = GridBagConstraints.REMAINDER;
			} else {
				c.gridwidth = 1;
			}
		}
		if (1 != toggleButtons % 3) {
			c.gridwidth = GridBagConstraints.REMAINDER;
			add(Box.createHorizontalGlue(), c);
		}
	}
	
	abstract private class Property {
		final PropertyDescriptor propertyDescriptor;

		Property(String propertyName) {
			try {
				propertyDescriptor = new PropertyDescriptor(propertyName, getAcceptableClass());
			} catch (Exception e) {
				throw new IllegalStateException(e);
			}
		}
		
		public PropertyDescriptor getPropertyDescriptor() {
			return propertyDescriptor;
		}
		
		abstract public void updateGuiFromFactory();  
	}

	abstract private class SliderProperty<T extends Number> extends Property {
		private final TextSlider<T> slider;

		SliderProperty(String propertyName, String propertyLabel, T min, T max) {
			super(propertyName);
			slider = initSlider(propertyLabel, min, max, readValueFromFactory());
			addListener();
		}

		abstract TextSlider<T> initSlider(String propertyLabel, T min, T max, T value) ;

		public TextSlider<T> getSlider() {
			return this.slider;
		}

		void addListener() {
			slider.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					writeSliderValueToFactory();
				}
			});
		}

		void writeSliderValueToFactory() {
			try {
				propertyDescriptor.getWriteMethod().invoke(object, slider.getValue());
				if (alwaysCallUpdateMethdod) updateMethod.invoke(object);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@SuppressWarnings("unchecked")
		T readValueFromFactory() {
			try {
				return (T) propertyDescriptor.getReadMethod().invoke(object);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public void updateGuiFromFactory() {
			try {
				slider.setValue(readValueFromFactory());
			} catch (Exception e) {
				e.printStackTrace();
			}	
		}
}
	

	private class IntegerSliderProperty extends SliderProperty<Integer> {
		IntegerSliderProperty(String propertyName, Integer min, Integer max) {
			this(propertyName, propertyName, min, max);
		}

		IntegerSliderProperty(String propertyName, String propertyLabel, Integer min, Integer max) {
			super(propertyName, propertyLabel, min, max);
		}

		@Override
		TextSlider<Integer> initSlider(String propertyLabel, Integer min, Integer max, Integer value) {
			TextSlider<Integer> slider = new TextSlider.Integer(propertyLabel, JSlider.HORIZONTAL, min, max, value);
			return slider;
		}
	}

	
	private class DoubleSliderProperty extends SliderProperty<Double> {
		DoubleSliderProperty(String propertyName, Double min, Double max) {
			this(propertyName, propertyName, min, max);
		}

		DoubleSliderProperty(String propertyName, String propertyLabel, Double min, Double max) {
			super(propertyName, propertyLabel, min, max);
		}

		@Override
		TextSlider<Double> initSlider(String propertyLabel, Double min, Double max, Double value) {
			TextSlider<Double> slider = new TextSlider.Double(propertyLabel, JSlider.HORIZONTAL, min, max, value);
			return slider;
		}
	}
	
	
	private class ToggleProperty extends Property {
		private final JToggleButton button;
		
		ToggleProperty(String propertyName) {
			this(propertyName, propertyName);
		}
		
		ToggleProperty(String propertyName, String propertyLabel) {
			super(propertyName);
			button = new JToggleButton(propertyLabel, readStateFromFactory());
			button.setBorder(new EmptyBorder(2, 2, 2, 2));
			button.setMinimumSize(button.getPreferredSize());
			button.setToolTipText(propertyName);
			addListener();
		}
		
		public JToggleButton getButton() {
			return button;
		}
		
		void addListener() {
			button.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					writeButtonStateToFactory();
				}
			});
		}

		void writeButtonStateToFactory() {
			try {
				propertyDescriptor.getWriteMethod().invoke(object, button.isSelected());
				if (alwaysCallUpdateMethdod) updateMethod.invoke(object);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		boolean readStateFromFactory() {
			try {
				return (Boolean) propertyDescriptor.getReadMethod().invoke(object);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public void updateGuiFromFactory() {
			try {
				button.setSelected(readStateFromFactory());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
