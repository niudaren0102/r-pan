package xyz.xlls.rpan.server.modules.recycle.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;
import xyz.xlls.rpan.core.constants.RPanConstants;
import xyz.xlls.rpan.core.exception.RPanBusinessException;
import xyz.xlls.rpan.server.common.event.file.FileRestoreEvent;
import xyz.xlls.rpan.server.modules.file.context.QueryFileContext;
import xyz.xlls.rpan.server.modules.file.entity.RPanUserFile;
import xyz.xlls.rpan.server.modules.file.enums.DelFlagEnum;
import xyz.xlls.rpan.server.modules.file.service.IUserFileService;
import xyz.xlls.rpan.server.modules.file.service.impl.UserFileServiceImpl;
import xyz.xlls.rpan.server.modules.file.vo.RPanUserFileVO;
import xyz.xlls.rpan.server.modules.recycle.context.QueryRecycleFileListContext;
import xyz.xlls.rpan.server.modules.recycle.context.RestoreContext;
import xyz.xlls.rpan.server.modules.recycle.service.IRecycleService;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 回收站模块业务处理类
 */
@Service
public class RecycleServiceImpl implements IRecycleService, ApplicationContextAware {
    @Autowired
    private IUserFileService userFileService;
    private ApplicationContext applicationContext;

    /**
     * 查询用户的回收站文件列表
     *
     * @param context
     * @return
     */
    @Override
    public List<RPanUserFileVO> recycles(QueryRecycleFileListContext context) {
        QueryFileContext fileContext = new QueryFileContext();
        fileContext.setUserId(context.getUserId());
        fileContext.setDelFlag(DelFlagEnum.YES.getCode());
        return userFileService.getFileList(fileContext);
    }

    /**
     * 文件还原
     * 1、检查操作权限
     * 2、检查是不是可以还原
     * 3、执行文件还原的操作
     * 4、执行文件还原的后置操作
     *
     * @param context
     */
    @Override
    public void restore(RestoreContext context) {
        CheckRestorePermission(context);
        CheckRestoreFilename(context);
        doRestore(context);
        afterRestore(context);
    }

    /**
     * 文件还原的后置操作
     * 1、发布文件还原事件
     * @param context
     */
    private void afterRestore(RestoreContext context) {
        FileRestoreEvent restoreEvent=new FileRestoreEvent(this,context.getFileIdList());
        applicationContext.publishEvent(restoreEvent);
    }

    /**
     * 执行文件还原的动作
     *
     * @param context
     */
    private void doRestore(RestoreContext context) {
        List<RPanUserFile> records = context.getRecords();
        records.forEach(record -> {
            record.setDelFlag(DelFlagEnum.NO.getCode());
            record.setUpdateUser(context.getUserId());
            record.setUpdateTime(new Date());
        });
        boolean updateFlag = userFileService.updateBatchById(records);
        if (!updateFlag) {
            throw new RPanBusinessException("文件还原失败");
        }
    }

    /**
     * 检查要还原的文件名称是不是被占用
     * 1、要还原的文件列表中有同一文件夹下面相同名称的文件 不允许还原
     * 2、要还原的文件当前父文件夹下存在有同名文件 不允许还原
     *
     * @param context
     */
    private void CheckRestoreFilename(RestoreContext context) {
        List<RPanUserFile> records = context.getRecords();
        Set<String> filenameSet = records.stream().map(record -> record.getFilename() + RPanConstants.COMMON_SEPARATOR + record.getParentId()).collect(Collectors.toSet());
        if (filenameSet.size() != records.size()) {
            throw new RPanBusinessException("文件还原失败，该还原文件中存在同名文件，请逐个还原并重命名");
        }
        for (RPanUserFile record : records) {
            LambdaQueryWrapper<RPanUserFile> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(RPanUserFile::getUserId, context.getUserId());
            queryWrapper.eq(RPanUserFile::getParentId, record.getParentId());
            queryWrapper.eq(RPanUserFile::getFilename, record.getFilename());
            queryWrapper.eq(RPanUserFile::getDelFlag, DelFlagEnum.NO.getCode());
            if (userFileService.count(queryWrapper) > 0) {
                throw new RPanBusinessException("文件" + record.getFilename() + "还原失败，该文件夹下面已经存在了相同名称的文件或文件夹，请重命名后再执行文件还原");
            }
        }
    }

    /**
     * 检查文件还原的操作权限
     *
     * @param context
     */
    private void CheckRestorePermission(RestoreContext context) {
        List<Long> fileIdList = context.getFileIdList();
        List<RPanUserFile> records = userFileService.listByIds(fileIdList);
        if (CollectionUtil.isEmpty(records)) {
            throw new RPanBusinessException("文件还原失败");
        }
        Set<Long> userIdSet = records.stream().map(RPanUserFile::getUserId).collect(Collectors.toSet());
        if (userIdSet.size() > 1) {
            throw new RPanBusinessException("你无权执行文件还原");
        }
        if (!userIdSet.contains(context.getUserId())) {
            throw new RPanBusinessException("你无权执行文件还原");
        }
        context.setRecords(records);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext=applicationContext;
    }
}
