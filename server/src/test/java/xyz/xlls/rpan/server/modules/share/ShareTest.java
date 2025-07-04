package xyz.xlls.rpan.server.modules.share;

import cn.hutool.core.lang.Assert;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import xyz.xlls.rpan.server.RPanServerLauncher;
import xyz.xlls.rpan.server.modules.file.context.CreateFolderContext;
import xyz.xlls.rpan.server.modules.file.service.IUserFileService;
import xyz.xlls.rpan.server.modules.share.context.CreateShareUrlContext;
import xyz.xlls.rpan.server.modules.share.context.QueryShareUrlListContext;
import xyz.xlls.rpan.server.modules.share.enums.ShareDayTypeEnum;
import xyz.xlls.rpan.server.modules.share.enums.ShareTypeEnum;
import xyz.xlls.rpan.server.modules.share.service.IShareService;
import xyz.xlls.rpan.server.modules.share.vo.RPanShareUrlListVO;
import xyz.xlls.rpan.server.modules.share.vo.RPanShareUrlVO;
import xyz.xlls.rpan.server.modules.user.context.UserLoginContext;
import xyz.xlls.rpan.server.modules.user.context.UserRegisterContext;
import xyz.xlls.rpan.server.modules.user.service.IUserService;
import xyz.xlls.rpan.server.modules.user.vo.UserInfoVO;

import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = RPanServerLauncher.class)
@Transactional
public class ShareTest {
    @Autowired
    private IUserService userService;
    @Autowired
    private IUserFileService userFileService;
    @Autowired
    private IShareService shareService;

    /**
     * 创建分享链接成功
     */
    @Test
    public void testCreateShareUrlSuccess(){
        Long userId = register();
        UserInfoVO info = info(userId);
        //创建一个文件夹
        CreateFolderContext context=new CreateFolderContext();
        context.setParentId(info.getRootFileId());
        context.setUserId(userId);
        context.setFolderName("test");
        Long fileId = userFileService.createFolder(context);
        Assert.notNull(fileId);
        CreateShareUrlContext createShareUrlContext=new CreateShareUrlContext();
        createShareUrlContext.setShareName("test");
        createShareUrlContext.setShareType(ShareTypeEnum.NEED_SHARE_CODE.getCode());
        createShareUrlContext.setShareDayType(ShareDayTypeEnum.SEVEN_DAYS_VALIDITY.getCode());
        createShareUrlContext.setShareFileIdList(Lists.newArrayList(fileId));
        createShareUrlContext.setUserId(userId);
        RPanShareUrlVO vo = shareService.create(createShareUrlContext);
        Assert.notNull(vo);
    }
    /**
     * 查询分享链接列表成功
     */
    @Test
    public void testQueryShareUrlListSuccess(){
        Long userId = register();
        UserInfoVO info = info(userId);
        //创建一个文件夹
        CreateFolderContext context=new CreateFolderContext();
        context.setParentId(info.getRootFileId());
        context.setUserId(userId);
        context.setFolderName("test");
        Long fileId = userFileService.createFolder(context);
        Assert.notNull(fileId);
        CreateShareUrlContext createShareUrlContext=new CreateShareUrlContext();
        createShareUrlContext.setShareName("test");
        createShareUrlContext.setShareType(ShareTypeEnum.NEED_SHARE_CODE.getCode());
        createShareUrlContext.setShareDayType(ShareDayTypeEnum.SEVEN_DAYS_VALIDITY.getCode());
        createShareUrlContext.setShareFileIdList(Lists.newArrayList(fileId));
        createShareUrlContext.setUserId(userId);
        RPanShareUrlVO vo = shareService.create(createShareUrlContext);
        Assert.notNull(vo);

        QueryShareUrlListContext queryShareUrlListContext = new QueryShareUrlListContext();
        queryShareUrlListContext.setUserId(userId);
        List<RPanShareUrlListVO> result = shareService.getShares(queryShareUrlListContext);
        Assert.notEmpty(result);
        Assert.isTrue(result.size()==1);
    }
    /**
     * 用户注册
     * @return 新用户的id
     */
    private Long register(){
        UserRegisterContext context = createUserRegisterContext();
        Long register = userService.register(context);
        Assert.isTrue(register >0L);
        return register;
    }

    /**
     * 查询登录用户的基本信息
     * @param userId
     * @return
     */
    private UserInfoVO info(Long userId){
        UserInfoVO info = userService.info(userId);
        Assert.notNull(info);
        return info;
    }
    /**
     * 构建注册用户上下文信息
     * @return
     */
    private UserRegisterContext createUserRegisterContext() {
        UserRegisterContext context=new UserRegisterContext();
        context.setUsername("admin");
        context.setPassword("123456");
        context.setQuestion("test");
        context.setAnswer("test");
        return context;

    }

    /**
     * 构建登录用户上下文信息
     * @return
     */
    private UserLoginContext createUserLoginContext() {
        UserLoginContext context=new UserLoginContext();
        context.setUsername("admin");
        context.setPassword("123456");
        return context;

    }
}
