package com.java.agentflow.async;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
public class ExecutionStateService {

    private static final Logger log = LoggerFactory.getLogger(ExecutionStateService.class);
    private static final Duration LOCK_TTL = Duration.ofMinutes(10);
    private static final Duration STATE_TTL = Duration.ofHours(24);

    private final StringRedisTemplate redis;

    public ExecutionStateService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public boolean tryLock(UUID executionId) {
        String key = lockKey(executionId);
        Boolean acquired = redis.opsForValue().setIfAbsent(key, "locked", LOCK_TTL);
        return Boolean.TRUE.equals(acquired);
    }

    public void unlock(UUID executionId) {
        redis.delete(lockKey(executionId));
    }

    public int incrementRetry(UUID executionId) {
        String key = retryKey(executionId);
        Long count = redis.opsForValue().increment(key);
        redis.expire(key, STATE_TTL);
        return count != null ? count.intValue() : 1;
    }

    public void markFailed(UUID executionId, String error) {
        String key = stateKey(executionId);
        redis.opsForValue().set(key, "FAILED:" + error, STATE_TTL);
    }

    public void markRunning(UUID executionId) {
        redis.opsForValue().set(stateKey(executionId), "RUNNING", STATE_TTL);
    }

    public String getState(UUID executionId) {
        return redis.opsForValue().get(stateKey(executionId));
    }

    private String lockKey(UUID id) {
        return "lock:" + id;
    }

    private String retryKey(UUID id) {
        return "retry:" + id;
    }

    private String stateKey(UUID id) {
        return "state:" + id;
    }
}
