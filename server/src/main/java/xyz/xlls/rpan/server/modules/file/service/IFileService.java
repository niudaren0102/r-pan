package xyz.xlls.rpan.server.modules.file.service;

import xyz.xlls.rpan.server.modules.file.entity.RPanFile;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author Administrator
* @description 针对表【r_pan_file(物理文件信息表)】的数据库操作Service
* @createDate 2024-10-22 15:04:09
*/
public interface IFileService extends IService<RPanFile> {
    /**
     * 根据用户id和文件唯一标识符查询文件信息
     * @param userId
     * @param identifier
     * @return
     */
    List<RPanFile> getFileByUserIdAndIdentifier(Long userId, String identifier);
}
