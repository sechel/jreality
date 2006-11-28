package org.sunflow.core.light;

import org.sunflow.SunflowAPI;
import org.sunflow.core.LightSample;
import org.sunflow.core.LightSource;
import org.sunflow.core.ParameterList;
import org.sunflow.core.Ray;
import org.sunflow.core.ShadingState;
import org.sunflow.image.Color;
import org.sunflow.math.OrthoNormalBasis;
import org.sunflow.math.Point3;
import org.sunflow.math.Vector3;

public class DirectionalLight implements LightSource {
	private Vector3 dir;
	private Color power;
	
	private double distortion=0.000089;
	
	private int samples=8;

    OrthoNormalBasis onb;

	public DirectionalLight() {
		dir = new Vector3(0, 1, 0);
		power = Color.WHITE;
		onb = OrthoNormalBasis.makeFromW(dir);
	}

	public boolean update(ParameterList pl, SunflowAPI api) {
		dir = pl.getVector("dir", dir);
		samples = pl.getInt("samples", 8);
		dir.normalize();
		onb = OrthoNormalBasis.makeFromW(dir);
		power = pl.getColor("power", power);
		return true;
	}

	public int getNumSamples() {
		return samples;
	}

	public boolean isVisible(ShadingState state) {
		Vector3 n = state.getNormal();
		return (Vector3.dot(dir, n) > 0.0);
	}

	public void getSample(int i, ShadingState state, LightSample dest) {
		// prepare shadow ray
		Vector3 lightDir=dir;
		if (distortion != 0) {
	        double h = 1-state.getRandom(i, 0)*distortion;
	        double theta = 2*state.getRandom(i, 1)*Math.PI;
	        double us=Math.sqrt(1-h*h);
	        
	        float l1 = (float)(us*Math.cos(theta));
	        float l2 = (float)(us*Math.sin(theta));
	        float l3 = (float)h;
	
	        lightDir = new Vector3(l1, l2, l3);
	                
	        onb.transform(lightDir);
		}
		dest.setShadowRay(new Ray(state.getPoint(), lightDir));
        dest.getShadowRay().setMax(Float.MAX_VALUE);
		dest.setRadiance(power, power);
		dest.traceShadow(state);
	}

	public void getPhoton(double randX1, double randY1, double randX2, double randY2, Point3 p, Vector3 dir, Color power) {
	}

	public boolean isAdaptive() {
		return false;
	}

	public float getPower() {
		return power.getLuminance();
	}
}
