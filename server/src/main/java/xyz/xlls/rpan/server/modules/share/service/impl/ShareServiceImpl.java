package xyz.xlls.rpan.server.modules.share.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.assertj.core.util.Lists;
import org.assertj.core.util.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.transaction.annotation.Transactional;
import xyz.xlls.rpan.core.constants.RPanConstants;
import xyz.xlls.rpan.core.exception.RPanBusinessException;
import xyz.xlls.rpan.core.response.ResponseCode;
import xyz.xlls.rpan.core.utils.IdUtil;
import xyz.xlls.rpan.core.utils.JwtUtil;
import xyz.xlls.rpan.core.utils.UUIDUtil;
import xyz.xlls.rpan.server.common.config.PanServerConfig;
import xyz.xlls.rpan.server.common.event.log.ErrorLogEvent;
import xyz.xlls.rpan.server.modules.file.constants.FileConstants;
import xyz.xlls.rpan.server.modules.file.context.CopyFileContext;
import xyz.xlls.rpan.server.modules.file.context.FileDownloadContext;
import xyz.xlls.rpan.server.modules.file.context.QueryFileContext;
import xyz.xlls.rpan.server.modules.file.entity.RPanUserFile;
import xyz.xlls.rpan.server.modules.file.enums.DelFlagEnum;
import xyz.xlls.rpan.server.modules.file.service.IUserFileService;
import xyz.xlls.rpan.server.modules.file.vo.RPanUserFileVO;
import xyz.xlls.rpan.server.modules.share.constants.ShareConstant;
import xyz.xlls.rpan.server.modules.share.context.*;
import xyz.xlls.rpan.server.modules.share.entity.RPanShare;
import xyz.xlls.rpan.server.modules.share.entity.RPanShareFile;
import xyz.xlls.rpan.server.modules.share.enums.ShareDayTypeEnum;
import xyz.xlls.rpan.server.modules.share.enums.ShareStatusEnum;
import xyz.xlls.rpan.server.modules.share.service.IShareFileService;
import xyz.xlls.rpan.server.modules.share.service.IShareService;
import xyz.xlls.rpan.server.modules.share.mapper.RPanShareMapper;
import org.springframework.stereotype.Service;
import xyz.xlls.rpan.server.modules.share.vo.*;
import xyz.xlls.rpan.server.modules.user.entity.RPanUser;
import xyz.xlls.rpan.server.modules.user.service.IUserService;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Administrator
 * @description 针对表【r_pan_share(用户分享表)】的数据库操作Service实现
 * @createDate 2024-10-22 15:08:03
 */
@Service
public class ShareServiceImpl extends ServiceImpl<RPanShareMapper, RPanShare>
        implements IShareService, ApplicationContextAware {
    @Autowired
    private PanServerConfig config;
    @Autowired
    private IShareFileService shareFileService;
    @Autowired
    private IUserFileService userFileService;
    @Autowired
    private IUserService userService;
    @Autowired
    private ApplicationContext applicationContext;

    /**
     * 创建分享链接
     * 1、拼装分享实体，保存到数据库
     * 2、保存分享和对应文件的关联关系
     * 3、拼装返回实体并返回
     *
     * @param context
     * @return
     */
    @Transactional(rollbackFor = RPanBusinessException.class)
    @Override
    public RPanShareUrlVO create(CreateShareUrlContext context) {
        saveShare(context);
        saveShareFiles(context);
        return assembleShareVO(context);
    }

    /**
     * 查询用户的分享列表
     *
     * @param context
     * @return
     */
    @Override
    public List<RPanShareUrlListVO> getShares(QueryShareUrlListContext context) {
        return baseMapper.selectShareVOListByUserId(context.getUserId());
    }

    /**
     * 取消分享链接
     * 1、检验用户操作权限
     * 2、删除对应的分享记录
     * 3、删除对应的分享文件关联关系记录
     *
     * @param context
     */
    @Transactional(rollbackFor = RPanBusinessException.class)
    @Override
    public void cancelShare(CancelShareUrlContext context) {
        checkUserCancelSharePermission(context);
        doCancelShare(context);
        doCancelShareFiles(context);

    }

    /**
     * 校验分享码
     * 1、检查分享的状态是不是正常
     * 2、校验分享码是不是正确
     * 3、生成一个短时间的分享token返回给上游
     *
     * @param context
     * @return
     */
    @Override
    public String checkShareCode(CheckShareCodeContext context) {
        RPanShare record = checkShareStatus(context.getShareId());
        context.setRecord(record);
        doCheckShareCode(context);
        return generateShareToken(context);
    }

    /**
     * 1、校验分享的状态
     * 2、初始化分享实体
     * 3、查询分享的主体信息
     * 4、查询分享的文件列表
     * 5、查询分享者的信息
     *
     * @param context
     * @return
     */
    @Override
    public ShareDetailVO detail(QueryShareDetailContext context) {
        RPanShare record = checkShareStatus(context.getShareId());
        context.setRecord(record);
        initShareVo(context);
        assembleMainShareInfo(context);
        assembleShareFilesInfo(context);
        assembleShareUserInfo(context);
        return context.getVo();
    }

    /**
     * 1、校验分享的状态
     * 2、初始化分享实体
     * 3、查询分享的主体信息
     * 4、查询分享者信息
     * @param context
     * @return
     */
    @Override
    public ShareSimpleDetailVO simpleDetail(QueryShareSimpleDetailContext context) {
        RPanShare record = checkShareStatus(context.getShareId());
        context.setRecord(record);
        initShareSimpleVo(context);
        assembleMainShareSimpleInfo(context);
        assembleShareSimpleUserInfo(context);
        return context.getVo();
    }

    /**
     * 获取下一级文件列表
     * 1、校验分享状态
     * 2、校验文件的ID是在分享的文件列表中
     * 3、查询对应的子文件列表，返回
     * @param context
     * @return
     */
    @Override
    public List<RPanUserFileVO> fileList(QueryChildFileListContext context) {
        RPanShare record = checkShareStatus(context.getShareId());
        context.setRecord(record);
        List<RPanUserFileVO> allUserFileRecords= checkFileIdIsOnShareStatusAndGetAllShareUserFiles(context.getShareId(),Lists.newArrayList(context.getParentId()));
        Map<Long, List<RPanUserFileVO>> parentIdFileListMap = allUserFileRecords.stream().collect(Collectors.groupingBy(RPanUserFileVO::getParentId));
        List<RPanUserFileVO> rPanUserFileVOS = parentIdFileListMap.get(context.getParentId());
        if(CollectionUtil.isEmpty(rPanUserFileVOS)){
            return Lists.newArrayList();
        }
        return allUserFileRecords;
    }

    /**
     * 转存至我的网盘
     * 1、校验分享状态
     * 2、校验文件ID是否合法
     * 3、委托文件模块做文件拷贝的操作
     * @param context
     */
    @Override
    public void saveFiles(ShareSaveContext context) {
        checkShareStatus(context.getShareId());
        checkFileIdIsOnShareStatus(context.getShareId(),context.getFileIdList());
        doSaveFiles(context);
    }

    /**
     * 分享的文件下载
     * 1、校验分享状态
     * 2、校验文件ID的合法性
     * 3、执行文件下载的动作
     * @param context
     */
    @Override
    public void download(ShareFileDownloadContext context) {
        checkShareStatus(context.getShareId());
        checkFileIdIsOnShareStatus(context.getShareId(),Lists.newArrayList(context.getFileId()));
        doDownload(context);
    }

    /**
     * 刷新受影响的对应的分享的状态
     * 1、查询所有的受影响的ID集合
     * 2、去判断每一个分享对应的文件以及所有的父文件信息均为正常，该种情况吧分享的状态变为正常
     * 3、如果有分享的文件或者父文件信息被删除，变更该分享的状态为有文件被删除
     * @param allAvailableFileIdList
     */
    @Override
    public void refreshStatus(List<Long> allAvailableFileIdList) {
       List<Long> shareIdList= getShareIdListByFileIdList(allAvailableFileIdList);
       if(CollectionUtil.isEmpty(shareIdList)){
           return;
       }
       Set<Long> shareIdSet= Sets.newHashSet(shareIdList);
       shareIdSet.stream().forEach(this::refreshOneShareStatus);

    }

    /**
     * 刷新一个分享的分享状态
     * 1、查询对应的分享信息、判断有效
     * 2、去判断每一个分享对应的文件以及所有的父文件信息均为正常，该种情况吧分享的状态变为正常
     * 3、如果有分享的文件或者父文件信息被删除，变更该分享的状态为有文件被删除
     * @param shareId
     */
    private void refreshOneShareStatus(Long shareId) {
        RPanShare record= this.getById(shareId);
        if(Objects.isNull(record)){
            return;
        }
        ShareStatusEnum shareStatus=ShareStatusEnum.NORMAL;
        if(!checkShareFileAvailable(shareId)){
            shareStatus=ShareStatusEnum.FILE_DELETED;
        }
        if(ObjectUtil.equal(record.getShareStatus(),shareStatus)){
            return;
        }
        doChangeShareStatus(shareId,shareStatus);
    }

    /**
     * 执行刷新文件分享的状态动作
     * @param shareId
     * @param shareStatus
     */
    private void doChangeShareStatus(Long shareId, ShareStatusEnum shareStatus) {
        LambdaUpdateWrapper<RPanShare> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(RPanShare::getShareId,shareId);
        updateWrapper.set(RPanShare::getShareStatus,shareStatus.getCode());
        boolean update = this.update(updateWrapper);
        if(!update){
            applicationContext.publishEvent(new ErrorLogEvent(this,"更新分享状态失败，请手动更改状态，分享ID为："+shareId+"，分享状态改为："+shareStatus.getDesc(),RPanConstants.ZERO_LONG));
        }
    }

    /**
     * 检查该分享的文件以及所有的父文件均为正常状态
     * @param shareId
     * @return
     */
    private boolean checkShareFileAvailable(Long shareId) {
        List<Long> shareFileIdList = getShareFileIdList(shareId);
        for (Long fileId : shareFileIdList) {
            if(!checkUpFileAvailable(fileId)){
                return false;
            }
        }
        return true;
    }

    /**
     * 检查该文件以及所有的文件夹信息均为正常状态
     * @param fileId
     * @return
     */
    private boolean checkUpFileAvailable(Long fileId) {
        RPanUserFile record = userFileService.getById(fileId);
        if(Objects.isNull(record)){
            return false;
        }
        if(Objects.equals(record.getDelFlag(),DelFlagEnum.YES.getCode())){
            return false;
        }
        if(Objects.equals(record.getParentId(), FileConstants.TOP_PARENT_ID)){
            return true;
        }
        return checkUpFileAvailable(record.getParentId());
    }


    /**
     * 通过文件ID查询
     * @param allAvailableFileIdList
     * @return
     */
    private List<Long> getShareIdListByFileIdList(List<Long> allAvailableFileIdList) {
        LambdaQueryWrapper<RPanShareFile> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.select(RPanShareFile::getShareId);
        queryWrapper.in(RPanShareFile::getFileId,allAvailableFileIdList);
        List<Long> shareIdList = shareFileService.listObjs(queryWrapper, value -> (Long) value);
        return shareIdList;
    }

    /**
     * 执行分享文件下载的动作
     * 委托文件模块
     * @param context
     */
    private void doDownload(ShareFileDownloadContext context) {
        FileDownloadContext fileDownloadContext=new FileDownloadContext();
        fileDownloadContext.setFileId(context.getFileId());
        fileDownloadContext.setUserId(context.getUserId());
        fileDownloadContext.setResponse(context.getResponse());
        userFileService.download(fileDownloadContext);
    }

    /**
     * 执行保存我的网盘动作
     * @param context
     */
    private void doSaveFiles(ShareSaveContext context) {
        CopyFileContext copyFileContext = new CopyFileContext();
        copyFileContext.setFileIdList(context.getFileIdList());
        copyFileContext.setUserId(context.getUserId());
        copyFileContext.setTargetParentId(context.getTargetParentId());
        userFileService.copy(copyFileContext);
    }

    /**
     * 校验文件ID是否属于某一个分享
     * @param shareId
     * @param fileIdList
     */
    private void checkFileIdIsOnShareStatus(Long shareId, List<Long> fileIdList) {
        checkFileIdIsOnShareStatusAndGetAllShareUserFiles(shareId, fileIdList);
    }

    /**
     * 检验文件是否处于分享状态，返回该分享的所有文件列表
     * @param shareId
     * @param fileIdList
     * @return
     */
    private List<RPanUserFileVO> checkFileIdIsOnShareStatusAndGetAllShareUserFiles(Long shareId, List<Long> fileIdList) {
        List<Long> shareFileIdList = getShareFileIdList(shareId);
        if (CollectionUtil.isEmpty(shareFileIdList)) {
            return Lists.newArrayList();
        }
        List<RPanUserFile> allFileRecords = userFileService.findAllFileRecordsByFileIdList(fileIdList);
        if (CollectionUtil.isEmpty(allFileRecords)) {
            return Lists.newArrayList();
        }
        List<RPanUserFile> allFileRecordsNoDel = allFileRecords.stream().filter(Objects::nonNull)
                .filter(record -> ObjectUtil.equal(record.getDelFlag(), DelFlagEnum.NO.getCode()))
                .collect(Collectors.toList());
        List<Long> allFileIdList = allFileRecordsNoDel.stream().map(RPanUserFile::getFileId).collect(Collectors.toList());
        if(allFileIdList.containsAll(fileIdList)){
             return userFileService.transferVoList(allFileRecordsNoDel);
        }
        throw new RPanBusinessException(ResponseCode.SHARE_FILE_MISS);
    }


    /**
     * 拼装简单文件分享详情的用户信息
     * @param context
     */
    private void assembleShareSimpleUserInfo(QueryShareSimpleDetailContext context) {
        RPanUser record = userService.getById(context.getRecord().getCreateUser());
        if(Objects.isNull(record)){
            throw new RPanBusinessException("用户信息查询失败");
        }
        ShareUserInfoVO shareUserInfoVO=new ShareUserInfoVO();
        shareUserInfoVO.setUsername(encryptUsername(record.getUsername()));
        shareUserInfoVO.setUserId(record.getUserId());
        context.getVo().setShareUserInfoVO(shareUserInfoVO);
    }

    /**
     * 填充简单分享详情实体信息
     * @param context
     */
    private void assembleMainShareSimpleInfo(QueryShareSimpleDetailContext context) {
        RPanShare record = context.getRecord();
        ShareSimpleDetailVO vo = context.getVo();
        vo.setShareId(record.getShareId());
        vo.setShareName(record.getShareName());
    }

    /**
     * 初始化简单分享详情的VO对象
     * @param context
     */
    private void initShareSimpleVo(QueryShareSimpleDetailContext context) {
        ShareSimpleDetailVO vo = new ShareSimpleDetailVO();
        context.setVo(vo);
    }

    /**
     * 查询分享者的信息
     * @param context
     */
    private void assembleShareUserInfo(QueryShareDetailContext context) {
        RPanUser record = userService.getById(context.getRecord().getCreateUser());
        if(Objects.isNull(record)){
            throw new RPanBusinessException("用户信息查询失败");
        }
        ShareUserInfoVO shareUserInfoVO=new ShareUserInfoVO();
        shareUserInfoVO.setUsername(encryptUsername(record.getUsername()));
        shareUserInfoVO.setUserId(record.getUserId());
        context.getVo().setShareUserInfoVO(shareUserInfoVO);

    }

    /**
     * 加密用户名称
     * @param username
     * @return
     */
    private String encryptUsername(String username) {
        StringBuffer stringBuffer =new StringBuffer(username);
        stringBuffer.replace(RPanConstants.TWO_INT, username.length()-RPanConstants.TWO_INT, RPanConstants.COMMON_ENCRYPT_STR);
        return stringBuffer.toString();
    }

    /**
     * 查询文件对应的分享列表
     * 1、查询分享的对应ID集合
     * 2、根据文件ID来查询文件列表信息
     *
     * @param context
     */
    private void assembleShareFilesInfo(QueryShareDetailContext context) {
        List<Long> fileIdList = getShareFileIdList(context.getShareId());
        QueryFileContext queryFileContext = new QueryFileContext();
        queryFileContext.setUserId(context.getRecord().getCreateUser());
        queryFileContext.setDelFlag(DelFlagEnum.NO.getCode());
        queryFileContext.setFileIdList(fileIdList);
        List<RPanUserFileVO> rPanUserFileVOList = userFileService.getFileList(queryFileContext);
        context.getVo().setRPanUserFileVOList(rPanUserFileVOList);
    }

    /**
     * 查询分享对应的文件ID集合
     * @param shareId
     * @return
     */
    private List<Long> getShareFileIdList(Long shareId){
        if(Objects.isNull(shareId)){
            return Lists.newArrayList();
        }
        LambdaQueryWrapper<RPanShareFile> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(RPanShareFile::getFileId);
        queryWrapper.eq(RPanShareFile::getShareId, shareId);
        List<Long> fileIdList = shareFileService.listObjs(queryWrapper, value -> (Long) value);
        return fileIdList;
    }
    /**
     * 查询分享的主体信息
     *
     * @param context
     */
    private void assembleMainShareInfo(QueryShareDetailContext context) {
        RPanShare record = context.getRecord();
        ShareDetailVO vo = context.getVo();
        vo.setShareId(record.getShareId());
        vo.setShareName(record.getShareName());
        vo.setCreateTime(record.getCreateTime());
        vo.setShareDay(record.getShareDay());
        vo.setShareEndTime(record.getShareEndTime());
    }

    /**
     * 初始化文件详情的VO实体
     *
     * @param context
     */
    private void initShareVo(QueryShareDetailContext context) {
        ShareDetailVO vo = new ShareDetailVO();
        context.setVo(vo);
    }

    /**
     * 生成一个短期的分享token
     *
     * @param context
     */
    private String generateShareToken(CheckShareCodeContext context) {
        RPanShare record = context.getRecord();
        String token = JwtUtil.generateToken(UUIDUtil.getUUID(), ShareConstant.SHARE_ID, record.getShareId(), ShareConstant.ONE_HOUR_LONG);
        return token;
    }

    /**
     * 校验分享码是否正确
     *
     * @param context
     */
    private void doCheckShareCode(CheckShareCodeContext context) {
        RPanShare record = context.getRecord();
        if (!Objects.equals(context.getShareCode(), record.getShareCode())) {
            throw new RPanBusinessException("分享码错误");
        }
    }

    /**
     * 检查分享的状态是否正常
     *
     * @param context
     */
    private RPanShare checkShareStatus(Long shareId) {
        RPanShare record = this.getById(shareId);
        if (ObjectUtil.isNull(record)) {
            throw new RPanBusinessException(ResponseCode.SHARE_CANCELLED);
        }
        if (Objects.equals(ShareStatusEnum.FILE_DELETED.getCode(), record.getShareStatus())) {
            throw new RPanBusinessException(ResponseCode.SHARE_FILE_MISS);
        }
        if (!Objects.equals(ShareDayTypeEnum.PERMANENT_VALIDITY.getCode(), record.getShareDayType())) {
            if (record.getShareEndTime().before(new Date())) {
                throw new RPanBusinessException(ResponseCode.SHARE_EXPIRE);
            }
        }
        return record;
    }

    /**
     * 取消文件和分享的关联关系数据
     *
     * @param context
     */
    private void doCancelShareFiles(CancelShareUrlContext context) {
        LambdaQueryWrapper<RPanShareFile> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(RPanShareFile::getShareId, context.getShareIdList());
        queryWrapper.eq(RPanShareFile::getCreateUser, context.getUserId());
        if (!shareFileService.remove(queryWrapper)) {
            throw new RPanBusinessException("取消文件分享失败");
        }
    }

    /**
     * 取消文件分享的动作
     *
     * @param context
     */
    private void doCancelShare(CancelShareUrlContext context) {
        List<Long> shareIdList = context.getShareIdList();
        if (!removeByIds(shareIdList)) {
            throw new RPanBusinessException("取消分享失败");
        }
    }

    /**
     * 检查用户是否拥有取消对应分享链接的权限
     *
     * @param context
     */
    private void checkUserCancelSharePermission(CancelShareUrlContext context) {
        List<Long> shareIdList = context.getShareIdList();
        Long userId = context.getUserId();
        List<RPanShare> records = listByIds(shareIdList);
        if (CollectionUtil.isEmpty(records)) {
            throw new RPanBusinessException("您无权限取消分享的动作");
        }
        records.forEach(record -> {
            if (!Objects.equals(record.getCreateUser(), userId)) {
                throw new RPanBusinessException("您无权限取消分享的动作");
            }
        });
    }

    /**
     * 拼装对应的返回VO
     *
     * @param context
     * @return
     */
    private RPanShareUrlVO assembleShareVO(CreateShareUrlContext context) {
        RPanShare record = context.getRecord();
        RPanShareUrlVO vo = new RPanShareUrlVO();
        vo.setShareName(record.getShareName());
        vo.setShareId(record.getShareId());
        vo.setShareStatus(record.getShareStatus());
        vo.setShareUrl(record.getShareUrl());
        vo.setShareCode(record.getShareCode());
        return vo;
    }

    /**
     * 保存分享和分享文件的关联关系
     *
     * @param context
     */
    private void saveShareFiles(CreateShareUrlContext context) {
        SaveShareFilesContext saveShareFilesContext = new SaveShareFilesContext();
        saveShareFilesContext.setUserId(context.getUserId());
        saveShareFilesContext.setShareId(context.getRecord().getShareId());
        saveShareFilesContext.setShareFileIdList(context.getShareFileIdList());
        shareFileService.saveShareFiles(saveShareFilesContext);
    }

    /**
     * 拼装分享的实体，并保存到数据库中
     *
     * @param context
     */
    private void saveShare(CreateShareUrlContext context) {
        RPanShare record = new RPanShare();
        record.setShareId(IdUtil.get());
        record.setShareName(context.getShareName());
        record.setShareType(context.getShareType());
        record.setShareDayType(context.getShareDayType());
        Integer shareDay = ShareDayTypeEnum.getShareDayByCode(context.getShareDayType());
        if (ObjectUtil.equal(shareDay, RPanConstants.MINUS_ONE_INT)) {
            throw new RPanBusinessException("无效的分享有效期");
        }
        record.setShareDay(shareDay);
        record.setShareEndTime(DateUtil.offsetDay(new Date(), shareDay));
        record.setShareUrl(createShareUrl(record.getShareId()));
        record.setShareCode(createShareCode());
        record.setShareStatus(ShareStatusEnum.NORMAL.getCode());
        record.setCreateUser(context.getUserId());
        record.setCreateTime(new Date());
        if (!this.save(record)) {
            throw new RPanBusinessException("保存分享信息失败");
        }
        context.setRecord(record);
    }

    /**
     * 创建分享的分享码
     *
     * @return
     */
    private String createShareCode() {
        return RandomUtil.randomString(4);
    }

    /**
     * 创建分享的url
     *
     * @param shareId
     * @return
     */
    private String createShareUrl(Long shareId) {
        if (Objects.isNull(shareId)) {
            throw new RPanBusinessException("分享的ID不能为空");
        }
        String sharePrefix = config.getSharePrefix();
        if (sharePrefix.lastIndexOf(RPanConstants.SLASH_STR) == RPanConstants.MINUS_ONE_INT) {
            sharePrefix += RPanConstants.SLASH_STR;
        }
        return sharePrefix + shareId;
    }

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
}




