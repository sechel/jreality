package de.jreality.tutorial.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


/**
 * @author Charles Gunn, G. Paul Peters
 * 
 * This is the generic base for the TextSliders.
 *
 * @param <T> data type.
 */
public abstract class TextSlider<T extends Number> extends JPanel  {
	
	/** An <code>enum</code> indicating the possible compositions of the {@link TextSlider}. 
	 */
	public static enum SliderComposition {SliderOnly,SliderAndTextField,SliderTextFieldAndMaxMinButtons}
	public static final SliderComposition DEFAULT_SLIDER_COMPOSITION=SliderComposition.SliderAndTextField;

	private static final int TEXT_FIELD_COLUMNS = 6;
	private static final float MAX_TEXT_FIELD_STRETCH = 1.5f;
	private static final float MAX_TEXT_FIELD_SHRINK = .8f;
	private static final int PREFERRED_HEIGHT = 35;
	
	private final JSlider slider;
	private final JLabel label;
	private final JTextField textField;
	private String textContents;
	private T min, max;
	
	private TextSlider(String label, int orientation, 
			T min, T max, T value, 
			int sliderMin, int sliderMax, int sliderValue, 
			SliderComposition sliderComp)	{
		super();
		if (sliderMax < sliderValue) sliderMax= sliderValue;
		this.label  = new JLabel(label, JLabel.LEFT);
	    textField = new JTextField();
		slider = new JSlider(orientation, sliderMin, sliderMax, sliderValue);
		Font  f = new Font("Helvetica",Font.PLAIN, 10);
	    textField.setFont(f);
	    slider.setMinimumSize(new Dimension(10, 3));
	    this.min=min; this.max=max; 
	    
		textField.setText(getFormattedValue(sliderToText()));
	    textContents = textField.getText();
	    textField.setColumns(TEXT_FIELD_COLUMNS);
	    textField.setEditable(true);
	    Dimension d = textField.getPreferredSize();
	    textField.setMaximumSize(new Dimension((int)(d.width*MAX_TEXT_FIELD_STRETCH),(int) (d.height*MAX_TEXT_FIELD_STRETCH)));
	    textField.setMinimumSize(new Dimension((int)(d.width*MAX_TEXT_FIELD_SHRINK),(int) (d.height*MAX_TEXT_FIELD_SHRINK)));
	    textField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				adjustSliderMinMax();
				slider.setValue(textToSlider());
				textContents = textField.getText();
				fireActionPerformed();
				textField.setForeground(Color.black);
			}
 	    	
 	    });
	    textField.addCaretListener(new CaretListener() {
			public void caretUpdate(CaretEvent e) {
				if (textField.getText().compareTo(textContents) != 0)
					textField.setForeground(Color.RED);
			}	    	
	    });
		slider.addChangeListener( new ChangeListener()	{
		    public void stateChanged(ChangeEvent e) {
			    textField.setText(getFormattedValue(sliderToText()));
		        fireActionPerformed();
		    }
		});
		setLayout(new BoxLayout(this,BoxLayout.LINE_AXIS));
		add(this.label);
		add(Box.createHorizontalStrut(8));
		add(textField);
		final Component s1 = Box.createHorizontalStrut(2);
		add(s1);
		add(slider);

		final JButton minButton=new JButton("min");
		minButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setMin(getValue());
			}
		});
		
		final JButton maxButton=new JButton("max");
		maxButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setMax(getValue());
			}
		});
			
		final Component s2 = Box.createHorizontalStrut(8);
		add(s2);
		add(minButton);
		final Component s3 = Box.createHorizontalStrut(2);
		add(s3);
		add(maxButton);
		//don't stretch the Buttons
		minButton.setMaximumSize(minButton.getPreferredSize());
		maxButton.setMaximumSize(maxButton.getPreferredSize());

		//visibility of elements
		int preferredSize=slider.getPreferredSize().width
			+ textField.getPreferredSize().width  
			+ this.label.getPreferredSize().width
			+ minButton.getPreferredSize().width 
			+ maxButton.getPreferredSize().width
			+ 70;
		switch (sliderComp) {
		case SliderOnly: 
			textField.setVisible(false); 
			s1.setVisible(false);
			preferredSize-=textField.getPreferredSize().width+8;
		case SliderAndTextField: maxButton.setVisible(false); 
			minButton.setVisible(false);
			s2.setVisible(false);
			s3.setVisible(false);
			preferredSize-=minButton.getPreferredSize().width + maxButton.getPreferredSize().width+50;
		}

		setPreferredSize(new Dimension(preferredSize,PREFERRED_HEIGHT));
		setMinimumSize(getPreferredSize());
		setMaximumSize(new Dimension(10000,PREFERRED_HEIGHT));
	}
	
	public T getMin() {
		return min;
	}
	public T getMax() {
		return max;
	}
	public void setMin(T min) {
		this.min=min;
	    Vector<ActionListener> remember = listeners;
	    listeners = null;
	    textField.postActionEvent();
	    listeners=remember;
	}
	public void setMax(T max) {
		this.max=max;
	    Vector<ActionListener> remember = listeners;
	    listeners = null;
	    textField.postActionEvent();
	    listeners=remember;
	}
	
	abstract void adjustSliderMinMax();
	abstract T sliderToText();
	abstract int textToSlider();
	abstract int numberToSlider(T n);
	abstract String getFormattedValue(T n);

	public T getValue()	{
		return sliderToText();
	}

	public void setValue(T n)	{
	    Vector<ActionListener> remember = listeners;
	    listeners = null;
		slider.setValue(numberToSlider(n));
	    textField.setText(getFormattedValue(n));
	    textField.postActionEvent();
	    listeners = remember;
		//textField.setText(sliderToText().toString());
	}


	Vector<ActionListener> listeners;
	
	public void addActionListener(ActionListener l)	{
		if (listeners == null)	listeners = new Vector<ActionListener>();
		if (listeners.contains(l)) return;
		listeners.add(l);
	}
	
	public void removeActionListener(ActionListener l)	{
		if (listeners == null)	return;
		listeners.remove(l);
	}

	public Vector<ActionListener> getActionListeners() {
		return listeners;
	}

	
	public void fireActionPerformed()	{
		if (listeners == null) return;
		if (!listeners.isEmpty())	{
			ActionEvent ae = new ActionEvent(this, 0, "");
			for (int i = 0; i<listeners.size(); ++i)	{
				ActionListener l = (ActionListener) listeners.get(i);
				l.actionPerformed(ae);
			}
		}
	}
	
	public static class Integer extends TextSlider<java.lang.Integer>	{
		public Integer(String l, int orientation, int min, int max, int value)	{
			this(l, orientation, min, max, value, DEFAULT_SLIDER_COMPOSITION);
		}

		public Integer(String l, int orientation, int min, int max, int value, SliderComposition sliderComp) {
			this(l, orientation, min, max, value, min, max, value, sliderComp);
		}
		
		public Integer(String l, int orientation, 
			int min, int max, int value, 
			int sliderMin, int sliderMax, int sliderValue, 
			SliderComposition sliderComp) {
			super(l, orientation, min, max, value, sliderMin, sliderMax, sliderValue, sliderComp);
		}
		java.lang.Integer sliderToText() {
			return super.slider.getValue();
		}
			
		int textToSlider()	{
			return java.lang.Integer.valueOf(super.textField.getText());
		}
		
		int numberToSlider(java.lang.Integer val)	{
			return val;	
		}
		
		void adjustSliderMinMax() {
			int foo = textToSlider();
			if (foo > super.slider.getMaximum()) setMax(foo); 
			if (foo < super.slider.getMinimum()) setMin(foo); 
		}
		@Override
		String getFormattedValue(java.lang.Integer n) {
			return String.format("%d",n);
		}
		@Override
		public void setMax(java.lang.Integer max) {
		    Vector<ActionListener> remember = listeners;
		    listeners = null;
			String text=super.textField.getText();
			super.slider.setMaximum(max);
			super.textField.setText(text);
		    listeners=remember;
			super.setMax(max); 
		}
		@Override
		public void setMin(java.lang.Integer min) {
		    Vector<ActionListener> remember = listeners;
		    listeners = null;
			String text=super.textField.getText();
			super.slider.setMinimum(min);
			super.textField.setText(text);
		    listeners=remember;
			super.setMin(min); 
		}

		@Override
		public void setValue(java.lang.Integer n) {
			if (n > super.slider.getMaximum()) setMax(n);
			if (n < super.slider.getMinimum()) setMin(n); 		
			super.setValue(n);
		}
	}
	
	private static double scaler = 10E8;
	public static class Double extends TextSlider<java.lang.Double>	{
		public Double(String l, int orientation, double min, double max, double value)	{
			this(l, orientation, min, max, value, DEFAULT_SLIDER_COMPOSITION);
		}
		public Double(String l, int orientation, double min, double max, double value, SliderComposition sliderComp)	{
			super(l, orientation, min, max, value, 
					0, (int) scaler,  ((int) (scaler*(value-min)/(max-min))), sliderComp);
		}
		
		@Override
		java.lang.Double sliderToText() {
			return sliderToDouble(super.slider.getValue());
		}
		
		@Override
		int textToSlider()	{
			return numberToSlider(java.lang.Double.valueOf(super.textField.getText()));
		}
		
		@Override
		int numberToSlider(java.lang.Double val)	{
			return ((int) (scaler * (val-super.min)/(super.max-super.min)));			
		}
		
		double sliderToDouble(int val) { 
			return (super.min + (super.max-super.min)*(val/scaler)); 
		}

		
		@Override
		void adjustSliderMinMax() {
			double val= java.lang.Double.valueOf(super.textField.getText());
			//System.err.println("value is "+val);
			if (val > super.max) {
				setMax(val);
//				System.err.println("Setting max to "+max);
			}
			if (val < super.min) {
				setMin(val); 
//				System.err.println("Setting min to "+min);
			}
		}
						
		String getFormattedValue(java.lang.Double n) {
			return String.format("%8.4g",n);
		}
	}
	
	public static class DoubleLog extends TextSlider.Double	{
		public DoubleLog(String l, int orientation, double min, double max, double value)	{
			this(l, orientation, min, max,value,DEFAULT_SLIDER_COMPOSITION);
		}
		public DoubleLog(String l, int orientation, double min, double max, double value, SliderComposition sliderComp)	{
			super(l, orientation, min, max, (max-min)*(Math.log(value/min)/Math.log(max/min)),sliderComp);
			if (min < 0 || max < 0)
				throw new IllegalArgumentException("DoubleLog slider only accepts positive limits");
		}
		
		@Override
		int numberToSlider(java.lang.Double val)	{
			double f = Math.log(val.doubleValue()/getMin())/Math.log(getMax()/getMin());
			int ret = ((int) (scaler * f));
			return ret;		
		}
		
		@Override
		double sliderToDouble(int val)		{ 
			double f = val/scaler;
			double a = Math.pow(getMin(), 1.0-f);
			double b = Math.pow(getMax(), f);
			double ret = a*b; 
			return ret;
		}

				
	}

	public static class IntegerLog extends TextSlider.DoubleLog	{
		
		
		public IntegerLog(String l, int orientation, double min, double max, double value) {
			super(l, orientation, min, max, value);
		}

		@Override
		double sliderToDouble(int val)		{ 
			double f = val/scaler;
			double a = Math.pow(getMin(), 1.0-f);
			double b = Math.pow(getMax(), f);
			// have to correct some round-off errors due to integer conversion + log distortions
			int iret = (int) (a*b+.5);
			double ret = (double) iret;
			return ret;
		}

		@Override
		String getFormattedValue(java.lang.Double n) {
			System.err.println("integerlog format = "+n);
			return String.format("%8d",((int)(n+.001)));
		}
				
	}

}
