package xyz.xlls.rpan.server.modules.file.service;

import xyz.xlls.rpan.server.modules.file.context.FileChunkSaveContext;
import xyz.xlls.rpan.server.modules.file.context.QueryUploadedChunksContext;
import xyz.xlls.rpan.server.modules.file.context.QueryUploadedChunksRecordContext;
import xyz.xlls.rpan.server.modules.file.entity.RPanFile;
import xyz.xlls.rpan.server.modules.file.entity.RPanFileChunk;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

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

    /**
     * 查询已上传用户分片
     * @param context
     * @return
     */
    List<Integer> queryUploadedChunks(QueryUploadedChunksContext context);

    /**
     * 查询已上传用户分片记录实体
     * @param queryUploadedChunksContext
     * @return
     */
    List<RPanFileChunk> queryUploadedChunksRecord(QueryUploadedChunksRecordContext queryUploadedChunksContext);
}
