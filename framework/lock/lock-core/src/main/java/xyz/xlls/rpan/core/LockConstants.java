package xyz.xlls.rpan.core;

/**
 * 锁相关通用常量类
 */
public interface LockConstants {
    /**
     * 公用lock的名称
     */
    String R_PAN_LOCK="r-pan-lock";
    /**
     * 公用lock的path
     * 主要针对zk等节点型软件
     */
    String R_PAN_LOCK_PATH="/r-pan-lock";
}
