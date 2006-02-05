/*
 * Created on Apr 20, 2005
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

import java.awt.Color;

import de.jreality.math.Matrix;
import de.jreality.scene.Transformation;
import de.jreality.scene.data.AttributeEntity;

/**
 * 
 * TODO: comment this
 * 
 * @author weissman
 *  
 */
public interface Texture2D extends AttributeEntity {

  // we share constants with OpenGL
  // texture formats
  public static final int GL_RGB = 0x1907;
  public static final int GL_RGBA = 0x1908;
  public static final int GL_ALPHA = 0x1906;
  public static final int GL_LUMINANCE = 0x1909;
  public static final int GL_LUMINANCE_ALPHA = 0x190A;
  public static final int GL_INTENSITY = 0x8049;
  // types of application of texture
  public static final int GL_DECAL = 0x2101;
  public static final int GL_MODULATE = 0x2100;
  public static final int GL_REPLACE = 0x1E01;
  public static final int GL_BLEND = 0x0BE2;
  public static final int GL_ADD = 0x0104;
  public static final int GL_COMBINE = 0x8570;
  public static final int GL_COMBINE_RGB = 0x8571;
  public static final int GL_COMBINE_ALPHA = 0x8572;
  public static final int GL_RGB_SCALE = 0x8573;
  public static final int GL_ALPHA_SCALE = 0x0D1C;
  public static final int GL_ADD_SIGNED = 0x8574;
  public static final int GL_INTERPOLATE = 0x8575;
  public static final int GL_CONSTANT = 0x8576;
  public static final int GL_PRIMARY_COLOR = 0x8577;
  public static final int GL_PREVIOUS = 0x8578;
  public static final int GL_SOURCE0_RGB = 0x8580;
  public static final int GL_SOURCE1_RGB = 0x8581;
  public static final int GL_SOURCE2_RGB = 0x8582;
  public static final int GL_SOURCE0_ALPHA = 0x8588;
  public static final int GL_SOURCE1_ALPHA = 0x8589;
  public static final int GL_SOURCE2_ALPHA = 0x858A;
  public static final int GL_OPERAND0_RGB = 0x8590;
  public static final int GL_OPERAND1_RGB = 0x8591;
  public static final int GL_OPERAND2_RGB = 0x8592;
  public static final int GL_OPERAND0_ALPHA = 0x8598;
  public static final int GL_OPERAND1_ALPHA = 0x8599;
  public static final int GL_OPERAND2_ALPHA = 0x859A;
  public static final int GL_SUBTRACT = 0x84E7;
  public static final int GL_DOT3_RGB = 0x86AE;
  public static final int GL_DOT3_RGBA = 0x86AF;
  public static final int GL_ONE = 1;
  public static final int GL_SRC_COLOR = 0x0300;
  public static final int GL_ONE_MINUS_SRC_COLOR = 0x0301;
  public static final int GL_SRC_ALPHA = 0x0302;
  public static final int GL_ONE_MINUS_SRC_ALPHA = 0x0303;
  public static final int GL_DST_ALPHA = 0x0304;
  public static final int GL_ONE_MINUS_DST_ALPHA = 0x0305;
  public static final int GL_DST_COLOR = 0x0306;
  public static final int GL_ONE_MINUS_DST_COLOR = 0x0307;
  public static final int GL_SRC_ALPHA_SATURATE = 0x0308;
  // types of filtering of texture image
  public static final int GL_NEAREST = 0x2600;
  public static final int GL_LINEAR = 0x2601;
  public static final int GL_NEAREST_MIPMAP_NEAREST = 0x2700;
  public static final int GL_NEAREST_MIPMAP_LINEAR = 0x2702;
  public static final int GL_LINEAR_MIPMAP_NEAREST = 0x2701;
  public static final int GL_LINEAR_MIPMAP_LINEAR = 0x2703;
  // types of behavior outside of [0,1]x[0,1]
  public static final int GL_MIRRORED_REPEAT =  0x8370;
  public static final int GL_REPEAT = 0x2901;
  public static final int GL_CLAMP = 0x2900;
  public static final int GL_CLAMP_TO_EDGE = 0x812F;
  public static final int CLAMP = 0;
  public static final int REPEAT = 1;

  // default values
  public static final double SSCALE_DEFAULT=1;
  public static final double TSCALE_DEFAULT=1;
  public static final int REPEAT_S_DEFAULT=GL_REPEAT;
  public static final int REPEAT_T_DEFAULT=GL_REPEAT;

  public static final int APPLY_MODE_DEFAULT = GL_MODULATE;
  public static final int MAG_FILTER_DEFAULT = GL_LINEAR;
  public static final int MIN_FILTER_DEFAULT = GL_LINEAR;
  public static final int COMBINE_MODE_DEFAULT = GL_INTERPOLATE;

  public static final Matrix TEXTURE_MATRIX_DEFAULT=new Matrix();
  public static final Color BLEND_COLOR_DEFAULT=Color.WHITE;
  
  /**
   * @return Returns the repeatS.
   */
  public abstract int getRepeatS();

  /**
   * @param repeatS
   *          The repeatS to set.
   */
  public abstract void setRepeatS(int repeatS);

  /**
   * @return Returns the repeatT.
   */
  public abstract int getRepeatT();

  /**
   * @param repeatT
   *          The repeatT to set.
   */
  public abstract void setRepeatT(int repeatT);

  /**
   * @return
   */
  public abstract int getMagFilter();

  /**
   * @return
   */
  public abstract int getMinFilter();

  /**
   * @param i
   */
  public abstract void setMagFilter(int i);

  /**
   * @param i
   */
  public abstract void setMinFilter(int i);

  /**
   * @return
   */
  public abstract Matrix getTextureMatrix();

  /**
   * @param ds
   */
  public abstract void setTextureMatrix(Matrix matrix);

  /**
   * @return Returns the applyMode.
   */
  public abstract int getApplyMode();

  /**
   * @param applyMode
   *          The applyMode to set.
   */
  public abstract void setApplyMode(int applyMode);

  public abstract Color getBlendColor();

  public abstract void setBlendColor(Color blendColor);

  public abstract int getCombineMode();

  public abstract void setCombineMode(int combineMode);
  
  public abstract void setImage(ImageData image);
  
  public abstract ImageData getImage();
}