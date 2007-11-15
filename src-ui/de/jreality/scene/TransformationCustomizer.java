package de.jreality.scene;

import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.Customizer;
import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.text.NumberFormatter;

import de.jreality.math.FactoredMatrix;
import de.jreality.scene.event.TransformationEvent;
import de.jreality.scene.event.TransformationListener;


/**
 * Transformation inspector.
 * 
 * @author msommer
 */
public class TransformationCustomizer  extends JPanel implements Customizer, TransformationListener {

	private Transformation transformation;
	private FactoredMatrix matrix;
	
	private static final int X=0, Z=2, ANGLE=3;  //Y=1
	private static final int TRANSLATION=0, ROTATION=1, SCALE=2;
	
	private double[] translation, rotation, scale;
	private double angle;
	private Entry[] tEntries, rEntries, sEntries;
	private String[] labels = {"X", "Y", "Z", "angle"};
	

	public TransformationCustomizer() {
		this(null);
	}

	public TransformationCustomizer(Transformation t) {
		
		//set up layout
//		Box tBox = Box.createHorizontalBox();
		JPanel tBox = new JPanel(new GridLayout(1,5));
		JPanel rBox = new JPanel(new GridLayout(1,5));
		JPanel sBox = new JPanel(new GridLayout(1,5));
//		tBox.setAlignmentY(JComponent.LEFT_ALIGNMENT);
//		tBox.setBorder(BorderFactory.createTitledBorder("translation"));
//		Box rBox = Box.createHorizontalBox();
//		rBox.setBorder(BorderFactory.createTitledBorder("rotation"));
//		Box sBox = Box.createHorizontalBox();
//		sBox.setBorder(BorderFactory.createTitledBorder("scale"));
		Font f = new Font("Helvetica", Font.PLAIN, 12);
		JLabel label = new JLabel("translate", JLabel.LEFT);
//		label.setFont(f);
		tBox.add(label);
		label = new JLabel("rotate", JLabel.LEFT);
		label.setFont(f);
		rBox.add(label);
		label = new JLabel("scale", JLabel.LEFT);
		label.setFont(f);
		sBox.add(label);
		tEntries = new Entry[3];
		rEntries = new Entry[4];
		sEntries = new Entry[3];
		for (int i=X; i<=Z; i++) {
			tBox.add(tEntries[i] = new Entry(labels[i], 0, TRANSLATION));
			rBox.add(rEntries[i] = new Entry(labels[i], 0, ROTATION));
			sBox.add(sEntries[i] = new Entry(labels[i], 0, SCALE));
		}
		tBox.add(Box.createHorizontalGlue());
		rBox.add(rEntries[ANGLE] = new Entry(labels[ANGLE], angle, ROTATION));
//		rBox.add(Box.createVerticalGlue());
		sBox.add(Box.createHorizontalGlue());
		Box vbox = Box.createVerticalBox();
		vbox.add(tBox);
		vbox.add(rBox);
		vbox.add(sBox);
		add(vbox);
		
//		addComponentListener(new ComponentAdapter() {
//			public void componentShown(ComponentEvent e) {
//				System.out.println(".componentShown()");
//			}
//		});
		
		if (t!=null) setObject(t);
	}

	
	public void setObject(Object t) {
		if (transformation!=null) transformation.removeTransformationListener(this);
		transformation = (Transformation) t;
		transformation.addTransformationListener(this);
		
		update();
	}
	
	
	private void update() {
		
		matrix = new FactoredMatrix(transformation.getMatrix());
		
		translation = matrix.getTranslation();
		rotation = matrix.getRotationAxis();
		angle = matrix.getRotationAngle();
		scale = matrix.getStretch();
		//update entries
		for (int i=X; i<=Z; i++) {
			tEntries[i].setValue(translation[i]);
			rEntries[i].setValue(rotation[i]);
			sEntries[i].setValue(scale[i]);
		}
		rEntries[ANGLE].setValue(angle);
		
		repaint();
	}
	
	
	private void updateTransformation(int type) {
		switch (type) {
		case TRANSLATION:
			for (int i=X; i<=Z; i++) translation[i] = tEntries[i].getValue();
			matrix.setTranslation(translation);
			break;
		case ROTATION:
			for (int i=X; i<=Z; i++) rotation[i] = rEntries[i].getValue();
			angle = rEntries[ANGLE].getValue();
			matrix.setRotation(angle, rotation);
			break;
		case SCALE:
			for (int i=X; i<=Z; i++) scale[i] = sEntries[i].getValue();
			matrix.setStretch(scale);
			break;
		}
		transformation.setMatrix(matrix.getArray());
	}
	
	
	public void transformationMatrixChanged(TransformationEvent ev) {
//		if (isShowing()) 
		update();
	}
	

//----------------------------------------------------------------------
	//testing layout
	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.getContentPane().add(new TransformationCustomizer(new Transformation("transformation")));
		
		frame.pack();
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	
//----------------------------------------------------------------------
	private class Entry extends JPanel implements ActionListener {
		private JLabel label;
		private JFormattedTextField textField;
		private NumberFormatter formatter = null;
		private NumberFormat numberFormat = null;
		private double value;
		private int type;

		public Entry(String label, double d, int type) {
			super();
			value = d;
			this.type = type;
			this.label = new JLabel(label + ":", JLabel.LEFT);
			this.label.setFont(new Font("Helvetica", Font.PLAIN, 12));

			numberFormat = NumberFormat.getNumberInstance();
			formatter = new NumberFormatter(numberFormat);
			formatter.setValueClass(Double.class);
			textField = new JFormattedTextField(formatter);
			textField.setFont(new Font("Courier", Font.PLAIN, 12));
			textField.setValue(new Double(d));
			textField.setColumns(8); // get some space
			textField.addActionListener(this);
			Box box = Box.createHorizontalBox();
			box.setBorder(BorderFactory.createEtchedBorder());
			box.add(this.label);
			box.add(Box.createHorizontalStrut(4));
			box.add(textField);
			add(box);
		}

		public void actionPerformed(ActionEvent arg0) {
			updateValue();
			updateTransformation(type);
		}

		public void updateValue() {
			if (!textField.isEditValid()) {  //text is invalid.
				Toolkit.getDefaultToolkit().beep();
				textField.selectAll();
			} else
				try { //text is valid
					textField.commitEdit();  //so use test
					java.lang.Double dd = (java.lang.Double) textField.getValue();
					value = dd.doubleValue();
				} catch (java.text.ParseException exc) {
					exc.printStackTrace();
				}
		}

		public void setValue(double d) {
			textField.setValue(new Double(d));
			value = d;
		}

		public double getValue() {
			return value;
		}
		
	}//end of class Entry

}
