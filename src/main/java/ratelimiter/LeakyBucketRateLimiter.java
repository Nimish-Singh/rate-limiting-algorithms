package ratelimiter;

import java.util.HashMap;
import java.util.Map;

public class LeakyBucketRateLimiter implements RateLimiter {
    private final Map<Integer, LeakyBucket> customerBucketsMap;
    private final int maxRequestCapacity;
    private final int leakRate;
    private final int leakWindow;

    public LeakyBucketRateLimiter(int maxRequestCapacity, int leakRate, int leakWindow) {
        this.customerBucketsMap = new HashMap<>();
        /*
            Different from token bucket in the sense that token bucket refills tokens regularly
            whereas leaky bucket refills only as per leakRate, when tokens become free
         */
        this.maxRequestCapacity = maxRequestCapacity;
        this.leakRate = leakRate;
        this.leakWindow = leakWindow;
    }

    @Override
    public boolean shouldAllowRequest(int customerId, int timestamp) {
        processAndLeakRequests(customerId, timestamp);

        LeakyBucket bucket = customerBucketsMap.get(customerId);

        int currentRequestCount = bucket.getCurrentRequestCount();

        if (currentRequestCount < maxRequestCapacity) {
            customerBucketsMap.put(customerId, new LeakyBucket(currentRequestCount + 1, timestamp));
            return true;
        }

        return false;
    }

    // "Leak requests" means take out requests that have been processed by now
    private void processAndLeakRequests(int customerId, int timestamp) {
        if (!customerBucketsMap.containsKey(customerId)) {
            LeakyBucket bucket = new LeakyBucket(0, timestamp);
            customerBucketsMap.put(customerId, bucket);
            return;
        }

        LeakyBucket bucket = customerBucketsMap.get(customerId);
        int lastUpdatedAt = bucket.getLastUpdatedAt();
        int currentRequestCount = bucket.getCurrentRequestCount();
        int leakedRequests = ((timestamp - lastUpdatedAt) / leakWindow) * leakRate;

        if (leakedRequests > 0) {
            int remainingRequests = Math.max(0, currentRequestCount - leakedRequests);
            LeakyBucket newBucket = new LeakyBucket(remainingRequests, timestamp);
            customerBucketsMap.put(customerId, newBucket);
        }
    }
}

class LeakyBucket {
    private final int currentRequestCount;
    private final int lastUpdatedAt;

    public LeakyBucket(int currentRequestCount, int lastUpdatedAt) {
        this.currentRequestCount = currentRequestCount;
        this.lastUpdatedAt = lastUpdatedAt;
    }

    public int getCurrentRequestCount() {
        return currentRequestCount;
    }

    public int getLastUpdatedAt() {
        return lastUpdatedAt;
    }
}