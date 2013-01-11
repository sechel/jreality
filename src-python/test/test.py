from de.jreality.plugin import JRViewerUtility
from de.jreality.geometry import Primitives

content = JRViewerUtility.getContentPlugin(C);
content.setContent(Primitives.torus(0.4, 0.1, 20, 20));