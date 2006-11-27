package de.jreality.sunflow;

public class RenderOptions {
	private int resolutionX = 240;
	private int resolutionY = 180;
	private boolean useOriginalLights = false;
	private float globalIllumination = .2f;
	private int aaMin = -2;
	private int aaMax = 0;
	private int depthsDiffuse = 1;
	private int depthsReflection = 4;
	private int depthsRefraction = 4;
	
	public int getAaMax() {
		return aaMax;
	}
	public void setAaMax(int aaMax) {
		this.aaMax = aaMax;
	}
	public int getAaMin() {
		return aaMin;
	}
	public void setAaMin(int aaMin) {
		this.aaMin = aaMin;
	}
	public int getDepthsDiffuse() {
		return depthsDiffuse;
	}
	public void setDepthsDiffuse(int depthsDiffuse) {
		this.depthsDiffuse = depthsDiffuse;
	}
	public int getDepthsReflection() {
		return depthsReflection;
	}
	public void setDepthsReflection(int depthsReflection) {
		this.depthsReflection = depthsReflection;
	}
	public int getDepthsRefraction() {
		return depthsRefraction;
	}
	public void setDepthsRefraction(int depthsRefraction) {
		this.depthsRefraction = depthsRefraction;
	}
	public float getGlobalIllumination() {
		return globalIllumination;
	}
	public void setGlobalIllumination(float globalIllumination) {
		this.globalIllumination = globalIllumination;
	}
	public int getResolutionX() {
		return resolutionX;
	}
	public void setResolutionX(int resolutionX) {
		this.resolutionX = resolutionX;
	}
	public int getResolutionY() {
		return resolutionY;
	}
	public void setResolutionY(int resolutionY) {
		this.resolutionY = resolutionY;
	}
	public boolean isUseOriginalLights() {
		return useOriginalLights;
	}
	public void setUseOriginalLights(boolean useOriginalLights) {
		this.useOriginalLights = useOriginalLights;
	}
}
