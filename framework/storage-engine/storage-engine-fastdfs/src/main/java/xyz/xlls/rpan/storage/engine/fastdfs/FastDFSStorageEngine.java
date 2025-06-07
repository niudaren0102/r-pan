package xyz.xlls.rpan.storage.engine.fastdfs;

import org.springframework.stereotype.Component;
import xyz.xlls.rpan.storage.engine.core.AbstractStorageEngine;
import xyz.xlls.rpan.storage.engine.core.context.DeleteFileContext;
import xyz.xlls.rpan.storage.engine.core.context.StoreFileChunkContext;
import xyz.xlls.rpan.storage.engine.core.context.StoreFileContext;

import java.io.IOException;

/**
 * 基于FastDFS实现的文件存储引擎
 */
@Component
public class FastDFSStorageEngine extends AbstractStorageEngine {
    @Override
    protected void doStore(StoreFileContext context) throws IOException {

    }

    @Override
    protected void doDelete(DeleteFileContext context) throws IOException {

    }

    @Override
    protected void doStoreChunk(StoreFileChunkContext context) {

    }
}
