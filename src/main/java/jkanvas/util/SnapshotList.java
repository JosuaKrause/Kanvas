package jkanvas.util;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;

/**
 * A list that is readable only through snapshots. The list can always be
 * modified but when a snapshot is obtained all changes afterwards are not
 * present in this snapshot. It is advised to only use one snapshot at a time.
 * Elements can not be explicitly removed but will cease to exist when not
 * referenced by some other source. In turn getting elements may result in
 * <code>null</code> pointers to indicate that this element no longer exists. To
 * reduce the number of <code>null</code> pointers garbage collection is
 * performed when needed.
 * 
 * @author Joschi <josua.krause@gmail.com>
 * @param <T> The element type.
 */
public final class SnapshotList<T> {

  /** All registered objects. */
  private final List<WeakReference<T>> list = new ArrayList<>();

  /**
   * The list that is filled instead of {@link #list} when there are
   * {@link #snapshots}. The elements are added when no {@link #snapshots} are
   * active any more. This list is also used as monitor for all changing
   * operations and setting {@link #snapshots}.
   */
  private final List<T> toBeAdded = new ArrayList<>();

  /**
   * The number of active snapshots. When there are snapshots the {@link #list}
   * is guaranteed not to change.
   */
  private int snapshots;

  /**
   * Adds an object. Elements are guaranteed to appear only once in the list.
   * 
   * @param elem The object. Must be non-<code>null</code>.
   */
  public void add(final T elem) {
    Objects.requireNonNull(elem);
    synchronized(toBeAdded) {
      toBeAdded.add(elem);
    }
  }

  /**
   * A snapshot of the given list. Getter methods may return <code>null</code>
   * pointers to indicate that the element in question has been removed from
   * memory recently. The snapshot is best used as a resource:
   * 
   * <pre>
   * try (Snapshot<...> s = snapshotList.getSnapshot()) {
   *   // ... do stuff
   * }
   * </pre>
   * 
   * @author Joschi <josua.krause@gmail.com>
   * @param <T> The element type.
   */
  public static final class Snapshot<T> implements AutoCloseable, Iterable<T> {

    /** The list. */
    private final List<WeakReference<T>> content;
    /** The snapshot list. */
    private SnapshotList<T> list;
    /** Whether we encountered any <code>null</code> pointers. */
    protected boolean hasNull;

    /**
     * Creates a snapshot.
     * 
     * @param list The snapshot list.
     * @param content The snapshot content.
     */
    public Snapshot(final SnapshotList<T> list, final List<WeakReference<T>> content) {
      this.list = list;
      this.content = content;
      list.startSnapshot();
    }

    /** Ensures that the snapshot is still open. */
    private void ensureOpen() {
      if(list == null) throw new IllegalStateException("snapshot already closed");
    }

    /**
     * Getter.
     * 
     * @param index The index.
     * @return Returns the element at the given position. The result may be
     *         <code>null</code> indicating that the element was just recently
     *         removed.
     */
    public T get(final int index) {
      ensureOpen();
      final T res = content.get(index).get();
      if(res == null) {
        hasNull = true;
      }
      return res;
    }

    /**
     * Getter.
     * 
     * @return The size of the list.
     */
    public int size() {
      ensureOpen();
      return content.size();
    }

    @Override
    public Iterator<T> iterator() {
      ensureOpen();
      final List<WeakReference<T>> content = this.content;
      return new Iterator<T>() {

        private int pos;

        @Override
        public boolean hasNext() {
          return pos < content.size();
        }

        @Override
        public T next() {
          if(!hasNext()) throw new NoSuchElementException();
          final T res = content.get(pos++).get();
          if(res == null) {
            hasNull = true;
          }
          return res;
        }

        @Override
        public void remove() {
          throw new UnsupportedOperationException();
        }

      };
    }

    @Override
    public void close() {
      if(list == null) return;
      list.endSnapshot(hasNull);
      list = null;
    }

  } // Snapshot

  /**
   * Actually adds the elements of the waiting list.
   * 
   * @param gc Whether to also remove all <code>null</code> pointers.
   */
  private void addAll(final boolean gc) {
    // must be in synchronization
    if(gc) {
      int i = 0;
      int k = 0;
      while(k < list.size()) {
        final WeakReference<T> ref = list.get(k);
        final T t = ref.get();
        if(t == null) {
          ++k;
          continue;
        }
        if(k > i) {
          list.set(i, ref);
        }
        ++i;
        ++k;
      }
      while(k > i) {
        list.remove(--k);
      }
    }
    if(toBeAdded.isEmpty()) return;
    final Set<T> contained = new HashSet<>();
    for(final WeakReference<T> el : list) {
      // we do not care for null elements
      contained.add(el.get());
    }
    for(final T add : toBeAdded) {
      if(contained.contains(add)) {
        continue;
      }
      list.add(new WeakReference<T>(add));
      contained.add(add);
    }
    toBeAdded.clear();
  }

  /** Starts a snapshot. */
  protected void startSnapshot() {
    synchronized(toBeAdded) {
      if(snapshots <= 0) {
        addAll(false);
      }
      ++snapshots;
    }
  }

  /**
   * Ends a snapshot.
   * 
   * @param gc Whether to remove all <code>null</code> pointers when this
   *          snapshot was the last.
   */
  protected void endSnapshot(final boolean gc) {
    synchronized(toBeAdded) {
      --snapshots;
      if(snapshots > 0) return;
      addAll(gc);
    }
  }

  /**
   * Creates a snapshot. Getter methods may return <code>null</code> pointers to
   * indicate that the element in question has been removed from memory
   * recently. The snapshot is best used as a resource:
   * 
   * <pre>
   * try (Snapshot<...> s = snapshotList.getSnapshot()) {
   *   // ... do stuff
   * }
   * </pre>
   * 
   * @return Creates a snapshot.
   */
  public Snapshot<T> getSnapshot() {
    return new Snapshot<>(this, list);
  }

  /**
   * Getter.
   * 
   * @return The number of currently active snapshots.
   */
  public int activeSnapshots() {
    return snapshots;
  }

}
