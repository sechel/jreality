package de.jreality.audio;

public interface AudioAttributes {
	public static final String DIRECTIONLESS_PROCESSOR_KEY = "directionlessProcessor";
	public static final String SPEED_OF_SOUND_KEY = "speedOfSound";
	public static final String VOLUME_GAIN_KEY = "volumeGain";
	public static final String DISTANCE_CUE_KEY = "distanceCue";
	
	public static final float DEFAULT_GAIN = 1f;
	public static final float DEFAULT_SPEED_OF_SOUND = 332f;
	public static final DistanceCue DEFAULT_DISTANCE_CUE = DistanceCue.DEFAULT_CUE;
}
