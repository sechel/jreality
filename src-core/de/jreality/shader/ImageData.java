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
package de.jreality.shader;

import java.awt.*;
import java.awt.image.*;
import java.io.IOException;
import java.io.Serializable;
import java.util.logging.Level;

import javax.imageio.ImageIO;

import de.jreality.math.Rn;
import de.jreality.util.Input;
import de.jreality.util.LoggingSystem;


/**
 * 
 * This class covers data of an image either 
 * created as a {@link java.awt.Image} or as a
 * {@link byte[]} array containing RGBA values 
 * for each pixel (row/column order ?).
 * 
 * If bytes or Image is available use a Constructor -
 * otherwise use a factory method to load an image
 * from an {@link de.jreality.util.Input} source
 * 
 * <p>
 * Note: this class is immutable. The Constructors 
 * that have an image parameter just extract the data
 * from the given image and don't reference it any longer.
 * byte[] data is copied.
 * 
 * Pending: do we want to keep the original image
 * to pass out as RO instance?
 * 
 * @author weissman 
 */
public class ImageData implements Serializable {

  private transient byte[] handOutArray;
  private transient Image img;
  private byte[] byteArray;
  private int width;
  private int height;

  public static ImageData load(Input input) throws IOException {
    return new ImageData(loadInput(input));
  }

  public static ImageData load(Input input, double[] channelMatrix) throws IOException {
    return new ImageData(loadInput(input), channelMatrix);
  }
  
  private static Image loadInput(Input in) throws IOException {
    String urlString = in.toString().toLowerCase();
    if (urlString.endsWith(".jpeg") || urlString.endsWith(".jpg")
        || urlString.endsWith(".gif") || urlString.endsWith(".png"))
        return Toolkit.getDefaultToolkit().getImage(in.toURL());
    LoggingSystem.getLogger(ImageData.class).log(Level.INFO,
        "loading " + in + " trying ImageIO");
    Image img = ImageIO.read(in.toURL());
    if (img == null)
      throw new IOException("read failed: " + in);
    else
      return img;
  }

  public ImageData(byte[] data, int width, int height) {
    if (data.length != 4*width*height)
      throw new IllegalArgumentException("data doesn't match image dimensions"); 
    byteArray = new byte[data.length];
    System.arraycopy(data, 0, byteArray, 0, data.length);
    this.width = width;
    this.height = height;
  }

  public ImageData(Image img) {
    this(img, null);
  }

  public ImageData(Image img, int width, int height) {
    this(img, width, height, null);
  }

  public ImageData(Image img, double[] channelMatrix) {
    this(img, img.getWidth(null), img.getHeight(null), channelMatrix);
  }

  public ImageData(Image img, int width, int height, double[] channelMatrix) {
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
    return new ROImage(bi);
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

  public int getHeight() {
    return height;
  }
  public int getWidth() {
    return width;
  }
  /**
   * @return a readonly instance of the Image
   */
  public Image getImage() {
    return img == null ? img = createImage() : img;
  }
  /**
   * TODO: change this to ByteArray ?
   * @return a copy of the byte data
   */
  public byte[] getByteArray() {
    if (handOutArray == null) handOutArray=(byte[]) byteArray.clone();
    else System.arraycopy(byteArray, 0, handOutArray, 0, byteArray.length);
    return handOutArray;
  }

  /**
   * applies the given matrix to all pixel values.
   *
   * @param channelArithmeticMatrix the matrix to multiply the byte array with
   * @return the transformed byte array 
   */
  public byte[] getByteArray(double[] channelArithmeticMatrix) {
    int numBytes = byteArray.length;
    byte[] ret = new byte[numBytes];
    if (channelArithmeticMatrix != null) {
      double[] pixel = new double[4];
      for (int j = 0; j < numBytes; j += 4) {
        for (int i = 0; i < 4; ++i)
          pixel[i] = (double) byteArray[j + i];
        Rn.matrixTimesVector(pixel, channelArithmeticMatrix, pixel);
        for (int i = 0; i < 4; ++i)
          ret[j + i] = (byte) pixel[i];
      }
    }
    return ret;
  }
  
  public String toString() {
    return "ImageData: width=" + width + " height=" + height;
  }
  /**
   * this class prevents a created BufferedImage from being changed
   */
  private final class ROImage extends Image {
    private final Image img;
    ROImage(Image img) {
      this.img=img;
    }
    public boolean equals(Object obj) {
      return img.equals(obj);
    }
    public void flush() {
      img.flush();
    }
    public Graphics getGraphics() {
      return img.getGraphics();
    }
    public int getHeight(ImageObserver observer) {
      return img.getHeight(observer);
    }
    public Object getProperty(String name, ImageObserver observer) {
      return img.getProperty(name, observer);
    }
    public Image getScaledInstance(int width, int height, int hints) {
      return img.getScaledInstance(width, height, hints);
    }
    public ImageProducer getSource() {
      return img.getSource();
    }
    public int getWidth(ImageObserver observer) {
      return img.getWidth(observer);
    }
    public int hashCode() {
      return img.hashCode();
    }
    public String toString() {
      return img.toString();
    }
  }
}
