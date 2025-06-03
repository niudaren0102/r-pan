package xyz.xlls.rpan.storage.engine.core;

import cn.hutool.core.lang.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import xyz.xlls.rpan.cache.core.constants.CacheConstants;
import xyz.xlls.rpan.core.exception.RPanFrameworkException;
import xyz.xlls.rpan.storage.engine.core.context.DeleteFileContext;
import xyz.xlls.rpan.storage.engine.core.context.StoreFileContext;

import java.io.IOException;
import java.util.Objects;

/**
 * 顶级存储引擎的公用父类
 */
public abstract class AbstractStorageEngine implements StorageEngine{
    @Autowired
    private CacheManager cacheManager;

    /**
     * 功用的获取缓存的方法
     * @return
     */
    protected Cache getCache() {
        if(Objects.isNull(cacheManager)){
            throw new RPanFrameworkException("the cache manager is empty!");
        }
        return cacheManager.getCache(CacheConstants.R_PAN_CACHE_NAME);
    }

    /**
     * 存储物理文件
     * 1、参数校验
     * 2、执行动作
     * @param context
     * @throws IOException
     */
    @Override
    public void store(StoreFileContext context) throws IOException {
        checkStoreFileContext(context);
        doStore(context);
    }

    /**
     * 执行保存物理文件的动作
     * 下沉到具体的子类去实现
     * @param context
     */
    protected abstract void doStore(StoreFileContext context) throws IOException;

    /**
     * 校验上传物理文件的上下文信息
     * @param context
     */
    private void checkStoreFileContext(StoreFileContext context) {
        Assert.notBlank(context.getFilename(),"文件名称不能为空");
        Assert.notNull(context.getTotalSize(),"文件总大小不能为空");
        Assert.notBlank(context.getFilename(),"文件不能为空");
    }

    /**
     * 删除物理文件
     * 1、校验参数
     * 2、执行动作
     * @param context
     * @throws IOException
     */
    @Override
    public void delete(DeleteFileContext context) throws IOException {
        checkDeleteFileContext(context);
        doDelete(context);
    }

    /**
     * 执行删除无路文件的动作
     * 下沉到子类去实现
     * @param context
     */
    protected abstract void doDelete(DeleteFileContext context) throws IOException;

    /**
     * 校验删除物理文件的上下文信息
     * @param context
     */
    private void checkDeleteFileContext(DeleteFileContext context) {
        Assert.notEmpty(context.getRealFilePathList(),"要删除的文件路径列表不能为空");
    }
}
