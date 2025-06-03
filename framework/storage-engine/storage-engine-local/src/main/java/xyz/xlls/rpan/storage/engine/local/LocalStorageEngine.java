package xyz.xlls.rpan.storage.engine.local;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import xyz.xlls.rpan.core.utils.FileUtil;
import xyz.xlls.rpan.storage.engine.core.AbstractStorageEngine;
import xyz.xlls.rpan.storage.engine.core.context.DeleteFileContext;
import xyz.xlls.rpan.storage.engine.core.context.StoreFileContext;
import xyz.xlls.rpan.storage.engine.local.config.LocalStoreEngineConfig;

import java.io.File;
import java.io.IOException;

/**
 * 本地文件引擎实现类
 */
@Component
public class LocalStorageEngine extends AbstractStorageEngine {
    @Autowired
    private LocalStoreEngineConfig localStoreEngineConfig;
    @Override
    protected void doStore(StoreFileContext context) throws IOException {
        String basePath=localStoreEngineConfig.getRootFilePath();
        String realFilePath=FileUtil.generateStoreFileRealPath(basePath, context.getFilename());
        FileUtil.writeStream2File(context.getInputStream(),new File(realFilePath),context.getTotalSize());
        context.setRealPath(realFilePath);
    }

    @Override
    protected void doDelete(DeleteFileContext context) throws IOException {
        FileUtil.deleteFiles(context.getRealFilePathList());
    }
}
