package xyz.xlls.rpan.core.key;

import xyz.xlls.rpan.core.LockContext;

/**
 * 锁的key的生成器顶级接口
 */
public interface KeyGenerator {
    /**
     * 生成锁的Key
     * @param lockContext
     * @return
     */
    String generateKey(LockContext lockContext);
}
