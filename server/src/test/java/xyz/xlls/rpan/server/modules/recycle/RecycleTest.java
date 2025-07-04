package xyz.xlls.rpan.server.modules.recycle;

import cn.hutool.core.lang.Assert;
import com.google.common.collect.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import xyz.xlls.rpan.core.exception.RPanBusinessException;
import xyz.xlls.rpan.server.RPanServerLauncher;
import xyz.xlls.rpan.server.modules.file.context.CreateFolderContext;
import xyz.xlls.rpan.server.modules.file.context.DeleteFileContext;
import xyz.xlls.rpan.server.modules.file.service.IUserFileService;
import xyz.xlls.rpan.server.modules.file.vo.RPanUserFileVO;
import xyz.xlls.rpan.server.modules.recycle.context.DeleteContext;
import xyz.xlls.rpan.server.modules.recycle.context.QueryRecycleFileListContext;
import xyz.xlls.rpan.server.modules.recycle.context.RestoreContext;
import xyz.xlls.rpan.server.modules.recycle.service.IRecycleService;
import xyz.xlls.rpan.server.modules.user.context.UserLoginContext;
import xyz.xlls.rpan.server.modules.user.context.UserRegisterContext;
import xyz.xlls.rpan.server.modules.user.service.IUserService;
import xyz.xlls.rpan.server.modules.user.vo.UserInfoVO;

import java.util.ArrayList;
import java.util.List;

/**
 * 回收站模块单元测试类
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = RPanServerLauncher.class)
@Transactional
public class RecycleTest {
    @Autowired
    private IUserService userService;
    @Autowired
    private IUserFileService userFileService;
    @Autowired
    private IRecycleService recycleService;
    /**
     * 测试查询回收站文件列表成功
     */
    @Test
    public void testQueryRecycleFileListSuccess() {
        Long userId = register();
        UserInfoVO info = info(userId);
        //创建一个文件夹
        CreateFolderContext context=new CreateFolderContext();
        context.setParentId(info.getRootFileId());
        context.setUserId(userId);
        context.setFolderName("test");
        Long fileId = userFileService.createFolder(context);
        Assert.notNull(fileId);
        //删除该文件夹
        DeleteFileContext deleteFileContext=new DeleteFileContext();
        List<Long> fileIdList=new ArrayList<>();
        fileIdList.add(fileId);
        deleteFileContext.setFileIdList(fileIdList);
        deleteFileContext.setUserId(userId);
        userFileService.deleteFile(deleteFileContext);
        //查询回收站列表，校验列表的长度为1
        QueryRecycleFileListContext queryRecycleFileListContext = new QueryRecycleFileListContext();
        queryRecycleFileListContext.setUserId(userId);
        List<RPanUserFileVO> recycles = recycleService.recycles(queryRecycleFileListContext);
        Assert.notEmpty(recycles);
        Assert.isTrue(recycles.size()==1);
    }

    /**
     * 测试文件还原成功
     */
    @Test
    public void testFileRestoreSuccess(){
        Long userId = register();
        UserInfoVO info = info(userId);
        //创建一个文件夹
        CreateFolderContext context=new CreateFolderContext();
        context.setParentId(info.getRootFileId());
        context.setUserId(userId);
        context.setFolderName("test");
        Long fileId = userFileService.createFolder(context);
        Assert.notNull(fileId);
        //删除该文件夹
        DeleteFileContext deleteFileContext=new DeleteFileContext();
        List<Long> fileIdList=new ArrayList<>();
        fileIdList.add(fileId);
        deleteFileContext.setFileIdList(fileIdList);
        deleteFileContext.setUserId(userId);
        userFileService.deleteFile(deleteFileContext);
        //文件还原
        RestoreContext restoreContext=new RestoreContext();
        restoreContext.setUserId(userId);
        restoreContext.setFileIdList(Lists.newArrayList(fileId));
        recycleService.restore(restoreContext);
    }

    /**
     * 测试文件还原失败，没有操作权限
     */
    @Test(expected = RPanBusinessException.class)
    public void testFileRestoreFileByWrongUserId(){
        Long userId = register();
        UserInfoVO info = info(userId);
        //创建一个文件夹
        CreateFolderContext context=new CreateFolderContext();
        context.setParentId(info.getRootFileId());
        context.setUserId(userId);
        context.setFolderName("test");
        Long fileId = userFileService.createFolder(context);
        Assert.notNull(fileId);
        //删除该文件夹
        DeleteFileContext deleteFileContext=new DeleteFileContext();
        List<Long> fileIdList=new ArrayList<>();
        fileIdList.add(fileId);
        deleteFileContext.setFileIdList(fileIdList);
        deleteFileContext.setUserId(userId);
        userFileService.deleteFile(deleteFileContext);
        //文件还原
        RestoreContext restoreContext=new RestoreContext();
        restoreContext.setUserId(userId+1);
        restoreContext.setFileIdList(Lists.newArrayList(fileId));
        recycleService.restore(restoreContext);
    }

    /**
     * 测试文件还原失败，文件名重复
     */
    @Test(expected = RPanBusinessException.class)
    public void testFileRestoreFileByWrongFileName1(){
        Long userId = register();
        UserInfoVO info = info(userId);
        //创建一个文件夹
        CreateFolderContext context=new CreateFolderContext();
        context.setParentId(info.getRootFileId());
        context.setUserId(userId);
        context.setFolderName("test");
        Long fileId1 = userFileService.createFolder(context);
        Assert.notNull(fileId1);
        //删除该文件夹
        DeleteFileContext deleteFileContext=new DeleteFileContext();
        List<Long> fileIdList=new ArrayList<>();
        fileIdList.add(fileId1);
        deleteFileContext.setFileIdList(fileIdList);
        deleteFileContext.setUserId(userId);
        userFileService.deleteFile(deleteFileContext);
        //创建一个同名的文件夹
        CreateFolderContext context2=new CreateFolderContext();
        context2.setParentId(info.getRootFileId());
        context2.setUserId(userId);
        context2.setFolderName("test");
        Long fileId2 = userFileService.createFolder(context2);
        Assert.notNull(fileId2);
        //文件还原
        RestoreContext restoreContext=new RestoreContext();
        restoreContext.setUserId(userId);
        restoreContext.setFileIdList(Lists.newArrayList(fileId1));
        recycleService.restore(restoreContext);
    }
    /**
     * 测试文件还原失败，回收站俩个相同的文件名
     */
    @Test(expected = RPanBusinessException.class)
    public void testFileRestoreFileByWrongFileName2(){
        Long userId = register();
        UserInfoVO info = info(userId);
        //创建一个文件夹
        CreateFolderContext context=new CreateFolderContext();
        context.setParentId(info.getRootFileId());
        context.setUserId(userId);
        context.setFolderName("test");
        Long fileId1 = userFileService.createFolder(context);
        Assert.notNull(fileId1);
        //删除该文件夹
        DeleteFileContext deleteFileContext=new DeleteFileContext();
        List<Long> fileIdList=new ArrayList<>();
        fileIdList.add(fileId1);
        deleteFileContext.setFileIdList(fileIdList);
        deleteFileContext.setUserId(userId);
        userFileService.deleteFile(deleteFileContext);
        //创建一个同名的文件夹
        CreateFolderContext context2=new CreateFolderContext();
        context2.setParentId(info.getRootFileId());
        context2.setUserId(userId);
        context2.setFolderName("test");
        Long fileId2 = userFileService.createFolder(context2);
        Assert.notNull(fileId2);
        //删除该同名的文件
        DeleteFileContext deleteFileContext2=new DeleteFileContext();
        List<Long> fileIdList2=new ArrayList<>();
        fileIdList2.add(fileId2);
        deleteFileContext2.setFileIdList(fileIdList2);
        deleteFileContext2.setUserId(userId);
        userFileService.deleteFile(deleteFileContext2);
        //文件还原
        RestoreContext restoreContext=new RestoreContext();
        restoreContext.setUserId(userId);
        restoreContext.setFileIdList(Lists.newArrayList(fileId1,fileId2));
        recycleService.restore(restoreContext);
    }

    /**
     * 测试文件删除失败，没有操作权限
     */
    @Test(expected = RPanBusinessException.class)
    public void testFileDeleteFileByWrongUserId(){
        Long userId = register();
        UserInfoVO info = info(userId);
        //创建一个文件夹
        CreateFolderContext context=new CreateFolderContext();
        context.setParentId(info.getRootFileId());
        context.setUserId(userId);
        context.setFolderName("test");
        Long fileId = userFileService.createFolder(context);
        Assert.notNull(fileId);
        //删除该文件夹
        DeleteFileContext deleteFileContext=new DeleteFileContext();
        List<Long> fileIdList=new ArrayList<>();
        fileIdList.add(fileId);
        deleteFileContext.setFileIdList(fileIdList);
        deleteFileContext.setUserId(userId);
        userFileService.deleteFile(deleteFileContext);
        //文件还原
        DeleteContext deleteContext=new DeleteContext();
        deleteContext.setUserId(userId+1);
        deleteContext.setFileIdList(Lists.newArrayList(fileId));
        recycleService.delete(deleteContext);
    }

    /**
     * 测试文件还原成功
     */
    @Test
    public void testFileDeleteSuccess(){
        Long userId = register();
        UserInfoVO info = info(userId);
        //创建一个文件夹
        CreateFolderContext context=new CreateFolderContext();
        context.setParentId(info.getRootFileId());
        context.setUserId(userId);
        context.setFolderName("test");
        Long fileId = userFileService.createFolder(context);
        Assert.notNull(fileId);
        //删除该文件夹
        DeleteFileContext deleteFileContext=new DeleteFileContext();
        List<Long> fileIdList=new ArrayList<>();
        fileIdList.add(fileId);
        deleteFileContext.setFileIdList(fileIdList);
        deleteFileContext.setUserId(userId);
        userFileService.deleteFile(deleteFileContext);
        //文件还原
        DeleteContext deleteContext=new DeleteContext();
        deleteContext.setUserId(userId);
        deleteContext.setFileIdList(Lists.newArrayList(fileId));
        recycleService.delete(deleteContext);
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
