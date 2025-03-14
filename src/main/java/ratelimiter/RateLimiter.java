package ratelimiter;

public interface RateLimiter {
    boolean shouldAllowRequest(int customerId, int timestamp);
}
