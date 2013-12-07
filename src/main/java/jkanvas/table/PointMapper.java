package jkanvas.table;

import java.awt.Color;

import jkanvas.animation.CircleList;

/**
 * Maps points to rows in a table.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public class PointMapper extends ListMapper<CircleList> {

  /** The first feature. */
  private final int f1;
  /** The second feature. */
  private final int f2;
  /** The size of the area of points. */
  private final double size;
  /** The size of the points. */
  private final double pointSize;

  /**
   * Creates a point map.
   * 
   * @param table The table.
   * @param f1 The first feature.
   * @param f2 The second feature.
   * @param size The size of the area of points.
   * @param pointSize The size of the points.
   */
  public PointMapper(final DataTable table, final int f1, final int f2,
      final double size, final double pointSize) {
    super(table);
    this.f1 = f1;
    this.f2 = f2;
    this.size = size;
    this.pointSize = pointSize;
  }

  @Override
  protected CircleList createList() {
    return new CircleList(getTable().rows(), Color.BLACK, null);
  }

  @Override
  protected int createForRow(final CircleList pl, final int r) {
    final DataTable table = getTable();
    return pl.addPoint(table.getMinMaxScaled(r, f1) * size,
        table.getMinMaxScaled(r, f2) * size, pointSize);
  }

  /**
   * Getter.
   * 
   * @return The size of the area of points.
   */
  public double getSize() {
    return size;
  }

}
