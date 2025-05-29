package xyz.xlls.rpan.server.modules.file.converter;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import xyz.xlls.rpan.server.modules.file.context.CreateFolderContext;
import xyz.xlls.rpan.server.modules.file.context.UpdateFilenameContext;
import xyz.xlls.rpan.server.modules.file.entity.RPanUserFile;
import xyz.xlls.rpan.server.modules.file.po.CreateFolderPO;
import xyz.xlls.rpan.server.modules.file.po.UpdateFilenamePO;
import xyz.xlls.rpan.server.modules.user.context.*;
import xyz.xlls.rpan.server.modules.user.entity.RPanUser;
import xyz.xlls.rpan.server.modules.user.po.*;
import xyz.xlls.rpan.server.modules.user.vo.UserInfoVO;

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
}
