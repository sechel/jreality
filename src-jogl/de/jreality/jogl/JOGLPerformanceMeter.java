package de.jreality.jogl;

public class JOGLPerformanceMeter {
	transient private static boolean collectFrameRate = true;
	transient protected double framerate;
	transient protected int nodeCount = 0;
	transient int frameCount = 0;
	transient long[] history = new long[20], clockTime = new long[20];
	transient long beginRenderTime;

	private JOGLRenderer jr;

	protected JOGLPerformanceMeter(JOGLRenderer jr) {
		this.jr = jr;
	}

	protected void beginFrame() {
		nodeCount = jr.renderingState.polygonCount = 0;
		if (collectFrameRate)
			beginRenderTime = System.currentTimeMillis();
	}

	protected void endFrame() {
		++frameCount;
		int j = (frameCount % 20);
		clockTime[j] = beginRenderTime;
		history[j] = System.currentTimeMillis() - beginRenderTime;
	}

	public double getFramerate() {
		long totalTime = 0;
		for (int i = 0; i < 20; ++i)
			totalTime += history[i];
		framerate = 20 * 1000.0 / totalTime;
		return framerate;
	}

	public double getClockrate() {
		int j = frameCount % 20;
		int k = (frameCount + 1) % 20;
		long totalTime = clockTime[j] - clockTime[k];
		double clockrate = 20 * 1000.0 / totalTime;
		return clockrate;

	}

}
