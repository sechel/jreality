package de.jreality.sunflow;


import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import de.jtem.beans.DoubleSpinnerEditor;
import de.jtem.beans.IntegerSpinnerEditor;

public class RenderOptionsBeanInfo extends SimpleBeanInfo {

		public static class CoefficientEditor extends DoubleSpinnerEditor {
			public CoefficientEditor() {
				super(false);
			}
			@Override
			protected void customize() {
				model.setMinimum(new Double(0));
				model.setMaximum(new Double(1.));
				model.setStepSize(new Double(.1));
			}
		}
		
		public static class IntEditor extends IntegerSpinnerEditor {
			public IntEditor() {
				super(false);
			}
			@Override
			protected void customize() {
				model.setMinimum(null);
				model.setMaximum(null);
				model.setStepSize(new Integer(1));
			}
		}
		
		public static class PositiveIntEditor extends IntegerSpinnerEditor {
			public PositiveIntEditor() {
				super(false);
			}
			@Override
			protected void customize() {
				model.setMinimum(new Integer(0));
				model.setMaximum(null);
				model.setStepSize(new Integer(1));
			}
		}
		


		public PropertyDescriptor[] getPropertyDescriptors() {
			Class beanClass = RenderOptions.class;
			try {  
				PropertyDescriptor useOriginalLights =
					new PropertyDescriptor("useOriginalLights", beanClass);

				PropertyDescriptor globalIllumination =
					new PropertyDescriptor("globalIllumination", beanClass);
				globalIllumination.setPropertyEditorClass(CoefficientEditor.class);

				PropertyDescriptor aaMin =
					new PropertyDescriptor("aaMin", beanClass);
				aaMin.setPropertyEditorClass(IntEditor.class);
				
				PropertyDescriptor aaMax =
					new PropertyDescriptor("aaMax", beanClass);
				aaMax.setPropertyEditorClass(PositiveIntEditor.class);
				
				PropertyDescriptor aaSamples =
					new PropertyDescriptor("aaSamples", beanClass);
				aaSamples.setPropertyEditorClass(PositiveIntEditor.class);
				
				PropertyDescriptor directionalLightSamplesMin =
					new PropertyDescriptor("directionalLightSamplesMin", beanClass);
				directionalLightSamplesMin.setPropertyEditorClass(PositiveIntEditor.class);

				PropertyDescriptor depthsDiffuse =
					new PropertyDescriptor("depthsDiffuse", beanClass);
				depthsDiffuse.setPropertyEditorClass(PositiveIntEditor.class);

				PropertyDescriptor depthsReflection =
					new PropertyDescriptor("depthsReflection", beanClass);
				depthsReflection.setPropertyEditorClass(PositiveIntEditor.class);

				PropertyDescriptor depthsRefraction =
					new PropertyDescriptor("depthsRefraction", beanClass);
				depthsRefraction.setPropertyEditorClass(PositiveIntEditor.class);

				PropertyDescriptor rv[] = {
						useOriginalLights,
						globalIllumination,
						aaMin,
						aaMax,
						aaSamples,
						directionalLightSamplesMin,
						depthsDiffuse,
						depthsReflection,
						depthsRefraction
				};
				return rv;
			} catch (IntrospectionException e) {
				throw new Error(e.toString());
			}
		}
	}
