package ratelimiter;

import java.util.HashMap;
import java.util.Map;

public class SlidingWindowCounterRateLimiter implements RateLimiter {
    private final Map<Integer, RequestCounter> customerRequestCounterMap;
    private final int windowSize;
    private final int maxAllowedRequests;

    public SlidingWindowCounterRateLimiter(int windowSize, int maxAllowedRequests) {
        this.windowSize = windowSize;
        this.maxAllowedRequests = maxAllowedRequests;
        customerRequestCounterMap = new HashMap<>();
    }

    @Override
    public boolean shouldAllowRequest(int customerId, int timestamp) {
        customerRequestCounterMap.putIfAbsent(customerId, new RequestCounter(0, 0, 0));

        RequestCounter requestCounter = customerRequestCounterMap.get(customerId);

        int elapsedTime = timestamp - requestCounter.getCurrentWindowStart();

        if (elapsedTime >= windowSize) {
            int newCurrentWindowStart = requestCounter.getCurrentWindowStart() + (elapsedTime / windowSize) * windowSize;
            int newPreviousWindowCount = elapsedTime >= 2 * windowSize ? 0 : requestCounter.getCurrentWindowCount();
            int newCurrentWindowCount = 0;

            customerRequestCounterMap.put(customerId, new RequestCounter(newPreviousWindowCount, newCurrentWindowCount, newCurrentWindowStart));
        }

        requestCounter = customerRequestCounterMap.get(customerId);

        double previousWindowWeight = 1.0 - (double) elapsedTime / windowSize;
        int effectiveRequestCount = (int) (requestCounter.getCurrentWindowCount() + requestCounter.getPreviousWindowCount() * previousWindowWeight);

        if (effectiveRequestCount < maxAllowedRequests) {
            customerRequestCounterMap.put(customerId, new RequestCounter(requestCounter.getPreviousWindowCount(), requestCounter.getCurrentWindowCount() + 1, requestCounter.getCurrentWindowStart()));
            return true;
        }

        return false;
    }
}

class RequestCounter {
    private final int previousWindowCount;
    private final int currentWindowCount;
    private final int currentWindowStart;

    public RequestCounter(int previousWindowCount, int currentWindowCount, int currentWindowStart) {
        this.previousWindowCount = previousWindowCount;
        this.currentWindowCount = currentWindowCount;
        this.currentWindowStart = currentWindowStart;
    }

    public int getPreviousWindowCount() {
        return previousWindowCount;
    }

    public int getCurrentWindowCount() {
        return currentWindowCount;
    }

    public int getCurrentWindowStart() {
        return currentWindowStart;
    }
}
