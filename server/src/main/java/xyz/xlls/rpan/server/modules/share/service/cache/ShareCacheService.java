package xyz.xlls.rpan.server.modules.share.service.cache;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import xyz.xlls.rpan.server.common.cache.AbstractManualCacheService;
import xyz.xlls.rpan.server.modules.share.entity.RPanShare;
import xyz.xlls.rpan.server.modules.share.mapper.RPanShareMapper;

/**
 * 手动缓存实现分享业务查询等操作
 */
@Component("shareManualCacheService")
public class ShareCacheService extends AbstractManualCacheService<RPanShare> {
    @Autowired
    private RPanShareMapper mapper;
    @Override
    protected BaseMapper<RPanShare> getBaseMapper() {
        return mapper;
    }

    @Override
    public String getKeyFormat() {
        return "SHARE:ID:%s";
    }
}
