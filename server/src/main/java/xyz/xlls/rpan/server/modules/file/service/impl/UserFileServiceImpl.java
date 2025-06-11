package xyz.xlls.rpan.server.modules.file.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.transaction.annotation.Transactional;
import xyz.xlls.rpan.core.constants.RPanConstants;
import xyz.xlls.rpan.core.exception.RPanBusinessException;
import xyz.xlls.rpan.core.utils.FileUtil;
import xyz.xlls.rpan.core.utils.IdUtil;
import xyz.xlls.rpan.server.common.event.file.DeleteFileEvent;
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
import xyz.xlls.rpan.server.modules.file.vo.FileChunkUploadVO;
import xyz.xlls.rpan.server.modules.file.vo.RPanUserFileVo;
import xyz.xlls.rpan.server.modules.file.vo.UploadedChunksVO;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
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
    /**
     * 创建文件夹信息
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
     * @param userId
     * @return
     */
    @Override
    public RPanUserFile getUserRootFile(Long userId) {
        LambdaQueryWrapper<RPanUserFile> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RPanUserFile::getUserId, userId);
        queryWrapper.eq(RPanUserFile::getParentId,FileConstants.TOP_PARENT_ID);
        queryWrapper.eq(RPanUserFile::getDelFlag,DelFlagEnum.NO.getCode());
        queryWrapper.eq(RPanUserFile::getFolderFlag,FolderFlagEnum.YES.getCode());
        return this.getOne(queryWrapper);
    }

    /**
     * 查询用户的文件列表
     * @param context
     * @return
     */
    @Override
    public List<RPanUserFileVo> getFileList(QueryFileContext context) {
        return baseMapper.selectFileList(context);
    }

    /**
     * 更新文件名称
     * 1、校验更新文件名称的条件
     * 2、执行更新文件名称的操作
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
     * @param context
     */
    @Override
    public void deleteFile(DeleteFileContext context) {
        checkFileDeleteCondition(context);
        doDeleteFile(context);
        afterFileDelete(context);
    }

    /**
     *  文件秒传
     *  1、通过文件的唯一标识，查找对应的实体文件记录
     *  2、如果没有查到，直接返回秒传失败
     *  3、如果查到记录，直接挂载关联管理，返回秒传成功即可
     * @param context
     * @return
     */
    @Override
    public boolean secUpload(SecUploadContext context) {
        RPanFile record=getFileByUserIdAndIdentifier(context.getUserId(), context.getIdentifier());
        if(Objects.isNull(record)){
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
     * @param context
     */
    private void saveFile(FileUploadContext context) {
        FileSaveContext fileSaveContext=fileConverter.fileUploadContext2FileSaveContext(context);
        fileService.saveFile(fileSaveContext);
        context.setRecord(fileSaveContext.getRecord());
    }

    private RPanFile getFileByUserIdAndIdentifier(Long userId, String identifier) {
        List<RPanFile> records= fileService.getFileByUserIdAndIdentifier(userId, identifier);
        if(CollectionUtil.isEmpty(records)){
            return null;
        }
        return records.get(RPanConstants.ZERO_INT);
    }

    /**
     * 文件删除的后置操作
     * 1、对外发布文件删除的事件
     * @param context
     */
    private void afterFileDelete(DeleteFileContext context) {
        DeleteFileEvent deleteFileEvent = new DeleteFileEvent(this,context.getFileIdList());
        applicationContext.publishEvent(deleteFileEvent);
    }

    /**
     * 执行删除文件的后置操作
     * @param context
     */
    private void doDeleteFile(DeleteFileContext context) {
        List<Long> fileIdList = context.getFileIdList();
        LambdaUpdateWrapper<RPanUserFile> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(RPanUserFile::getFileId,  fileIdList);
        updateWrapper.set(RPanUserFile::getDelFlag,DelFlagEnum.YES.getCode());
        updateWrapper.set(RPanUserFile::getUpdateTime,new Date());
        if(!this.update(updateWrapper)){
            throw new RPanBusinessException("文件删除失败");
        }
    }

    /**
     *  文件分片上传
     *  1、上传实体文件
     *  2、保存分片文件记录、
     *  3、校验是否全部分片上传完成
     * @param context
     * @return
     */
    @Override
    public FileChunkUploadVO chunkUpload(FileChunkUploadContext context) {
        FileChunkSaveContext fileChunkSaveContext=fileConverter.fileChunkUploadContext2FileChunkSaveContext(context);
        fileChunkService.saveChunkFile(fileChunkSaveContext);
        FileChunkUploadVO vo=new FileChunkUploadVO();
        vo.setMergeFlag(fileChunkSaveContext.getMergeFlagEnum().getCode());

        return vo;
    }

    /**
     * 查询用户已上传的分片列表
     * 1、查询已上传的分片列表
     * 2、封装返回实体
     * @param context
     * @return
     */
    @Override
    public UploadedChunksVO getUploadedChunks(QueryUploadedChunksContext context) {
        List<Integer> uploadedChunks =fileChunkService.queryUploadedChunks(context);
        UploadedChunksVO vo = new UploadedChunksVO();
        vo.setUploadedChunks(uploadedChunks);
        return vo;
    }

    /**
     * 文件分片合并
     * 1、文件分片物理合并
     * 2、保存文件实体记录
     * 3、保存文件用户关系映射
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
     * 合并文件分片并保存物理记录
     * @param context
     */
    private void mergeFileChunkAndSaveFile(FileChunkMergeContext context) {
        FileChunkMergeAndSaveContext fileChunkMergeAndSaveContext=fileConverter.fileChunkMergeContext2FileChunkMergeAndSaveContext(context);
        fileService.mergeFileChunkAndSaveFile(fileChunkMergeAndSaveContext);
        context.setRecord(fileChunkMergeAndSaveContext.getRecord());
    }

    /**
     * 删除文件之前的前置校验
     * 1、文件ID合法校验
     * 2、用户具有删除该文件的权限
     * @param context
     */
    private void checkFileDeleteCondition(DeleteFileContext context) {
        List<Long> fileIdList = context.getFileIdList();
        List<RPanUserFile> rPanUserFiles = this.listByIds(fileIdList);
        if(rPanUserFiles.size()!=fileIdList.size()){
            throw new RPanBusinessException("存在不合法的文件记录");
        }
        Set<Long> fileIdSet = rPanUserFiles.stream().map(RPanUserFile::getFileId).collect(Collectors.toSet());
        int oldSize = fileIdSet.size();
        fileIdSet.addAll(fileIdList);
        int newSize = fileIdSet.size();
        if(newSize!=oldSize){
            throw new RPanBusinessException("存在不合法文件记录");
        }
        Set<Long> userIdSet = rPanUserFiles.stream().map(RPanUserFile::getUserId).collect(Collectors.toSet());
        if(userIdSet.size()!=1){
            throw new RPanBusinessException("存在不合法文件记录");
        }
        Long dbUserId = userIdSet.stream().findFirst().get();
        if(!Objects.equals(dbUserId,context.getUserId())){
            throw new RPanBusinessException("当前登录用户没有删除该文件的权限");
        }
    }

    /**
     * 执行文件重命名的操作
     * @param context
     */
    private void doUpdateFilename(UpdateFilenameContext context) {
        RPanUserFile entity = context.getEntity();
        entity.setFilename(context.getNewFilename());
        entity.setUserId(context.getUserId());
        entity.setUpdateTime(new Date());
        if(!this.updateById(entity)){
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
        if(Objects.isNull(entity)){
            throw new RPanBusinessException("该文件ID无效");
        }
        if(!Objects.equals(entity.getUserId(),context.getUserId())){
            throw new RPanBusinessException("当前登录的用户没有修改改文件名称的权限");
        }
        if(Objects.equals(entity.getFilename(),context.getNewFilename())){
            throw new RPanBusinessException("请换一个新的文件名称来修改");
        }
        LambdaQueryWrapper<RPanUserFile> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(RPanUserFile::getParentId,entity.getParentId());
        queryWrapper.eq(RPanUserFile::getFilename,context.getNewFilename());
        int count = this.count(queryWrapper);
        if(count>0){
            throw new RPanBusinessException("该文件名称已被占用");
        }
        context.setEntity(entity);
    }
    /****************************private****************************/
    /**
     * 保存用户文件的映射记录
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
                              String fileSizeDesc){
        RPanUserFile entity= assembleRPanUserFile(parentId,userId,filename,folderFlagEnum,fileType,realFileId,fileSizeDesc);
        if(!save(entity)){
            throw new RPanBusinessException("保存文件信息失败！");
        }
        return entity.getFileId();
    }

    /**
     * 用户文件映射关系实体转化
     * 1、构建并填充实体信息
     * 2、处理文件命名一直的问题
     * a->b ,b （z）
     * @param parentId
     * @param userId
     * @param filename
     * @param folderFlagEnum
     * @param fileType
     * @param realFileId
     * @param fileSizeDesc
     * @return
     */
    private RPanUserFile assembleRPanUserFile(Long parentId, Long userId, String filename, FolderFlagEnum folderFlagEnum,Integer fileType, Long realFileId, String fileSizeDesc) {
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
     * @param entity
     */
    private void handleDuplicateFilename(RPanUserFile entity) {
        String filename=entity.getFilename(),
        newFilenameWithoutSuffix,
        newFilenameSuffix;
        int newFilenamePointPosition=filename.lastIndexOf(RPanConstants.POINT_STR);
        if(newFilenamePointPosition==RPanConstants.MINUS_ONE_INT){
            newFilenameWithoutSuffix=filename;
            newFilenameSuffix=RPanConstants.EMPTY_STR;
        }else{
            newFilenameWithoutSuffix=filename.substring(RPanConstants.ZERO_INT,newFilenamePointPosition);
            newFilenameSuffix=filename.replace(newFilenameWithoutSuffix, StringUtils.EMPTY);
        }
        int count=getDuplicateFilename(entity,newFilenameWithoutSuffix);
        if(count==0){
            return;
        }
        String newFilename=assembleNewFilename(newFilenameWithoutSuffix,count,newFilenameSuffix);
        entity.setFilename(newFilename);
    }

    /**
     * 拼装新文件名称
     * 拼装规则参考操作系统从夫文件名称的重命名规范
     * @param newFilenameWithoutSuffix
     * @param count
     * @param newFilenameSuffix
     * @return
     */
    private String assembleNewFilename(String newFilenameWithoutSuffix, int count, String newFilenameSuffix) {
        String newFilename=new StringBuilder(newFilenameWithoutSuffix)
                .append(FileConstants.CN_LEFT_PARENTHESES_STR)
                .append(count)
                .append(FileConstants.CN_RIGHT_PARENTHESES_STR)
                .append(newFilenameSuffix)
                .toString();
        return newFilename;
    }

    /**
     * 查找同一文件夹下面同名文件数量
     * @param entity
     * @param newFilenameWithoutSuffix
     * @return
     */
    private int getDuplicateFilename(RPanUserFile entity, String newFilenameWithoutSuffix) {
        LambdaQueryWrapper<RPanUserFile> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(RPanUserFile::getParentId,entity.getParentId());
        queryWrapper.eq(RPanUserFile::getFolderFlag,entity.getFolderFlag());
        queryWrapper.eq(RPanUserFile::getUserId,entity.getUserId());
        queryWrapper.eq(RPanUserFile::getDelFlag,DelFlagEnum.NO.getCode());
        queryWrapper.likeLeft(RPanUserFile::getFilename,newFilenameWithoutSuffix);
        String sql = queryWrapper.getTargetSql();
        return count(queryWrapper);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext=applicationContext;
    }
}




