// a sample groovy script using jReality classes

package de.jreality.tutorial.misc

import de.jreality.geometry.*
import de.jreality.scene.*
import de.jreality.util.*
import de.jreality.plugin.*

    sgc = new SceneGraphComponent("test")
    sgc.setGeometry(Primitives.icosahedron())
    ap = new Appearance()
    ap.setAttribute("polygonShader.diffuseColor", java.awt.Color.RED)
    sgc.setAppearance(ap)
    Viewer v = JRViewer.display(sgc)
