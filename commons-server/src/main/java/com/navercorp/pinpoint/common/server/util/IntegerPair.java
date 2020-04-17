package com.navercorp.pinpoint.common.server.util;

import java.util.Comparator;

public class IntegerPair {
    private int first;
    private int second;

    public IntegerPair(int first, int second) {
        this.first = first;
        this.second = second;
    }

    public int getFirst() {
        return first;
    }

    public int getSecond() {
        return second;
    }

    public void incrementFirst() {
        addFirst(1);
    }

    public void addFirst(int delta) {
        first += delta;
    }


    public void incrementSecond() {
        addSecond(1);
    }

    public void addSecond(int delta) {
        second += delta;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IntegerPair)) return false;

        IntegerPair that = (IntegerPair) o;

        if (first != that.first) return false;
        return second == that.second;
    }

    @Override
    public int hashCode() {
        int result = (int) (first ^ (first >>> 32));
        result = 31 * result + (int) (second ^ (second >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "IntegerPair{" +
                "first=" + first +
                ", second=" + second +
                '}';
    }

}
