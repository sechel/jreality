package de.jreality.jogl.shader;

import java.awt.Color;

import de.jreality.math.Matrix;
import de.jreality.shader.ImageData;
import de.jreality.shader.Texture2D;

/**
 * This class is essentially a cache for the Texture2D used in the constructor.
 * @author Charles Gunn
 *
 */
public class JOGLTexture2D implements Texture2D {
	Texture2D proxy;
	protected int applyMode,
		combineMode,
		combineModeAlpha,
		combineModeColor;
	protected Color blendColor;
	protected String externalSource;
	protected ImageData image;
	protected Integer magFilter;
	protected Integer minFilter;
	protected Matrix textureMatrix;
	protected Integer repeatS;
	protected Integer repeatT;
	public JOGLTexture2D(Texture2D t)	{
		super();
		proxy = t;
		update();
	}
	
	public void update()	{
		applyMode = proxy.getApplyMode();
		blendColor = proxy.getBlendColor();
		combineMode = proxy.getCombineModeColor();
		combineModeAlpha = proxy.getCombineModeAlpha();
		image = proxy.getImage();
		textureMatrix = proxy.getTextureMatrix();
		minFilter = proxy.getMinFilter();
		magFilter = proxy.getMagFilter();
		repeatS = proxy.getRepeatS();
		repeatT = proxy.getRepeatT();
		externalSource = proxy.getExternalSource();
	}
	
	public Integer getApplyMode() {
		return applyMode;
	}

	public Color getBlendColor() {
		return blendColor;
	}

	public Integer getCombineMode() {
		return combineMode;
	}

	public Integer getCombineModeAlpha() {
		return combineModeAlpha;
	}

	public Integer getCombineModeColor() {
		return combineModeColor;
	}

	public String getExternalSource() {
		return externalSource;
	}

	public ImageData getImage() {
		return image;
	}

	public Integer getMagFilter() {
		return magFilter;
	}

	public Integer getMinFilter() {
		return minFilter;
	}

	public Integer getOperand0Alpha() {
		return proxy.getOperand0Alpha();
	}

	public Integer getOperand0Color() {
		return proxy.getOperand0Color();
	}

	public Integer getOperand1Alpha() {
		return proxy.getOperand1Alpha();
	}

	public Integer getOperand1Color() {
		return proxy.getOperand1Color();
	}

	public Integer getOperand2Alpha() {
		return proxy.getOperand2Alpha();
	}

	public Integer getOperand2Color() {
		return proxy.getOperand2Color();
	}

	public Integer getRepeatS() {
		return repeatS;
	}

	public Integer getRepeatT() {
		return repeatT;
	}

	public Integer getSource0Alpha() {
		return proxy.getSource0Alpha();
	}

	public Integer getSource0Color() {
		return proxy.getSource0Color();
	}

	public Integer getSource1Alpha() {
		return proxy.getSource1Alpha();
	}

	public Integer getSource1Color() {
		return proxy.getSource1Color();
	}

	public Integer getSource2Alpha() {
		return proxy.getSource2Alpha();
	}

	public Integer getSource2Color() {
		return proxy.getSource2Color();
	}

	public Matrix getTextureMatrix() {
		return textureMatrix;
	}

	public void setApplyMode(Integer applyMode) {
		// TODO Auto-generated method stub
		
	}

	public void setBlendColor(Color blendColor) {
		// TODO Auto-generated method stub
		
	}

	public void setCombineMode(Integer combineMode) {
		// TODO Auto-generated method stub
		
	}

	public void setCombineModeAlpha(Integer i) {
		// TODO Auto-generated method stub
		
	}

	public void setCombineModeColor(Integer i) {
		// TODO Auto-generated method stub
		
	}

	public void setExternalSource(String b) {
		// TODO Auto-generated method stub
		
	}

	public void setImage(ImageData image) {
		// TODO Auto-generated method stub
		
	}

	public void setMagFilter(Integer i) {
		// TODO Auto-generated method stub
		
	}

	public void setMinFilter(Integer i) {
		// TODO Auto-generated method stub
		
	}

	public void setOperand0Alpha(Integer i) {
		// TODO Auto-generated method stub
		
	}

	public void setOperand0Color(Integer i) {
		// TODO Auto-generated method stub
		
	}

	public void setOperand1Alpha(Integer i) {
		// TODO Auto-generated method stub
		
	}

	public void setOperand1Color(Integer i) {
		// TODO Auto-generated method stub
		
	}

	public void setOperand2Alpha(Integer i) {
		// TODO Auto-generated method stub
		
	}

	public void setOperand2Color(Integer i) {
		// TODO Auto-generated method stub
		
	}

	public void setRepeatS(Integer repeatS) {
		// TODO Auto-generated method stub
		
	}

	public void setRepeatT(Integer repeatT) {
		// TODO Auto-generated method stub
		
	}

	public void setSource0Alpha(Integer i) {
		// TODO Auto-generated method stub
		
	}

	public void setSource0Color(Integer i) {
		// TODO Auto-generated method stub
		
	}

	public void setSource1Alpha(Integer i) {
		// TODO Auto-generated method stub
		
	}

	public void setSource1Color(Integer i) {
		// TODO Auto-generated method stub
		
	}

	public void setSource2Alpha(Integer i) {
		// TODO Auto-generated method stub
		
	}

	public void setSource2Color(Integer i) {
		// TODO Auto-generated method stub
		
	}

	public void setTextureMatrix(Matrix matrix) {
		// TODO Auto-generated method stub
		
	}

}
