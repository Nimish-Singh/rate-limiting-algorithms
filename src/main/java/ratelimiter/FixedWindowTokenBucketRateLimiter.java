package ratelimiter;

import java.util.HashMap;
import java.util.Map;

public class FixedWindowTokenBucketRateLimiter implements RateLimiter {
    // customer -> window start -> remaining tokens map
    /*
        Such a mapping allows us to not have to clear the map for every window start (which we would have to do with a customer -> remaining token map)
        The downside is that this map keeps on growing and will need to be cleaned up
     */
    private final Map<Integer, Map<Integer, Integer>> customerRemainingTokensMap;
    private final int windowSize;
    private final int maxTokens;
    private int windowStart;

    public FixedWindowTokenBucketRateLimiter(int windowSize, int maxTokens) {
        this.windowSize = windowSize;
        this.maxTokens = maxTokens;
        this.windowStart = 0;
        this.customerRemainingTokensMap = new HashMap<>();
    }

    @Override
    public boolean shouldAllowRequest(int customerId, int timestamp) {
        // Move to the new window if the current window has expired
        if (timestamp >= windowStart + windowSize) {
            windowStart = (timestamp / windowSize) * windowSize;
        }

        // Retrieve the remaining tokens for this customer or initialize if not present
        Map<Integer, Integer> windowStartTokensRemainingMap = customerRemainingTokensMap.getOrDefault(customerId, new HashMap<>());
        int remainingTokens = windowStartTokensRemainingMap.getOrDefault(windowStart, maxTokens);

        // Cleanup: Remove entries of expired windows
        /*
            If cleanup operation needs to be optimised then we can replace the map with a treeMap.
            But that will impact the insertion complexity. It is a tradeoff
         */
        cleanupExpiredWindows(windowStartTokensRemainingMap);

        if (remainingTokens > 0) {
            // Allow the request and decrement the tokens
            windowStartTokensRemainingMap.put(windowStart, remainingTokens - 1);
            customerRemainingTokensMap.put(customerId, windowStartTokensRemainingMap);
            return true;
        }

        // Request denied
        return false;
    }

    private void cleanupExpiredWindows(Map<Integer, Integer> windowStartTokensRemainingMap) {
        // Remove expired window entries
        windowStartTokensRemainingMap.entrySet().removeIf(entry -> entry.getKey() < windowStart);
    }
}