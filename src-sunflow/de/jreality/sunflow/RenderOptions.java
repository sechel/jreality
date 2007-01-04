package de.jreality.sunflow;

import java.util.prefs.Preferences;

public class RenderOptions {
	private boolean progessiveRender = true;
	private boolean threadsLowPriority = false;
	private double ambientOcclusionBright = .2f;
	private int aaMin = -2;
	private int aaMax = 0;
	private int ambientOcclusionSamples = 16;
	private int depthsDiffuse = 1;
	private int depthsReflection = 2;
	private int depthsRefraction = 2;
	private String giEngine = "ambocc";
	private int causticsEmit = 0;
	private int causticsGather = 50;
	private double causticsRadius = 0.5;
	private double causticsFilter = 1.1;
	
	private String shaderOverride;
	
	public int getCausticsGather() {
		return causticsGather;
	}

	public void setCausticsGather(int causticGather) {
		this.causticsGather = causticGather;
	}

	public int getCausticsEmit() {
		return causticsEmit;
	}

	public void setCausticsEmit(int causticPhotons) {
		this.causticsEmit = causticPhotons;
	}

	public double getCausticsRadius() {
		return causticsRadius;
	}

	public void setCausticsRadius(double causticRadius) {
		this.causticsRadius = causticRadius;
	}

	public String getShaderOverride() {
		return shaderOverride;
	}

	public void setShaderOverride(String shaderOverride) {
		this.shaderOverride = shaderOverride;
	}

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
		prefs.put(prefix+"giEngine", giEngine);
		prefs.putBoolean(prefix+"progressiveRender", progessiveRender);
		prefs.putInt(prefix+"aaMin", aaMin);
		prefs.putInt(prefix+"aaMax", aaMax);
		prefs.putInt(prefix+"depthsDiffuse", depthsDiffuse);
		prefs.putInt(prefix+"depthsReflection", depthsReflection);
		prefs.putInt(prefix+"depthsRefraction", depthsRefraction);
		prefs.putInt(prefix+"ambientOcclusionSamples", ambientOcclusionSamples);
		prefs.putDouble(prefix+"ambientOcclusionBright", ambientOcclusionBright);
		prefs.putInt(prefix+"causticsEmit", causticsEmit);
		prefs.putInt(prefix+"causticsGather", causticsGather);
		prefs.putDouble(prefix+"causticsRadius", causticsRadius);
		prefs.putDouble(prefix+"causticsFilter", causticsFilter);
	}
	
	public void restoreFromPreferences(Preferences prefs, String prefix, RenderOptions defaults) {
		setProgessiveRender(prefs.getBoolean(prefix+"progressiveRender", defaults.isProgessiveRender()));
		setAaMin(prefs.getInt(prefix+"aaMin", defaults.getAaMin()));
		setAaMax(prefs.getInt(prefix+"aaMax", defaults.getAaMax()));
		setDepthsDiffuse(prefs.getInt(prefix+"depthsDiffuse", defaults.getDepthsDiffuse()));
		setDepthsReflection(prefs.getInt(prefix+"depthsReflection", defaults.getDepthsReflection()));
		setDepthsRefraction(prefs.getInt(prefix+"depthsRefraction", defaults.getDepthsRefraction()));
		setAmbientOcclusionSamples(prefs.getInt(prefix+"ambientOcclusionSamples", defaults.getAmbientOcclusionSamples()));
		setAmbientOcclusionBright(prefs.getDouble(prefix+"ambientOcclusionBright", defaults.getAmbientOcclusionBright()));
		setGiEngine(prefs.get(prefix+"giEngine", defaults.getGiEngine()));
		setCausticsEmit(prefs.getInt(prefix+"causticsEmit", defaults.getCausticsEmit()));
		setCausticsGather(prefs.getInt(prefix+"causticsGather", defaults.getCausticsGather()));
		setCausticsRadius(prefs.getDouble(prefix+"causticsRadius", defaults.getCausticsRadius()));
		setCausticsFilter(prefs.getDouble(prefix+"causticsFilter", defaults.getCausticsFilter()));
	}

	public boolean isThreadsLowPriority() {
		return threadsLowPriority;
	}

	public void setThreadsLowPriority(boolean threadsLowPriority) {
		this.threadsLowPriority = threadsLowPriority;
	}

	public String getGiEngine() {
		return giEngine;
	}

	public void setGiEngine(String giEngine) {
		this.giEngine = giEngine;
	}

	public double getCausticsFilter() {
		return causticsFilter;
	}

	public void setCausticsFilter(double causticFilter) {
		this.causticsFilter = causticFilter;
	}
}
