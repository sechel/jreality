package de.jreality.scene;

import java.awt.Color;

import de.jreality.scene.event.*;

/**
 * LightSoft is the super class to all lights in the scene.
 * It carries a color and an intensity as the only common properties of all lights.
 * @author <a href="mailto:hoffmann@math.tu-berlin.de">Tim Hoffmann</a>
 * 
 * TODO: make lights immutable - make a LightFactory
 * 
 */
public abstract class Light extends SceneGraphNode {
  
  private transient LightListener lightListener;
  private transient boolean lightChanged;
  
  private Color color= new Color(1f, 1f, 1f);
  private double intensity = 0.75;
  private boolean global =true;

	public Color getColor() {
    startReader();
    try {
      return color;
    } finally {
      finishReader();
    }
	}
	public void setColor(Color color) {
    startWriter();
    if (this.color != color) fireLightChanged();
		this.color = color;
    finishWriter();
	}
  
  /**
   * get the current color of this light as a triple of floats, premultiplied by the intensity factor.
   * @return float[]
   */
  public float[] getScaledColorAsFloat() {
    float[] cc = getColor().getRGBColorComponents(null);
    for (int i = 0; i<3; ++i)  cc[i] *= intensity;
    return cc;
  }

    /**
     * Get this light's intensity
     * @return double the intensity
     */
    public double getIntensity() {
      startReader();
      try {
        return intensity;
      } finally {
        finishReader();
      }
    }

    /**
     * Sets the intensity.
     * @param intensity the intensity
     */
    public void setIntensity(double intensity) {
      startWriter();
      if (this.intensity != intensity) fireLightChanged();
      this.intensity = intensity;
      finishWriter();
    }

    /**
     * @return Returns wether the light is global for the scene.
     */
    public boolean isGlobal() {
      startReader();
      try {
        return global;
      } finally {
        finishReader();
      }
    }

    /**
     * @param global: setting wether the light is global for the scene.
     */
    public void setGlobal(boolean global) {
      startWriter();
      if (this.global != global) fireLightChanged();
      this.global = global;
      finishWriter();
    }
 
    public void accept(SceneGraphVisitor v) {
      startReader();
      try {
        v.visit(this);
      } finally {
        finishReader();
      }
    }
  
    static void superAccept(Light l, SceneGraphVisitor v) {
      l.superAccept(v);
    }
    private void superAccept(SceneGraphVisitor v) {
      super.accept(v);
    }

    public void addLightListener(LightListener listener) {
      startReader();
      lightListener=LightEventMulticaster.add(lightListener, listener);
      finishReader();
    }

    public void removeLightListener(LightListener listener) {
      startReader();
      lightListener=LightEventMulticaster.remove(lightListener, listener);
      finishReader();
    }
    
    protected void fireLightChanged() {
      lightChanged=true;
    }
    
    protected void fireLightChangedImpl() {
      if (lightListener != null) lightListener.lightChanged(new LightEvent(this));
    }

    protected void writingFinished() {
      if (lightChanged) fireLightChangedImpl();
      lightChanged=false;
    }

}
