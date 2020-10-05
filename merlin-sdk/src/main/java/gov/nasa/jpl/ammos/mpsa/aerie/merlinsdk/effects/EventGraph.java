package gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.effects;

import java.util.Objects;
import java.util.function.Function;

/**
 * An immutable tree-representation of a graph of sequentially- and concurrently-composed events.
 *
 * <p>
 * An event graph is a <a href="https://en.wikipedia.org/wiki/Series-parallel_graph">series-parallel graph</a>
 * whose edges represent atomic events. Event graphs may be composed sequentially (in series) or concurrently (in
 * parallel).
 * </p>
 *
 * <p>
 * As with many recursive tree-like structures, an event graph is utilized by accepting a {@link Projection} object and
 * traversing the series-parallel structure recursively. A projection provides methods for each type of node in the tree
 * representation (empty, atomic event, sequential composition, and parallel composition). For each node, the projection
 * computes a result that will be provided to the same projection at the parent node. The result of the traversal is the
 * value computed by the projection at the root node.
 * </p>
 *
 * <p>
 * Different domains may interpret each event differently, and so evaluate the same event graph under different
 * projections. An event may have no particular effect in one domain, while being critically important to another
 * domain.
 * </p>
 *
 * @param <Event> The type of event to be stored in the graph structure.
 * @see Projection
 * @see EffectTrait
 */
public abstract class EventGraph<Event> implements EffectExpression<Event> {
  private EventGraph() {}

  public abstract boolean isEmpty();

  // The behavior of the empty graph is independent of the parameterized Event type,
  // so we cache a single instance and re-use it for all Event types.
  private static final EventGraph<?> EMPTY = new EventGraph<>() {
    @Override
    public <Effect> Effect evaluate(final EffectTrait<Effect> trait, final Function<Object, Effect> substitution) {
      return trait.empty();
    }

    @Override
    public boolean isEmpty() {
      return true;
    }
  };

  /**
   * Create an empty event graph.
   *
   * @param <Event> The type of event that might be contained by this event graph.
   * @return An empty event graph.
   */
  @SuppressWarnings("unchecked")
  public static <Event> EventGraph<Event> empty() {
    return (EventGraph<Event>) EventGraph.EMPTY;
  }

  /**
   * Create an event graph consisting of a single atomic event.
   *
   * @param atom An atomic event.
   * @param <Event> The type of the given atomic event.
   * @return An event graph consisting of a single atomic event.
   */
  public static <Event> EventGraph<Event> atom(final Event atom) {
    Objects.requireNonNull(atom);

    return new EventGraph<>() {
      @Override
      public <Effect> Effect evaluate(final EffectTrait<Effect> trait, final Function<Event, Effect> substitution) {
        return substitution.apply(atom);
      }

      @Override
      public boolean isEmpty() {
        return false;
      }
    };
  }

  /**
   * Create an event graph by combining multiple event graphs of the same type in sequence.
   *
   * @param prefix The first event graph to apply.
   * @param suffix The second event graph to apply.
   * @param <Event> The type of atomic event contained by these graphs.
   * @return An event graph consisting of a sequence of subgraphs.
   */
  public static <Event> EventGraph<Event> sequentially(final EventGraph<Event> prefix, final EventGraph<Event> suffix) {
    Objects.requireNonNull(prefix);
    Objects.requireNonNull(suffix);

    if (prefix.isEmpty()) return suffix;
    if (suffix.isEmpty()) return prefix;

    return new EventGraph<>() {
      @Override
      public <Effect> Effect evaluate(final EffectTrait<Effect> trait, final Function<Event, Effect> substitution) {
        return trait.sequentially(prefix.evaluate(trait, substitution), suffix.evaluate(trait, substitution));
      }

      @Override
      public boolean isEmpty() {
        return false;
      }
    };
  }

  /**
   * Create an event graph by combining multiple event graphs of the same type in parallel.
   *
   * @param left An event graph to apply concurrently.
   * @param right An event graph to apply concurrently.
   * @param <Event> The type of atomic event contained by these graphs.
   * @return An event graph consisting of a set of concurrent subgraphs.
   */
  public static <Event> EventGraph<Event> concurrently(final EventGraph<Event> left, final EventGraph<Event> right) {
    Objects.requireNonNull(left);
    Objects.requireNonNull(right);

    if (left.isEmpty()) return right;
    if (right.isEmpty()) return left;

    return new EventGraph<>() {
      @Override
      public <Effect> Effect evaluate(final EffectTrait<Effect> trait, final Function<Event, Effect> substitution) {
        return trait.concurrently(left.evaluate(trait, substitution), right.evaluate(trait, substitution));
      }

      @Override
      public boolean isEmpty() {
        return false;
      }
    };
  }

  /**
   * Create an event graph by combining multiple event graphs of the same type in sequence.
   *
   * @param segments A series of event graphs to combine in sequence.
   * @param <Event> The type of atomic event contained by these graphs.
   * @return An event graph consisting of a sequence of subgraphs.
   */
  @SafeVarargs
  public static <Event> EventGraph<Event> sequentially(final EventGraph<Event>... segments) {
    var acc = EventGraph.<Event>empty();
    for (final var segment : segments) acc = sequentially(acc, segment);
    return acc;
  }

  /**
   * Create an event graph by combining multiple event graphs of the same type in parallel.
   *
   * @param branches A set of event graphs to combine in parallel.
   * @param <Event> The type of atomic event contained by these graphs.
   * @return An event graph consisting of a set of concurrent subgraphs.
   */
  @SafeVarargs
  public static <Event> EventGraph<Event> concurrently(final EventGraph<Event>... branches) {
    var acc = EventGraph.<Event>empty();
    for (final var branch : branches) acc = concurrently(acc, branch);
    return acc;
  }

  @Override
  public String toString() {
    return EffectExpressionDisplay.displayGraph(this);
  }
}