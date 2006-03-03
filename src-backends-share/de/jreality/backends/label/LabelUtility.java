package de.jreality.backends.label;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.image.BufferedImage;

public class LabelUtility {
	
	private static FontRenderContext frc;
	
	//TODO is there a better way to get a FontRenderContext???
	static {
		BufferedImage bi = new BufferedImage(1,1,BufferedImage.TYPE_INT_ARGB);
		frc = bi.createGraphics().getFontRenderContext();
	}
  private LabelUtility() {}

  
  private static Color TRANSPARENT = new Color(0,0,0,0);
  
  public static Image createImageFromString(String s, Font f,Color color) {
	  Rectangle r = f.getStringBounds(s,frc).getBounds();
	  BufferedImage img = new BufferedImage(r.width,r.height,BufferedImage.TYPE_INT_ARGB);
	  Graphics2D g = (Graphics2D) img.getGraphics();
	  g.setBackground(TRANSPARENT);
	  g.clearRect(0,0,r.width,r.height);
	  g.setColor(color);
	  g.setFont(f);
    LineMetrics lineMetrics = f.getLineMetrics(s,frc);
    float border = lineMetrics.getAscent()-lineMetrics.getDescent();
    g.drawString(s,0,r.height-(int)(border/2.));
	  return img;
  }
  
}
