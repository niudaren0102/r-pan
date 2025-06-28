package xyz.xlls.rpan.storage.engine.fastdfs;

import cn.hutool.core.collection.CollectionUtil;
import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.proto.storage.DownloadByteArray;
import com.github.tobato.fastdfs.service.DefaultTrackerClient;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import xyz.xlls.rpan.core.constants.RPanConstants;
import xyz.xlls.rpan.core.exception.RPanBusinessException;
import xyz.xlls.rpan.core.utils.FileUtil;
import xyz.xlls.rpan.storage.engine.core.AbstractStorageEngine;
import xyz.xlls.rpan.storage.engine.core.context.*;
import xyz.xlls.rpan.storage.engine.fastdfs.config.FastDFSStorageEngineConfig;
import xyz.xlls.rpan.storage.engine.fastdfs.config.UserDefaultTrackerClient;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * 基于FastDFS实现的文件存储引擎
 */
@Component
public class FastDFSStorageEngine extends AbstractStorageEngine {
    @Autowired
    private FastFileStorageClient client;
    @Autowired
    private FastDFSStorageEngineConfig config;
    @Autowired
    private UserDefaultTrackerClient defaultTrackerClient;


    @Override
    protected void doStore(StoreFileContext context) throws IOException {
        StorePath storePath=client.uploadFile(config.getGroup(), context.getInputStream(),context.getTotalSize(), FileUtil.getFileExtName(context.getFilename()));
        context.setRealPath(storePath.getFullPath());
    }

    @Override
    protected void doDelete(DeleteFileContext context) throws IOException {
        List<String> realFilePathList = context.getRealFilePathList();
        if(CollectionUtil.isNotEmpty(realFilePathList)){
            realFilePathList.forEach(client::deleteFile);
        }
    }

    @Override
    protected void doStoreChunk(StoreFileChunkContext context) throws IOException{
        throw new RPanBusinessException("FastDFS不支持分片上传的操作");
    }

    @Override
    protected void doMergeFile(MergeFileContext context) throws IOException{
        throw new RPanBusinessException("FastDFS不支持分片上传的操作");
    }

    @Override
    protected void doReadFile(ReadFileContext context) throws IOException {
        String realPath = context.getRealPath();
        String group = realPath.substring(RPanConstants.ZERO_INT, realPath.indexOf(RPanConstants.SLASH_STR));
        String path=realPath.substring(realPath.indexOf(RPanConstants.SLASH_STR)+RPanConstants.ONE_INT);
        DownloadByteArray downloadByteArray=new DownloadByteArray();
        byte[] bytes = client.downloadFile(group, path, downloadByteArray);
        OutputStream outputStream = context.getOutputStream();
        outputStream.write(bytes);
        outputStream.flush();
        outputStream.close();





    }
}
