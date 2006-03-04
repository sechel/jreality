package de.jreality.shader;

import java.awt.Color;
import java.awt.Font;

public interface DefaultTextShader extends TextShader {

  Object CREATE_DEFAULT=new Object();
  
	final static Color DIFFUSE_COLOR_DEFAULT = Color.BLACK;
	final static double SCALE_DEFAULT = 0.05;
	
	final static Font FONT_DEFAULT = new Font("Sans Serif",Font.PLAIN,12);
	
	Color getDiffuseColor();
	void setDiffuseColor(Color c);
	  
	Double getScale();
	void setScale(Double s);
	
	Font getFont();
	void setFont(Font f);
	
}
