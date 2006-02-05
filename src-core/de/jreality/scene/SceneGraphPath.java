/*
 * Created on Dec 9, 2003
 *
 * This file is part of the jReality package.
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
package de.jreality.scene;

import java.util.*;

import de.jreality.math.Rn;
import de.jreality.util.SceneGraphUtility;

/**
 * A SceneGraphPath represents a directed connection in the scene graph. Technically it is a list of 
 * SceneGraphComponents. It may also include, optionally, a SceneGraphNode contained in the final SceneGraphComponent.
 * This allows addressing of the sub-nodes contained as fields in the SceneGraphComponent (such as lights, camera, 
 * geometry, appearance). But it is not required that the path ends in such a SceneGraphNode; it can also
 * end in a SceneGraphComponent.
 * <b>Note:</b> This class takes no care of the elements being inserted. The method isValid()
 * gives information if this path exists in the scenegraph
 * @version 1.0
 * @author <a href="mailto:hoffmann@math.tu-berlin.de">Tim Hoffmann</a>
 * @author weissman cleanup
 *
 */
public class SceneGraphPath implements Cloneable {

  protected LinkedList path = new LinkedList();			// a list of SceneGraphComponents

  public static SceneGraphPath fromList(List list) {
    SceneGraphPath path=new SceneGraphPath();
    path.path.addAll(list);
    return path;
  }

	public String toString() {
    if(path.isEmpty()) return "<< empty path >>";
    StringBuffer sb = new StringBuffer();
    for(int ix=0, n=path.size(); ix < n; ix++)
    {
      sb.append(((SceneGraphNode)path.get(ix)).getName()).append(" : ");
    }
    sb.setLength(sb.length()-3);
    return sb.toString();
  }
  
  public Object clone() {
    SceneGraphPath path=new SceneGraphPath();
    path.path.addAll(this.path);
    return path;
  }
  
  public List toList() {
    return new ArrayList(path);
  }

  // TODO write own Iterator classes...
  
  public ListIterator iterator() {
    return Collections.unmodifiableList(path).listIterator();
  }
  public ListIterator iterator(int start) {
    return Collections.unmodifiableList(path).listIterator(start);
  }
  
  /**
   * 
   * @param start how many knodes from the end of the path should we leave out?
   * i.e.: p.reverseIterator(p.getLength()) gives the same result as p.reverseIterator()
   * @return a reverse iterator from the given position
   */
  public Iterator reverseIterator(int start) {
    final ListIterator iter = iterator(start);
    return new Iterator() {

      public void remove() {
          iter.remove();
      }

      public boolean hasNext() {
          return iter.hasPrevious();
      }

      public Object next() {
          return iter.previous();
      }
    };
  }

  public Iterator reverseIterator() {
    return reverseIterator(path.size());
  }

  /**
	 * Gives the length of the path
	 * @return int
	 */
	public int getLength() {
		return path.size();
	}
  
  public final void push(final SceneGraphNode c) {
    path.add(c);
  }
  /**
   * lets this path unchanged
   * @return a new path that is equal to this path after calling path.push(c)
   */
  public final SceneGraphPath pushNew(final SceneGraphNode c) {
    SceneGraphPath ret = SceneGraphPath.fromList(path);
    ret.path.add(c);
    return ret;
  }

  public final void pop() {
    path.removeLast();
  }
  /**
   * lets this path unchanged
   * @return a new path that is equal to this path after calling path.pop()
   */
  public final SceneGraphPath popNew() {
    SceneGraphPath ret = SceneGraphPath.fromList(path);
    ret.path.removeLast();
    return ret;
  }
    
  public SceneGraphNode getFirstElement() {
    return (SceneGraphNode) path.getFirst();
  }

  public SceneGraphNode getLastElement() {
    return (SceneGraphNode) path.getLast();
  }

  public SceneGraphComponent getLastComponent() {
    if (!(path.getLast() instanceof SceneGraphComponent)) return (SceneGraphComponent) path.get(path.size()-2);
    return (SceneGraphComponent) path.getLast();
  }

	public void clear()	{
		path.clear();
	}
	
  /**
   * checks if the path is really an existing path in the scenegraph.
   * 
   * @return true if the path exists
   */
	public boolean isValid()
  {
    if (path.size()==0) return true;
    Iterator i = path.iterator();
    SceneGraphNode parent = (SceneGraphNode) i.next();
    try {
  		for ( ; i.hasNext(); )	{
  			SceneGraphNode child = (SceneGraphNode) i.next();
  			if(!((SceneGraphComponent)parent).isDirectAncestor(child)) return false;
        if (i.hasNext()) parent = child;
  		}
    } catch (ClassCastException cce) {
      // this happens if a non-component node is somwhere IN the path
      return false;
    }
		return true;
	}

	public boolean equals(Object p) {
		if (p instanceof SceneGraphPath)
    		return isEqual((SceneGraphPath) p);
		return false;
	}
	
	public boolean isEqual(SceneGraphPath anotherPath)
	{
		if (anotherPath == null || path.size() != anotherPath.getLength())	return false;

		for (int i=0; i<path.size(); ++i)	{
				if (!path.get(i).equals(anotherPath.path.get(i))) return false;
		}
		return true;
	}

  public boolean startsWith(SceneGraphPath potentialPrefix) {
      if (getLength() < potentialPrefix.getLength()) return false;
      Iterator i1 = iterator();
      Iterator i2 = potentialPrefix.iterator();
      for (; i2.hasNext();) {
          if (i1.next() != i2.next()) return false;
      }
      return true;
  }
  /*** matrix calculations ***/
	
	public double[] getMatrix(double[] aMatrix)
	{
		return getMatrix(aMatrix, 0, path.size()-1);
	}

  public double[] getMatrix(double[] aMatrix, int begin)
  {
    return getMatrix(aMatrix, begin, path.size()-1);
  }

  public double[] getInverseMatrix(double[] invMatrix)
	{
		return getInverseMatrix(invMatrix, 0, path.size()-1);
	}

  public double[] getInverseMatrix(double[] invMatrix, int begin)
  {
    return getInverseMatrix(invMatrix, begin, path.size()-1);
  }

  public double[] getMatrix(double[] aMatrix, int begin, int end)
	{
		final double[] myMatrix;
		if (aMatrix == null) myMatrix = new double[16];
		else myMatrix = aMatrix;
		Rn.setIdentityMatrix(myMatrix);

    for (ListIterator it = path.listIterator(begin); it.nextIndex() <= end; )  
    {
      Object currObj = it.next();
      if (currObj instanceof SceneGraphComponent) {
        SceneGraphComponent currComp = (SceneGraphComponent)currObj;
        Transformation tt = currComp.getTransformation();
        if (tt == null) continue;
        Rn.times(myMatrix, myMatrix, tt.getMatrix());         
      }
    }
		return myMatrix;
	}
	
	public double[] getInverseMatrix(double[] aMatrix, int begin, int end)
	{
		double[] mat = getMatrix(aMatrix, begin, end);
		return Rn.inverse(mat, mat);
	}

}
