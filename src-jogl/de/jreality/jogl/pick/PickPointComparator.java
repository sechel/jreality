/*
 * Created on Aug 16, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package de.jreality.jogl.pick;

import java.util.Comparator;

/**
 * @author gunn
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class PickPointComparator implements Comparator {

		private PickPointComparator()	{
			super();
		}
		public static PickPointComparator sharedInstance = null;
		static 	{
			sharedInstance = new PickPointComparator();
		}
		
		public int compare(Object o1, Object o2) {
			if (((PickPoint) o1).pointNDC[2] > ((PickPoint) o2).pointNDC[2]) return 1;
			else if (((PickPoint) o1).pointNDC[2] == ((PickPoint) o2).pointNDC[2]) return 0;
			else return -1;
		}

}
