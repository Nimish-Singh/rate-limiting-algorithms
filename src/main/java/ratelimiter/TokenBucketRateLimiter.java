package ratelimiter;

import java.util.HashMap;
import java.util.Map;

public class TokenBucketRateLimiter implements RateLimiter {
    // customer -> last refill time -> remaining tokens map
    /*
        Such a mapping is required to make it work correctly for rate limites for different users
     */
    private final Map<Integer, Map<Integer, Integer>> customerTokenBuckets;
    // Maximum tokens the bucket can hold
    private final int maxTokens;
    // Tokens added per second
    private final int refillRate;

    public TokenBucketRateLimiter(int maxTokens, int refillRate) {
        this.maxTokens = maxTokens;
        this.refillRate = refillRate;
        this.customerTokenBuckets = new HashMap<>();
    }

    @Override
    public boolean shouldAllowRequest(int customerId, int timestamp) {
        refillTokens(customerId, timestamp);

        Map<Integer, Integer> lastRefillTimeTokensMap = customerTokenBuckets.get(customerId);

        Integer lastRefillTimestamp = lastRefillTimeTokensMap.keySet().stream().findFirst().orElse(timestamp);
        int currentTokens = lastRefillTimeTokensMap.get(lastRefillTimestamp);

        if (currentTokens > 0) {
            lastRefillTimeTokensMap.put(lastRefillTimestamp, currentTokens - 1);
            return true;
        }

        return false;
    }

    private void refillTokens(int customerId, int timestamp) {
        if (!customerTokenBuckets.containsKey(customerId)) {
            Map<Integer, Integer> lastRefillTimeTokensMap = new HashMap<>();
            lastRefillTimeTokensMap.put(timestamp, maxTokens);
            customerTokenBuckets.put(customerId, lastRefillTimeTokensMap);
            return;
        }

        Map<Integer, Integer> lastRefillTimeTokensMap = customerTokenBuckets.get(customerId);
        int lastRefillTime = lastRefillTimeTokensMap.keySet().stream().findFirst().get();
        int currentTokens = lastRefillTimeTokensMap.get(lastRefillTime);
        int tokensToRefill = (timestamp - lastRefillTime) * refillRate;

        if (tokensToRefill > 0) {
            int tokensToReset = Math.min(currentTokens + tokensToRefill, maxTokens);
            // Effectively keeping this map as a single-entry map
            lastRefillTimeTokensMap.remove(lastRefillTime);
            lastRefillTimeTokensMap.put(timestamp, tokensToReset);
        }
    }
}
