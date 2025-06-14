package xyz.xlls.rpan.storage.engine.oss;

import org.springframework.stereotype.Component;
import xyz.xlls.rpan.storage.engine.core.AbstractStorageEngine;
import xyz.xlls.rpan.storage.engine.core.context.*;

import java.io.IOException;

/**
 * 基于OSS的文件存储引擎实现类
 */
@Component
public class OSSStorageEngine extends AbstractStorageEngine {
    @Override
    protected void doStore(StoreFileContext context) throws IOException {

    }

    @Override
    protected void doDelete(DeleteFileContext context) throws IOException {

    }

    @Override
    protected void doStoreChunk(StoreFileChunkContext context) throws IOException{

    }

    @Override
    protected void doMergeFile(MergeFileContext context) throws IOException{

    }

    @Override
    protected void doReadFile(ReadFileContext context) throws IOException {

    }
}
