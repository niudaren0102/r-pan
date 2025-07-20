package xyz.xlls.rpan.server.common.listener.file;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import xyz.xlls.rpan.core.constants.RPanConstants;
import xyz.xlls.rpan.server.common.event.file.FilePhysicalDeleteEvent;
import xyz.xlls.rpan.server.common.event.log.ErrorLogEvent;
import xyz.xlls.rpan.server.modules.file.entity.RPanFile;
import xyz.xlls.rpan.server.modules.file.entity.RPanUserFile;
import xyz.xlls.rpan.server.modules.file.enums.FolderFlagEnum;
import xyz.xlls.rpan.server.modules.file.service.IFileService;
import xyz.xlls.rpan.server.modules.file.service.IUserFileService;
import xyz.xlls.rpan.storage.engine.core.StorageEngine;
import xyz.xlls.rpan.storage.engine.core.context.DeleteFileContext;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 文件物理删除监听器
 */
@Component
public class FilePhysicalDeleteListener implements ApplicationContextAware {
    @Autowired
    private IFileService fileService;
    @Autowired
    private StorageEngine storageEngine;
    @Autowired
    private IUserFileService userFileService;
    private ApplicationContext applicationContext;
    /**
     * 监听文件物理删除事件执行器
     * 该执行器是一个资源释放器，释放被物理删除的文件列表中关联的实体记录
     * 1、查询所有无引用的实体文件记录
     * 2、删除记录
     * 3、物理清理文件（委托存储引擎）
     * @param event
     */
    @EventListener(classes = FilePhysicalDeleteEvent.class)
    @Async("eventListenerTaskExecutor")
    public void physicalDeleteFile(FilePhysicalDeleteEvent event){
        List<RPanUserFile> allRecords = event.getAllRecords();
        if(ObjectUtil.isNotEmpty(allRecords)){
            return;
        }
        List<Long> realFileIdList = findAllUnusedRealFileIdList(allRecords);
        List<RPanFile> realFileRecords = fileService.listByIds(realFileIdList);
        if(CollectionUtil.isEmpty(realFileRecords)){
            return;
        }
        if(!fileService.removeByIds(realFileIdList)){
            applicationContext.publishEvent(new ErrorLogEvent(this,"文件实体文件记录："+ JSON.toJSONString(realFileIdList)+"物理删除失败，请执行手动删除",RPanConstants.ZERO_LONG));
            return;
        }
        physicalDeleteFileByStoreageEngine(realFileRecords);
    }

    /**
     * 委托文件存储引擎执行物理文件的删除
     * @param realFileRecords
     */
    private void physicalDeleteFileByStoreageEngine(List<RPanFile> realFileRecords) {
        List<String> realPathList = realFileRecords.stream().map(RPanFile::getRealPath).collect(Collectors.toList());
        DeleteFileContext context=new DeleteFileContext();
        context.setRealFilePathList(realPathList);
        try {
            storageEngine.delete(context);
        } catch (IOException e) {
            applicationContext.publishEvent(new ErrorLogEvent(this,"文件实体文件记录："+ JSON.toJSONString(realPathList)+"物理删除失败，请执行手动删除",RPanConstants.ZERO_LONG));
        }
    }

    /**
     * 查找所有没有被引用的真实文件记录ID集合
     * @param allRecords
     * @return
     */
    private List<Long> findAllUnusedRealFileIdList(List<RPanUserFile> allRecords) {
        List<Long> realFileIdList = allRecords.stream()
                .filter(record -> ObjectUtil.notEqual(record.getFolderFlag(), FolderFlagEnum.YES.getCode()))
                .filter(this::isUnUsed)
                .map(RPanUserFile::getRealFileId)
                .collect(Collectors.toList());
        return realFileIdList;
    }

    /**
     * 检验文件的真是文件ID是不是没有被引用了
     * @param rPanUserFile
     * @return
     */
    private boolean isUnUsed(RPanUserFile rPanUserFile) {
        Long realFileId = rPanUserFile.getRealFileId();
        LambdaQueryWrapper<RPanUserFile> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(RPanUserFile::getRealFileId, realFileId);
        return userFileService.count(queryWrapper)== RPanConstants.ZERO_INT.intValue();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext=applicationContext;
    }
}
