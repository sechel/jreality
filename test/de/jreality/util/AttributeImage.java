/*
 * Created on Apr 21, 2005
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
package de.jreality.util;

import java.awt.Component;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.logging.Level;

import javax.imageio.ImageIO;

import de.jreality.reader.Input;
import de.jreality.scene.Texture2D;

/**
 * 
 * TODO: comment this
 * 
 * @author weissman
 *  
 */
public class AttributeImage implements Serializable {

  private transient Image img;
  private byte[] byteArray;
  private int width;
  private int height;

  public static AttributeImage load(Input input) throws IOException {
    return new AttributeImage(loadInput(input));
  }

  private static Image loadInput(Input in) throws IOException {
    String urlString = in.toString().toLowerCase();
    if (urlString.endsWith(".jpeg") || urlString.endsWith(".jpg")
        || urlString.endsWith(".gif") || urlString.endsWith(".png"))
        return Toolkit.getDefaultToolkit().getImage(in.toURL());
    LoggingSystem.getLogger(Texture2D.class).log(Level.INFO,
        "loading " + in + " trying ImageIO");
    Image img = ImageIO.read(in.toURL());
    if (img == null)
      throw new IOException("read failed: " + in);
    else
      return img;
  }

  public AttributeImage(byte[] data, int width, int height) {
    byteArray = new byte[data.length];
    this.width = width;
    this.height = height;
  }

  public AttributeImage(Image img) {
    this(img, null);
  }

  public AttributeImage(Image img, int width, int height) {
    this(img, width, height, null);
  }

  public AttributeImage(Image img, double[] channelMatrix) {
    this(img, img.getWidth(null), img.getHeight(null), channelMatrix);
  }

  public AttributeImage(Image img, int width, int height, double[] channelMatrix) {
    if (width == -1 || height == -1) {
      long st = System.currentTimeMillis();
      wait(img);
      st = System.currentTimeMillis() - st;
      LoggingSystem.getLogger(this).log(Level.FINER,
          "waited {0} ms for image loading.", new Long(st));
      width = img.getWidth(null);
      height = img.getHeight(null);
    }
    this.width = width;
    this.height = height;
    readBytes(img, channelMatrix);
  }

  static void wait(Image img) {
    MediaTracker t = new MediaTracker(new Component() {
    });
    t.addImage(img, 0);
    for (boolean gotIt = false; !gotIt;)
      try {
        t.waitForAll();
        gotIt = true;
      } catch (InterruptedException e) {
        throw new Error();
      }
  }

  private Image createImage() {
    BufferedImage bi = new BufferedImage(width, height,
        BufferedImage.TYPE_INT_ARGB);
    WritableRaster raster = bi.getRaster();
    int[] pix = new int[4];
    for (int y = 0, ptr = 0; y < height; y++)
      for (int x = 0; x < width; x++, ptr += 4) {
        pix[0] = byteArray[ptr + 3];
        pix[1] = byteArray[ptr];
        pix[2] = byteArray[ptr + 1];
        pix[3] = byteArray[ptr + 2];
        raster.setPixel(x, y, pix);
      }
    return bi;
  }

  private void readBytes(Image theImage, double[] channelArithmeticMatrix) {
    if (byteArray == null) {
      int[] pixelsI = new int[width * height];
      PixelGrabber p = new PixelGrabber(theImage, 0, 0, width, height, pixelsI,
          0, width);
      try {
        p.grabPixels();
      } catch (InterruptedException e) {
      }
      int num = pixelsI.length << 2;
      byteArray = new byte[num];
      for (int i = 0, j = 0; j < num; i++, j += 4) {
        final int px = pixelsI[i];
        byteArray[j + 3] = (byte) (px >>> 24);
        byteArray[j] = (byte) (px >>> 16);
        byteArray[j + 1] = (byte) (px >>> 8);
        byteArray[j + 2] = (byte) px;
      }
      if (channelArithmeticMatrix != null) {
        double[] pixel = new double[4];
        for (int j = 0; j < num; j += 4) {
          for (int i = 0; i < 4; ++i)
            pixel[i] = (double) byteArray[j + i];
          Rn.matrixTimesVector(pixel, channelArithmeticMatrix, pixel);
          for (int i = 0; i < 4; ++i)
            byteArray[j + i] = (byte) pixel[i];
        }
      }
    }
  }

  public Image getImage() {
    return img == null ? img = createImage() : img;
  }

  public byte[] getByteArray() {
    //System.arraycopy(byteArray, 0, handoutByteArray, 0, byteArray.length);
    return byteArray;
  }

  public String toString() {
    return "AttributeImage: width=" + width + " height=" + height;
  }
  public int getHeight() {
    return height;
  }
  public int getWidth() {
    return width;
  }
}
