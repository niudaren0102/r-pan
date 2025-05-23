package xyz.xlls.rpan.server.modules.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
import xyz.xlls.rpan.server.modules.file.service.IUserFileService;
import xyz.xlls.rpan.server.modules.user.constants.UserConstants;
import xyz.xlls.rpan.server.modules.user.context.UserLoginContext;
import xyz.xlls.rpan.server.modules.user.context.UserRegisterContext;
import xyz.xlls.rpan.server.modules.user.converter.UserConverter;
import xyz.xlls.rpan.server.modules.user.entity.RPanUser;
import xyz.xlls.rpan.server.modules.user.service.IUserService;
import xyz.xlls.rpan.server.modules.user.mapper.RPanUserMapper;
import org.springframework.stereotype.Service;

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
     * 生成并保存登录之后的凭证
     *
     * @param userLoginContext
     */
    private void generateAndSaveAccessToken(UserLoginContext userLoginContext) {
        RPanUser entity = userLoginContext.getEntity();
        String accessToken = JwtUtil.generateToken(entity.getUsername(), UserConstants.LOGIN_USER_ID, entity.getUserId(), UserConstants.ONE_DAY_LONG);
        Cache cache = cacheManager.getCache(CacheConstants.R_PAN_CACHE_NAME);
        cache.put(UserConstants.USER_LOGIN_PREFIX, accessToken);
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
        if(Objects.equals(encryptPassword,dbPassword)){
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
    private void doRegister(UserRegisterContext userRegisterContext) {
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




