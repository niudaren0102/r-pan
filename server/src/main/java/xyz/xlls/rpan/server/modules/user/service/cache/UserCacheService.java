package xyz.xlls.rpan.server.modules.user.service.cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import xyz.xlls.rpan.cache.core.constants.CacheConstants;
import xyz.xlls.rpan.server.common.cache.AnnotationCacheService;
import xyz.xlls.rpan.server.modules.user.entity.RPanUser;
import xyz.xlls.rpan.server.modules.user.mapper.RPanUserMapper;

import java.io.Serializable;
@Component(value = "userAnnotationCacheService")
public class UserCacheService implements AnnotationCacheService<RPanUser> {
    @Autowired
    private RPanUserMapper mapper;
    @Override
    @Cacheable(cacheNames= CacheConstants.R_PAN_CACHE_NAME,keyGenerator = "userIdKeyGenerator",sync = true)
    public RPanUser getById(Serializable id) {
        return mapper.selectById(id);
    }

    @Override
    public boolean updateById(Serializable id, RPanUser entity) {
        return mapper.updateById(entity)==1;
    }

    @Override
    public boolean removeById(Serializable id) {
        return mapper.deleteById(id)==1;
    }
}
