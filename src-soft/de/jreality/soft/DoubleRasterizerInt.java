/*
 * Created on Dec 5, 2003
 *
 * This file is part of the jReality package.
 * 
 * This program is free software; you can redistribute and/or modify 
 * it under the terms of the GNU General Public License as published 
 * by the Free Software Foundation; either version 2 of the license, or
 * any later version.
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITTNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License 
 * along with this program; if not, write to the 
 * Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307
 * USA 
 */
package de.jreality.soft;

//import java.awt.Graphics;
import java.util.Arrays;

/**
 * 
 * @version 1.0
 * @author timh
 * @deprecated
 */
public class DoubleRasterizerInt extends ModularDoublePolygonRasterizer {
    private static final int OPAQUE = (255 << 24);
    
  protected double zBuffer[];
  private int w= 0;
  private int h= 0;
  


  // there are two main ways to deal with color pixels:
  // an array of ints or an array of bytes three times the size.
  // note that in the latter case, the buffered image is assumed to have type
  // TYPE_3BYTE_BGR!

  //use this for bytes packed in an int
  //protected int pixels[];
  //and this for three discrete bytes per color chanel
  private int[] pixels;
  private int background;
  /**
   * 
   */
  public DoubleRasterizerInt(int[] pixelBuffer) {
    super();
    pixels=pixelBuffer;
  }

 
  protected final void setPixel(final int x, final int y, final double z, final double red, final double green, final double blue, final double transparency) {
      // set the pixel here!
      //if(x<0 ||x>=w|| y<0||y>=h) return;

      int pos= (x + w * y);
      if (z > zBuffer[pos])
          return;
      
      //        if (true)  {
      if (transparency == 0) {

          //use this if the color channels are packed in an int
          //pixels[pos]  = (255 << 24) |  (aprI>>FIXP) << 16 |  (apgI>>FIXP) << 8 | (apbI>>FIXP);

          // and this for three discrete bytes:
          pixels[pos]= OPAQUE | ((((int)red)<<16) + (((int)green)<<8) + ((int) (blue)));
      } else {
          int trans =(int)(255*transparency);
          final int sample = pixels[pos];
          int sb = sample & 0xff;
          int sg = (sample>>8) & 0xff;
          int sr = (sample>>16) & 0xff;

//          int r = (  (/*oneMinusTransparency*/((int)red) + (int)(trans*sr))*257)    &0xff0000;
//          int g = (( (/*oneMinusTransparency*/((int)green) + (int)(trans*sg))*257)>>8)&0x00ff00;
//          int b = (  (/*oneMinusTransparency*/((int)blue) + (int)(trans*sb))*257)>>16;

          int r = (  (/*oneMinusTransparency*/((int)red)*255   + (trans*sr))*257)    &0xff0000;
          int g = (( (/*oneMinusTransparency*/((int)green)*255 + (trans*sg))*257)>>8)&0x00ff00;
          int b = (  (/*oneMinusTransparency*/((int)blue)*255  + (trans*sb))*257)>>16;
          
          pixels[pos]  = OPAQUE | r | g | b;


      }
      zBuffer[pos]= z;
  }
  
  public void setBackground(int argb) {
      background=argb;
  }
  /* (non-Javadoc)
   * @see de.jreality.soft.Renderer#clear()
   */
  public void clear() {
      Arrays.fill(zBuffer, Double.MAX_VALUE);
      Arrays.fill(pixels, background);
  }

  /* (non-Javadoc)
   * @see de.jreality.soft.PolygonRasterizer#setWindow(int, int, int, int)
   */
  public void setWindow(int xmin, int xmax, int ymin, int ymax) {
    super.setWindow(xmin, xmax, ymin, ymax);
    int nw=xmax-xmin, nh=ymax-ymin;
    if(nw!=w||nh!=h) {
      w=nw;
      h=nh;
      final int numPx=w*h;
      zBuffer=new double[numPx];
    }
  }


/* (non-Javadoc)
 * @see de.jreality.soft.PolygonRasterizer#start()
 */
public void start() {
    // TODO Auto-generated method stub
    
}


/* (non-Javadoc)
 * @see de.jreality.soft.PolygonRasterizer#stop()
 */
public void stop() {
    // TODO Auto-generated method stub
    
}


  
}
