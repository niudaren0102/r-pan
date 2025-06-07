package xyz.xlls.rpan.server.modules.file.service;

import xyz.xlls.rpan.server.modules.file.context.FileChunkSaveContext;
import xyz.xlls.rpan.server.modules.file.entity.RPanFileChunk;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author Administrator
* @description 针对表【r_pan_file_chunk(文件分片信息表)】的数据库操作Service
* @createDate 2024-10-22 15:04:09
*/
public interface IFileChunkService extends IService<RPanFileChunk> {
    /**
     * 文件分片保存
     * @param fileChunkSaveContext
     */
    void saveChunkFile(FileChunkSaveContext fileChunkSaveContext);
}
