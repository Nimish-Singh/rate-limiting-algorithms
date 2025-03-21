package ratelimiter;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

// This is not actually a famous rate limiting algo but the first rough attempt to implement it in some way
public class FixedWindowTokenBucketRateLimiter implements RateLimiter {
    // customer -> window start -> remaining tokens map
    /*
        Such a mapping allows us to not have to clear the map for every window start (which we would have to do with a customer -> remaining token map)
        The downside is that this map keeps on growing and will need to be cleaned up
     */
    private final ConcurrentMap<Integer, ConcurrentMap<Integer, AtomicInteger>> customerRemainingTokensMap;
    private final int windowSize;
    private final int maxTokens;
    private final AtomicInteger windowStart;

    public FixedWindowTokenBucketRateLimiter(int windowSize, int maxTokens) {
        this.windowSize = windowSize;
        this.maxTokens = maxTokens;
        this.windowStart = new AtomicInteger(0);
        this.customerRemainingTokensMap = new ConcurrentHashMap<>();
    }

    @Override
    public boolean shouldAllowRequest(int customerId, int timestamp) {
        // Move to the new window if the current window has expired
        int newWindowStart = (timestamp / windowSize) * windowSize;
        windowStart.updateAndGet(existing -> Math.max(existing, newWindowStart));

        // Retrieve the remaining tokens for this customer or initialize if not present
        ConcurrentMap<Integer, AtomicInteger> windowStartTokensRemainingMap = customerRemainingTokensMap.getOrDefault(customerId, new ConcurrentHashMap<>());
        AtomicInteger remainingTokens = windowStartTokensRemainingMap.getOrDefault(windowStart, new AtomicInteger(maxTokens));

        // Cleanup: Remove entries of expired windows
        /*
            If cleanup operation needs to be optimised then we can replace the map with a treeMap.
            But that will impact the insertion complexity. It is a tradeoff
         */
        cleanupExpiredWindows(windowStartTokensRemainingMap);

        if (remainingTokens.decrementAndGet() >= 0) {
            // Allow the request and decrement the tokens
            windowStartTokensRemainingMap.put(windowStart.get(), remainingTokens);
            customerRemainingTokensMap.put(customerId, windowStartTokensRemainingMap);
            return true;
        }

        // Request denied
        return false;
    }

    private void cleanupExpiredWindows(ConcurrentMap<Integer, AtomicInteger> windowStartTokensRemainingMap) {
        // Remove expired window entries
        int currentWindowStart = windowStart.get();
        windowStartTokensRemainingMap.entrySet().removeIf(entry -> entry.getKey() < currentWindowStart);
    }
}