package xyz.xlls.rpan.server.modules.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.dao.DuplicateKeyException;
import xyz.xlls.rpan.cache.core.constants.CacheConstants;
import xyz.xlls.rpan.core.exception.RPanBusinessException;
import xyz.xlls.rpan.core.response.ResponseCode;
import xyz.xlls.rpan.core.utils.IdUtil;
import xyz.xlls.rpan.core.utils.JwtUtil;
import xyz.xlls.rpan.core.utils.PasswordUtil;
import xyz.xlls.rpan.server.modules.file.constants.FileConstants;
import xyz.xlls.rpan.server.modules.file.context.CreateFolderContext;
import xyz.xlls.rpan.server.modules.file.entity.RPanUserFile;
import xyz.xlls.rpan.server.modules.file.service.IUserFileService;
import xyz.xlls.rpan.server.modules.user.constants.UserConstants;
import xyz.xlls.rpan.server.modules.user.context.*;
import xyz.xlls.rpan.server.modules.user.converter.UserConverter;
import xyz.xlls.rpan.server.modules.user.entity.RPanUser;
import xyz.xlls.rpan.server.modules.user.service.IUserService;
import xyz.xlls.rpan.server.modules.user.mapper.RPanUserMapper;
import org.springframework.stereotype.Service;
import xyz.xlls.rpan.server.modules.user.vo.UserInfoVO;

import java.util.Date;
import java.util.Objects;

/**
* @author Administrator
* @description 针对表【r_pan_user(用户信息表)】的数据库操作Service实现
* @createDate 2024-10-22 14:59:10
*/
@Service
public class UserServiceImpl extends ServiceImpl<RPanUserMapper, RPanUser>
    implements IUserService {
    @Autowired
    private UserConverter userConverter;
    @Autowired
    private IUserFileService userFileService;
    @Autowired
    private CacheManager cacheManager;
    /**
     * 用户注册的业务实现
     * 需要实现的功能点
     * 1、注册用户信息
     * 2、创建新用户的根本目录信息
     *
     * 需要实现的技术难点
     * 1、该业务是幂等的
     * 2、要保证用户名全局唯一
     *
     * 实现技术难点的处理方案
     * 1、幂等性通过数据库表对用户名字段添加唯一索引，我们上游业务捕获对应的冲突异常，转化返回
     * @param userRegisterContext
     * @return
     */
    @Override
    public Long register(UserRegisterContext userRegisterContext) {
        assembleUserEntity(userRegisterContext);
        doRegister(userRegisterContext);
        createUserRootFolder(userRegisterContext);
        return userRegisterContext.getEntity().getUserId();
    }

    /**
     * 用户登陆业务实现
     * 需要实现的功能：
     * 1、用户的登录信息校验
     * 2、生成一个具有实效性的accessToken
     * 3、将accessToken缓存起来、去实现单机登录
     *
     * @param userLoginContext
     * @return
     */
    @Override
    public String login(UserLoginContext userLoginContext) {
        checkLoginInfo(userLoginContext);
        generateAndSaveAccessToken(userLoginContext);
        return userLoginContext.getAccessToken();
    }

    /**
     * 用户退出登录
     * 1、清空用户的登录凭证缓存
     * @param userId
     */
    @Override
    public void exit(Long userId) {
        try{
            Cache cache = cacheManager.getCache(CacheConstants.R_PAN_CACHE_NAME);
            cache.evict(UserConstants.USER_LOGIN_PREFIX + userId);
        }catch (Exception e){
            e.printStackTrace();
            throw new RPanBusinessException("用户退出登录失败");
        }
    }

    /**
     * 用户忘记密码-校验用户名称
     * @param checkUsernameContext
     * @return
     */
    @Override
    public String checkUsername(CheckUsernameContext checkUsernameContext) {
        String question=baseMapper.selectQuestionByUsername(checkUsernameContext.getUsername());
        if(StringUtils.isBlank(question)){
            throw new RPanBusinessException("没有此用户");
        }
        return question;
    }

    /**
     * 用户忘记密码-校验密保答案
     * @param checkAnswerContext
     * @return
     */
    @Override
    public String checkAnswer(CheckAnswerContext checkAnswerContext) {
        LambdaQueryWrapper<RPanUser> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(RPanUser::getUsername,checkAnswerContext.getUsername());
        queryWrapper.eq(RPanUser::getQuestion,checkAnswerContext.getQuestion());
        queryWrapper.eq(RPanUser::getAnswer,checkAnswerContext.getAnswer());
        int count = this.count(queryWrapper);
        if(count==0){
            throw new RPanBusinessException("密保答案错误");
        }
        return generateAndSaveCheckAnswerToken(checkAnswerContext);
    }

    /**
     * 重置用户密码
     * 1、校验token是不是有效
     * 2、重置密码
     * @param resetPasswordContext
     */
    @Override
    public void resetPassword(ResetPasswordContext resetPasswordContext) {
        checkForgetPasswordToken(resetPasswordContext);
        checkAndResetUserPassword(resetPasswordContext);
    }

    /**
     *  在线修改密码
     *  1、校验旧密码
     *  2、重置新密码
     *  3、退出当前的登录状态
     * @param changePasswordContext
     */
    @Override
    public void changePassword(ChangePasswordContext changePasswordContext) {
        checkOldPassword(changePasswordContext);
        doChangePassword(changePasswordContext);
        exitLoginStatus(changePasswordContext);
    }

    /**
     * 查询在线用户的基本信息
     * 1、查询用户的基本信息实体
     * 2、查询用户的根文件夹信息
     * 3、拼装VO对象返回
     * @param userId
     * @return
     */
    @Override
    public UserInfoVO info(Long userId) {
        RPanUser entity = this.getById(userId);
        if(Objects.isNull(entity)){
            throw new RPanBusinessException("用户信息查询失败");
        }
        RPanUserFile rPanUserFile=getUserRootFileInfo(userId);
        if(Objects.isNull(rPanUserFile)){
            throw new RPanBusinessException("查询用户跟文件夹信息失败");
        }
        return userConverter.assembleUserInfoVO(entity,rPanUserFile);
    }

    /**
     * 获取用户根目录信息
     * @param userId
     * @return
     */
    private RPanUserFile getUserRootFileInfo(Long userId) {
        return userFileService.getUserRootFile(userId);
    }

    /**
     * 退出用户的登录状态
     * @param changePasswordContext
     */
    private void exitLoginStatus(ChangePasswordContext changePasswordContext) {
        this.exit(changePasswordContext.getUserId());
    }

    /**
     * 修改新密码
     * @param changePasswordContext
     */
    private void doChangePassword(ChangePasswordContext changePasswordContext) {
        String newPassword = changePasswordContext.getNewPassword();
        RPanUser entity = changePasswordContext.getEntity();
        String salt = entity.getSalt();
        String encryptNewPassword = PasswordUtil.encryptPassword(salt,newPassword);
        entity.setPassword(encryptNewPassword);
        entity.setUpdateTime(new Date());
        boolean result = this.updateById(entity);
        if(!result){
            throw new RPanBusinessException("修改用户密码失败");
        }
    }

    /**
     * 校验用户的旧密码
     * 该步骤会查询并封装用户的实体信息到上下文对象中
     * @param changePasswordContext
     */
    private void checkOldPassword(ChangePasswordContext changePasswordContext) {
        Long userId = changePasswordContext.getUserId();
        String oldPassword = changePasswordContext.getOldPassword();
        RPanUser entity = this.getById(userId);
        if(Objects.isNull(entity)){
            throw new RPanBusinessException("用户信息不存在");
        }
        changePasswordContext.setEntity(entity);
        String encryptedOldPassword = PasswordUtil.encryptPassword(entity.getSalt(),  oldPassword);
        String dbEncryptedPassword = entity.getPassword();
        if(!Objects.equals(encryptedOldPassword,dbEncryptedPassword)){
            throw new RPanBusinessException("旧密码不正确");
        }
    }

    /**
     * 校验用户信息并重置密码
     * @param resetPasswordContext
     */
    private void checkAndResetUserPassword(ResetPasswordContext resetPasswordContext) {
        String username = resetPasswordContext.getUsername();
        String password=resetPasswordContext.getPassword();
        RPanUser entity = getRPanUserByUsername(username);
        if(Objects.isNull(entity)){
            throw new RPanBusinessException("用户信息不存在");
        }
        String newDbPassword=PasswordUtil.encryptPassword(entity.getSalt(),password);
        entity.setPassword(newDbPassword);
        entity.setUpdateTime(new Date());
        boolean result = this.updateById(entity);
        if(!result){
            throw new RPanBusinessException("重置用户密码失败");
        }
    }

    private void checkForgetPasswordToken(ResetPasswordContext resetPasswordContext) {
        String token = resetPasswordContext.getToken();
        Object value  = JwtUtil.analyzeToken(token, UserConstants.FORGET_USERNAME);
        if(Objects.isNull(value)){
            throw new RPanBusinessException(ResponseCode.TOKEN_EXPIRE);
        }
        String tokenUsername=String.valueOf(value);
        if(!Objects.equals(tokenUsername, resetPasswordContext.getUsername())){
            throw new RPanBusinessException("token错误");
        }
    }

    /**
     * 生成用户忘记密码-校验密保答案通过的临时token
     * token的实效时间为五分钟之后
     * @param checkAnswerContext
     * @return
     */
    private String generateAndSaveCheckAnswerToken(CheckAnswerContext checkAnswerContext) {
        String token=JwtUtil.generateToken(checkAnswerContext.getUsername(),UserConstants.FORGET_USERNAME,checkAnswerContext.getUsername(),UserConstants.FIVE_MINUTES_LONG);
        return token;
    }

    /**
     * 生成并保存登录之后的凭证
     *
     * @param userLoginContext
     */
    private void generateAndSaveAccessToken(UserLoginContext userLoginContext) {
        RPanUser entity = userLoginContext.getEntity();
        String accessToken = JwtUtil.generateToken(entity.getUsername(), UserConstants.LOGIN_USER_ID, entity.getUserId(), UserConstants.ONE_DAY_LONG);
        Cache cache = cacheManager.getCache(CacheConstants.R_PAN_CACHE_NAME);
        cache.put(UserConstants.USER_LOGIN_PREFIX+entity.getUserId(), accessToken);
        userLoginContext.setAccessToken(accessToken);
    }

    /**
     * 校验用户名密码
     * @param userLoginContext
     */
    private void checkLoginInfo(UserLoginContext userLoginContext) {
        String username = userLoginContext.getUsername();
        String password = userLoginContext.getPassword();
        RPanUser entity=getRPanUserByUsername(username);
        if(Objects.isNull(entity)){
            throw new RPanBusinessException("用户名不存在");
        }
        String salt = entity.getSalt();
        String encryptPassword = PasswordUtil.encryptPassword(salt, password);
        String dbPassword=entity.getPassword();
        if(!Objects.equals(encryptPassword,dbPassword)){
            throw new RPanBusinessException("密码信息不正确");
        }
        userLoginContext.setEntity(entity);
    }

    /**
     * 根据用户名获取用户实体信息
     * @param username
     * @return
     */
    private RPanUser getRPanUserByUsername(String username) {
        LambdaQueryWrapper<RPanUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RPanUser::getUsername, username);
        return getOne(queryWrapper);
    }

    /**
     * 创建用户的根目录信息
     * @param userRegisterContext
     */
    private void createUserRootFolder(UserRegisterContext userRegisterContext) {
        CreateFolderContext createFolderContext = new CreateFolderContext();
        createFolderContext.setParentId(FileConstants.TOP_PARENT_ID);
        createFolderContext.setUserId(userRegisterContext.getEntity().getUserId());
        createFolderContext.setFolderName(FileConstants.ALL_FILE_CN_STR);
        userFileService.createFolder(createFolderContext);
    }

    /**
     * 实现注册用户的业务
     * 需要捕获数据库的唯一索引冲突异常，来实现全局用户名称唯一
     * @param userRegisterContext
     */
    private void doRegister(UserRegisterContext userRegisterContext)  {
        RPanUser entity = userRegisterContext.getEntity();
        if(Objects.nonNull(entity)) {
            try{
                if(!save(entity)){
                    throw new RPanBusinessException("用户注册失败");
                }
            }catch (DuplicateKeyException  duplicateKeyException){
                throw new RPanBusinessException("用户名已存在");
            }
            return;
        }
        throw new RPanBusinessException(ResponseCode.ERROR);
    }

    /**
     * 实体转化
     * 由上下文信息转化成用户实体，封装进上下文
     * @param userRegisterContext
     * @return
     */
    private void assembleUserEntity(UserRegisterContext userRegisterContext) {
        RPanUser entity=userConverter.serRegisterContext2RPanUser(userRegisterContext);
        String salt = PasswordUtil.getSalt();
        String dbPassword = PasswordUtil.encryptPassword(salt, userRegisterContext.getPassword());
        entity.setUserId(IdUtil.get());
        entity.setSalt(salt);
        entity.setPassword(dbPassword);
        entity.setCreateTime(new Date());
        entity.setUpdateTime(new Date());
        userRegisterContext.setEntity(entity);
    }
}




