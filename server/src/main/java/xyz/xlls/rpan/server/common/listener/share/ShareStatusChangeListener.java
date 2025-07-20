package xyz.xlls.rpan.server.common.listener.share;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import xyz.xlls.rpan.server.common.event.file.DeleteFileEvent;
import xyz.xlls.rpan.server.common.event.file.FileRestoreEvent;
import xyz.xlls.rpan.server.modules.file.entity.RPanUserFile;
import xyz.xlls.rpan.server.modules.file.enums.DelFlagEnum;
import xyz.xlls.rpan.server.modules.file.service.IUserFileService;
import xyz.xlls.rpan.server.modules.share.service.IShareService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 监听文件状态变更导致分享状态变更的处理器
 */
@Component
public class ShareStatusChangeListener {
    @Autowired
    private IUserFileService userFileService;
    @Autowired
    private IShareService shareService;
    /**
     * 监听文件被删除之后，刷新所有受影响的分享状态
     * @param event
     */
    @EventListener(DeleteFileEvent.class)
    public void changeShare2FileDeleted(DeleteFileEvent event){
        List<Long> fileIdList = event.getFileIdList();
        if(CollectionUtil.isEmpty(fileIdList)){
            return;
        }
        List<RPanUserFile> allRecords = userFileService.findAllFileRecordsByFileIdList(fileIdList);
        List<Long> allAvailableFileIdList = allRecords.stream().filter(record -> ObjectUtil.equal(record.getDelFlag(), DelFlagEnum.NO.getCode()))
                .map(RPanUserFile::getFileId)
                .collect(Collectors.toList());
        allAvailableFileIdList.addAll(fileIdList);
        shareService.refreshStatus(allAvailableFileIdList);
    }

    /**
     * 监听文件被换源后，刷新所有受影响的分享状态
     * @param event
     */
    @EventListener(FileRestoreEvent.class)
    public void changeShare2Normal(FileRestoreEvent event){
        List<Long> fileIdList = event.getFileIdList();
        if(CollectionUtil.isEmpty(fileIdList)){
            return;
        }
        List<RPanUserFile> allRecords = userFileService.findAllFileRecordsByFileIdList(fileIdList);
        List<Long> allAvailableFileIdList = allRecords.stream().filter(record -> ObjectUtil.equal(record.getDelFlag(), DelFlagEnum.NO.getCode()))
                .map(RPanUserFile::getFileId)
                .collect(Collectors.toList());
        shareService.refreshStatus(allAvailableFileIdList);
    }
}
