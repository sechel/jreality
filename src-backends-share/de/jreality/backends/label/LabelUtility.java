package de.jreality.backends.label;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.font.TextLayout;
import java.awt.image.BufferedImage;

import de.jreality.geometry.GeometryUtility;
import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.geometry.Primitives;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Transformation;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.AttributeEntityUtility;
import de.jreality.scene.data.StorageModel;
import de.jreality.shader.ImageData;
import de.jreality.shader.Texture2D;

public class LabelUtility {
	
	private static FontRenderContext frc;
	
	//TODO is there a better way to get a FontRenderContext???
	static {
		BufferedImage bi = new BufferedImage(1,1,BufferedImage.TYPE_INT_ARGB);
		frc = bi.createGraphics().getFontRenderContext();
	}
  private LabelUtility() {}

  
  private static Color TRANSPARENT = new Color(0,0,0,0);
  
  public static BufferedImage createImageFromString(String s, Font f,Color color) {
	  //Rectangle r = f.getStringBounds(s,frc).getBounds();
	  TextLayout tl = new TextLayout(s,f,frc);
	  Rectangle r = tl.getBounds().getBounds();
	  
	  BufferedImage img = new BufferedImage(r.width,r.height,BufferedImage.TYPE_INT_ARGB);
	  Graphics2D g = (Graphics2D) img.getGraphics();
	  g.setBackground(TRANSPARENT);
	  g.clearRect(0,0,r.width,r.height);
	  g.setColor(color);
	  g.setFont(f);
	  LineMetrics lineMetrics = f.getLineMetrics(s,frc);
		
	  final float border = r.height - tl.getDescent();

    g.drawString(s,0,border);
	  return img;
  }
 
  private static IndexedFaceSet bb = Primitives.texturedSquare(new double[]{0,1,0,1,1,0,1,0,0,0,0,0});

  	public static SceneGraphComponent sceneGraphForLabel(SceneGraphComponent sgc, double xscale, double yscale,double[] offset, double[] camToObj, double[] position)  {
  		if (sgc == null) sgc = new SceneGraphComponent();
  		if (sgc.getGeometry() == null) {
  			//IndexedFaceSet bb = Primitives.texturedSquare(new double[]{0,1,0,1,1,0,1,0,0,0,0,0});
  			sgc.setGeometry(bb);
  		}
  		if (sgc.getTransformation() == null)	sgc.setTransformation(new Transformation());
  		// TODO the following method isn't working correctly for the position argument!
  		sgc.getTransformation().setMatrix(P3.calculateBillboardMatrix(null, xscale, yscale, offset, camToObj,position, Pn.EUCLIDEAN ));

  		return sgc;
  	}
  }
