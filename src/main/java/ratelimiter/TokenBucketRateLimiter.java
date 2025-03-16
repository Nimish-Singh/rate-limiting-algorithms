package ratelimiter;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class TokenBucketRateLimiter implements RateLimiter {
    private final ConcurrentMap<Integer, TokenBucket> customerTokenBuckets;
    // Maximum tokens the bucket can hold
    private final int maxTokens;
    // Tokens added per second
    private final int refillRate;
    // How often to refill tokens
    private final int refillWindow;

    public TokenBucketRateLimiter(int maxTokens, int refillRate, int refillWindow) {
        this.maxTokens = maxTokens;
        this.refillRate = refillRate;
        this.refillWindow = refillWindow;
        this.customerTokenBuckets = new ConcurrentHashMap<>();
    }

    @Override
    public boolean shouldAllowRequest(int customerId, int timestamp) {
        refillTokens(customerId, timestamp);

        TokenBucket bucket = customerTokenBuckets.get(customerId);

        int lastRefillTimestamp = bucket.getLastRefillTime();
        int currentTokens = bucket.getRemainingTokens();

        if (currentTokens > 0) {
            customerTokenBuckets.put(customerId, new TokenBucket(lastRefillTimestamp, currentTokens - 1));
            return true;
        }

        return false;
    }

    private void refillTokens(int customerId, int timestamp) {
        if (!customerTokenBuckets.containsKey(customerId)) {
            TokenBucket bucket = new TokenBucket(timestamp, maxTokens);
            customerTokenBuckets.put(customerId, bucket);
            return;
        }

        /*
            Ideally for concurrent situations, get() and put() separately are susceptible to race condition.
            In order to fix it, use compute() to combine the logic for checking and then updating map
         */
        TokenBucket bucket = customerTokenBuckets.get(customerId);
        int lastRefillTime = bucket.getLastRefillTime();
        int currentTokens = bucket.getRemainingTokens();
        int tokensToRefill = ((timestamp - lastRefillTime) / refillWindow) * refillRate;

        if (tokensToRefill > 0) {
            int tokensToReset = Math.min(currentTokens + tokensToRefill, maxTokens);
            TokenBucket newBucket = new TokenBucket(timestamp, tokensToReset);
            customerTokenBuckets.put(customerId, newBucket);
        }
    }
}

// Is thread-safe because of immutability
class TokenBucket {
    private final int lastRefillTime;
    private final int remainingTokens;

    public TokenBucket(int lastRefillTime, int remainingTokens) {
        this.lastRefillTime = lastRefillTime;
        this.remainingTokens = remainingTokens;
    }

    public int getLastRefillTime() {
        return lastRefillTime;
    }

    public int getRemainingTokens() {
        return remainingTokens;
    }
}