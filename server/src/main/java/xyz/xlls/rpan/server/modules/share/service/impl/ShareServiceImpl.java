package xyz.xlls.rpan.server.modules.share.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import xyz.xlls.rpan.core.constants.RPanConstants;
import xyz.xlls.rpan.core.exception.RPanBusinessException;
import xyz.xlls.rpan.core.utils.IdUtil;
import xyz.xlls.rpan.server.common.config.PanServerConfig;
import xyz.xlls.rpan.server.modules.share.context.CancelShareUrlContext;
import xyz.xlls.rpan.server.modules.share.context.CreateShareUrlContext;
import xyz.xlls.rpan.server.modules.share.context.QueryShareUrlListContext;
import xyz.xlls.rpan.server.modules.share.context.SaveShareFilesContext;
import xyz.xlls.rpan.server.modules.share.entity.RPanShare;
import xyz.xlls.rpan.server.modules.share.entity.RPanShareFile;
import xyz.xlls.rpan.server.modules.share.enums.ShareDayTypeEnum;
import xyz.xlls.rpan.server.modules.share.enums.ShareStatusEnum;
import xyz.xlls.rpan.server.modules.share.service.IShareFileService;
import xyz.xlls.rpan.server.modules.share.service.IShareService;
import xyz.xlls.rpan.server.modules.share.mapper.RPanShareMapper;
import org.springframework.stereotype.Service;
import xyz.xlls.rpan.server.modules.share.vo.RPanShareUrlListVO;
import xyz.xlls.rpan.server.modules.share.vo.RPanShareUrlVO;

import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @author Administrator
 * @description 针对表【r_pan_share(用户分享表)】的数据库操作Service实现
 * @createDate 2024-10-22 15:08:03
 */
@Service
public class ShareServiceImpl extends ServiceImpl<RPanShareMapper, RPanShare>
        implements IShareService {
    @Autowired
    private PanServerConfig config;
    @Autowired
    private IShareFileService shareFileService;

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
     * @param context
     */
    @Transactional(rollbackFor = RPanBusinessException.class)
    @Override
    public void cancelShare(CancelShareUrlContext context) {
        checkUserCancelSharePermission(context);
        doCancelShare( context);
        doCancelShareFiles(context);

    }

    /**
     * 取消文件和分享的关联关系数据
     * @param context
     */
    private void doCancelShareFiles(CancelShareUrlContext context) {
        LambdaQueryWrapper<RPanShareFile> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(RPanShareFile::getShareId,context.getShareIdList());
        queryWrapper.eq(RPanShareFile::getCreateUser,context.getUserId());
        if(!shareFileService.remove(queryWrapper)){
            throw new RPanBusinessException("取消文件分享失败");
        }
    }

    /**
     * 取消文件分享的动作
     * @param context
     */
    private void doCancelShare(CancelShareUrlContext context) {
        List<Long> shareIdList = context.getShareIdList();
        if(!removeByIds(shareIdList)){
            throw new RPanBusinessException("取消分享失败");
        }
    }

    /**
     * 检查用户是否拥有取消对应分享链接的权限
     * @param context
     */
    private void checkUserCancelSharePermission(CancelShareUrlContext context) {
        List<Long> shareIdList = context.getShareIdList();
        Long userId = context.getUserId();
        List<RPanShare> records = listByIds(shareIdList);
        if(CollectionUtil.isEmpty( records)){
            throw new RPanBusinessException("您无权限取消分享的动作");
        }
       records.forEach(record->{
           if(!Objects.equals(record.getCreateUser(),userId)){
               throw new RPanBusinessException("您无权限取消分享的动作");
           }
       });
    }

    /**
     * 拼装对应的返回VO
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
        if(!this.save(record)){
            throw new RPanBusinessException("保存分享信息失败");
        }
        context.setRecord(record);
    }

    /**
     * 创建分享的分享码
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
}




