package jkanvas.nodelink;

import java.awt.Graphics2D;
import java.awt.Shape;

import jkanvas.animation.AnimatedPosition;

/**
 * Realizes the actual painting of edges.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 * @param <T> The node type.
 */
public interface EdgeRealizer<T extends AnimatedPosition> {

  /**
   * The shape of the edge between nodes. The order of the nodes is not defined.
   * The shape should not be smaller than the visible edge.
   * 
   * @param from The source node.
   * @param to The sink node.
   * @return The shape of the edge.
   */
  Shape createLineShape(T from, T to);

  /**
   * Draws edges. The order of the nodes is not defined.
   * 
   * @param g The graphics context.
   * @param from The source node.
   * @param to The sink node.
   */
  void drawLines(Graphics2D g, T from, T to);

}