package outoftower.map.definition;

import java.util.Objects;

/** Canonical key for an undirected edge. */
public final class EdgeKey implements Comparable<EdgeKey> {
    private final String first;
    private final String second;

    private EdgeKey(String first, String second) {
        this.first = first;
        this.second = second;
    }

    public static EdgeKey of(String first, String second) {
        if (first.compareTo(second) <= 0) {
            return new EdgeKey(first, second);
        }
        return new EdgeKey(second, first);
    }

    public String getFirst() { return first; }
    public String getSecond() { return second; }

    public boolean contains(String nodeId) {
        return first.equals(nodeId) || second.equals(nodeId);
    }

    @Override
    public int compareTo(EdgeKey other) {
        int firstCompare = first.compareTo(other.first);
        return firstCompare != 0 ? firstCompare : second.compareTo(other.second);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof EdgeKey)) return false;
        EdgeKey other = (EdgeKey) obj;
        return first.equals(other.first) && second.equals(other.second);
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }

    @Override
    public String toString() {
        return first + "<->" + second;
    }
}
