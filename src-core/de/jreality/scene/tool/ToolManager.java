/*
 * Created on Apr 3, 2005
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
package de.jreality.scene.tool;

import java.util.*;

import de.jreality.scene.Geometry;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphNode;
import de.jreality.scene.SceneGraphPath;

/**
 * 
 * @author weissman
 *  
 */
public class ToolManager {

    private final HashSet toolsWithPick = new HashSet();
    private final HashMap toolToPaths = new HashMap();
    
    boolean addTool(Tool tool, SceneGraphPath path) {
    	boolean first = pathsForTool(tool).isEmpty();
        if (!pathsForTool(tool).contains(path))
        	pathsForTool(tool).add(path);	// clone path, perhaps?
        else
            throw new IllegalStateException("Tool "+tool+" already registered with path="+path);
        if (!tool.getActivationSlots().isEmpty() && !toolsWithPick.contains(tool))
        	toolsWithPick.add(tool);
        return first;
    }
    
    boolean removeTool(Tool tool, SceneGraphPath path) {
      if (pathsForTool(tool).contains(path))
        pathsForTool(tool).remove(path);
      else
        throw new IllegalStateException();
      if (pathsForTool(tool).isEmpty()) {
      	if (!tool.getActivationSlots().isEmpty())
          toolsWithPick.remove(tool);
        return true;
      }
      return false;
    }
    
    void cleanUp() {
        toolToPaths.clear();
        toolsWithPick.clear();        
    }

    /**
     * @return all tools in the viewer's scene
     */
    Set getTools() {
        return Collections.unmodifiableSet(toolToPaths.keySet());
    }

    /**
     * @param candidate
     * @return
     */
    boolean needsPick(Tool candidate) {
        return toolsWithPick.contains(candidate);
    }

    private List pathsForTool(Tool t) {
        if (!toolToPaths.containsKey(t)) {
            toolToPaths.put(t, new LinkedList());
        }
        return (List) toolToPaths.get(t);
    }

    SceneGraphPath getPathForTool(Tool tool, SceneGraphPath pickPath) {
        if (pickPath == null) {
            if (pathsForTool(tool).size() != 1)
                    throw new IllegalStateException(
                            "ambigous path without pick");
            return (SceneGraphPath) pathsForTool(tool).get(0);
        }
        for (Iterator i = pathsForTool(tool).iterator(); i.hasNext();) {
            SceneGraphPath path = (SceneGraphPath) i.next();
            if (pickPath.startsWith(path)) return path;
        }
        return null;
    }

    Collection selectToolsForPath(SceneGraphPath pickPath, int depth, HashSet candidates) {  
        for(Iterator iter = pickPath.reverseIterator(depth); iter.hasNext();) {
            SceneGraphNode node = (SceneGraphNode) iter.next();
            List tools;
            if (node instanceof SceneGraphComponent) {
              tools = ((SceneGraphComponent) node).getTools();
            } else continue;
            List copy = new LinkedList();
            copy.addAll(tools);
            copy.retainAll(candidates);
            if (!copy.isEmpty()) return copy;
        }
        return Collections.EMPTY_SET;
    }

}
