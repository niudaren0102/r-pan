package xyz.xlls.rpan.server.modules.share.service;

import xyz.xlls.rpan.server.modules.share.context.SaveShareFilesContext;
import xyz.xlls.rpan.server.modules.share.entity.RPanShareFile;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author Administrator
* @description 针对表【r_pan_share_file(用户分享文件表)】的数据库操作Service
* @createDate 2024-10-22 15:08:03
*/
public interface IShareFileService extends IService<RPanShareFile> {
    /**
     * 保存分享的文件对应关系
     * @param context
     */
    void saveShareFiles(SaveShareFilesContext context);
}
