/*
 * Created on Jan 2, 2004
 *
 */
package de.jreality.jogl;

/**
 * @author gunn
 * This class is a utility class for describing how accessory arrays such as normals
 * and colors are assigned to the underlying geometry.  There are three alternatives:
 * PER_PART,  PER_FACE, and PER_VERTEX.
 */
public class ElementBinding {

		public final static int PER_PART = 1;
		public final static int PER_FACE = 2;
		public final static int PER_VERTEX = 4;
		public final static int PER_EDGE = 8;
		protected int binding;
		/**
		 * 
		 */
		public ElementBinding() {
			this(PER_VERTEX);
		}
		
		public ElementBinding(int value)	{
			super();
			binding = value;
		}
		/**
		 * @return
		 */
		public int getBinding() {
			return binding;
		}

		/**
		 * @param i
		 */
		public void setBinding(int i) {
			binding = i;
		}
		
		public void setVertexBinding()	{
			binding = PER_VERTEX;
		}

		public void setFaceBinding()	{
			binding = PER_FACE;
		}

		public void setPartBinding()	{
			binding = PER_PART;
		}
		
		public boolean isVertexBinding(int b)	{
			return b == PER_VERTEX;
		}

		public boolean isFaceBinding(int b)	{
			return b == PER_FACE;
		}

		public boolean isPartBinding(int b)	{
			return b == PER_PART;
		}


}
