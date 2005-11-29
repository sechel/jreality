package de.jreality.ui.beans;

public class DoubleEditor extends NumberEditor {

  public String getAsText() {
    if (getValue() == null) return "";
    else return ((Double)getValue()).toString();
  }

  public void setAsText(String text) {
    if (text == null) setValue(null);
    else {
      try {
        Double val = new Double(Double.parseDouble(text));
        setValue(val);
      } catch (NumberFormatException nfe) {
        setValue(null);
      }
    }
  }

}
