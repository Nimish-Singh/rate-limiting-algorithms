package ratelimiter;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

public class SlidingWindowLogRateLimiter implements RateLimiter {
    /*
        To further optimise on storage, we can use TreeMap instead of Queue, with mapping between timestamp and count of requests at that timestamp- this prevents us from storing each separate timestamp.
        The time complexity will still be O(windowSize) in both.
     */
    private final ConcurrentMap<Integer, ConcurrentLinkedQueue<Integer>> customerRequestTimestampsMap;
    private final int windowSize;
    private final int maxAllowedRequests;

    public SlidingWindowLogRateLimiter(int maxAllowedRequests, int windowSize) {
        this.maxAllowedRequests = maxAllowedRequests;
        this.windowSize = windowSize;
        this.customerRequestTimestampsMap = new ConcurrentHashMap<>();
    }

    @Override
    public boolean shouldAllowRequest(int customerId, int timestamp) {
        // computeIfAbsent() is thread-safe
        ConcurrentLinkedQueue<Integer> requestTimestamps = customerRequestTimestampsMap.computeIfAbsent(customerId, k -> new ConcurrentLinkedQueue<>());

        while (!requestTimestamps.isEmpty() && requestTimestamps.peek() < timestamp - windowSize)
            requestTimestamps.poll();

        if (requestTimestamps.size() < maxAllowedRequests) {
            requestTimestamps.offer(timestamp);
            return true;
        }

        return false;
    }
}
