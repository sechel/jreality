package de.jreality.shader;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import de.jtem.beans.ChoiceEditor;

public class Texture2DBeanInfo extends SimpleBeanInfo {
	
	public static class ApplyModeEditor extends ChoiceEditor {
		protected void defineValuesToStrings() {
			valuesToStrings.put(new Integer(Texture2D.GL_MODULATE), "modulate");
			valuesToStrings.put(new Integer(Texture2D.GL_COMBINE), "combine");
			valuesToStrings.put(new Integer(Texture2D.GL_REPLACE), "replace");
			valuesToStrings.put(new Integer(Texture2D.GL_DECAL), "decal");
		}
	}
	
	public PropertyDescriptor[] getPropertyDescriptors() {
		Class beanClass = Texture2D.class;
		try {  
			PropertyDescriptor applyMode =
				new PropertyDescriptor("applyMode", beanClass);
			applyMode.setPropertyEditorClass(
					ApplyModeEditor.class
			);
			PropertyDescriptor image =
				new PropertyDescriptor("image", beanClass);
			PropertyDescriptor magFilter =
				new PropertyDescriptor("magFilter", beanClass);
			PropertyDescriptor minFilter =
				new PropertyDescriptor("minFilter", beanClass);
			PropertyDescriptor blendColor =
				new PropertyDescriptor("blendColor", beanClass);
			PropertyDescriptor combineMode =
				new PropertyDescriptor("combineMode", beanClass);
			PropertyDescriptor textureMatrix =
				new PropertyDescriptor("textureMatrix", beanClass);
			PropertyDescriptor repeatS =
				new PropertyDescriptor("repeatS", beanClass);
			PropertyDescriptor repeatT =
				new PropertyDescriptor("repeatT", beanClass);
			
			PropertyDescriptor rv[] = {
					minFilter,
					magFilter,
					image,
					blendColor,
					textureMatrix,
					repeatS,
					repeatT,
					applyMode,
					combineMode
			};
			return rv;
		} catch (IntrospectionException e) {
			throw new Error(e.toString());
		}
	}
}
