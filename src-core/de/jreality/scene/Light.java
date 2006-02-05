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
  
    Color color;
    double intensity = 0.75f;
    private boolean global =true;
    /**
     * Constructor for LightSoft. Initializes the color to [1,1,1].
     */
    public Light() {
        super();
        color = new java.awt.Color(1f, 1f, 1f);
    }

    /**
     * get the current color of this light as a triple of floats.
     * @return float[]
     */
    public float[] getColorAsFloat() {
        return color.getRGBComponents(null);
    }
    
    /**
     * get the current color of this light as a triple of floats, premultiplied by the intensity factor.
     * @return float[]
     */
    public float[] getScaledColorAsFloat() {
    		float[] cc = color.getRGBComponents(null);
    		for (int i = 0; i<3; ++i)	 cc[i] *= intensity;
        return cc;
    }
    

    /**
     * set the color of this light. The argument is a triple of floats.
     * @param newColor
     * @deprecated
     */
    public void setColorAsFloat(float[] newColor) {
        checkReadOnly();
        color = new java.awt.Color(newColor[0], newColor[1], newColor[2]);
    }

    
	public Color getColor() {
		return color;
	}
	public void setColor(Color color) {
		this.color = color;
    // TODO: compare with old value?
    fireLightChanged();
	}
    /**
     * Get this light's intensity
     * @return double the intensity
     */
    public double getIntensity() {
        return intensity;
    }

    /**
     * Sets the intensity.
     * @param intensity the intensity
     */
    public void setIntensity(double intensity) {
        this.intensity = intensity;
        // TODO: compare with old value?
        fireLightChanged();
    }

 
    public void accept(SceneGraphVisitor v) {
      v.visit(this);
    }
  
    static void superAccept(Light l, SceneGraphVisitor v) {
      l.superAccept(v);
    }
    private void superAccept(SceneGraphVisitor v) {
      super.accept(v);
    }
    /**
     * @return Returns wether the light is global for the scene.
     */
    public boolean isGlobal() {
        return global;
    }

    /**
     * @param global: setting wether the light is global for the scene.
     */
    public void setGlobal(boolean global) {
        this.global = global;
        // TODO: compare with old value?
        fireLightChanged();
    }

    public void addLightListener(LightListener listener) {
      lightListener=
        LightEventMulticaster.add(lightListener, listener);
    }

    public void removeLightListener(LightListener listener) {
      lightListener=
        LightEventMulticaster.remove(lightListener, listener);
    }
    
    protected void fireLightChanged() {
      final LightListener cl = lightListener;
      if (cl != null) cl.lightChanged(new LightEvent(this));
    }


}
