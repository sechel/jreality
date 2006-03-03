package de.jreality.shader;

import java.awt.Color;
import java.awt.Font;

public interface DefaultTextShader extends TextShader {

	final static Color DIFFUSE_COLOR_DEFAULT = Color.BLACK;
	final static double SCALE_DEFAULT = 1.;
	
	final static Font FONT_DEFAULT = new Font("Sans Serif",Font.PLAIN,12);
	
	Color getDiffuseColor();
	void setDiffuseColor(Color c);
	  
	double getScale();
	void setScale(double s);
	
	Font getFont();
	void setFont(Font f);
	
}
