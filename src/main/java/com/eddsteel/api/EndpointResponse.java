package com.eddsteel.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class EndpointResponse implements Serializable {
    private static final long serialVersionUID = -5910478419843215463L;

    private final String label;
    private final List<Integer> integers;
    private final Long integerCount;

    public EndpointResponse(
            @JsonProperty("label") String label,
            @JsonProperty("integers") List<Integer> integers,
            @JsonProperty("integerCount") Long integerCount
    ) {
        this.label = label;
        this.integers = integers;
        this.integerCount = integerCount;
    }

    @Override
    public String toString() {
        return String.format(
                "PartitionListResponse: endpoint = %s, partitionGroups = %s, error = %s, partitionCount = %d",
                label,
                integers.toString(),
                integerCount
        );
    }

    public String getLabel() {
        return label;
    }

    public List<Integer> getIntegers() {
        return integers;
    }

    public Long getIntegerCount() {
        return integerCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EndpointResponse that = (EndpointResponse) o;
        return Objects.equals(label, that.label) &&
                Objects.equals(integers, that.integers) &&
                Objects.equals(integerCount, that.integerCount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(label, integers, integerCount);
    }
}
