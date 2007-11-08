package de.jreality.scene;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.Customizer;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;


/**
 * Transformation inspector.
 * 
 * @author msommer
 */
public class TransformationCustomizer  extends JPanel implements Customizer {

	private Transformation transformation;
	private JTextField name;


	public TransformationCustomizer() {
		this(null);
	}

	public TransformationCustomizer(Transformation t) {
		super(new BorderLayout());
		add(new JLabel("Name: "), BorderLayout.WEST);
		name = new JTextField("");
		name.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("changed");
			}
		});
		add(name);
		
		if (t!=null) setObject(t);
	}

	public void setObject(Object t) {
		transformation = (Transformation) t;
		name.setText(transformation.getName());
	}

}
