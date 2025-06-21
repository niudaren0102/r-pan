package xyz.xlls.rpan.server.modules.file.service;

import xyz.xlls.rpan.server.modules.file.context.*;
import xyz.xlls.rpan.server.modules.file.entity.RPanUserFile;
import com.baomidou.mybatisplus.extension.service.IService;
import xyz.xlls.rpan.server.modules.file.vo.FileChunkUploadVO;
import xyz.xlls.rpan.server.modules.file.vo.FolderTreeNodeVO;
import xyz.xlls.rpan.server.modules.file.vo.RPanUserFileVo;
import xyz.xlls.rpan.server.modules.file.vo.UploadedChunksVO;

import java.util.List;

/**
* @author Administrator
* @description 针对表【r_pan_user_file(用户文件信息表)】的数据库操作Service
* @createDate 2024-10-22 15:04:10
*/
public interface IUserFileService extends IService<RPanUserFile> {
    /**
     * 创建文件夹信息
     * @param context
     * @return
     */
    Long createFolder(CreateFolderContext context);

    /**
     * 查询用户的根文件夹信息
     * @param userId
     * @return
     */
    RPanUserFile getUserRootFile(Long userId);

    /**
     * 查询用户文件列表
     * @param context
     * @return
     */
    List<RPanUserFileVo> getFileList(QueryFileContext context);

    /**
     * 更新文件名称
     * @param context
     */
    void updateFilename(UpdateFilenameContext context);

    /**
     * 批量删除用户文件
     * @param context
     */
    void deleteFile(DeleteFileContext context);

    /**
     * 文件秒传
     * @param context
     * @return
     */
    boolean secUpload(SecUploadContext context);

    /**
     * 单文件上传
     * @param context
     */
    void upload(FileUploadContext context);

    /**
     * 文件分片上传
     * @param context
     * @return
     */
    FileChunkUploadVO chunkUpload(FileChunkUploadContext context);

    /**
     * 查询用户已上传的分片列表
     * @param context
     * @return
     */
    UploadedChunksVO getUploadedChunks(QueryUploadedChunksContext context);

    /**
     * 文件分片合并
     * @param context
     */
    void mergeFile(FileChunkMergeContext context);

    /**
     * 文件下载
     * @param context
     */
    void download(FileDownloadContext context);

    /**
     * 文件预览
     * @param filePreviewContext
     */
    void preview(FilePreviewContext filePreviewContext);

    /**
     *  查询用户的文件夹树
     * @param queryFolderTreeContext
     * @return
     */
    List<FolderTreeNodeVO> getFolderTree(QueryFolderTreeContext queryFolderTreeContext);

    /**
     * 文件转移
     * @param transferFileContext
     */
    void transfer(TransferFileContext transferFileContext);

    /**
     * 文件复制
     * @param context
     */
    void copy(CopyFileContext context);
}
