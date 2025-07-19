package xyz.xlls.rpan.server.modules.file.converter;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import xyz.xlls.rpan.server.modules.file.context.*;
import xyz.xlls.rpan.server.modules.file.entity.RPanUserFile;
import xyz.xlls.rpan.server.modules.file.po.*;
import xyz.xlls.rpan.server.modules.file.vo.FolderTreeNodeVO;
import xyz.xlls.rpan.server.modules.file.vo.RPanUserFileVO;
import xyz.xlls.rpan.storage.engine.core.context.StoreFileChunkContext;

/**
 * 文件模块实体转化工具
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface FileConverter {
    @Mapping(target = "parentId",expression = "java(xyz.xlls.rpan.core.utils.IdUtil.decrypt(createFolderPO.getParentId()))")
    @Mapping(target = "userId",expression = "java(xyz.xlls.rpan.server.common.utils.UserIdUtil.get())")
    CreateFolderContext createFolderPO2CreateFolderContext(CreateFolderPO createFolderPO);
    @Mapping(target = "fileId",expression = "java(xyz.xlls.rpan.core.utils.IdUtil.decrypt(updateFilenamePO.getFileId()))")
    @Mapping(target = "userId",expression = "java(xyz.xlls.rpan.server.common.utils.UserIdUtil.get())")
    UpdateFilenameContext updateFilenamePO2UpdateFilenameContext(UpdateFilenamePO updateFilenamePO);
    @Mapping(target = "userId",expression = "java(xyz.xlls.rpan.server.common.utils.UserIdUtil.get())")
    DeleteFileContext deleteFilePO2DeleteFileContext(DeleteFilePO deleteFilePO);
    @Mapping(target = "parentId",expression = "java(xyz.xlls.rpan.core.utils.IdUtil.decrypt(secUploadPO.getParentId()))")
    @Mapping(target = "userId",expression = "java(xyz.xlls.rpan.server.common.utils.UserIdUtil.get())")
    SecUploadContext secUploadPO2SecUploadContext(SecUploadPO secUploadPO);
    @Mapping(target = "parentId",expression = "java(xyz.xlls.rpan.core.utils.IdUtil.decrypt(fileUploadPO.getParentId()))")
    @Mapping(target = "userId",expression = "java(xyz.xlls.rpan.server.common.utils.UserIdUtil.get())")
    FileUploadContext fileUploadPO2FileUploadContext(FileUploadPO fileUploadPO);
    @Mapping(target = "record",ignore = true)
    FileSaveContext fileUploadContext2FileSaveContext(FileUploadContext context);
    @Mapping(target = "userId",expression = "java(xyz.xlls.rpan.server.common.utils.UserIdUtil.get())")
    FileChunkUploadContext chunkUploadPO2ChunkUploadContext(FileChunkUploadPO fileChunkUploadPO);

    FileChunkSaveContext fileChunkUploadContext2FileChunkSaveContext(FileChunkUploadContext context);
    @Mapping(target = "realPath",ignore = true)
    StoreFileChunkContext fileChunkSaveContext2StoreFileChunkContext(FileChunkSaveContext fileChunkSaveContext);
    @Mapping(target = "userId",expression = "java(xyz.xlls.rpan.server.common.utils.UserIdUtil.get())")
    QueryUploadedChunksContext queryUploadedChunksPO2QueryUploadedChunksContext(QueryUploadedChunksPO queryUploadedChunksPO);
    @Mapping(target = "userId",expression = "java(xyz.xlls.rpan.server.common.utils.UserIdUtil.get())")
    @Mapping(target = "parentId",expression = "java(xyz.xlls.rpan.core.utils.IdUtil.decrypt(fileChunkMergePO.getParentId()))")
    FileChunkMergeContext fileChunkMergePO2FileChunkMergeContext(FileChunkMergePO fileChunkMergePO);

    FileChunkMergeAndSaveContext fileChunkMergeContext2FileChunkMergeAndSaveContext(FileChunkMergeContext context);

    QueryUploadedChunksRecordContext fileChunkMergeAndSaveContext2QueryUploadedChunksRecordContext(FileChunkMergeAndSaveContext fileChunkMergeAndSaveContext);
    @Mapping(target = "label",source = "record.filename")
    @Mapping(target = "id",source = "record.fileId")
    @Mapping(target = "children",expression = "java(org.assertj.core.util.Lists.newArrayList())")
    FolderTreeNodeVO rPanUserFile2FolderTreeNodeVO(RPanUserFile record);
    RPanUserFileVO rPanUserFile2RPanUserFileVO(RPanUserFile record);
}
