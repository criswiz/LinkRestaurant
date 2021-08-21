package com.sensei.linkrestaurant.Model;

import java.util.List;

public class FCMResponse {
    private long multicast_Id;
    private int success, failure, canonical_ids;
    private List<FCMResult> results;

    public FCMResponse() {
    }

    public long getMulticast_Id() {
        return multicast_Id;
    }

    public void setMulticast_Id(long multicast_Id) {
        this.multicast_Id = multicast_Id;
    }

    public int getSuccess() {
        return success;
    }

    public void setSuccess(int success) {
        this.success = success;
    }

    public int getFailure() {
        return failure;
    }

    public void setFailure(int failure) {
        this.failure = failure;
    }

    public int getCanonical_ids() {
        return canonical_ids;
    }

    public void setCanonical_ids(int canonical_ids) {
        this.canonical_ids = canonical_ids;
    }

    public List<FCMResult> getResults() {
        return results;
    }

    public void setResults(List<FCMResult> results) {
        this.results = results;
    }
}
