/*
 * Created on Dec 8, 2004
 *
 * This file is part of the de.jreality.remote.util package.
 * 
 * This program is free software; you can redistribute and/or modify 
 * it under the terms of the GNU General Public License as published 
 * by the Free Software Foundation; either version 2 of the license, or
 * any later version.
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITTNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License 
 * along with this program; if not, write to the 
 * Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307
 * USA 
 */
package de.jreality.remote.util;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiUnavailableException;
import java.util.Timer;
import java.util.TimerTask;


/**
 * @author weissman
 *
 **/
public class SoundUtil {


	static MidiChannel channel;
	static Synthesizer synth = null;

	static {
		try {
		  synth = MidiSystem.getSynthesizer();
		} catch (MidiUnavailableException e) {
		  System.out.println("no midi");
		}
		if (synth != null) {
		  try {
		    synth.open();
		  } catch (MidiUnavailableException e) {
		    System.out.println("no midi");
		  }
		  channel = synth.getChannels()[0];
		}
	}

	public static void play(final int key, int program, int note, int delay) {
	  synchronized(channel) {
	    channel.programChange(program);
	    channel.noteOn(key, note);
	  }
	  Timer timer = new Timer();
	  TimerTask task = new TimerTask() {
	    public void run() {
	      synchronized(channel) {
	        channel.noteOff(key);
	      }
	    }
	  };
	  timer.schedule(task, delay);
	}
	
	public static void play(int key, int delay) {
		play(key, 112, 64, delay);
	}
	 
}
