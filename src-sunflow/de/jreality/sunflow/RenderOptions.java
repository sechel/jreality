package de.jreality.sunflow;

import java.util.prefs.Preferences;

public class RenderOptions {
	private String bakingInstance = "";
	private boolean progessiveRender = true;
	private boolean useOriginalLights = false;
	private boolean threadsLowPriority = false;
	private double ambientOcclusionBright = .2f;
	private int aaMin = -2;
	private int aaMax = 0;
	private int ambientOcclusionSamples = 16;
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
	
	public void savePreferences(Preferences prefs, String prefix) {
		prefs.putBoolean(prefix+"progressiveRender", progessiveRender);
		prefs.putBoolean(prefix+"useOriginalLights", useOriginalLights);
		prefs.putInt(prefix+"aaMin", aaMin);
		prefs.putInt(prefix+"aaMax", aaMax);
		prefs.putInt(prefix+"depthsDiffuse", depthsDiffuse);
		prefs.putInt(prefix+"depthsReflection", depthsReflection);
		prefs.putInt(prefix+"depthsRefraction", depthsRefraction);
		prefs.putInt(prefix+"ambientOcclusionSamples", ambientOcclusionSamples);
		prefs.putDouble(prefix+"ambientOcclusionBright", ambientOcclusionBright);
	}
	
	public void restoreFromPreferences(Preferences prefs, String prefix, RenderOptions defaults) {
		setProgessiveRender(prefs.getBoolean(prefix+"progressiveRender", defaults.isProgessiveRender()));
		setUseOriginalLights(prefs.getBoolean(prefix+"useOriginalLights", defaults.isUseOriginalLights()));
		setAaMin(prefs.getInt(prefix+"aaMin", defaults.getAaMin()));
		setAaMax(prefs.getInt(prefix+"aaMax", defaults.getAaMax()));
		setDepthsDiffuse(prefs.getInt(prefix+"depthsDiffuse", defaults.getDepthsDiffuse()));
		setDepthsReflection(prefs.getInt(prefix+"depthsReflection", defaults.getDepthsReflection()));
		setDepthsRefraction(prefs.getInt(prefix+"depthsRefraction", defaults.getDepthsRefraction()));
		setAmbientOcclusionSamples(prefs.getInt(prefix+"ambientOcclusionSamples", defaults.getAmbientOcclusionSamples()));
		setAmbientOcclusionBright(prefs.getDouble(prefix+"ambientOcclusionBright", defaults.getAmbientOcclusionBright()));
	}

	public String getBakingInstance() {
		return bakingInstance;
	}

	public void setBakingInstance(String bakingInstance) {
		this.bakingInstance = bakingInstance;
	}

	public boolean isThreadsLowPriority() {
		return threadsLowPriority;
	}

	public void setThreadsLowPriority(boolean threadsLowPriority) {
		this.threadsLowPriority = threadsLowPriority;
	}
}
