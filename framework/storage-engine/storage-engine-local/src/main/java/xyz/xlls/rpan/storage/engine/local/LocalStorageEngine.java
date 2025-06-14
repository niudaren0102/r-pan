package xyz.xlls.rpan.storage.engine.local;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import xyz.xlls.rpan.core.utils.FileUtil;
import xyz.xlls.rpan.storage.engine.core.AbstractStorageEngine;
import xyz.xlls.rpan.storage.engine.core.context.*;
import xyz.xlls.rpan.storage.engine.local.config.LocalStoreEngineConfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

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

    /**
     * 执行保存文件分片的动作
     * @param context
     * @throws IOException
     */
    @Override
    protected void doStoreChunk(StoreFileChunkContext context) throws IOException{
        String basePath=localStoreEngineConfig.getRootFileChunkPath();
        String realFilePath=FileUtil.generateStoreFileChunkRealPath(basePath,context.getIdentifier(), context.getChunkNumber());
        FileUtil.writeStream2File(context.getInputStream(),new File(realFilePath),context.getTotalSize());
        context.setRealPath(realFilePath);
    }

    /**
     * 执行合并文件分片的动作
     * @param context
     */
    @Override
    protected void doMergeFile(MergeFileContext context) throws IOException{
        String basePath=localStoreEngineConfig.getRootFilePath();
        String realFilePath=FileUtil.generateStoreFileRealPath(basePath, context.getFilename());
        FileUtil.createFile(new File(realFilePath));
        List<String> chunkRealPathList = context.getRealPathList();
        for (String chunkRealPath : chunkRealPathList) {
            FileUtil.appendWrite(Paths.get(realFilePath),new File(chunkRealPath).toPath());
        }
        FileUtil.deleteFiles(chunkRealPathList);
    }

    /**
     * 读取文件内容并写入到输出流
     * @param context
     * @throws IOException
     */
    @Override
    protected void doReadFile(ReadFileContext context) throws IOException {
        File file = new File(context.getRealPath());
        FileUtil.writeFile2OutputStream(new FileInputStream(file),context.getOutputStream(),file.length());
    }
}
