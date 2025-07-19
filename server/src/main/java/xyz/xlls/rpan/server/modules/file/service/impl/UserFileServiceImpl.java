package xyz.xlls.rpan.server.modules.file.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import xyz.xlls.rpan.core.constants.RPanConstants;
import xyz.xlls.rpan.core.exception.RPanBusinessException;
import xyz.xlls.rpan.core.utils.FileUtil;
import xyz.xlls.rpan.core.utils.IdUtil;
import xyz.xlls.rpan.server.common.event.file.DeleteFileEvent;
import xyz.xlls.rpan.server.common.event.search.UserSearchEvent;
import xyz.xlls.rpan.server.common.utils.HttpUtil;
import xyz.xlls.rpan.server.modules.file.constants.FileConstants;
import xyz.xlls.rpan.server.modules.file.context.*;
import xyz.xlls.rpan.server.modules.file.converter.FileConverter;
import xyz.xlls.rpan.server.modules.file.entity.RPanFile;
import xyz.xlls.rpan.server.modules.file.entity.RPanUserFile;
import xyz.xlls.rpan.server.modules.file.enums.DelFlagEnum;
import xyz.xlls.rpan.server.modules.file.enums.FileTypeEnum;
import xyz.xlls.rpan.server.modules.file.enums.FolderFlagEnum;
import xyz.xlls.rpan.server.modules.file.service.IFileChunkService;
import xyz.xlls.rpan.server.modules.file.service.IFileService;
import xyz.xlls.rpan.server.modules.file.service.IUserFileService;
import xyz.xlls.rpan.server.modules.file.mapper.RPanUserFileMapper;
import org.springframework.stereotype.Service;
import xyz.xlls.rpan.server.modules.file.vo.*;
import xyz.xlls.rpan.storage.engine.core.StorageEngine;
import xyz.xlls.rpan.storage.engine.core.context.ReadFileContext;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Administrator
 * @description 针对表【r_pan_user_file(用户文件信息表)】的数据库操作Service实现
 * @createDate 2024-10-22 15:04:09
 */
@Service
public class UserFileServiceImpl extends ServiceImpl<RPanUserFileMapper, RPanUserFile>
        implements IUserFileService, ApplicationContextAware {
    private ApplicationContext applicationContext;
    @Autowired
    private IFileService fileService;
    @Autowired
    private FileConverter fileConverter;
    @Autowired
    private IFileChunkService fileChunkService;
    @Autowired
    private StorageEngine storageEngine;

    /**
     * 创建文件夹信息
     *
     * @param context
     * @return
     */
    @Override
    public Long createFolder(CreateFolderContext context) {
        return saveUserFile(context.getParentId(),
                context.getFolderName(),
                FolderFlagEnum.YES,
                null,
                null,
                context.getUserId(),
                null);
    }

    /**
     * 获取文件根目录信息
     *
     * @param userId
     * @return
     */
    @Override
    public RPanUserFile getUserRootFile(Long userId) {
        LambdaQueryWrapper<RPanUserFile> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RPanUserFile::getUserId, userId);
        queryWrapper.eq(RPanUserFile::getParentId, FileConstants.TOP_PARENT_ID);
        queryWrapper.eq(RPanUserFile::getDelFlag, DelFlagEnum.NO.getCode());
        queryWrapper.eq(RPanUserFile::getFolderFlag, FolderFlagEnum.YES.getCode());
        return this.getOne(queryWrapper);
    }

    /**
     * 查询用户的文件列表
     *
     * @param context
     * @return
     */
    @Override
    public List<RPanUserFileVO> getFileList(QueryFileContext context) {
        return baseMapper.selectFileList(context);
    }

    /**
     * 更新文件名称
     * 1、校验更新文件名称的条件
     * 2、执行更新文件名称的操作
     *
     * @param context
     */
    @Override
    public void updateFilename(UpdateFilenameContext context) {
        checkUpdateFilenameCondition(context);
        doUpdateFilename(context);
    }

    /**
     * 批量删除用户文件
     * 1、校验删除的条件是否符合
     * 2、执行批量删除的动作
     * 3、发布批量删除文件的事件、给其他模块订阅使用
     *
     * @param context
     */
    @Override
    public void deleteFile(DeleteFileContext context) {
        checkFileDeleteCondition(context);
        doDeleteFile(context);
        afterFileDelete(context);
    }

    /**
     * 文件秒传
     * 1、通过文件的唯一标识，查找对应的实体文件记录
     * 2、如果没有查到，直接返回秒传失败
     * 3、如果查到记录，直接挂载关联管理，返回秒传成功即可
     *
     * @param context
     * @return
     */
    @Override
    public boolean secUpload(SecUploadContext context) {
        RPanFile record = getFileByUserIdAndIdentifier(context.getUserId(), context.getIdentifier());
        if (Objects.isNull(record)) {
            return false;
        }
        saveUserFile(context.getParentId(),
                context.getFilename(),
                FolderFlagEnum.NO,
                FileTypeEnum.getFileTypeCode(FileUtil.getFileSuffix(context.getFilename())),
                record.getFileId(),
                context.getUserId(),
                record.getFileSizeDesc());
        return true;
    }

    /**
     * 单文件上传
     * 1、上传文件并保存实体文件记录
     * 2、保存用户文件的关系记录
     *
     * @param context
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void upload(FileUploadContext context) {
        saveFile(context);
        saveUserFile(
                context.getParentId(),
                context.getFilename(),
                FolderFlagEnum.NO,
                FileTypeEnum.getFileTypeCode(FileUtil.getFileSuffix(context.getFilename())),
                context.getRecord().getFileId(),
                context.getUserId(),
                context.getRecord().getFileSizeDesc()
        );
    }

    /**
     * 上传文件并保存实体文件记录
     * 委托给实体文件的Service去完成该操作
     *
     * @param context
     */
    private void saveFile(FileUploadContext context) {
        FileSaveContext fileSaveContext = fileConverter.fileUploadContext2FileSaveContext(context);
        fileService.saveFile(fileSaveContext);
        context.setRecord(fileSaveContext.getRecord());
    }

    private RPanFile getFileByUserIdAndIdentifier(Long userId, String identifier) {
        List<RPanFile> records = fileService.getFileByUserIdAndIdentifier(userId, identifier);
        if (CollectionUtil.isEmpty(records)) {
            return null;
        }
        return records.get(RPanConstants.ZERO_INT);
    }

    /**
     * 文件删除的后置操作
     * 1、对外发布文件删除的事件
     *
     * @param context
     */
    private void afterFileDelete(DeleteFileContext context) {
        DeleteFileEvent deleteFileEvent = new DeleteFileEvent(this, context.getFileIdList());
        applicationContext.publishEvent(deleteFileEvent);
    }

    /**
     * 执行删除文件的后置操作
     *
     * @param context
     */
    private void doDeleteFile(DeleteFileContext context) {
        List<Long> fileIdList = context.getFileIdList();
        LambdaUpdateWrapper<RPanUserFile> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(RPanUserFile::getFileId, fileIdList);
        updateWrapper.set(RPanUserFile::getDelFlag, DelFlagEnum.YES.getCode());
        updateWrapper.set(RPanUserFile::getUpdateTime, new Date());
        if (!this.update(updateWrapper)) {
            throw new RPanBusinessException("文件删除失败");
        }
    }

    /**
     * 文件分片上传
     * 1、上传实体文件
     * 2、保存分片文件记录、
     * 3、校验是否全部分片上传完成
     *
     * @param context
     * @return
     */
    @Override
    public FileChunkUploadVO chunkUpload(FileChunkUploadContext context) {
        FileChunkSaveContext fileChunkSaveContext = fileConverter.fileChunkUploadContext2FileChunkSaveContext(context);
        fileChunkService.saveChunkFile(fileChunkSaveContext);
        FileChunkUploadVO vo = new FileChunkUploadVO();
        vo.setMergeFlag(fileChunkSaveContext.getMergeFlagEnum().getCode());
        return vo;
    }

    /**
     * 查询用户已上传的分片列表
     * 1、查询已上传的分片列表
     * 2、封装返回实体
     *
     * @param context
     * @return
     */
    @Override
    public UploadedChunksVO getUploadedChunks(QueryUploadedChunksContext context) {
        List<Integer> uploadedChunks = fileChunkService.queryUploadedChunks(context);
        UploadedChunksVO vo = new UploadedChunksVO();
        vo.setUploadedChunks(uploadedChunks);
        return vo;
    }

    /**
     * 文件分片合并
     * 1、文件分片物理合并
     * 2、保存文件实体记录
     * 3、保存文件用户关系映射
     *
     * @param context
     */
    @Override
    public void mergeFile(FileChunkMergeContext context) {
        mergeFileChunkAndSaveFile(context);
        saveUserFile(
                context.getParentId(),
                context.getFilename(),
                FolderFlagEnum.NO,
                FileTypeEnum.getFileTypeCode(FileUtil.getFileSuffix(context.getFilename())),
                context.getRecord().getFileId(),
                context.getUserId(),
                context.getRecord().getFileSizeDesc());
    }

    /**
     * 文件下载
     * 1、参数校验：文件是否存在，文件是否属于该用户
     * 2、校验该文件是不是一个文件夹
     * 3、执行下载的动作
     *
     * @param context
     */
    @Override
    public void download(FileDownloadContext context) {
        RPanUserFile record = this.getById(context.getFileId());
        checkOperatePermission(record, context.getUserId());
        if (checkIsFolder(record)) {
            throw new RPanBusinessException("文件夹暂不支持下载");
        }
        doDownload(record, context.getResponse());
    }

    /**
     * 文件预览
     * 1、参数校验：文件是否存在，文件是否属于该用户
     * 2、校验该文件是不是一个文件夹
     * 3、执行预览的动作
     * @param context
     */
    @Override
    public void preview(FilePreviewContext context) {
        RPanUserFile record = this.getById(context.getFileId());
        checkOperatePermission(record, context.getUserId());
        if (checkIsFolder(record)) {
            throw new RPanBusinessException("文件夹暂不支持预览");
        }
        doPreview(record, context.getResponse());
    }

    /**
     * 查询用户的文件夹树
     * 1、查询该用户的所有文件夹列表
     * 2、在内存中拼装文件夹树
     * @param queryFolderTreeContext
     * @return
     */
    @Override
    public List<FolderTreeNodeVO> getFolderTree(QueryFolderTreeContext queryFolderTreeContext) {
        List<RPanUserFile> record=queryFolderRecords(queryFolderTreeContext.getUserId());
        List<FolderTreeNodeVO> result=assembleFolderTreeNodeVOList(record);
        return result;
    }

    /**
     * 文件转移
     * 1、参数校验
     * 2、转移动作
     * @param transferFileContext
     */
    @Override
    public void transfer(TransferFileContext transferFileContext) {
        checkTransferCondition(transferFileContext);
        doTransfer(transferFileContext);
    }

    /**
     * 文件复制
     * 1、条件校验
     * 2、复制动作
     * @param context
     */
    @Override
    public void copy(CopyFileContext context) {
        checkCopyCondition(context);
        doCopy(context);
    }

    /**
     * 文件列表搜索
     * 1、执行文件搜索
     * 2、拼装文件的父文件夹名称
     * 3、执行文件搜素的后置动作
     * @param context
     * @return
     */
    @Override
    public List<FileSearchResultVO> search(FileSearchContext context) {
        List<FileSearchResultVO> result=doSearch(context);
        fillParentFileName(result);
        afterSearch(context);
        return result;
    }

    /**
     * 获取面包屑列表
     * 1、获取所有文件夹信息
     * 2、拼接需要用到的面包屑列表
     * @param context
     * @return
     */
    @Override
    public List<BreadcrumbVO> getBreadcrumbs(QueryBreadcrumbsContext context) {
        List<RPanUserFile> folderRecords = queryFolderRecords(context.getUserId());
        Map<Long, BreadcrumbVO> prepareBreadcrumbs = folderRecords.stream().map(BreadcrumbVO::transfer).collect(Collectors.toMap(BreadcrumbVO::getId, a -> a));
        BreadcrumbVO currentNode;
        Long fileId= context.getFileId();
        List<BreadcrumbVO> result= Lists.newLinkedList();
        do{
            currentNode=prepareBreadcrumbs.get(fileId);
            if(Objects.nonNull(currentNode)){
                result.add(0,currentNode);
                fileId=currentNode.getParentId();
            }

        }while (Objects.nonNull(currentNode));
        return result;
    }

    /**
     * 递归查询所有的子文件信息
     * @param records
     * @return
     */
    @Override
    public List<RPanUserFile> findAllFileRecords(List<RPanUserFile> records) {
        List<RPanUserFile> result=new ArrayList<>();
        if(CollectionUtil.isEmpty(result)){
            return result;
        }
        long folderCount = result.stream().filter(record -> ObjectUtil.equal(record.getFolderFlag(), FolderFlagEnum.YES.getCode())).count();
        if(folderCount==0){
            return result;
        }
        result.forEach(record->doFindAllChildRecords(result,record));
        return Collections.emptyList();
    }

    @Override
    public List<RPanUserFile> findAllFileRecordsByFileIdList(List<Long> fileIdList) {
        if(CollectionUtil.isEmpty(fileIdList)){
            return Lists.newArrayList();
        }
        List<RPanUserFile> records = listByIds(fileIdList);
        if(CollectionUtil.isEmpty(records)){
            return Lists.newArrayList();
        }
        return  this.findAllFileRecords(records);
    }

    @Override
    public List<RPanUserFileVO> transferVoList(List<RPanUserFile> records) {
        if(CollectionUtil.isEmpty(records)){
            return Lists.newArrayList();
        }
        return records.stream().map(fileConverter::rPanUserFile2RPanUserFileVO).collect(Collectors.toList());
    }

    /**
     * 递归查询所有子文件列表，忽略是否删除的标识
     * @param result
     * @param record
     */
    private void doFindAllChildRecords(List<RPanUserFile> result, RPanUserFile record) {
        if (Objects.isNull(record)) {
            return;
        }
        if (!checkIsFolder(record)) {
            return;
        }
        List<RPanUserFile> childRecords = findChildRecordsIgnoreDelFlag(record.getFileId());
        if (CollectionUtil.isEmpty(childRecords)) {
            return;
        }
        result.addAll(childRecords);
        childRecords.stream()
                .filter(childRecord-> ObjectUtil.equal(childRecord.getFolderFlag(), FolderFlagEnum.YES.getCode()))
                .forEach(childRecord->doFindAllChildRecords(result,childRecord));
    }

    /**
     * 查询文件下面的文件记录，忽略删除表示
     * @param fileId
     * @return
     */
    private List<RPanUserFile> findChildRecordsIgnoreDelFlag(Long fileId) {
        LambdaQueryWrapper<RPanUserFile> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(RPanUserFile::getParentId, fileId);
        List<RPanUserFile> childRecords = list(queryWrapper);
        return childRecords;
    }

    /**
     * 搜索的后置操作
     * 1、发布文件搜索的时间
     *
     * @param context
     */
    private void afterSearch(FileSearchContext context) {
        UserSearchEvent userSearchEvent=new UserSearchEvent(this, context.getKeyword(), context.getUserId());
        applicationContext.publishEvent(userSearchEvent);
    }

    /**
     * 填充文件列表的父文件夹名称
     * @param result
     */
    private void fillParentFileName(List<FileSearchResultVO> result) {
        if(CollectionUtil.isEmpty(result)){
            return;
        }
        List<Long> parentIdList = result.stream().map(FileSearchResultVO::getParentId).collect(Collectors.toList());
        List<RPanUserFile> parentRecords = listByIds(parentIdList);
        Map<Long, String> fileId2filenameMap = parentRecords.stream().collect(Collectors.toMap(RPanUserFile::getFileId, RPanUserFile::getFilename));
        result.stream().forEach(vo->vo.setParentFileName(fileId2filenameMap.get(vo.getParentId())));
    }

    /**
     * 搜索文件列表
     * @param context
     * @return
     */
    private List<FileSearchResultVO> doSearch(FileSearchContext context) {
        return this.baseMapper.searchFile(context);
    }

    /**
     * 执行文件复制的动作
     * @param context
     */
    private void doCopy(CopyFileContext context) {
        List<RPanUserFile> prepareRecords = context.getPrepareRecords();
        if(CollectionUtil.isNotEmpty(prepareRecords)){
           List<RPanUserFile> allRecords=Lists.newArrayList();
           prepareRecords.stream().forEach(record->assembleCopyChildRecord(allRecords,record,context.getTargetParentId(),context.getUserId()));
           if(!saveBatch(allRecords)){
               throw new RPanBusinessException("文件复制失败");
           }
        }
    }

    /**
     * 拼装当前文件记录以及所有子文件记录
     * @param allRecords
     * @param record
     * @param targetParentId
     * @param userId
     */
    private void assembleCopyChildRecord(List<RPanUserFile> allRecords, RPanUserFile record, Long targetParentId, Long userId) {
        Long newFileId = IdUtil.get();
        Long oldFileId = record.getFileId();
        record.setParentId(targetParentId);
        record.setFileId(newFileId);
        record.setUserId(userId);
        record.setCreateUser(userId);
        record.setCreateTime(new Date());
        record.setUpdateUser(userId);
        record.setUpdateTime(new Date());
        handleDuplicateFilename(record);
        allRecords.add(record);
        if(checkIsFolder(record)){
            List<RPanUserFile>  childRecords=findChildRecords(oldFileId);
            if(CollectionUtil.isEmpty(childRecords)){
                return;
            }
            childRecords.stream().forEach(childRecord->assembleCopyChildRecord(allRecords,childRecord,newFileId,userId));
        }
    }

    /**
     * 查找下一级的文件记录
     * @param parentId
     * @return
     */
    private List<RPanUserFile> findChildRecords(Long parentId) {
        LambdaQueryWrapper<RPanUserFile> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(RPanUserFile::getParentId,parentId);
        queryWrapper.eq(RPanUserFile::getDelFlag,DelFlagEnum.NO.getCode());
        return this.list(queryWrapper);
    }

    /**
     * 文件复制的条件校验
     * 1、目标文件必须是一个文件夹
     * 2、选中的要转移的文件列表中不能含有目标文件夹以及子文件夹
     * @param context
     */
    private void checkCopyCondition(CopyFileContext context) {
        Long targetParentId = context.getTargetParentId();
        RPanUserFile targetParent = this.getById(targetParentId);
        if(!checkIsFolder(targetParent)){
            throw new RPanBusinessException("目标文件不是一个文件夹");
        }
        List<Long> fileIdList = context.getFileIdList();
        List<RPanUserFile> prepareRecord = listByIds(fileIdList);
        context.setPrepareRecords(prepareRecord);
        if(checkIsChildFolder(prepareRecord,targetParentId,context.getUserId())){
            throw new RPanBusinessException("目标文件夹ID不能是选中文件列表中的文件夹ID或其子文件夹ID");
        }
    }

    /**
     * 执行文件转移的动作
     * @param transferFileContext
     */
    private void doTransfer(TransferFileContext transferFileContext) {
        List<RPanUserFile> prepareRecords = transferFileContext.getPrepareRecords();
        prepareRecords.stream().forEach(record -> {
            record.setParentId(transferFileContext.getTargetParentId());
            record.setUserId(transferFileContext.getUserId());
            record.setCreateUser(transferFileContext.getUserId());
            record.setCreateTime(new Date());
            record.setUpdateUser(transferFileContext.getUserId());
            record.setUpdateTime(new Date());
            handleDuplicateFilename(record);
        });
        if(!this.updateBatchById(prepareRecords)){
            throw new RPanBusinessException("文件转移失败");
        }
    }

    /**
     * 文件转移的条件校验
     * 1、目标文件必须是一个文件夹
     * 2、选中的要转移的文件列表中不能含有目标文件夹以及子文件夹
     * A-->B A能转移到B文件夹中
     * @param transferFileContext
     */
    private void checkTransferCondition(TransferFileContext transferFileContext) {
        Long targetParentId = transferFileContext.getTargetParentId();
        RPanUserFile targetParent = this.getById(targetParentId);
        if(!checkIsFolder(targetParent)){
            throw new RPanBusinessException("目标文件不是一个文件夹");
        }
        List<Long> fileIdList = transferFileContext.getFileIdList();
        List<RPanUserFile> prepareRecord = listByIds(fileIdList);
        transferFileContext.setPrepareRecords(prepareRecord);
        if(checkIsChildFolder(prepareRecord,targetParentId,transferFileContext.getUserId())){
            throw new RPanBusinessException("目标文件夹ID不能是选中文件列表中的文件夹ID或其子文件夹ID");
        }
    }

    /**
     * 校验目标文件夹ID是要操作的文件记录的ID以及子文件夹ID
     * 1、要操作的文件列表中没有文件夹，那就直接返回false
     * 2、拼装文件夹ID一起所有子文件ID，判断存在即可
     * @param prepareRecord
     * @param targetParentId
     * @param userId
     * @return
     */
    private boolean checkIsChildFolder(List<RPanUserFile> prepareRecord, Long targetParentId, Long userId) {
        prepareRecord = prepareRecord.stream().filter(record -> Objects.equals(record.getFolderFlag(), FolderFlagEnum.YES.getCode())).collect(Collectors.toList());
        if(CollectionUtil.isEmpty(prepareRecord)){
            return false;
        }
        List<RPanUserFile> folderRecords = queryFolderRecords(userId);
        Map<Long, List<RPanUserFile>> folderRecordMap = folderRecords.stream().collect(Collectors.groupingBy(RPanUserFile::getParentId));
        List<RPanUserFile> unavailableFolderRecordList = Lists.newArrayList();
        unavailableFolderRecordList.addAll(prepareRecord);
        prepareRecord.forEach(record->findAllChildFolderRecord(unavailableFolderRecordList,folderRecordMap,record));
        List<Long> unavailableFolderRecordIds = unavailableFolderRecordList.stream().map(RPanUserFile::getFileId).collect(Collectors.toList());
        return unavailableFolderRecordIds.contains(targetParentId);
    }

    /**
     * 查找文件夹下所有子文件夹记录
     * @param unavailableFolderRecordList
     * @param folderRecordMap
     * @param record
     */
    private void findAllChildFolderRecord(List<RPanUserFile> unavailableFolderRecordList, Map<Long, List<RPanUserFile>> folderRecordMap, RPanUserFile record) {
        if(Objects.isNull(record)){
            return;
        }
        List<RPanUserFile> childFolderRecordList = folderRecordMap.get(record.getFileId());
        if(CollectionUtil.isEmpty(childFolderRecordList)){
            return;
        }
        unavailableFolderRecordList.addAll(childFolderRecordList);
        childFolderRecordList.stream().forEachOrdered(childRecord->findAllChildFolderRecord(unavailableFolderRecordList,folderRecordMap,childRecord));
    }

    /**
     * 拼装文件夹树列表
     * @param record
     * @return
     */
    private List<FolderTreeNodeVO> assembleFolderTreeNodeVOList(List<RPanUserFile> record) {
        if(CollectionUtil.isEmpty(record)){
            return Lists.newArrayList();
        }
        List<FolderTreeNodeVO> mappedFolderTreeNodeVOList = record.stream().map(fileConverter::rPanUserFile2FolderTreeNodeVO).collect(Collectors.toList());
        Map<Long, List<FolderTreeNodeVO>> mappedFolderTreeNodeVOMap = mappedFolderTreeNodeVOList.stream().collect(Collectors.groupingBy(FolderTreeNodeVO::getParentId));
        for (FolderTreeNodeVO folderTreeNodeVO : mappedFolderTreeNodeVOList) {
            List<FolderTreeNodeVO> children = mappedFolderTreeNodeVOMap.get(folderTreeNodeVO.getId());
            if(CollectionUtil.isNotEmpty(children)){
                folderTreeNodeVO.getChildren().addAll(children);
            }
        }
        return mappedFolderTreeNodeVOList.stream().filter(folderTreeNodeVO -> Objects.equals(folderTreeNodeVO.getParentId(),FileConstants.TOP_PARENT_ID)).collect(Collectors.toList());
    }

    /**
     * 查询用户所有有效的文件夹信息
     * @param userId
     * @return
     */
    private List<RPanUserFile> queryFolderRecords(Long userId) {
        LambdaQueryWrapper<RPanUserFile> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(RPanUserFile::getUserId,userId);
        queryWrapper.eq(RPanUserFile::getDelFlag,DelFlagEnum.NO.getCode());
        queryWrapper.eq(RPanUserFile::getFolderFlag, FolderFlagEnum.YES.getCode());
        return this.list(queryWrapper);
    }

    /**
     * 执行文件预览的动作
     * 1、查询文件的真实存储路径
     * 2、添加跨域的公共响应头
     * 3、委托文件存储引擎去读取文件内容到响应的输出流中
     * @param record
     * @param response
     */
    private void doPreview(RPanUserFile record, HttpServletResponse response) {
        Long realFileId = record.getRealFileId();
        RPanFile rPanFile = fileService.getById(realFileId);
        if(Objects.isNull(rPanFile)){
            throw new RPanBusinessException("当前的文件记录不存在");
        }
        addCommonResponseHeader(response, rPanFile.getFilePreviewContentType());
        realFile2OutputStream(rPanFile.getRealPath(),response);
    }

    /**
     * 执行文件下载的动作
     * 1、查询文件的真实存储路径
     * 2、添加跨域的公共响应头
     * 3、拼装下载文件的名称、长度等响应信息
     * 4、委托文件存储引擎去读取文件内容到响应的输出流中
     * @param record
     * @param response
     */
    private void doDownload(RPanUserFile record, HttpServletResponse response) {
        Long realFileId = record.getRealFileId();
        RPanFile rPanFile = fileService.getById(realFileId);
        if(Objects.isNull(rPanFile)){
            throw new RPanBusinessException("当前的文件记录不存在");
        }
        addCommonResponseHeader(response, MediaType.APPLICATION_OCTET_STREAM_VALUE);
        addDownloadAttribute(response,record,rPanFile);
        realFile2OutputStream(rPanFile.getRealPath(),response);
    }

    /**
     * 委托文件存储引擎去读取内容并写出到输出流中
     * @param realPath
     * @param response
     */
    private void realFile2OutputStream(String realPath, HttpServletResponse response) {

        try {
            ReadFileContext context=new ReadFileContext();
            context.setRealPath(realPath);
            context.setOutputStream(response.getOutputStream());
            storageEngine.readFile(context);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RPanBusinessException("文件下载失败");
        }

    }

    /**
     * 添加文件下载的属性信息
     * @param response
     * @param record
     * @param rPanFile
     */
    private void addDownloadAttribute(HttpServletResponse response, RPanUserFile record, RPanFile rPanFile) {
        try {
            response.addHeader(FileConstants.CONTENT_DISPOSITION_STR,FileConstants.CONTENT_DISPOSITION_VALUE_PREFIX_STR+new String(record.getFilename().getBytes(FileConstants.GB2312_STR),FileConstants.ISO_8859_1_STR));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new RPanBusinessException("文件下载失败");
        }
        response.setContentLengthLong(Long.valueOf(rPanFile.getFileSize()));

    }

    /**
     * 添加公共的文件读取响应头
     * @param response
     * @param contentTypeValue
     */
    private void addCommonResponseHeader(HttpServletResponse response, String contentTypeValue) {
        response.reset();
        HttpUtil.addCorsResponseHeaders(response);
        response.addHeader(FileConstants.CONTENT_TYPE_STR,contentTypeValue);
        response.setContentType(contentTypeValue);
    }

    /**
     * 检查当前记录是不是一个文件夹
     *
     * @param record
     * @return
     */
    private boolean checkIsFolder(RPanUserFile record) {
        if (Objects.isNull(record)) {
            throw new RPanBusinessException("当前文件记录不存在");
        }
        return Objects.equals(record.getFolderFlag(),FolderFlagEnum.YES.getCode());
    }

    /**
     * 校验用户的操作权限
     * 1、文件记录继续存在
     * 2、文件记录的创建者必须是该登录用户
     *
     * @param record
     * @param userId
     */
    private void checkOperatePermission(RPanUserFile record, Long userId) {
        if (Objects.isNull(record)) {
            throw new RPanBusinessException("当前文件记录不存在");
        }
        if (!Objects.equals(userId, record.getUserId())) {
            throw new RPanBusinessException("您没有该文件的操作权限");
        }
    }

    /**
     * 合并文件分片并保存物理记录
     *
     * @param context
     */
    private void mergeFileChunkAndSaveFile(FileChunkMergeContext context) {
        FileChunkMergeAndSaveContext fileChunkMergeAndSaveContext = fileConverter.fileChunkMergeContext2FileChunkMergeAndSaveContext(context);
        fileService.mergeFileChunkAndSaveFile(fileChunkMergeAndSaveContext);
        context.setRecord(fileChunkMergeAndSaveContext.getRecord());
    }

    /**
     * 删除文件之前的前置校验
     * 1、文件ID合法校验
     * 2、用户具有删除该文件的权限
     *
     * @param context
     */
    private void checkFileDeleteCondition(DeleteFileContext context) {
        List<Long> fileIdList = context.getFileIdList();
        List<RPanUserFile> rPanUserFiles = this.listByIds(fileIdList);
        if (rPanUserFiles.size() != fileIdList.size()) {
            throw new RPanBusinessException("存在不合法的文件记录");
        }
        Set<Long> fileIdSet = rPanUserFiles.stream().map(RPanUserFile::getFileId).collect(Collectors.toSet());
        int oldSize = fileIdSet.size();
        fileIdSet.addAll(fileIdList);
        int newSize = fileIdSet.size();
        if (newSize != oldSize) {
            throw new RPanBusinessException("存在不合法文件记录");
        }
        Set<Long> userIdSet = rPanUserFiles.stream().map(RPanUserFile::getUserId).collect(Collectors.toSet());
        if (userIdSet.size() != 1) {
            throw new RPanBusinessException("存在不合法文件记录");
        }
        Long dbUserId = userIdSet.stream().findFirst().get();
        if (!Objects.equals(dbUserId, context.getUserId())) {
            throw new RPanBusinessException("当前登录用户没有删除该文件的权限");
        }
    }

    /**
     * 执行文件重命名的操作
     *
     * @param context
     */
    private void doUpdateFilename(UpdateFilenameContext context) {
        RPanUserFile entity = context.getEntity();
        entity.setFilename(context.getNewFilename());
        entity.setUserId(context.getUserId());
        entity.setUpdateTime(new Date());
        if (!this.updateById(entity)) {
            throw new RPanBusinessException("文件重命名失败");
        }
    }

    /**
     * 更新文件名称
     * 1、文件ID是否有效的
     * 2、用户是否有权限更新该文件的文件名称
     * 3、新旧文件名称不能一样
     * 4、不能使用当前文件夹下面的字文件的名称
     *
     * @param context
     */
    private void checkUpdateFilenameCondition(UpdateFilenameContext context) {
        Long fileId = context.getFileId();
        RPanUserFile entity = this.getById(fileId);
        if (Objects.isNull(entity)) {
            throw new RPanBusinessException("该文件ID无效");
        }
        if (!Objects.equals(entity.getUserId(), context.getUserId())) {
            throw new RPanBusinessException("当前登录的用户没有修改改文件名称的权限");
        }
        if (Objects.equals(entity.getFilename(), context.getNewFilename())) {
            throw new RPanBusinessException("请换一个新的文件名称来修改");
        }
        LambdaQueryWrapper<RPanUserFile> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RPanUserFile::getParentId, entity.getParentId());
        queryWrapper.eq(RPanUserFile::getFilename, context.getNewFilename());
        int count = this.count(queryWrapper);
        if (count > 0) {
            throw new RPanBusinessException("该文件名称已被占用");
        }
        context.setEntity(entity);
    }
    /****************************private****************************/
    /**
     * 保存用户文件的映射记录
     *
     * @param parentId
     * @param filename
     * @param folderFlagEnum
     * @param fileType
     * @param realFileId
     * @param userId
     * @param fileSizeDesc
     * @return
     */
    private Long saveUserFile(Long parentId,
                              String filename,
                              FolderFlagEnum folderFlagEnum,
                              Integer fileType,
                              Long realFileId,
                              Long userId,
                              String fileSizeDesc) {
        RPanUserFile entity = assembleRPanUserFile(parentId, userId, filename, folderFlagEnum, fileType, realFileId, fileSizeDesc);
        if (!save(entity)) {
            throw new RPanBusinessException("保存文件信息失败！");
        }
        return entity.getFileId();
    }

    /**
     * 用户文件映射关系实体转化
     * 1、构建并填充实体信息
     * 2、处理文件命名一直的问题
     * a->b ,b （z）
     *
     * @param parentId
     * @param userId
     * @param filename
     * @param folderFlagEnum
     * @param fileType
     * @param realFileId
     * @param fileSizeDesc
     * @return
     */
    private RPanUserFile assembleRPanUserFile(Long parentId, Long userId, String filename, FolderFlagEnum folderFlagEnum, Integer fileType, Long realFileId, String fileSizeDesc) {
        RPanUserFile entity = new RPanUserFile();
        entity.setFileId(IdUtil.get());
        entity.setUserId(userId);
        entity.setParentId(parentId);
        entity.setRealFileId(realFileId);
        entity.setFilename(filename);
        entity.setFolderFlag(folderFlagEnum.getCode());
        entity.setFileSizeDesc(fileSizeDesc);
        entity.setFileType(fileType);
        entity.setDelFlag(DelFlagEnum.NO.getCode());
        entity.setCreateUser(userId);
        entity.setCreateTime(new Date());
        entity.setUpdateUser(userId);
        entity.setUpdateTime(new Date());
        handleDuplicateFilename(entity);
        return entity;
    }

    /**
     * 处理用户重复名称
     * 如果统一文件夹下面由文件名称重复
     * 按照系统级规则重命名文件
     *
     * @param entity
     */
    private void handleDuplicateFilename(RPanUserFile entity) {
        String filename = entity.getFilename(),
                newFilenameWithoutSuffix,
                newFilenameSuffix;
        int newFilenamePointPosition = filename.lastIndexOf(RPanConstants.POINT_STR);
        if (newFilenamePointPosition == RPanConstants.MINUS_ONE_INT) {
            newFilenameWithoutSuffix = filename;
            newFilenameSuffix = RPanConstants.EMPTY_STR;
        } else {
            newFilenameWithoutSuffix = filename.substring(RPanConstants.ZERO_INT, newFilenamePointPosition);
            newFilenameSuffix = filename.replace(newFilenameWithoutSuffix, StringUtils.EMPTY);
        }
        int count = getDuplicateFilename(entity, newFilenameWithoutSuffix);
        if (count == 0) {
            return;
        }
        String newFilename = assembleNewFilename(newFilenameWithoutSuffix, count, newFilenameSuffix);
        entity.setFilename(newFilename);
    }

    /**
     * 拼装新文件名称
     * 拼装规则参考操作系统从夫文件名称的重命名规范
     *
     * @param newFilenameWithoutSuffix
     * @param count
     * @param newFilenameSuffix
     * @return
     */
    private String assembleNewFilename(String newFilenameWithoutSuffix, int count, String newFilenameSuffix) {
        String newFilename = new StringBuilder(newFilenameWithoutSuffix)
                .append(FileConstants.CN_LEFT_PARENTHESES_STR)
                .append(count)
                .append(FileConstants.CN_RIGHT_PARENTHESES_STR)
                .append(newFilenameSuffix)
                .toString();
        return newFilename;
    }

    /**
     * 查找同一文件夹下面同名文件数量
     *
     * @param entity
     * @param newFilenameWithoutSuffix
     * @return
     */
    private int getDuplicateFilename(RPanUserFile entity, String newFilenameWithoutSuffix) {
        LambdaQueryWrapper<RPanUserFile> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RPanUserFile::getParentId, entity.getParentId());
        queryWrapper.eq(RPanUserFile::getFolderFlag, entity.getFolderFlag());
        queryWrapper.eq(RPanUserFile::getUserId, entity.getUserId());
        queryWrapper.eq(RPanUserFile::getDelFlag, DelFlagEnum.NO.getCode());
        queryWrapper.likeLeft(RPanUserFile::getFilename, newFilenameWithoutSuffix);
        String sql = queryWrapper.getTargetSql();
        return count(queryWrapper);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}




