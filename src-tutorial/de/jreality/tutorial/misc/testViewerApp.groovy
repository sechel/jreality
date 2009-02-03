// a sample groovy script using jReality classes
import de.jreality.geometry.*
import de.jreality.scene.*
import de.jreality.util.*
import de.jreality.ui.viewerapp.*

    sgc = new SceneGraphComponent("test")
    sgc.setGeometry(Primitives.icosahedron())
    ap = new Appearance()
    ap.setAttribute("polygonShader.diffuseColor", java.awt.Color.RED)
    sgc.setAppearance(ap)
    va = new ViewerApp(sgc)
    va.update()
    va.display()
    CameraUtility.encompass(va.getViewer())
