package xyz.xlls.rpan.storage.engine.core;

import xyz.xlls.rpan.storage.engine.core.context.*;

import java.io.IOException;

/**
 * 文件存储引擎的顶级接口
 */
public interface StorageEngine {
    /**
     * 存储物理文件
     * @param context
     * @throws IOException
     */
    void store(StoreFileContext context) throws IOException;

    /**
     * 删除物理文件
     * @param context
     * @throws IOException
     */
    void delete(DeleteFileContext context) throws IOException;

    /**
     * 存储物理文件的分片
     * @param context
     * @throws IOException
     */
    void storeChunk(StoreFileChunkContext context) throws IOException;

    /**
     * 合并文件分片
     * @param context
     * @throws IOException
     */
    void mergeFile(MergeFileContext context) throws  IOException;

    /**
     * 读取文件内容写入到输出流中
     * @param context
     * @throws IOException
     */
    void readFile(ReadFileContext context)throws IOException;
}
