package de.jreality.sunflow;

public class RenderOptions {
	private boolean useOriginalLights = false;
	private double globalIllumination = .2f;
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
	
	public double getGlobalIllumination() {
		return globalIllumination;
	}
	
	public void setGlobalIllumination(double globalIllumination) {
		this.globalIllumination = globalIllumination;
	}

	public boolean isUseOriginalLights() {
		return useOriginalLights;
	}
	
	public void setUseOriginalLights(boolean useOriginalLights) {
		this.useOriginalLights = useOriginalLights;
	}
}
