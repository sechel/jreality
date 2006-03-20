package de.jreality.shader;

import java.awt.Color;
import java.awt.Font;

public interface DefaultTextShader extends TextShader {

  Object CREATE_DEFAULT=new Object();
  
	final static Color DIFFUSE_COLOR_DEFAULT = Color.BLACK;
	final static double SCALE_DEFAULT = 0.05;
	
	final static Font FONT_DEFAULT = new Font("Sans Serif",Font.PLAIN,12);
	final static double[] OFFSET_DEFAULT = new double[]{0,0,0,1};
	final static Boolean SHOW_LABELS_DEFAULT = Boolean.TRUE;
  
	Color getDiffuseColor();
	void setDiffuseColor(Color c);
	  
	Double getScale();
	void setScale(Double s);
	
	Font getFont();
	void setFont(Font f);
  
	  double[] getOffset();
	  void setOffset(double[] o);
		
    /**
     * @deprecated
     */
	  Boolean getShowLabels();
    /**
     * @deprecated
     */
	  void setShowLabels(Boolean b);
		
}
