package org.sunflow.core.light;

import org.sunflow.SunflowAPI;
import org.sunflow.core.LightSample;
import org.sunflow.core.LightSource;
import org.sunflow.core.ParameterList;
import org.sunflow.core.Ray;
import org.sunflow.core.ShadingState;
import org.sunflow.image.Color;
import org.sunflow.math.Point3;
import org.sunflow.math.Vector3;

public class DirectionalLight implements LightSource {
	private Vector3 dir;
	private Color power;

	public DirectionalLight() {
		dir = new Vector3(0, 1, 0);
		power = Color.WHITE;
	}

	public boolean update(ParameterList pl, SunflowAPI api) {
		dir = pl.getVector("dir", dir);
		dir.normalize();
		power = pl.getColor("power", power);
		return true;
	}

	public int getNumSamples() {
		return 1;
	}

	public boolean isVisible(ShadingState state) {
		Vector3 n = state.getNormal();
		return (Vector3.dot(dir, n) > 0.0);
	}

	public void getSample(int i, ShadingState state, LightSample dest) {
		// prepare shadow ray
		dest.setShadowRay(new Ray(state.getPoint(), dir));
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
