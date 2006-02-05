/*
 * Created on Apr 10, 2005
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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import de.jreality.math.MatrixBuilder;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Transformation;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DoubleArray;
import de.jreality.scene.data.StorageModel;
import de.jreality.util.LoggingSystem;


/**
 *
 * TODO: comment this
 *
 * @author weissman
 *
 */
public class TranslateTool extends Tool {

    List activationSlots = new LinkedList();
    List usedSlots = new LinkedList();
    
    InputSlot activate = InputSlot.getDevice("TranslateActivation");
    InputSlot trafo = InputSlot.getDevice("DeltaTranslation");
    
    public TranslateTool() {
        activationSlots.add(activate);
        usedSlots.add(trafo);
    }
    
    public List getActivationSlots() {
        return activationSlots;
    }

    public List getCurrentSlots() {
        return usedSlots;
    }

    public List getOutputSlots() {
        return Collections.EMPTY_LIST;
    }

    SceneGraphComponent comp;

    public void activate(ToolContext tc) {
//        usedSlots.add(trafo);
        comp = tc.getRootToLocal().getLastComponent();
        if (comp.getTransformation() == null) comp.setTransformation(new Transformation());
    }

    public void perform(ToolContext tc) {
        MatrixBuilder.euclidian(comp.getTransformation())
        .times(tc.getTransformationMatrix(trafo).toDoubleArray(null))
        .assignTo(comp);
    }

    public void deactivate(ToolContext tc) {
//        usedSlots.remove(trafo);
    }
}
