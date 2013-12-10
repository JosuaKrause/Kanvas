package jkanvas.table;

import java.util.Objects;

/**
 * A table representing a collection of features.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public class FeatureTable extends DataTable {

  /** The features. */
  private final Feature[] features;
  /** The number of rows. */
  private final int rows;
  /** Whether all tables are caching. */
  private final boolean caching;

  /**
   * Creates a table from given features.
   * 
   * @param features The features. All features must have the same number of
   *          rows.
   */
  public FeatureTable(final Feature[] features) {
    Objects.requireNonNull(features);
    boolean caching = true;
    int rows = -1;
    for(int r = 0; r < features.length; ++r) {
      final Feature feature = features[r];
      caching = feature.getTable().isCaching() && caching;
      final int l = feature.rows();
      if(rows < 0) {
        rows = l;
      } else if(rows != l) throw new IllegalArgumentException(
          "inconsistent row count: " + rows + " " + l);
    }
    this.rows = rows >= 0 ? rows : 0;
    this.features = features.clone();
    this.caching = caching;
  }

  @Override
  public int rows() {
    return rows;
  }

  @Override
  public int cols() {
    return features.length;
  }

  @Override
  public double getAt(final int row, final int col) {
    return features[col].getElement(row);
  }

  @Override
  public String getName(final int col) {
    return features[col].getName();
  }

  @Override
  public boolean isCategorical(final int col) {
    return features[col].isCategorical();
  }

  @Override
  public boolean isCaching() {
    return caching;
  }

  @Override
  public DataTable cached() {
    return caching ? this : super.cached();
  }

  @Override
  public Feature getFeature(final int col) {
    return features[col];
  }

  @Override
  protected Feature[] features() {
    return features;
  }

  @Override
  public boolean hasCachedFeatures() {
    return true;
  }

  @Override
  protected double getCachedValue(final ColumnAggregation agg, final int col) {
    return features[col].getCachedValue(agg);
  }

  @Override
  protected void setCachedValue(final ColumnAggregation agg, final int col, final double v) {
    features[col].setCachedValue(agg, v);
  }

}