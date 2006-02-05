/*
 * Created on Nov 4, 2004
 *
 */
package de.jreality.scene;

/**
 * A point light class.  Situated at the origin (0,0,0,1); use scene graph transformations to
 * position it where you want.
 * @author gunn
 *
 */
public class PointLight extends Light {

	// Where are these fields defined?  [10.10.05 gunn]
	// I'm trying to debug my SpotLight but since I don't know what these values are intended to
	// represent, I'm having to guess.
	private double falloffA0 = 0.5;
	private double falloffA1 = 0.5;
	private double falloffA2 = 0;
	private boolean useShadowMap = false;
	private String shadowMap = "";
	private int shadowMapX = 512;
	private int shadowMapY = 512;

	/**
	 * @return double
	 */
	public double getFalloffA0() {
	    return falloffA0;
	}

	/**
	 * @return double
	 */
	public double getFalloffA1() {
	    return falloffA1;
	}

	/**
	 * @return double
	 */
	public double getFalloffA2() {
	    return falloffA2;
	}

	/**
	 * Sets the falloffA0.
	 * @param falloffA0 The falloffA0 to set
	 */
	public void setFalloffA0(double falloffA0) {
	    if (falloffA0 != this.falloffA0) {
	        this.falloffA0 = falloffA0;
          fireLightChanged();
	    }
	}

	/**
	 * Sets the falloffA1.
	 * @param falloffA1 The falloffA1 to set
	 */
	public void setFalloffA1(double falloffA1) {
	    if (falloffA1 != this.falloffA1) {
	        this.falloffA1 = falloffA1;
          fireLightChanged();
	    }
	}

	/**
	 * Sets the falloffA2.
	 * @param falloffA2 The falloffA2 to set
	 */
	public void setFalloffA2(double falloffA2) {
	    if (falloffA2 != this.falloffA2) {
	        this.falloffA2 = falloffA2;
          fireLightChanged();
	    }
	}

	public void setFalloff(double a0, double a1, double a2)	{
		falloffA0 = a0;
		falloffA1 = a1;
		falloffA2 = a2;
    // TODO: compare with old value?
    fireLightChanged();
	}
  
	/**
	 * @param atten
   * @deprecated do we need that method?
	 */
	public void setFalloff(double[] atten) {
		if (atten.length <= 2) 
			// TODO signal error
			return;
		falloffA0 = atten[0];
		falloffA1 = atten[1];
		falloffA2 = atten[2];
    // TODO: compare with old value?
    fireLightChanged();
	}

  /**
	 * @return String
	 */
	public String getShadowMap() {
	    return shadowMap;
	}

	/**
	 * @return boolean
	 */
	public boolean isUseShadowMap() {
	    return useShadowMap;
	}

	/**
	 * Sets the shadowMap.
	 * @param shadowMap The shadowMap to set
	 */
	public void setShadowMap(String shadowMap) {
	    this.shadowMap = shadowMap;
      // TODO: compare with old value?
      fireLightChanged();
}

	/**
	 * Sets the useShadowMap.
	 * @param useShadowMap The useShadowMap to set
	 */
	public void setUseShadowMap(boolean useShadowMap) {
	    if (useShadowMap != this.useShadowMap) {
	        this.useShadowMap = useShadowMap;
          fireLightChanged();
	    }
	}

	/**
	 * @return int
	 */
	public int getShadowMapX() {
	    return shadowMapX;
	}

	/**
	 * @return int
	 */
	public int getShadowMapY() {
	    return shadowMapY;
	}

	/**
	 * Sets the shadowMapX.
	 * @param shadowMapX The shadowMapX to set
	 */
	public void setShadowMapX(int shadowMapX) {
			this.shadowMapX = shadowMapX;
      // TODO: compare with old value?
      fireLightChanged();
	}

	/**
	 * Sets the shadowMapY.
	 * @param shadowMapY The shadowMapY to set
	 */
	public void setShadowMapY(int shadowMapY) {
	    		this.shadowMapY = shadowMapY;
          // TODO: compare with old value?
          fireLightChanged();
	    }

    public void accept(SceneGraphVisitor v) {
        v.visit(this);
      }
    static void superAccept(PointLight l, SceneGraphVisitor v) {
        l.superAccept(v);
      }
    private void superAccept(SceneGraphVisitor v) {
        super.accept(v);
      }
}
