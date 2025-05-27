package xyz.xlls.rpan.server.modules.user.converter;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import xyz.xlls.rpan.server.modules.user.context.*;
import xyz.xlls.rpan.server.modules.user.entity.RPanUser;
import xyz.xlls.rpan.server.modules.user.po.*;

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

    /**
     * CheckUsernamePO转CheckUsernameContext
     * @param checkUsernamePO
     * @return
     */
    CheckUsernameContext checkUsernamePO2CheckUsernameContext(CheckUsernamePO checkUsernamePO);

    /**
     * CheckAnswerPO转checkAnswerContext
     * @param checkAnswerPO
     * @return
     */
    CheckAnswerContext checkAnswerPO2CheckAnswerContext(CheckAnswerPO checkAnswerPO);

    /**
     * ResetPasswordPO转ResetPasswordContext
     * @param resetPasswordPO
     * @return
     */
    ResetPasswordContext resetPasswordPO2ResetPasswordContext(ResetPasswordPO resetPasswordPO);
}
