package ratelimiter;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LeakyBucketRateLimiterTest {
    private final LeakyBucketRateLimiter rateLimiter = new LeakyBucketRateLimiter(5, 2, 1);

    @Test
    public void shouldAllowRequestWhenMultipleCustomerRequestsIntertwine() {
        int aCustomerId = 100;
        int anotherCustomerId = 200;

        assertTrue(rateLimiter.shouldAllowRequest(aCustomerId, 11));
        assertTrue(rateLimiter.shouldAllowRequest(anotherCustomerId, 11));

        assertTrue(rateLimiter.shouldAllowRequest(aCustomerId, 12));
        assertTrue(rateLimiter.shouldAllowRequest(anotherCustomerId, 12));

        assertTrue(rateLimiter.shouldAllowRequest(aCustomerId, 12));
        assertTrue(rateLimiter.shouldAllowRequest(anotherCustomerId, 12));

        assertTrue(rateLimiter.shouldAllowRequest(aCustomerId, 12));
        assertTrue(rateLimiter.shouldAllowRequest(anotherCustomerId, 12));

        assertTrue(rateLimiter.shouldAllowRequest(aCustomerId, 12));
        assertTrue(rateLimiter.shouldAllowRequest(anotherCustomerId, 12));

        assertTrue(rateLimiter.shouldAllowRequest(aCustomerId, 12));
        assertTrue(rateLimiter.shouldAllowRequest(anotherCustomerId, 12));

        assertFalse(rateLimiter.shouldAllowRequest(aCustomerId, 12));
        assertFalse(rateLimiter.shouldAllowRequest(anotherCustomerId, 12));

        assertTrue(rateLimiter.shouldAllowRequest(aCustomerId, 13));
        assertTrue(rateLimiter.shouldAllowRequest(anotherCustomerId, 13));
    }

    @Test
    public void shouldAllowRequestWhenMultipleCustomerRequestsDoNotIntertwine() {
        int aCustomerId = 100;
        int anotherCustomerId = 200;

        assertTrue(rateLimiter.shouldAllowRequest(aCustomerId, 11));

        assertTrue(rateLimiter.shouldAllowRequest(anotherCustomerId, 12));
        assertTrue(rateLimiter.shouldAllowRequest(anotherCustomerId, 12));
        assertTrue(rateLimiter.shouldAllowRequest(anotherCustomerId, 12));
        assertTrue(rateLimiter.shouldAllowRequest(anotherCustomerId, 12));
        assertTrue(rateLimiter.shouldAllowRequest(anotherCustomerId, 12));
        assertFalse(rateLimiter.shouldAllowRequest(anotherCustomerId, 12));
    }
}
