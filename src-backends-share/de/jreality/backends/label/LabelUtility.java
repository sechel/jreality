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
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.HashMap;
import java.util.Iterator;
import java.util.WeakHashMap;

import de.jreality.geometry.GeometryUtility;
import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.geometry.Primitives;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.scene.Appearance;
import de.jreality.scene.Geometry;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.PointSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Transformation;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.AttributeEntityUtility;
import de.jreality.scene.data.DataList;
import de.jreality.scene.data.DoubleArrayArray;
import de.jreality.scene.data.IntArrayArray;
import de.jreality.scene.data.StorageModel;
import de.jreality.scene.data.StringArray;
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
  
  private static final ReferenceQueue refQueue = new ReferenceQueue();
  private static final HashMap geometryToMaps = new HashMap();
  
  private static BufferedImage[] createImages(Geometry geom, int type, StringArray labels, Color color, Font font) {
    BufferedImage[] ret = new BufferedImage[labels.getLength()];
    for (Object ref=refQueue.poll(); ref != null; ref=refQueue.poll()) {
      geometryToMaps.remove(ref);
    }
    HashMap keyToImageMap = (HashMap) geometryToMaps.get(geom);
    if (keyToImageMap == null) {
      keyToImageMap = new HashMap();
      geometryToMaps.put(geom, keyToImageMap);
    }
    Key key = new Key(geom, type, font, color);
    HashMap strToImages = (HashMap) keyToImageMap.get(key);
    if (strToImages == null) {
      strToImages = new HashMap();
      keyToImageMap.put(key, strToImages);
    }
    for (int i = 0, n=labels.getLength(); i < n; i++) {
      String str = labels.getValueAt(i);
      ret[i] = (BufferedImage) strToImages.get(str);
      if (ret[i] == null) {
        ret[i] = createImageFromString(str, font, color);
        strToImages.put(str, ret[i]);
      }
    }
    return ret;
  }
  
  public static BufferedImage[] createPointImages(PointSet ps, Font f, Color c) {
    DataList dl = ps.getVertexAttributes(Attribute.LABELS);
    if (dl == null) return null;
    StringArray sa = dl.toStringArray();
    return createImages(ps, Key.TYPE_POINTS, sa, c, f);
  }
  
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
    
    private static class Key {
      
      final static int TYPE_POINTS=0;
      final static int TYPE_EDGES=2;
      final static int TYPE_FACES=3;
      
      private Geometry geometry;
      private int type;
      private Font font;
      private Color color;

      private final int hash;
      
      Key(Geometry g, int t, Font f, Color c) {
        if (t<0 || t>3) throw new IllegalArgumentException("no such type");
        geometry=g; type=t; font=f; color=c;
        hash = 37*37*37*c.hashCode()+37*37*g.hashCode()+37*f.hashCode()+t;
      }
      
      public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Key)) return false;
        Key k = (Key) obj;
        return k.hashCode() == hashCode() && k.geometry.equals(geometry) && k.type == type && k.font.equals(font) && k.color.equals(color);
      }
      
      public int hashCode() {
        return hash;
      }
    }
  }
