package de.jreality.sunflow.core.camera;

import org.sunflow.SunflowAPI;
import org.sunflow.core.CameraLens;
import org.sunflow.core.ParameterList;
import org.sunflow.core.Ray;

public class OrthogonalLens implements CameraLens {

	private float aspect, fov;

	public OrthogonalLens() {
		fov = 90;
		aspect = 1;
		update();
	}

	public boolean update(ParameterList pl, SunflowAPI api) {
		// get parameters
		fov = pl.getFloat("fov", fov);
		aspect = pl.getFloat("aspect", aspect);
		update();
		return true;
	}

	private void update() {
		fov = (float) Math.tan(Math.toRadians(fov * 0.5f));
	}

	public Ray getRay(float x, float y, int imageWidth, int imageHeight, double lensX, double lensY, double time) {
//		System.out.println("x="+x+" y="+y);
		x= (x-imageWidth/2) / (imageWidth*fov);
		y= (y-imageHeight/2) / (imageHeight*fov*aspect);
		return new Ray(x, y, 0, 0, 0, -1);
	}
}