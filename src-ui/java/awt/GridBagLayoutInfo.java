package java.awt;

public class GridBagLayoutInfo implements java.io.Serializable {
	private static final long serialVersionUID = 1L;
int width, height;		/* number of cells horizontally, vertically */
  int startx, starty;		/* starting point for layout */
  int minWidth[];		/* largest minWidth in each column */
  int minHeight[];		/* largest minHeight in each row */
  double weightX[];		/* largest weight in each column */
  double weightY[];		/* largest weight in each row */

  GridBagLayoutInfo () {
      /* fix for 5055696 (avoiding AIOOBE by enlarging sizes) */
      minWidth = new int[GridBagLayout.MAXGRIDSIZE];
      minHeight = new int[GridBagLayout.MAXGRIDSIZE];
      weightX = new double[GridBagLayout.MAXGRIDSIZE];
      weightY = new double[GridBagLayout.MAXGRIDSIZE];
  }
}
