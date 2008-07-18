/*
 * Author	gunn
 * Created on Mar 30, 2005
 *
 */
package de.jreality.tutorial.util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * @author gunn
 *
 */
public abstract  class TextSlider extends JPanel  {
	public JSlider slider;
	public JLabel label;
//	public JFormattedTextField textField;
	public JTextField textField;
	protected String textContents;
	
	protected TextSlider(String l, int o, int min, int max, int c)	{
		super();
 	    //setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		max = (max < c) ? c : max;
//		System.err.println("Slider:  "+max+":"+c);
		slider = new JSlider(o, min, max, c);
		label  = new JLabel(l, JLabel.LEFT);
		Font  f = new Font("Helvetica",Font.PLAIN, 10);
	    slider.setFont(f);

		// here the constructor leaves off, the sub-classes do some sub-class specific work
		// and then call init(), which then results in the super-class init eventually being
		// called to finish the construction
	}
	
	protected void init()	{
	    textField = new JTextField();
	    textField.setText(getFormattedValue());
	    textContents = textField.getText();
	    textField.setColumns(10); //get some space
	    textField.setEditable(true);
	    textField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				textField.setForeground(Color.black);
				//System.err.println("changing color to black");
				adjustSliderMinMax();
				slider.setValue(textToSlider());
				textContents = textField.getText();
				broadcastChange();
			}
 	    	
 	    });
	    textField.addCaretListener(new CaretListener() {

			public void caretUpdate(CaretEvent e) {
				if (textField.getText().compareTo(textContents) != 0)
					textField.setForeground(Color.RED);
				//System.err.println("changing color to red");
			}
	    	
	    });
		slider.addChangeListener( new ChangeListener()	{
		    public void stateChanged(ChangeEvent e) {
			    textField.setText(getFormattedValue());
		        //textField.setText(sliderToText().toString());
		        broadcastChange();
		    }
		});
//		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		Box box = Box.createHorizontalBox();
		box.add(label);
		box.add(Box.createHorizontalStrut(8));
		box.add(textField);
		box.add(slider);
		box.add(Box.createHorizontalGlue());
//		setPreferredSize(new Dimension(400,40));
		setMaximumSize(new Dimension(10000,40));
		add(box);
	}
	
	public abstract Number getMin();
	public abstract Number getMax();
	abstract void adjustSliderMinMax();
	abstract Number sliderToText();
	abstract int textToSlider();
	abstract int numberToSlider(Number n);
	abstract String getFormattedValue();
	
	public static class Integer extends TextSlider	{
		int min, max, c;
		public Integer(String l, int o, int min, int max, int c)	{
			super(l, o, min, max, c);
			this.min = min;
			this.max = max;
			this.c = c;
			init();
		}
		Number sliderToText() {
			return new java.lang.Integer((slider.getValue()));
		}
			
		int textToSlider()	{
			java.lang.Integer dd = new java.lang.Integer(textField.getText());
			return dd.intValue();
		}
		
		int numberToSlider(Number val)	{
			return val.intValue();	
		}
		void adjustSliderMinMax() {
			int foo = textToSlider();
			if (foo > slider.getMaximum()) { max = foo; slider.setMaximum(foo); }
			if (foo < slider.getMinimum()) { min = foo; slider.setMinimum(foo); }
		}
		@Override
		String getFormattedValue() {
			return String.format("%d",sliderToText().intValue());
		}
//		@Override
//		void setMinMax(Number min, Number max) {
//			if (max.intValue() > slider.getMaximum()) { this.max = max.intValue(); slider.setMaximum(this.max); }
//			if (min.intValue() < slider.getMinimum()) { this.min = min.intValue(); slider.setMinimum(this.min); }
//			
//		}
		@Override
		public Number getMax() {
			return max;
		}
		@Override
		public Number getMin() {
			return min;
		}
				
	}
	private static double scaler = 10E8;
	public static class Double extends TextSlider	{
		double min, max, c;
		public Double(String l, int o, double min, double max, double c)	{
			super(l, o, 0, (int) scaler,  ((int) (scaler*(c-min)/(max-min)) ));
			this.min = min;
			this.max = max;
			this.c = c;
			init();
		}
		Number sliderToText() {
			return new java.lang.Double(sliderToDouble(slider.getValue()));
		}
		
		int textToSlider()	{
			java.lang.Double dd = new java.lang.Double(textField.getText());
			return numberToSlider(dd);
		}
		
		int numberToSlider(Number val)	{
			return ((int) (scaler * (val.doubleValue()-min)/(max-min)));			
		}
		
		double sliderToDouble(int val)		{ return (min + (max-min)*(val/scaler)); }

		void adjustSliderMinMax() {
			java.lang.Double dd = new java.lang.Double(textField.getText());
			double val = dd.doubleValue();
			System.err.println("value is "+val);
			
			if (val > max) {
				max  = val;
				System.err.println("Setting max to "+max);
			}
			if (val < min) {
				min = val; 
				System.err.println("Setting min to "+min);
			}
		}
						
		String getFormattedValue() {
			return String.format("%8.4g",sliderToText().doubleValue());
		}
		@Override
		public Number getMax() {
			return max;
		}
		@Override
		public Number getMin() {
			return min;
		}
				
	}
	
	public static class DoubleLog extends TextSlider.Double	{
		public DoubleLog(String l, int o, double min, double max, double c)	{
			super(l, o, min, max, (max-min)*(Math.log(c/min)/Math.log(max/min)));
			if (this.min < 0 || this.max < 0)
				throw new IllegalArgumentException("DoubleLog slider only accepts positive limits");
		    textField.setText(getFormattedValue());
		}
		
		@Override
		int numberToSlider(Number val)	{
			double f = Math.log(val.doubleValue()/min)/Math.log(max/min);
			int ret = ((int) (scaler * f));
			return ret;		
		}
		
		@Override
		double sliderToDouble(int val)		{ 
			double f = val/scaler;
			double a = Math.pow(min, 1.0-f);
			double b = Math.pow(max, f);
			double ret = a*b; 
			return ret;
		}

				
	}
	public Number getValue()	{
		return sliderToText();
	}
	public boolean changeFromOutside = false;
	public void setValue(Number n)	{
	    changeFromOutside = true;
	    Vector<ActionListener> remember = listeners;
	    listeners = null;
		slider.setValue(numberToSlider(n));
	    textField.setText(getFormattedValue());
	    changeFromOutside = false;
	    listeners = remember;
		//textField.setText(sliderToText().toString());
	}


	Vector<ActionListener> listeners;
	
	public void addActionListener(ActionListener l)	{
		if (listeners == null)	listeners = new Vector();
		if (listeners.contains(l)) return;
		listeners.add(l);
		//System.err.println("ToolManager: Adding geometry listener"+l+"to this:"+this);
	}
	
	public void removeActionListener(ActionListener l)	{
		if (listeners == null)	return;
		listeners.remove(l);
	}

	public void broadcastChange()	{
		if (listeners == null) return;
		//System.err.println("ToolManager: broadcasting"+listeners.size()+" listeners");
		if (!listeners.isEmpty())	{
			//System.err.println("ToolManager: broadcasting"+listeners.size()+" listeners");
			ActionEvent ae = new ActionEvent(this, changeFromOutside ? 1 : 0, "");
			for (int i = 0; i<listeners.size(); ++i)	{
				ActionListener l = (ActionListener) listeners.get(i);
				l.actionPerformed(ae);
			}
		}
	}

}
