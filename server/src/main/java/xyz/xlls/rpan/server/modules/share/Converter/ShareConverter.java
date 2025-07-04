package xyz.xlls.rpan.server.modules.share.Converter;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import xyz.xlls.rpan.server.modules.share.context.CreateShareUrlContext;
import xyz.xlls.rpan.server.modules.share.po.CreateShareUrlPO;

/**
 * 文件分享模块实体转化工具类
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ShareConverter {
    @Mapping(target = "userId",expression = "java(xyz.xlls.rpan.server.common.utils.UserIdUtil.get())")
    CreateShareUrlContext createShareUrlPO2CreateShareUrlContext(CreateShareUrlPO createShareUrlPO);
}
