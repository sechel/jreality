/*
 * Created on Aug 18, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package de.jreality.jogl.anim;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

/**
 * @author gunn
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class AnimationManager implements Animated {

	Vector anims;	// the objects we're managing
	Hashtable keyFrames;	// a hash-table from the anims into their key-frame values
	
	AnimationManager()	{
		super();
		anims = new Vector();
		keyFrames = new Hashtable();
	}
	public void setKeyFrameAtTime(double t) {
		for (Iterator iter = anims.iterator(); iter.hasNext(); )	{
			
		}
		
	}
	
	public void setValueAtTime(double t) {
	}

	public void addAnimated(Animated a)	{
		if (anims.indexOf(a) == -1) anims.add(a);
		keyFrames.put(a, new Vector());
	}
	
	public void removedAnimated(Animated a)	{
		anims.remove(a);
		keyFrames.remove(a);
	}
}
