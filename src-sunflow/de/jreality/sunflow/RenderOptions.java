package de.jreality.sunflow;

public class RenderOptions {
	private boolean progessiveRender = true;
	private boolean useOriginalLights = false;
	private double ambientOcclusionBright = .2f;
	private int aaMin = -2;
	private int aaMax = 0;
	private int ambientOcclusionSamples = 120;
	private int depthsDiffuse = 1;
	private int depthsReflection = 0;
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
	
	public double getAmbientOcclusionBright() {
		return ambientOcclusionBright;
	}
	
	public void setAmbientOcclusionBright(double globalIllumination) {
		this.ambientOcclusionBright = globalIllumination;
	}

	public boolean isUseOriginalLights() {
		return useOriginalLights;
	}
	
	public void setUseOriginalLights(boolean useOriginalLights) {
		this.useOriginalLights = useOriginalLights;
	}

	public int getAmbientOcclusionSamples() {
		return ambientOcclusionSamples;
	}

	public void setAmbientOcclusionSamples(int aaSamples) {
		this.ambientOcclusionSamples = aaSamples;
	}

	public boolean isProgessiveRender() {
		return progessiveRender;
	}

	public void setProgessiveRender(boolean progessiveRender) {
		this.progessiveRender = progessiveRender;
	}
}
