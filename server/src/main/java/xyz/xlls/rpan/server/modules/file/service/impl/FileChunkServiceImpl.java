package xyz.xlls.rpan.server.modules.file.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import xyz.xlls.rpan.core.exception.RPanBusinessException;
import xyz.xlls.rpan.core.utils.IdUtil;
import xyz.xlls.rpan.server.common.config.PanServerConfig;
import xyz.xlls.rpan.server.modules.file.context.FileChunkSaveContext;
import xyz.xlls.rpan.server.modules.file.context.QueryUploadedChunksContext;
import xyz.xlls.rpan.server.modules.file.context.QueryUploadedChunksRecordContext;
import xyz.xlls.rpan.server.modules.file.converter.FileConverter;
import xyz.xlls.rpan.server.modules.file.entity.RPanFile;
import xyz.xlls.rpan.server.modules.file.entity.RPanFileChunk;
import xyz.xlls.rpan.server.modules.file.enums.MergeFlagEnum;
import xyz.xlls.rpan.server.modules.file.service.IFileChunkService;
import xyz.xlls.rpan.server.modules.file.mapper.RPanFileChunkMapper;
import org.springframework.stereotype.Service;
import xyz.xlls.rpan.storage.engine.core.context.StoreFileChunkContext;
import xyz.xlls.rpan.storage.engine.local.LocalStorageEngine;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
* @author Administrator
* @description 针对表【r_pan_file_chunk(文件分片信息表)】的数据库操作Service实现
* @createDate 2024-10-22 15:04:09
*/
@Service
public class FileChunkServiceImpl extends ServiceImpl<RPanFileChunkMapper, RPanFileChunk>
    implements IFileChunkService {
    @Autowired
    private PanServerConfig panServerConfig;
    @Autowired
    private LocalStorageEngine localStorageEngine;
    @Autowired
    private FileConverter fileConverter;

    /**
     * 文件分片保存
     * 1、保存文件分片和记录
     * 2、判断文件分片是否全部上传完成
     * @param fileChunkSaveContext
     */
    @Override
    public synchronized void saveChunkFile(FileChunkSaveContext fileChunkSaveContext) {
        doSaveChunkFile(fileChunkSaveContext);
        doJudgeMergeFile(fileChunkSaveContext);
    }

    /**
     *  查询已上传用户分片
     * @param context
     * @return
     */
    @Override
    public List<Integer> queryUploadedChunks(QueryUploadedChunksContext context) {
        LambdaQueryWrapper<RPanFileChunk> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(RPanFileChunk::getChunkNumber);
        queryWrapper.eq(RPanFileChunk::getIdentifier, context.getIdentifier());
        queryWrapper.eq(RPanFileChunk::getCreateUser, context.getUserId());
        queryWrapper.gt(RPanFileChunk::getExpirationTime, new Date());
        return listObjs(queryWrapper,value->(Integer) value);
    }

    @Override
    public List<RPanFileChunk> queryUploadedChunksRecord(QueryUploadedChunksRecordContext queryUploadedChunksContext) {
        LambdaQueryWrapper<RPanFileChunk> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RPanFileChunk::getIdentifier, queryUploadedChunksContext.getIdentifier());
        queryWrapper.eq(RPanFileChunk::getCreateUser, queryUploadedChunksContext.getUserId());
        queryWrapper.gt(RPanFileChunk::getExpirationTime, new Date());
        return this.list(queryWrapper);
    }

    /**
     * 判断是否所有的分片均上传完成
     * @param fileChunkSaveContext
     */
    private void doJudgeMergeFile(FileChunkSaveContext fileChunkSaveContext) {
        LambdaQueryWrapper<RPanFileChunk> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(RPanFileChunk::getIdentifier,fileChunkSaveContext.getIdentifier());
        queryWrapper.eq(RPanFileChunk::getCreateUser,fileChunkSaveContext.getUserId());
        int count=this.count(queryWrapper);
        if(count==fileChunkSaveContext.getTotalChunks()){
            fileChunkSaveContext.setMergeFlagEnum(MergeFlagEnum.READY);
        }
    }

    /**
     * 执行文件分片上传保存的操作
     * 1、委托文件存储引擎存储文件分片
     * 2、保存文件分片记录
     * @param fileChunkSaveContext
     */
    private void doSaveChunkFile(FileChunkSaveContext fileChunkSaveContext) {
        doStoreFileChuck(fileChunkSaveContext);
        doSaveRecord(fileChunkSaveContext);

    }

    /**
     * 保存文件分片记录
     * @param fileChunkSaveContext
     */
    private void doSaveRecord(FileChunkSaveContext fileChunkSaveContext) {
        RPanFileChunk record=new RPanFileChunk();
        record.setId(IdUtil.get());
        record.setIdentifier(fileChunkSaveContext.getIdentifier());
        record.setRealPath(fileChunkSaveContext.getRealPath());
        record.setChunkNumber(fileChunkSaveContext.getChunkNumber());
        record.setExpirationTime(DateUtil.offsetDay(new Date(),panServerConfig.getChunkFileExpirationDays()));
        record.setCreateUser(fileChunkSaveContext.getUserId());
        record.setCreateTime(new Date());
        if(!this.save(record)){
            throw new RPanBusinessException("文件分片上传失败");
        }
    }

    /**
     * 委托文件存储引擎保存文件分片
     * @param fileChunkSaveContext
     */
    private void doStoreFileChuck(FileChunkSaveContext fileChunkSaveContext) {
        try {
            StoreFileChunkContext context=fileConverter.fileChunkSaveContext2StoreFileChunkContext(fileChunkSaveContext);
            context.setInputStream(fileChunkSaveContext.getFile().getInputStream());
            localStorageEngine.storeChunk(context);
            fileChunkSaveContext.setRealPath(context.getRealPath());
        }catch (IOException e){
            e.printStackTrace();
            throw new RPanBusinessException("文件分片上传失败");
        }
    }
}




