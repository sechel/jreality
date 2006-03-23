/*
 * Created on Aug 16, 2004
 *
 */
package de.jreality.jogl.pick;

import java.util.Comparator;


/**
 * @author gunn
 *
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
