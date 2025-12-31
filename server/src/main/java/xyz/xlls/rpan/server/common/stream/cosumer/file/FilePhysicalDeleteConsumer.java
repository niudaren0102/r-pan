package xyz.xlls.rpan.server.common.stream.cosumer.file;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import xyz.xlls.rpan.core.constants.RPanConstants;
import xyz.xlls.rpan.server.common.stream.channel.PanChannels;
import xyz.xlls.rpan.server.common.stream.event.file.FilePhysicalDeleteEvent;
import xyz.xlls.rpan.server.common.stream.event.log.ErrorLogEvent;
import xyz.xlls.rpan.server.modules.file.entity.RPanFile;
import xyz.xlls.rpan.server.modules.file.entity.RPanUserFile;
import xyz.xlls.rpan.server.modules.file.enums.FolderFlagEnum;
import xyz.xlls.rpan.server.modules.file.service.IFileService;
import xyz.xlls.rpan.server.modules.file.service.IUserFileService;
import xyz.xlls.rpan.storage.engine.core.StorageEngine;
import xyz.xlls.rpan.storage.engine.core.context.DeleteFileContext;
import xyz.xlls.rpan.stream.core.AbstractConsumer;
import xyz.xlls.rpan.stream.core.IStreamProducer;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 文件物理删除监听器
 */
@Component
public class FilePhysicalDeleteConsumer extends AbstractConsumer {
    @Autowired
    private IFileService fileService;
    @Autowired
    private StorageEngine storageEngine;
    @Autowired
    private IUserFileService userFileService;
    @Autowired
    @Qualifier(value = "defaultStreamProducer")
    private IStreamProducer producer;

    /**
     * 监听文件物理删除事件执行器
     * 该执行器是一个资源释放器，释放被物理删除的文件列表中关联的实体记录
     * 1、查询所有无引用的实体文件记录
     * 2、删除记录
     * 3、物理清理文件（委托存储引擎）
     * @param message
     */
    @StreamListener(PanChannels.PHYSICAL_DELETE_FILE_INPUT)
    public void physicalDeleteFile(Message<FilePhysicalDeleteEvent> message){
        if(isEmptyMessage(message)){
            return;
        }
        printLog(message);
        FilePhysicalDeleteEvent event = message.getPayload();
        List<RPanUserFile> allRecords = event.getAllRecords();
        if(ObjectUtil.isNotEmpty(allRecords)){
            return;
        }
        List<Long> realFileIdList = findAllUnusedRealFileIdList(allRecords);
        if(CollectionUtil.isEmpty(realFileIdList)){
            return;
        }
        List<RPanFile> realFileRecords = fileService.listByIds(realFileIdList);
        if(CollectionUtil.isEmpty(realFileRecords)){
            return;
        }
        if(!fileService.removeByIds(realFileIdList)){
            producer.sendMessage(PanChannels.ERROR_LOG_OUTPUT,new ErrorLogEvent("文件实体文件记录："+ JSON.toJSONString(realFileIdList)+"物理删除失败，请执行手动删除",RPanConstants.ZERO_LONG));
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
            producer.sendMessage(PanChannels.ERROR_LOG_OUTPUT ,new ErrorLogEvent("文件实体文件记录："+ JSON.toJSONString(realPathList)+"物理删除失败，请执行手动删除",RPanConstants.ZERO_LONG));
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

}
