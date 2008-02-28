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
		combineModeAlpha,
		combineModeColor,
		source0Color,
		source0Alpha,
		source1Color,
		source1Alpha,
		source2Color,
		source2Alpha,
		operand0Color,
		operand0Alpha,
		operand1Color,
		operand1Alpha,
		operand2Color,
		operand2Alpha;
	protected Color blendColor;
	protected String externalSource;
	protected ImageData image;
	protected Integer magFilter;
	protected Integer minFilter;
	protected Matrix textureMatrix;
	protected Integer repeatS;
	protected Integer repeatT;
	protected Boolean animated;
	public JOGLTexture2D(Texture2D t)	{
		super();
		proxy = t;
		update();
	}
	
	public void update()	{
		applyMode = proxy.getApplyMode();
		blendColor = proxy.getBlendColor();
		combineModeColor = proxy.getCombineModeColor();
		combineModeAlpha = proxy.getCombineModeAlpha();
		operand0Color = proxy.getOperand0Color();
		operand1Color = proxy.getOperand1Color();
		operand2Color = proxy.getOperand2Color();
		operand0Alpha = proxy.getOperand0Alpha();
		operand1Alpha = proxy.getOperand1Alpha();
		operand2Alpha = proxy.getOperand2Alpha();
		source0Color = proxy.getSource0Color();
		source1Color = proxy.getSource1Color();
		source2Color = proxy.getSource2Color();
		source0Alpha = proxy.getSource0Alpha();
		source1Alpha = proxy.getSource1Alpha();
		source2Alpha = proxy.getSource2Alpha();
		image = proxy.getImage();
		textureMatrix = proxy.getTextureMatrix();
		minFilter = proxy.getMinFilter();
		magFilter = proxy.getMagFilter();
		repeatS = proxy.getRepeatS();
		repeatT = proxy.getRepeatT();
		externalSource = proxy.getExternalSource();
		animated = proxy.getAnimated();
	}
	
	public Integer getApplyMode() {
		return applyMode;
	}

	public Color getBlendColor() {
		return blendColor;
	}

	public Integer getCombineMode() {
		return combineModeColor;
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
		return operand0Alpha;
	}

	public Integer getOperand0Color() {
		return operand0Color;
	}

	public Integer getOperand1Alpha() {
		return operand1Alpha;
	}

	public Integer getOperand1Color() {
		return operand1Color;
	}

	public Integer getOperand2Alpha() {
		return operand2Alpha;
	}

	public Integer getOperand2Color() {
		return operand2Color;
	}

	public Integer getRepeatS() {
		return repeatS;
	}

	public Integer getRepeatT() {
		return repeatT;
	}

	public Integer getSource0Alpha() {
		return source0Alpha;
	}

	public Integer getSource0Color() {
		return source0Color;
	}

	public Integer getSource1Alpha() {
		return source1Alpha;
	}

	public Integer getSource1Color() {
		return source1Color;
	}

	public Integer getSource2Alpha() {
		return source2Alpha;
	}

	public Integer getSource2Color() {
		return source2Color;
	}

	public Matrix getTextureMatrix() {
		return textureMatrix;
	}
	
	public Boolean getAnimated() {
		return animated;
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

	public void setAnimated(Boolean b) {
		// TODO Auto-generated method stub
		
	}

	public Boolean getMipmapMode() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setMipmapMode(Boolean b) {
		// TODO Auto-generated method stub
	}

}
