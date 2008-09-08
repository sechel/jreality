package de.jreality.writer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfAnnotation;
import com.lowagie.text.pdf.PdfAppearance;
import com.lowagie.text.pdf.PdfArray;
import com.lowagie.text.pdf.PdfBoolean;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfDictionary;
import com.lowagie.text.pdf.PdfDocument;
import com.lowagie.text.pdf.PdfEncodings;
import com.lowagie.text.pdf.PdfIndirectObject;
import com.lowagie.text.pdf.PdfIndirectReference;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfNumber;
import com.lowagie.text.pdf.PdfStamper;
import com.lowagie.text.pdf.PdfStream;
import com.lowagie.text.pdf.PdfString;
import com.lowagie.text.pdf.PdfWriter;

import de.jreality.io.JrScene;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphNode;
import de.jreality.util.SceneGraphUtility;
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
	    PDF_NAME_XN = "XN";
	

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

	/**
	 * Exports a given {@link JrScene} into a PDF document. 
	 * @param scene the scene to export
	 * @param out the output stream to export the data to
	 */
	public void writeScene(JrScene scene, OutputStream out) throws IOException {
		// Write U3D data to temporary file
		File u3dTmp = File.createTempFile("jralityU3DExport", "u3d");
		FileOutputStream u3dout = new FileOutputStream(u3dTmp); 
		WriterU3D.write(scene, u3dout);

		// Create PDF
		Rectangle pageSize = new Rectangle(50f, 50f);
		Document doc = new Document(pageSize);
		try {
			PdfWriter wr = PdfWriter.getInstance(doc, out);
			doc.open();			
			
			PdfStream oni = new PdfStream(PdfEncodings.convertToBytes("runtime.setCurrentTool(\"Rotate\");", null));
            oni.flateCompress();
			
            PdfStream stream = new PdfStream(new FileInputStream(u3dTmp), wr);
			stream.put(new PdfName("OnInstantiate"), wr.addToBody(oni).getIndirectReference());
			stream.put(PdfName.TYPE, new PdfName(PDF_NAME_3D)); // Mandatory keys
			stream.put(PdfName.SUBTYPE, new PdfName(PDF_NAME_U3D));
			stream.flateCompress();
			
			PdfIndirectReference streamRef = wr.addToBody(stream).getIndirectReference(); // Write stream contents, get reference to stream object, write actual stream length
			stream.writeLength();
			
            PdfDictionary dict = new PdfDictionary(new PdfName(PDF_NAME_3DVIEW));
            dict.put(new PdfName(PDF_NAME_XN), new PdfString("Default"));
            dict.put(new PdfName(PDF_NAME_IN), new PdfString("Unnamed"));
//            dict.put(new PdfName(PDF_NAME_MS), PdfName.M); // States that we have to provide camera-to-world coordinate transformation
//            dict.put(new PdfName(PDF_NAME_C2W), new PdfArray(new float[] {1, 0, 0, 0, 0, -1, 0, 1, 0, 3, -235, 28F})); // 3d transformation matrix (demo for teapot)
//            dict.put(PdfName.CO, new PdfNumber(235)); // Camera distance along z-axis (demo for teapot)
 
            PdfIndirectObject objRef = wr.addToBody(dict); // Write view dictionary, get reference
			
            PdfDictionary adi = new PdfDictionary();
            adi.put(PdfName.A, new PdfName("PO"));
            adi.put(new PdfName("DIS"), PdfName.I);
            
            PdfAppearance ap = PdfAppearance.createAppearance(wr, pageSize.getRight() - pageSize.getLeft(), pageSize.getTop() - pageSize.getBottom());
            ap.setBoundingBox(pageSize);
            
            PdfAnnotation annot = new PdfAnnotation(wr, pageSize);
            annot.put(PdfName.CONTENTS, new PdfString("3D Model"));
            annot.put(PdfName.SUBTYPE, new PdfName(PDF_NAME_3D)); // Mandatory keys
            annot.put(PdfName.TYPE, PdfName.ANNOT);
            annot.put(new PdfName(PDF_NAME_3DD), streamRef); // Reference to stream object
            annot.put(new PdfName(PDF_NAME_3DV), objRef.getIndirectReference()); // Reference to view dictionary object
            annot.put(new PdfName("3DI"), PdfBoolean.PDFFALSE);
            annot.setAppearance(PdfAnnotation.APPEARANCE_NORMAL, ap); // Assign appearance and page
            annot.setPage(1);
            annot.put(new PdfName("3DA"), adi);
 
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
	
}
