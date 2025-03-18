package ratelimiter;

import java.util.HashMap;
import java.util.Map;

public class FixedWindowCounterRateLimiter implements RateLimiter {
    private final Map<Integer, WindowCounter> customerWindowCounterMap;
    private final int maxAllowedRequests;
    private final int windowSize;
    private int currentWindowStart;

    public FixedWindowCounterRateLimiter(int maxAllowedRequests, int windowSize) {
        this.maxAllowedRequests = maxAllowedRequests;
        this.windowSize = windowSize;
        this.customerWindowCounterMap = new HashMap<>();
        currentWindowStart = 0;
    }

    @Override
    public boolean shouldAllowRequest(int customerId, int timestamp) {
        currentWindowStart += ((timestamp - currentWindowStart) / windowSize) * windowSize;

        if (!customerWindowCounterMap.containsKey(customerId)) {
            customerWindowCounterMap.put(customerId, new WindowCounter(1, currentWindowStart));
            return true;
        }

        WindowCounter windowCounter = customerWindowCounterMap.get(customerId);

        if (windowCounter.getLastUpdateTime() + windowSize <= timestamp) {
            customerWindowCounterMap.put(customerId, new WindowCounter(1, currentWindowStart));
            return true;
        }

        if (windowCounter.getOngoingRequestCount() == maxAllowedRequests) {
            return false;
        }

        customerWindowCounterMap.put(customerId, new WindowCounter(windowCounter.getOngoingRequestCount() + 1, currentWindowStart));
        return true;
    }
}

class WindowCounter {
    private final int ongoingRequestCount;
    private final int lastUpdateTime;

    public WindowCounter(int ongoingRequestCount, int lastUpdateTime) {
        this.ongoingRequestCount = ongoingRequestCount;
        this.lastUpdateTime = lastUpdateTime;
    }

    public int getOngoingRequestCount() {
        return ongoingRequestCount;
    }

    public int getLastUpdateTime() {
        return lastUpdateTime;
    }
}
