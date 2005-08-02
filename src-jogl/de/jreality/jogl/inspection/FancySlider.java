/*
 * Author	gunn
 * Created on Mar 30, 2005
 *
 */
package de.jreality.jogl.inspection;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.ParseException;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.NumberFormatter;

/**
 * @author gunn
 *
 */
public abstract  class FancySlider extends JPanel implements PropertyChangeListener {
	public JSlider slider;
	public JLabel label;
	public JFormattedTextField textField;
    NumberFormatter formatter = null;
    java.text.NumberFormat numberFormat = null;
	
	protected FancySlider(String l, int o, int min, int max, int c)	{
		super();
 	    setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		slider = new JSlider(o, min, max, c);
		//JOGLConfiguration.theLog.log(Level.FINER,"Initializing slider with min/max: "+min+"/"+max);
		label  = new JLabel(l, JLabel.LEFT);
		// here the constructor leaves off, the sub-classes do some sub-class specific work
		// and then call init(), which then results in the super-class init eventually being
		// called to finish the construction
	}
	
	protected void init()	{
	    textField = new JFormattedTextField(formatter);
	    textField.setValue(sliderToText());
	    textField.setColumns(8); //get some space
	    textField.addPropertyChangeListener(this);
 	    textField.getInputMap().put(KeyStroke.getKeyStroke(
                KeyEvent.VK_ENTER, 0),
                "check");
 	    // when the text field changes, update the value
	    textField.getActionMap().put("check", new AbstractAction() {
	   		final JFormattedTextField tf = textField;
			public void actionPerformed(ActionEvent e) {
				if (!tf.isEditValid()) { //The text is invalid.
					Toolkit.getDefaultToolkit().beep();
					tf.selectAll();
				} else try {                    //The text is valid,
					tf.commitEdit();     //so use it.
				} catch (java.text.ParseException exc) {exc.printStackTrace();}
			}
			});
	    // listen to slider and update text field
		slider.addChangeListener( new ChangeListener()	{
		    public void stateChanged(ChangeEvent e) {
		        JSlider source = (JSlider)e.getSource();
		        if (source.getValueIsAdjusting()) {
		        		textField.setValue(sliderToText());
		        } else {
		        		try {
						textField.commitEdit();
					} catch (ParseException e1) {
						e1.printStackTrace();
					}
		        }
		    }
		});
		// TODO
		// These alignment calls seem to have no effect
//		label.setAlignmentX(1.0f);
//		textField.setAlignmentX(0.6f);
//		slider.setAlignmentX(0.4f);
	    setMaximumSize(new Dimension(10000, 40));
	    setPreferredSize(new Dimension(10000, 40));
//		label.setMinimumSize(new Dimension( 50, 30));
//		textField.setMinimumSize(new Dimension( 50, 30));
//		slider.setMinimumSize(new Dimension( 50, 30));
		label.setPreferredSize(new Dimension( 100, 30));
		textField.setPreferredSize(new Dimension( 50, 30));
		slider.setPreferredSize(new Dimension( 200, 30));
		label.setMaximumSize(label.getPreferredSize());
		textField.setMaximumSize(textField.getPreferredSize());
		slider.setMinimumSize(new Dimension(100, 30));
		slider.setMaximumSize(new Dimension(1000, 30));
		Box box = new Box(SwingConstants.HORIZONTAL);
		box.add(label);
		//add(Box.createHorizontalGlue());
		box.add(textField);
		//add(Box.createHorizontalGlue());
		box.add(slider);	
		//add(Box.createHorizontalGlue());
		add(box);
	}
	
	abstract Number sliderToText();
	abstract int textToSlider();
	
	public static class Integer extends FancySlider	{
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
			java.lang.Integer dd = (java.lang.Integer) textField.getValue();
			return dd.intValue();
		}
		protected void init()	{
		    numberFormat =   java.text.NumberFormat.getIntegerInstance();
		    formatter = new NumberFormatter(numberFormat);
		    formatter.setMinimum(new java.lang.Integer(min));
		    formatter.setMaximum(new java.lang.Integer(max));	
			super.init();
		}
	}
	 private static double scaler = 10E8;
	public static class Double extends FancySlider	{
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
			java.lang.Double dd = (java.lang.Double) textField.getValue();
			double val = dd.doubleValue();
			return ((int) (scaler * (val-min)/(max-min)));
		}
		private  double sliderToDouble(int val)		{ return (min + (max-min)*(val/scaler)); }

		protected void init()	{
		    numberFormat =   java.text.NumberFormat.getNumberInstance();
		    formatter = new NumberFormatter(numberFormat);
		    formatter.setMinimum(new java.lang.Double(min));
		    formatter.setMaximum(new java.lang.Double(max));
		    super.init();
		}
		/**
		 * 
		 */
		public void setMinimum(double d) {
			min = d;
			formatter.setMinimum(new java.lang.Double(min));
		}

		public void setMaximum(double d) {
			max = d;
			formatter.setMaximum(new java.lang.Double(max));
		}

		
	}
	
	   /**
     * Listen to the text field.  This method detects when the
     * value of the text field (not necessarily the same
     * number as you'd get from getText) changes.
     */
    public void propertyChange(PropertyChangeEvent e) {
        if ("value".equals(e.getPropertyName())) {
            Number value = (Number)e.getNewValue();
            if (slider != null && value != null) {
                slider.setValue(textToSlider());
                //JOGLConfiguration.theLog.log(Level.FINER,"Setting slider to value: "+slider.getValue());
            }
        }
    }


}
