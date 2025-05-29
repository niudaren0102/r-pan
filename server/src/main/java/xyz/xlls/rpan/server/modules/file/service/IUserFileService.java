package xyz.xlls.rpan.server.modules.file.service;

import xyz.xlls.rpan.server.modules.file.context.CreateFolderContext;
import xyz.xlls.rpan.server.modules.file.context.UpdateFilenameContext;
import xyz.xlls.rpan.server.modules.file.entity.RPanUserFile;
import com.baomidou.mybatisplus.extension.service.IService;
import xyz.xlls.rpan.server.modules.file.context.QueryFileContext;
import xyz.xlls.rpan.server.modules.file.vo.RPanUserFileVo;

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
}
