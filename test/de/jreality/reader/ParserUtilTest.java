package de.jreality.reader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class ParserUtilTest {

	@Test
	public void testParseDoubleArray() throws IOException {
		double[] inCoords = {0.1, 0.2, 0.3};
		String str = "0.1 0.2 0.3";
		Reader r = new BufferedReader(new StringReader(str));
		StreamTokenizer st = createOBJStreamTokenizer(r);
		double[] coords = ParserUtil.parseDoubleArray(st);
		Assert.assertArrayEquals(inCoords, coords, 1E-10);
	}

	
	@Test
	public void testParseDoubleArray_Negative() throws IOException {
		double[] inCoords = {-0.1, 0.2, 0.3};
		String str = "-0.1 0.2 0.3";
		Reader r = new BufferedReader(new StringReader(str));
		StreamTokenizer st = createOBJStreamTokenizer(r);
		double[] coords = ParserUtil.parseDoubleArray(st);
		Assert.assertArrayEquals(inCoords, coords, 1E-10);
	}
	
	@Test
	public void testParseDoubleArray_Plus() throws IOException {
		double[] inCoords = {-0.1, 0.2, 0.3};
		String str = "-0.1 +0.2 0.3";
		Reader r = new BufferedReader(new StringReader(str));
		StreamTokenizer st = createOBJStreamTokenizer(r);
		double[] coords = ParserUtil.parseDoubleArray(st);
		Assert.assertArrayEquals(inCoords, coords, 1E-10);
	}
	
	@Test
	public void testParseDoubleArray_Exp() throws IOException {
		double[] inCoords = {-0.1, 0.2, 0.3};
		String str = "-1E-1 +2E-1 0.3";
		Reader r = new BufferedReader(new StringReader(str));
		StreamTokenizer st = createOBJStreamTokenizer(r);
		double[] coords = ParserUtil.parseDoubleArray(st);
		Assert.assertArrayEquals(inCoords, coords, 1E-10);
	}
	
	@Test
	public void testParseStringArray() throws IOException {
		String[] list = {"name1", "name2", "name3"}; 
		String str = "name1 name2 name3 ";
		Reader r = new BufferedReader(new StringReader(str));
		StreamTokenizer st = createOBJStreamTokenizer(r);
		List<String> strings = ParserUtil.parseStringArray(st);
		int i = 0;
		for(String s : strings) {
			Assert.assertTrue(s.equals(list[i++]));
		}
	}

	@Test
	public void testParseIntArray() throws IOException {
		double[] inIndices = {-1, 2, 3};
		String str = "-1 2 3";
		Reader r = new BufferedReader(new StringReader(str));
		StreamTokenizer st = createOBJStreamTokenizer(r);
		List<Integer> indices = ParserUtil.parseIntArray(st);
		int i = 0;
		for(int index : indices) {
			Assert.assertEquals(inIndices[i++],index,1E-10);
		}
	}

	private StreamTokenizer createOBJStreamTokenizer(Reader r) {
		StreamTokenizer st = new StreamTokenizer(r);
		st.resetSyntax();
		st.eolIsSignificant(true);
		st.wordChars('0', '9');
		st.wordChars('A', 'Z');
		st.wordChars('a', 'z');
		st.wordChars('_', '_');
		st.wordChars('.', '.');
		st.wordChars('-', '-');
		st.wordChars('+', '+');
		st.wordChars('\u00A0', '\u00FF');
		st.whitespaceChars('\u0000', '\u0020');
		st.commentChar('#');
		st.ordinaryChar('/');
		st.parseNumbers();
		return st;
	}
	
}
