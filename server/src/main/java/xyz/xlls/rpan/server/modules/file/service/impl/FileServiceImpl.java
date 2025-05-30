package xyz.xlls.rpan.server.modules.file.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import xyz.xlls.rpan.server.modules.file.entity.RPanFile;
import xyz.xlls.rpan.server.modules.file.service.IFileService;
import xyz.xlls.rpan.server.modules.file.mapper.RPanFileMapper;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
* @author Administrator
* @description 针对表【r_pan_file(物理文件信息表)】的数据库操作Service实现
* @createDate 2024-10-22 15:04:09
*/
@Service
public class FileServiceImpl extends ServiceImpl<RPanFileMapper, RPanFile>
    implements IFileService {

    @Override
    public List<RPanFile> getFileByUserIdAndIdentifier(Long userId, String identifier) {
        LambdaQueryWrapper<RPanFile> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RPanFile::getCreateUser,userId);
        queryWrapper.eq(RPanFile::getIdentifier,identifier);
        return this.list(queryWrapper);
    }
}




