package ratelimiter;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class SlidingWindowLogRateLimiter implements RateLimiter {
    /*
        To further optimise on storage, we can use TreeMap instead of Queue, with mapping between timestamp and count of requests at that timestamp- this prevents us from storing each separate timestamp.
        The time complexity will still be O(windowSize) in both.
     */
    private final Map<Integer, Queue<Integer>> customerRequestTimestampsMap;
    private final int windowSize;
    private final int maxAllowedRequests;

    public SlidingWindowLogRateLimiter(int maxAllowedRequests, int windowSize) {
        this.maxAllowedRequests = maxAllowedRequests;
        this.windowSize = windowSize;
        this.customerRequestTimestampsMap = new HashMap<>();
    }

    @Override
    public boolean shouldAllowRequest(int customerId, int timestamp) {
        if (!customerRequestTimestampsMap.containsKey(customerId)) {
            Queue<Integer> requestTimestamps = new LinkedList<>();
            requestTimestamps.offer(timestamp);

            customerRequestTimestampsMap.put(customerId, requestTimestamps);
            return true;
        }

        Queue<Integer> requestTimestamps = customerRequestTimestampsMap.get(customerId);

        while (!requestTimestamps.isEmpty() && requestTimestamps.peek() < timestamp - windowSize)
            requestTimestamps.poll();

        if (requestTimestamps.size() < maxAllowedRequests) {
            requestTimestamps.offer(timestamp);
            return true;
        }

        return false;
    }
}
