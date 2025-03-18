package ratelimiter;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FixedWindowCounterRateLimiterTest {
    private final FixedWindowCounterRateLimiter rateLimiter = new FixedWindowCounterRateLimiter(5, 10);

    @Test
    public void shouldAllowRequestWhenMultipleCustomerRequestsIntertwine() {
        int aCustomerId = 100;
        int anotherCustomerId = 200;

        assertTrue(rateLimiter.shouldAllowRequest(aCustomerId, 11));
        assertTrue(rateLimiter.shouldAllowRequest(anotherCustomerId, 11));

        assertTrue(rateLimiter.shouldAllowRequest(aCustomerId, 12));
        assertTrue(rateLimiter.shouldAllowRequest(anotherCustomerId, 12));

        assertTrue(rateLimiter.shouldAllowRequest(aCustomerId, 13));
        assertTrue(rateLimiter.shouldAllowRequest(anotherCustomerId, 13));

        assertTrue(rateLimiter.shouldAllowRequest(aCustomerId, 14));
        assertTrue(rateLimiter.shouldAllowRequest(anotherCustomerId, 14));

        assertTrue(rateLimiter.shouldAllowRequest(aCustomerId, 18));
        assertTrue(rateLimiter.shouldAllowRequest(anotherCustomerId, 18));

        assertFalse(rateLimiter.shouldAllowRequest(aCustomerId, 19));
        assertFalse(rateLimiter.shouldAllowRequest(anotherCustomerId, 19));

        assertTrue(rateLimiter.shouldAllowRequest(aCustomerId, 20));
        assertTrue(rateLimiter.shouldAllowRequest(anotherCustomerId, 20));
    }

    @Test
    public void shouldAllowRequestWhenMultipleCustomerRequestsDoNotIntertwine() {
        int aCustomerId = 100;
        int anotherCustomerId = 200;

        assertTrue(rateLimiter.shouldAllowRequest(aCustomerId, 12));

        assertTrue(rateLimiter.shouldAllowRequest(anotherCustomerId, 12));
        assertTrue(rateLimiter.shouldAllowRequest(anotherCustomerId, 12));
        assertTrue(rateLimiter.shouldAllowRequest(anotherCustomerId, 12));
        assertTrue(rateLimiter.shouldAllowRequest(anotherCustomerId, 12));
        assertTrue(rateLimiter.shouldAllowRequest(anotherCustomerId, 12));
        assertFalse(rateLimiter.shouldAllowRequest(anotherCustomerId, 12));
    }
}