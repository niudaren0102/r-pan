package xyz.xlls.rpan.server.modules.user.converter;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import xyz.xlls.rpan.server.modules.user.context.UserLoginContext;
import xyz.xlls.rpan.server.modules.user.context.UserRegisterContext;
import xyz.xlls.rpan.server.modules.user.entity.RPanUser;
import xyz.xlls.rpan.server.modules.user.po.UserLoginPO;
import xyz.xlls.rpan.server.modules.user.po.UserRegisterPO;

/**
 * 用户
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserConverter {
    /**
     * UserRegisterPO转化成UserRegisterContext
     * @param registerPO
     * @return
     */
    UserRegisterContext registerPO2UserRegisterContext(UserRegisterPO registerPO);

    /**
     * userRegisterContext转化成RPanUser
     * @param userRegisterContext
     * @return
     */
    @Mapping(target = "password", ignore = true)
    RPanUser serRegisterContext2RPanUser(UserRegisterContext userRegisterContext);

    /**
     * userLoginPO转化成UserLoginContext
     * @param userLoginPO
     * @return
     */
    UserLoginContext userLoginPO2UserLoginContext(UserLoginPO userLoginPO);
}
