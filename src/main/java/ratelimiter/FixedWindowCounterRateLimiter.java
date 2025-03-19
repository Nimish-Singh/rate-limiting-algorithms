package ratelimiter;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

public class FixedWindowCounterRateLimiter implements RateLimiter {
    private final ConcurrentMap<Integer, WindowCounter> customerWindowCounterMap;
    private final int maxAllowedRequests;
    private final int windowSize;
    private AtomicInteger currentWindowStart;

    public FixedWindowCounterRateLimiter(int maxAllowedRequests, int windowSize) {
        this.maxAllowedRequests = maxAllowedRequests;
        this.windowSize = windowSize;
        this.customerWindowCounterMap = new ConcurrentHashMap<>();
        currentWindowStart = new AtomicInteger(0);
    }

    @Override
    public boolean shouldAllowRequest(int customerId, int timestamp) {
        currentWindowStart.addAndGet((timestamp - currentWindowStart.get()) / windowSize * windowSize);

        // An example implementation of using compute() to make it fully thread safe by avoiding separate get() and put() calls
        customerWindowCounterMap.compute(customerId, (key, windowCounter) -> {
            if (windowCounter == null || windowCounter.getLastUpdateTime() + windowSize <= timestamp) {
                // Either a new user, or a new time window has started
                return new WindowCounter(1, currentWindowStart.get());
            }

            if (windowCounter.getOngoingRequestCount() < maxAllowedRequests) {
                // Allow request and increment the count
                return new WindowCounter(windowCounter.getOngoingRequestCount() + 1, currentWindowStart.get());
            }

            // If request limit is reached, return the same windowCounter (no update)
            // A special workaround hack to ensure that request count goes higher than maxAllowedRequests so that the check in last line works
            return new WindowCounter(windowCounter.getOngoingRequestCount() + 1, windowCounter.getLastUpdateTime());
        });

        return customerWindowCounterMap.get(customerId).getOngoingRequestCount() <= maxAllowedRequests;
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
