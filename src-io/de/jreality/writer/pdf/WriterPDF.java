package de.jreality.writer.pdf;

import static de.jreality.util.SceneGraphUtility.getPathsBetween;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfAnnotation;
import com.lowagie.text.pdf.PdfAppearance;
import com.lowagie.text.pdf.PdfArray;
import com.lowagie.text.pdf.PdfBoolean;
import com.lowagie.text.pdf.PdfDictionary;
import com.lowagie.text.pdf.PdfEncodings;
import com.lowagie.text.pdf.PdfIndirectReference;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfNumber;
import com.lowagie.text.pdf.PdfStream;
import com.lowagie.text.pdf.PdfString;
import com.lowagie.text.pdf.PdfWriter;

import de.jreality.io.JrScene;
import de.jreality.reader.ReaderJRS;
import de.jreality.scene.Camera;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphNode;
import de.jreality.scene.SceneGraphPath;
import de.jreality.util.Input;
import de.jreality.util.SceneGraphUtility;
import de.jreality.writer.SceneWriter;
import de.jreality.writer.u3d.U3DSceneUtility;
import de.jreality.writer.u3d.WriterU3D;

public class WriterPDF implements SceneWriter {

    public static final String 
	    PDF_NAME_3D = "3D",
	    PDF_NAME_3DD = "3DD",
	    PDF_NAME_3DV = "3DV",
	    PDF_NAME_3DVIEW = "3DView",
	    PDF_NAME_C2W = "C2W",
	    PDF_NAME_IN = "IN",
	    PDF_NAME_MS = "MS",
	    PDF_NAME_U3D = "U3D",
	    PDF_NAME_U3DPATH = "U3DPath",
	    PDF_NAME_XN = "XN";
	
    private static final String
//    	encompassScript = getJSScript("encompass.js"),
//    	rotateToolScript = getJSScript("rotateTool.js"),
    	defaultScript = getJSScript("defaultCamera.js");
    
	/**
	 * Exports a given {@link SceneGraphNode} into a PDF document. 
	 * @param c the scene graph node to export
	 * @param out the output stream to export the data to
	 */
	public void write(SceneGraphNode c, OutputStream out) throws IOException {
		SceneGraphComponent root = null;
		if (c instanceof SceneGraphComponent) root = (SceneGraphComponent) c;
		else {
			root = new SceneGraphComponent();
			SceneGraphUtility.addChildNode(root, c);
		}
		JrScene scene = new JrScene(root);
		writeScene(scene, out);
	}

	
	private static float[] toU3DMatrix(double[] M) {
		float[] R = new float[12];
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 3; j++) {
				R[i * 3 + j] = (float)M[j * 4 + i];
			}
		}
		return R;
	}
	
	
	
	/**
	 * Exports a given {@link JrScene} into a PDF document. 
	 * @param scene the scene to export
	 * @param out the output stream to export the data to
	 */
	public void writeScene(JrScene scene, OutputStream out) throws IOException {
		// Write U3D data to temporary file
		File u3dTmp = File.createTempFile("jralityPDFExport", "u3d");
//		File u3dTmp = new File("test2.u3d");
		FileOutputStream u3dout = new FileOutputStream(u3dTmp); 
		WriterU3D.write(scene, u3dout);

		List<SceneGraphComponent> cameraNodes = U3DSceneUtility.getViewNodes(scene);
		List<SceneGraphPath> camPaths = new ArrayList<SceneGraphPath>();
		for (SceneGraphComponent c : cameraNodes) {
			camPaths.addAll(getPathsBetween(scene.getSceneRoot(), c));
		}
		
		// Create PDF
		Rectangle pageSize = new Rectangle(50f, 50f);
		Document doc = new Document(pageSize);
		try {
			PdfWriter wr = PdfWriter.getInstance(doc, out);
			doc.open();			
			
			PdfStream oni = new PdfStream(PdfEncodings.convertToBytes(defaultScript, null));
            oni.flateCompress();
            PdfIndirectReference initScriptRef = wr.addToBody(oni).getIndirectReference();

            ArrayList<PdfIndirectReference> viewList = new ArrayList<PdfIndirectReference>(camPaths.size());
            for (SceneGraphPath path : camPaths) {
            	SceneGraphComponent c = path.getLastComponent();
            	Camera cam = c.getCamera();
            	float[] T1 = toU3DMatrix(path.getMatrix(null));
            	T1 = new float[]{0,0,0, 0,0,0, 0,0,0, 0,0,0};
	            PdfDictionary viewDict = new PdfDictionary(new PdfName(PDF_NAME_3DVIEW));
	            viewDict.put(new PdfName(PDF_NAME_MS), new PdfString("M"));viewDict.put(new PdfName(PDF_NAME_MS), new PdfString("M"));
//	            viewDict.put(PdfName.CO, new PdfNumber(10.0f));
	            viewDict.put(new PdfName("C2W"), new PdfArray(T1));
	            viewDict.put(new PdfName(PDF_NAME_XN), new PdfString(cam.getName()));
	            PdfIndirectReference ref = wr.addToBody(viewDict).getIndirectReference(); // Write view dictionary, get reference
	            viewList.add(ref);
            }
            
            PdfStream stream = new PdfStream(new FileInputStream(u3dTmp), wr);
			stream.put(new PdfName("OnInstantiate"), initScriptRef);
			stream.put(PdfName.TYPE, new PdfName(PDF_NAME_3D)); // Mandatory keys
			stream.put(PdfName.SUBTYPE, new PdfName(PDF_NAME_U3D));
			stream.put(new PdfName("VA"), new PdfArray(viewList));
			stream.put(new PdfName("VD"), new PdfNumber(0));
			stream.flateCompress();
			PdfIndirectReference u3dStreamRef = wr.addToBody(stream).getIndirectReference(); // Write stream contents, get reference to stream object, write actual stream length
			stream.writeLength();

            PdfDictionary activationDict = new PdfDictionary();
            activationDict.put(PdfName.A, new PdfName("PO"));
            activationDict.put(new PdfName("DIS"), PdfName.I);
            
            PdfAppearance ap = PdfAppearance.createAppearance(wr, pageSize.getRight() - pageSize.getLeft(), pageSize.getTop() - pageSize.getBottom());
            ap.setBoundingBox(pageSize);
            
            PdfAnnotation annot = new PdfAnnotation(wr, pageSize);
            annot.put(PdfName.CONTENTS, new PdfString("3D Model"));
            annot.put(PdfName.SUBTYPE, new PdfName(PDF_NAME_3D)); // Mandatory keys
            annot.put(PdfName.TYPE, PdfName.ANNOT);
            annot.put(new PdfName(PDF_NAME_3DD), u3dStreamRef); // Reference to stream object
            annot.put(new PdfName("3DI"), PdfBoolean.PDFTRUE);
            annot.put(new PdfName("3DV"), new PdfName("F")); // First view is the default one
            annot.setAppearance(PdfAnnotation.APPEARANCE_NORMAL, ap); // Assign appearance and page
            annot.put(new PdfName("3DA"), activationDict);
            annot.setPage(1);
            
 
            // Actually write annotation
            wr.addAnnotation(annot);
			
			doc.close();
		} catch (DocumentException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method cannot be used for PDF exporting. 
	 * It always throws an {@link UnsupportedOperationException}.
	 * @param scene unused
	 * @param out unused
	 */
	@Deprecated
	public void writeScene(JrScene scene, Writer out) throws IOException, UnsupportedOperationException {
		throw new UnsupportedOperationException("PDF is a binary file format");
	}

	
	/**
	 * Exports a given {@link JrScene} into a PDF document. 
	 * @param scene the scene to export
	 * @param out the output stream to export the data to
	 */
	public static void write(JrScene scene, OutputStream out) throws IOException {
		WriterU3D writer = new WriterU3D();
		writer.writeScene(scene, out);
	}
	
	
	
	private static String getJSScript(String name) {
		StringBuffer result = new StringBuffer();
		InputStreamReader inReader = new InputStreamReader(WriterPDF.class.getResourceAsStream(name));
		LineNumberReader in = new LineNumberReader(inReader);
		String line = null;
		try {
			while ((line = in.readLine()) != null) {
				result.append(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result.toString();
	}
	
	
	public static void main(String[] args) {
		ReaderJRS reader = new ReaderJRS();
		SceneGraphComponent root = null;
		try {
			root = reader.read(Input.getInput(WriterPDF.class.getResource("test.jrs")));
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		WriterPDF writer = new WriterPDF();
		FileOutputStream out;
		try {
			out = new FileOutputStream("test.pdf");
			writer.write(root, out);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
}
