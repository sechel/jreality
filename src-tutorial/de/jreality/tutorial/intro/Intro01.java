package de.jreality.tutorial.intro;

import de.jreality.plugin.BasicViewer;
import de.jreality.ui.viewerapp.ViewerApp;

/**
 * This class contains the first in a series of 8 simple introductory examples which mimic the
 * functionality of the 
 * <a href="http://www3.math.tu-berlin.de/jreality/mediawiki/index.php/User_Tutorial"> jReality User Tutorial 
 *</a>.  
 *
 * @author Charles Gunn
 *
 */
public class Intro01 {


	public static void main(String[] args)	{
		if (args.length == 0) 
			BasicViewer.display(null);
		else ViewerApp.display(null);

	}



}
