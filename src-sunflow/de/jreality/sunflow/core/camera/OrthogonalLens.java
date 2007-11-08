package de.jreality.sunflow.core.camera;

import org.sunflow.SunflowAPI;
import org.sunflow.core.CameraLens;
import org.sunflow.core.ParameterList;
import org.sunflow.core.Ray;

public class OrthogonalLens implements CameraLens {

	private float aspect, fov;
    
	public boolean update(ParameterList pl, SunflowAPI api) {
		// get parameters
        fov = pl.getFloat("fov", fov);
        aspect = pl.getFloat("aspect", aspect); //?
        return true;
    }

    public Ray getRay(float x, float y, int imageWidth, int imageHeight, double lensX, double lensY, double time) {
    	System.out.println("x="+x+" y="+y);
    	x-=(imageWidth/2);
    	y-=(imageHeight/2);
        return new Ray(x/700, y/700, 0, 0, 0, -1);
    }
}