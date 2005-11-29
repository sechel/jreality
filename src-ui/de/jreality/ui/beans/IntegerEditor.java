package de.jreality.ui.beans;

public class IntegerEditor extends NumberEditor {

  public String getAsText() {
    if (getValue() == null) return "";
    else return ((Integer)getValue()).toString();
  }

  public void setAsText(String text) {
    if (text == null) setValue(null);
    else {
      try {
        Integer val = new Integer(Integer.parseInt(text));
        setValue(val);
      } catch (NumberFormatException nfe) {
        setValue(null);
      }
    }
  }

}
