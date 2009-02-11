package de.jreality.tutorial.intro;

import java.io.IOException;
import java.net.URL;

import de.jreality.reader.Readers;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.ui.viewerapp.ViewerApp;
import de.jreality.util.CameraUtility;
import de.jreality.util.Input;

/**
 * This class contains the second in a series of 8 simple introductory examples which mimic the
 * functionality of the 
 * <a href="http://www3.math.tu-berlin.de/jreality/mediawiki/index.php/User_Tutorial"> jReality User Tutorial 
 *</a>.  ViewerApp with dodecahedron, navigator, and bean shell.
 *
 * @author Charles Gunn
 *
 */
public class Intro02 {

	public static void main(String[] args)	{
				ViewerApp va = ViewerApp.display(readDodec());
				va.setAttachNavigator(true);
				va.setExternalNavigator(false);
				va.update();
				CameraUtility.encompass(va.getCurrentViewer());
	}

	private static SceneGraphComponent readDodec() {
		URL url = Intro02.class.getResource("dodec.off");
		SceneGraphComponent scp = null;
		try {
			scp = Readers.read(Input.getInput(url));
// alternative to access the file as a URL
//			scp = Readers.read(Input.getInput("http://www3.math.tu-berlin.de/jreality/download/data/dodec.off"));
			scp.setName("Dodecahedron");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return scp;
	}

}
