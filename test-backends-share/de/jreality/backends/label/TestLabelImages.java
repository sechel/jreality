package de.jreality.backends.label;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class TestLabelImages extends JPanel {

	
	public void paintComponent(Graphics g) {
		Image i = LabelUtility.createImageFromString("hello World p g",
				new Font("Times New Roman",Font.ITALIC,23), Color.RED );
		g.drawImage(i,0,0,null);
	}

	public static void main(String[] args) {
		JFrame f = new JFrame("test label Images");
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.getContentPane().add(new TestLabelImages());
		f.setSize(300,300);
		f.setVisible(true);

	}

}
