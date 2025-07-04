package xyz.xlls.rpan.server.modules.share.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import xyz.xlls.rpan.core.exception.RPanBusinessException;
import xyz.xlls.rpan.core.utils.IdUtil;
import xyz.xlls.rpan.server.modules.share.context.SaveShareFilesContext;
import xyz.xlls.rpan.server.modules.share.entity.RPanShareFile;
import xyz.xlls.rpan.server.modules.share.service.IShareFileService;
import xyz.xlls.rpan.server.modules.share.mapper.RPanShareFileMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
* @author Administrator
* @description 针对表【r_pan_share_file(用户分享文件表)】的数据库操作Service实现
* @createDate 2024-10-22 15:08:03
*/
@Service
public class ShareFileServiceImpl extends ServiceImpl<RPanShareFileMapper, RPanShareFile>
    implements IShareFileService {
    /**
     * 保存分享的文件的对应关系
     * @param context
     */
    @Override
    public void saveShareFiles(SaveShareFilesContext context) {
        Long shareId = context.getShareId();
        List<Long> shareFileIdList = context.getShareFileIdList();
        Long userId = context.getUserId();
        List<RPanShareFile> records=new ArrayList<>();
        for (Long shareFileId: shareFileIdList) {
            RPanShareFile record = new RPanShareFile();
            record.setId(IdUtil.get());
            record.setShareId(shareId);
            record.setFileId(shareFileId);
            record.setCreateTime(new Date());
            record.setCreateUser(userId);
            records.add(record);
        }
        if(!this.saveBatch(records)){
            throw new RPanBusinessException("保存文件分享关联关系失败");
        }
    }
}




