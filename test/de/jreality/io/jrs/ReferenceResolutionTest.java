package de.jreality.io.jrs;

import java.io.InputStream;

import org.junit.Test;

import de.jreality.reader.ReaderJRS;
import de.jreality.util.Input;

public class ReferenceResolutionTest {

	@Test
	public void testResolveSceneRootReference() throws Exception {
		ReaderJRS r = new ReaderJRS();
		InputStream in = getClass().getResourceAsStream("ReferenceResolutionTest.jrs");
		r.read(new Input("String Input", in));
	}
	
}
