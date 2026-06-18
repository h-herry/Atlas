package com.atlas.common.cache.lock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@DisplayName("RedisDistributedLock 分布式锁测试")
@ExtendWith(MockitoExtension.class)
class RedisDistributedLockTest {

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private RLock rLock;

    @InjectMocks
    private RedisDistributedLock lock;

    @BeforeEach
    void setUp() {
        when(redissonClient.getLock(anyString())).thenReturn(rLock);
    }

    @Test
    @DisplayName("获取锁成功后应执行回调并返回结果")
    void should_execute_supplier_when_lock_acquired() throws InterruptedException {
        when(rLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        when(rLock.isHeldByCurrentThread()).thenReturn(true);

        String result = lock.execute("order:123", 3, -1, () -> "success");

        assertThat(result).isEqualTo("success");
        verify(rLock).unlock();
    }

    @Test
    @DisplayName("获取锁失败应抛出 RuntimeException")
    void should_throw_when_lock_not_acquired() throws InterruptedException {
        when(rLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(false);

        assertThatThrownBy(() -> lock.execute("order:123", 3, -1, () -> "data"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("系统繁忙");
    }

    @Test
    @DisplayName("发生 InterruptedException 应抛 RuntimeException")
    void should_throw_when_interrupted() throws InterruptedException {
        when(rLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class)))
                .thenThrow(new InterruptedException());

        assertThatThrownBy(() -> lock.execute("order:123", 3, -1, () -> "data"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("被中断");
    }

    @Test
    @DisplayName("简化版 execute 应使用默认 waitTime=3s leaseTime=-1")
    void should_use_defaults_when_simple_execute() throws InterruptedException {
        when(rLock.tryLock(eq(3L), eq(-1L), eq(TimeUnit.SECONDS))).thenReturn(true);
        when(rLock.isHeldByCurrentThread()).thenReturn(true);

        lock.execute("order:456", () -> "ok");

        verify(rLock).unlock();
    }

    @Test
    @DisplayName("未获取锁时不应 unlock")
    void should_not_unlock_when_not_acquired() throws InterruptedException {
        when(rLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(false);

        try {
            lock.execute("order:789", 3, -1, () -> "data");
        } catch (RuntimeException ignored) {}

        verify(rLock, never()).unlock();
    }
}
