/*
 * Created on Jun 29, 2005
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

import de.jreality.scene.data.AttributeEntity;


/**
 *
 * tagging interface for a reflection map. If an appearance
 * contains this Entity, then it must have 6 textures associated with it:
 *
 * @author weissman
 *
 */
public interface CubeMap extends AttributeEntity {
  
  public static final Color BLEND_COLOR_DEFAULT = new Color(1.0f, 1.0f, 1.0f, 0.6f);
  
  public Color getBlendColor();
  public void setBlendColor(Color blendColor);
  
  public ImageData getFront();
  public ImageData getBack();
  public ImageData getTop();
  public ImageData getBottom();
  public ImageData getLeft();
  public ImageData getRight();

  public void setFront(ImageData img);
  public void setBack(ImageData img);
  public void setTop(ImageData img);
  public void setBottom(ImageData img);
  public void setLeft(ImageData img);
  public void setRight(ImageData img);
  
}
