package xyz.xlls.rpan.server.modules.file.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import xyz.xlls.rpan.core.constants.RPanConstants;
import xyz.xlls.rpan.core.exception.RPanBusinessException;
import xyz.xlls.rpan.core.utils.IdUtil;
import xyz.xlls.rpan.server.modules.file.constants.FileConstants;
import xyz.xlls.rpan.server.modules.file.context.CreateFolderContext;
import xyz.xlls.rpan.server.modules.file.context.UpdateFilenameContext;
import xyz.xlls.rpan.server.modules.file.entity.RPanUserFile;
import xyz.xlls.rpan.server.modules.file.enums.DelFlagEnum;
import xyz.xlls.rpan.server.modules.file.enums.FolderFlagEnum;
import xyz.xlls.rpan.server.modules.file.service.IUserFileService;
import xyz.xlls.rpan.server.modules.file.mapper.RPanUserFileMapper;
import org.springframework.stereotype.Service;
import xyz.xlls.rpan.server.modules.file.context.QueryFileContext;
import xyz.xlls.rpan.server.modules.file.vo.RPanUserFileVo;

import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
* @author Administrator
* @description 针对表【r_pan_user_file(用户文件信息表)】的数据库操作Service实现
* @createDate 2024-10-22 15:04:09
*/
@Service
public class UserFileServiceImpl extends ServiceImpl<RPanUserFileMapper, RPanUserFile>
    implements IUserFileService {
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
     * 查找同一文件夹下面桶名文件数量
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

}




