package xyz.xlls.rpan.server.modules.file;

import cn.hutool.core.lang.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import xyz.xlls.rpan.core.exception.RPanBusinessException;
import xyz.xlls.rpan.server.RPanServerLauncher;
import xyz.xlls.rpan.server.modules.file.context.CreateFolderContext;
import xyz.xlls.rpan.server.modules.file.context.DeleteFileContext;
import xyz.xlls.rpan.server.modules.file.context.UpdateFilenameContext;
import xyz.xlls.rpan.server.modules.file.enums.DelFlagEnum;
import xyz.xlls.rpan.server.modules.file.service.IUserFileService;
import xyz.xlls.rpan.server.modules.file.context.QueryFileContext;
import xyz.xlls.rpan.server.modules.user.context.UserLoginContext;
import xyz.xlls.rpan.server.modules.user.context.UserRegisterContext;
import xyz.xlls.rpan.server.modules.user.service.IUserService;
import xyz.xlls.rpan.server.modules.file.vo.RPanUserFileVo;
import xyz.xlls.rpan.server.modules.user.vo.UserInfoVO;

import java.util.ArrayList;
import java.util.List;

/**
 * 文件模块单元测试类
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = RPanServerLauncher.class)
@Transactional
public class FileTest {
    @Autowired
    private IUserFileService userFileService;
    @Autowired
    private IUserService userService;

    /**
     * 测试用户查询文件列表成功
     */
    @Test
    public void testQueryUserFileListSuccess(){
        Long userId = register();
        UserInfoVO info = info(userId);
        QueryFileContext context = new QueryFileContext();
        context.setUserId(userId);
        context.setParentId(info.getRootFileId());
        context.setFileTypeArray(null);
        context.setDelFlag(DelFlagEnum.NO.getCode());
        List<RPanUserFileVo> result = userFileService.getFileList(context);
        Assert.isTrue(CollectionUtils.isEmpty(result));
    }

    /**
     * 测试创建文件夹成功
     */
    @Test
    public void testCreateFolderSuccess(){
        Long userId = register();
        UserInfoVO info = info(userId);
        CreateFolderContext context=new CreateFolderContext();
        context.setParentId(info.getRootFileId());
        context.setUserId(userId);
        context.setFolderName("test");
        Long fileId = userFileService.createFolder(context);
        Assert.notNull(fileId);
    }

    /**
     * 测试文件重命名失败-文件ID无效
     */
    @Test(expected = RPanBusinessException.class)
    public void testUpdateFilenameByWrongFileId(){
        Long userId = register();
        UserInfoVO info = info(userId);
        CreateFolderContext context=new CreateFolderContext();
        context.setParentId(info.getRootFileId());
        context.setUserId(userId);
        context.setFolderName("test");
        Long fileId = userFileService.createFolder(context);
        Assert.notNull(fileId);
        UpdateFilenameContext updateFilenameContext=new UpdateFilenameContext();

        updateFilenameContext.setUserId(userId);
        updateFilenameContext.setFileId(1L);
        updateFilenameContext.setNewFilename("test1");
        userFileService.updateFilename(updateFilenameContext);
    }

    /**
     * 测试文件重命名失败-用户ID无效
     */
    @Test(expected = RPanBusinessException.class)
    public void testUpdateFilenameByWrongUserId(){
        Long userId = register();
        UserInfoVO info = info(userId);
        CreateFolderContext context=new CreateFolderContext();
        context.setParentId(info.getRootFileId());
        context.setUserId(userId);
        context.setFolderName("test");
        Long fileId = userFileService.createFolder(context);
        Assert.notNull(fileId);
        UpdateFilenameContext updateFilenameContext=new UpdateFilenameContext();

        updateFilenameContext.setUserId(userId+1);
        updateFilenameContext.setFileId(fileId);
        updateFilenameContext.setNewFilename("test1");
        userFileService.updateFilename(updateFilenameContext);
    }
    /**
     * 测试文件重命名失败-文件名称重复
     */
    @Test(expected = RPanBusinessException.class)
    public void testUpdateFilenameByWrongNewFilename(){
        Long userId = register();
        UserInfoVO info = info(userId);
        CreateFolderContext context=new CreateFolderContext();
        context.setParentId(info.getRootFileId());
        context.setUserId(userId);
        context.setFolderName("test");
        Long fileId = userFileService.createFolder(context);
        Assert.notNull(fileId);
        UpdateFilenameContext updateFilenameContext=new UpdateFilenameContext();

        updateFilenameContext.setUserId(userId);
        updateFilenameContext.setFileId(fileId);
        updateFilenameContext.setNewFilename("test");
        userFileService.updateFilename(updateFilenameContext);
    }
    /**
     * 测试文件重命名失败-文件名称已被占用
     */
    @Test(expected = RPanBusinessException.class)
    public void testUpdateFilenameByFilenameExists(){
        Long userId = register();
        UserInfoVO info = info(userId);
        CreateFolderContext context=new CreateFolderContext();
        context.setParentId(info.getRootFileId());
        context.setUserId(userId);
        context.setFolderName("test");
        Long fileId = userFileService.createFolder(context);
        Assert.notNull(fileId);

        CreateFolderContext context1=new CreateFolderContext();
        context1.setParentId(info.getRootFileId());
        context1.setUserId(userId);
        context1.setFolderName("test1");
        Long fileId1 = userFileService.createFolder(context1);
        Assert.notNull(fileId1);

        UpdateFilenameContext updateFilenameContext=new UpdateFilenameContext();

        updateFilenameContext.setUserId(userId);
        updateFilenameContext.setFileId(fileId);
        updateFilenameContext.setNewFilename("test1");
        userFileService.updateFilename(updateFilenameContext);
    }

    /**
     * 测试更新文件名称成功
     */
    @Test
    public void testUpdateFilenameBySuccess(){
        Long userId = register();
        UserInfoVO info = info(userId);
        CreateFolderContext context=new CreateFolderContext();
        context.setParentId(info.getRootFileId());
        context.setUserId(userId);
        context.setFolderName("test");
        Long fileId = userFileService.createFolder(context);
        Assert.notNull(fileId);
        UpdateFilenameContext updateFilenameContext=new UpdateFilenameContext();

        updateFilenameContext.setUserId(userId);
        updateFilenameContext.setFileId(fileId);
        updateFilenameContext.setNewFilename("test1");
        userFileService.updateFilename(updateFilenameContext);
    }

    /**
     * 校验文件删除失败-非法的文件ID
     */
    @Test(expected = RPanBusinessException.class)
    public void testDeleteFileByWrongFileId(){
        Long userId = register();
        UserInfoVO info = info(userId);
        CreateFolderContext context=new CreateFolderContext();
        context.setParentId(info.getRootFileId());
        context.setUserId(userId);
        context.setFolderName("test");
        Long fileId = userFileService.createFolder(context);
        Assert.notNull(fileId);

        DeleteFileContext deleteFileContext=new DeleteFileContext();
        List<Long> fileIdList=new ArrayList<>();
        fileIdList.add(fileId+1);
        deleteFileContext.setFileIdList(fileIdList);
        deleteFileContext.setUserId(userId);
        userFileService.deleteFile(deleteFileContext);
    }

    /**
     * 校验文件删除失败-非法的用户ID
     */
    @Test(expected = RPanBusinessException.class)
    public void testDeleteFileByWrongUserId(){
        Long userId = register();
        UserInfoVO info = info(userId);
        CreateFolderContext context=new CreateFolderContext();
        context.setParentId(info.getRootFileId());
        context.setUserId(userId);
        context.setFolderName("test");
        Long fileId = userFileService.createFolder(context);
        Assert.notNull(fileId);

        DeleteFileContext deleteFileContext=new DeleteFileContext();
        List<Long> fileIdList=new ArrayList<>();
        fileIdList.add(fileId);
        deleteFileContext.setFileIdList(fileIdList);
        deleteFileContext.setUserId(userId+1);
        userFileService.deleteFile(deleteFileContext);
    }

    /**
     * 校验用户删除文件成功
     */
    @Test
    public void testDeleteFileSuccess()
    {
        Long userId = register();
        UserInfoVO info = info(userId);
        CreateFolderContext context=new CreateFolderContext();
        context.setParentId(info.getRootFileId());
        context.setUserId(userId);
        context.setFolderName("test");
        Long fileId = userFileService.createFolder(context);
        Assert.notNull(fileId);

        DeleteFileContext deleteFileContext=new DeleteFileContext();
        List<Long> fileIdList=new ArrayList<>();
        fileIdList.add(fileId);
        deleteFileContext.setFileIdList(fileIdList);
        deleteFileContext.setUserId(userId);
        userFileService.deleteFile(deleteFileContext);

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
