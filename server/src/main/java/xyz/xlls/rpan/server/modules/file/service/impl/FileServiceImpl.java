package xyz.xlls.rpan.server.modules.file.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import xyz.xlls.rpan.core.utils.FileUtil;
import xyz.xlls.rpan.core.utils.IdUtil;
import xyz.xlls.rpan.server.common.event.log.ErrorLogEvent;
import xyz.xlls.rpan.server.modules.file.context.FileSaveContext;
import xyz.xlls.rpan.server.modules.file.entity.RPanFile;
import xyz.xlls.rpan.server.modules.file.service.IFileService;
import xyz.xlls.rpan.server.modules.file.mapper.RPanFileMapper;
import org.springframework.stereotype.Service;
import xyz.xlls.rpan.storage.engine.core.StorageEngine;
import xyz.xlls.rpan.storage.engine.core.context.DeleteFileContext;
import xyz.xlls.rpan.storage.engine.core.context.StoreFileContext;

import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
* @author Administrator
* @description 针对表【r_pan_file(物理文件信息表)】的数据库操作Service实现
* @createDate 2024-10-22 15:04:09
*/
@Service
public class FileServiceImpl extends ServiceImpl<RPanFileMapper, RPanFile>
    implements IFileService, ApplicationContextAware {
    @Autowired
    private StorageEngine storageEngine;
    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public List<RPanFile> getFileByUserIdAndIdentifier(Long userId, String identifier) {
        LambdaQueryWrapper<RPanFile> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RPanFile::getCreateUser,userId);
        queryWrapper.eq(RPanFile::getIdentifier,identifier);
        return this.list(queryWrapper);
    }

    /**
     * 上传文件并保存实体记录
     * 1、上传单文件
     * 2、保存实体记录
     * @param context
     */
    @Override
    public void saveFile(FileSaveContext context) {
        storeMultipartFile(context);
        RPanFile record=doSaveFile(context.getFilename(),
                context.getRealPath(),
                context.getTotalSize(),
                context.getIdentifier(),
                context.getUserId());
        context.setRecord(record);
    }

    /**
     * 保存实体文件记录
     * @param filename
     * @param realPath
     * @param totalSize
     * @param identifier
     * @param userId
     * @return
     */
    private RPanFile doSaveFile(String filename, String realPath, Long totalSize, String identifier, Long userId) {
        RPanFile record=assembleRPanFile(filename,realPath,totalSize,identifier,userId);
        if(!this.save(record)){
            try{
                DeleteFileContext deleteFileContext=new DeleteFileContext();
                deleteFileContext.setRealFilePathList(Lists.newArrayList(realPath));
                storageEngine.delete(deleteFileContext);
            }catch (IOException e){
                e.printStackTrace();
                ErrorLogEvent errorLogEvent=new ErrorLogEvent(this,"文件物理删除失败，请执行手动删除！文件路径："+realPath,userId);
                applicationContext.publishEvent(errorLogEvent);
                throw new RuntimeException("");
            }
        }
        return record;
    }

    /**
     * 拼装文件实体对象
     * @param filename
     * @param realPath
     * @param totalSize
     * @param identifier
     * @param userId
     * @return
     */
    private RPanFile assembleRPanFile(String filename, String realPath, Long totalSize, String identifier, Long userId) {
        RPanFile record = new RPanFile();
        record.setFileId(IdUtil.get());
        record.setFilename(filename);
        record.setRealPath(realPath);
        record.setFileSize(String.valueOf(totalSize));
        record.setFileSizeDesc(FileUtil.byteCountToDisplaySize(totalSize));
        record.setFileSuffix(FileUtil.getFileSuffix(filename));
        record.setIdentifier(identifier);
        record.setCreateUser(userId);
        record.setCreateTime(new Date());
        return record;
    }

    /**
     * 上传单文件
     * 该方法委托给存储引擎实现
     * @param context
     */
    private void storeMultipartFile(FileSaveContext context) {
        try {
            StoreFileContext storeFileContext=new StoreFileContext();
            storeFileContext.setInputStream(context.getFile().getInputStream());
            storeFileContext.setFilename(context.getFilename());
            storeFileContext.setTotalSize(context.getTotalSize());
            storageEngine.store(storeFileContext);
            context.setRealPath(storeFileContext.getRealPath());
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("文件上传失败");
        }

    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext=applicationContext;
    }
}




