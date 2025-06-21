package xyz.xlls.rpan.server.modules.file;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Assert;
import lombok.AllArgsConstructor;
import org.assertj.core.util.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;
import xyz.xlls.rpan.core.exception.RPanBusinessException;
import xyz.xlls.rpan.core.utils.IdUtil;
import xyz.xlls.rpan.server.RPanServerLauncher;
import xyz.xlls.rpan.server.modules.file.context.*;
import xyz.xlls.rpan.server.modules.file.entity.RPanFile;
import xyz.xlls.rpan.server.modules.file.entity.RPanFileChunk;
import xyz.xlls.rpan.server.modules.file.enums.DelFlagEnum;
import xyz.xlls.rpan.server.modules.file.enums.MergeFlagEnum;
import xyz.xlls.rpan.server.modules.file.service.IFileChunkService;
import xyz.xlls.rpan.server.modules.file.service.IFileService;
import xyz.xlls.rpan.server.modules.file.service.IUserFileService;
import xyz.xlls.rpan.server.modules.file.vo.FileChunkUploadVO;
import xyz.xlls.rpan.server.modules.file.vo.FolderTreeNodeVO;
import xyz.xlls.rpan.server.modules.user.context.UserLoginContext;
import xyz.xlls.rpan.server.modules.user.context.UserRegisterContext;
import xyz.xlls.rpan.server.modules.user.service.IUserService;
import xyz.xlls.rpan.server.modules.file.vo.RPanUserFileVo;
import xyz.xlls.rpan.server.modules.user.vo.UserInfoVO;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

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
    @Autowired
    private IFileService fileService;
    @Autowired
    private IFileChunkService fileChunkService;

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
     * 测试秒传成功
     */
    @Test
    public void testSecUploadSuccess(){
        Long userId = register();
        UserInfoVO info = info(userId);
        String identifier = "identifier";
        RPanFile record=new RPanFile();
        record.setFileId(IdUtil.get());
        record.setFilename("filename");
        record.setRealPath("realpath");
        record.setFileSize("filesize");
        record.setFileSizeDesc("desc");
        record.setFileSuffix("suffix");
        record.setFilePreviewContentType("");
        record.setIdentifier(identifier);
        record.setCreateUser(userId);
        record.setCreateTime(new Date());
        boolean save = fileService.save(record);
        Assert.isTrue(save);
        SecUploadContext secUploadContext=new SecUploadContext();
        secUploadContext.setFilename("filename");
        secUploadContext.setIdentifier(identifier);
        secUploadContext.setParentId(info.getRootFileId());
        secUploadContext.setUserId(userId);
        boolean result  = userFileService.secUpload(secUploadContext);
        Assert.isTrue(result);
    }

    /**
     * 测试秒传失败
     */
    @Test
    public void testSecUploadFail(){
        Long userId = register();
        UserInfoVO info = info(userId);
        String identifier = "identifier";
        SecUploadContext secUploadContext=new SecUploadContext();
        secUploadContext.setFilename("filename");
        secUploadContext.setIdentifier(identifier);
        secUploadContext.setParentId(info.getRootFileId());
        secUploadContext.setUserId(userId);
        boolean result  = userFileService.secUpload(secUploadContext);
        Assert.isFalse(result);
    }

    /**
     * 测试单文件上传成功
     */
    @Test
    public void testUploadSuccess(){
        Long userId=register();
        UserInfoVO info = info(userId);
        FileUploadContext context = new FileUploadContext();
        MultipartFile file=genarateMultipartFile();
        context.setFile(file);
        context.setParentId(info.getRootFileId());
        context.setUserId(userId);
        context.setTotalSize(file.getSize());
        context.setIdentifier("12345678");
        context.setFilename(file.getOriginalFilename());
        userFileService.upload(context);
        QueryFileContext queryFileContext=new QueryFileContext();
        queryFileContext.setDelFlag(DelFlagEnum.NO.getCode());
        queryFileContext.setUserId(userId);
        queryFileContext.setParentId(info.getRootFileId());
        List<RPanUserFileVo> fileList = userFileService.getFileList(queryFileContext);
        Assert.notEmpty(fileList);
        Assert.isTrue(fileList.size()==1);

    }
    /**
     * 测试查询用户已上传的文件分片信息列表成功
     */
    @Test
    public void testQueryUploadedChunksSuccess(){
        Long register = register();
        UserInfoVO info = info(register);
        RPanFileChunk rPanFileChunk = new RPanFileChunk();
        rPanFileChunk.setId(IdUtil.get());
        rPanFileChunk.setIdentifier("test");
        rPanFileChunk.setRealPath("realPath");
        rPanFileChunk.setChunkNumber(1);
        rPanFileChunk.setExpirationTime(DateUtil.offsetDay(new Date(),1));
        rPanFileChunk.setCreateTime(new Date());
        rPanFileChunk.setCreateUser(register);
        boolean save = fileChunkService.save(rPanFileChunk);
        Assert.isTrue(save);
        QueryUploadedChunksContext context=new QueryUploadedChunksContext();
        context.setIdentifier("test");
        context.setUserId(register);
        List<Integer> uploadedChunks = fileChunkService.queryUploadedChunks(context);
        Assert.notEmpty(uploadedChunks);
        Assert.isTrue(uploadedChunks.size()==1);

    }

    /**
     * 测试文件分片上传成功
     */
    @Test
    public void uploadWithChunkTest() throws InterruptedException {
        Long userId = register();
        UserInfoVO info = info(userId);
        CountDownLatch countDownLatch=new CountDownLatch(10);
        for (int i = 0; i < 10; i++){
            new ChunkUploader(
                    countDownLatch,
                    i+1,
                    10,
                    userFileService,
                    userId,
                    info.getRootFileId()
            ).start();
        }
        countDownLatch.await();
    }

    /**
     * 测试文件夹树查询
     */
    @Test
    public void getFolderTreeNodeVOListTese(){
        Long userId = register();
        UserInfoVO info = info(userId);
        CreateFolderContext context=new CreateFolderContext();
        context.setParentId(info.getRootFileId());
        context.setUserId(userId);
        context.setFolderName("test-1");
        Long fileId = userFileService.createFolder(context);
        Assert.notNull(fileId);
        context.setFolderName("test-2");
        fileId = userFileService.createFolder(context);
        Assert.notNull(fileId);
        context.setFolderName("test-2-1");
        context.setParentId(fileId);
        fileId = userFileService.createFolder(context);
        Assert.notNull(fileId);
        QueryFolderTreeContext queryFolderTreeContext=new QueryFolderTreeContext();
        queryFolderTreeContext.setUserId(userId);
        List<FolderTreeNodeVO> folderTreeNodeVOList = userFileService.getFolderTree(queryFolderTreeContext);
        Assert.isTrue(folderTreeNodeVOList.size()==1);
        folderTreeNodeVOList.forEach(FolderTreeNodeVO::print);
    }

    /**
     * 测试文件转移成功
     */
    @Test
    public void testTransferFileSuccess() {
        Long userId = register();
        UserInfoVO info = info(userId);

        CreateFolderContext context=new CreateFolderContext();
        context.setParentId(info.getRootFileId());
        context.setUserId(userId);
        context.setFolderName("folder1");
        Long folder1 = userFileService.createFolder(context);
        Assert.notNull(folder1);

        context.setFolderName("folder2");
        Long folder2 = userFileService.createFolder(context);
        Assert.notNull(folder2);

        TransferFileContext transferFileContext=new TransferFileContext();
        transferFileContext.setTargetParentId(folder1);
        transferFileContext.setFileIdList(Lists.newArrayList(folder2));
        transferFileContext.setUserId(userId);
        userFileService.transfer(transferFileContext);
        QueryFileContext queryFileContext=new QueryFileContext();
        queryFileContext.setUserId(userId);
        queryFileContext.setParentId(info.getRootFileId());
        queryFileContext.setDelFlag(DelFlagEnum.NO.getCode());
        List<RPanUserFileVo> fileList = userFileService.getFileList(queryFileContext);
        Assert.notEmpty(fileList);
    }

    /**
     * 测试文件转移失败，目标文件夹是要转移的文件列表中的文件夹或者是其子文件夹
     */
    @Test(expected = RPanBusinessException.class)
    public void testTransferFileFail() {
        Long userId = register();
        UserInfoVO info = info(userId);

        CreateFolderContext context=new CreateFolderContext();
        context.setParentId(info.getRootFileId());
        context.setUserId(userId);
        context.setFolderName("folder1");
        Long folder1 = userFileService.createFolder(context);
        Assert.notNull(folder1);

        context.setFolderName("folder2");
        context.setParentId(folder1);
        Long folder2 = userFileService.createFolder(context);
        Assert.notNull(folder2);

        TransferFileContext transferFileContext=new TransferFileContext();
        transferFileContext.setTargetParentId(folder2);
        transferFileContext.setFileIdList(Lists.newArrayList(folder1));
        transferFileContext.setUserId(userId);
        userFileService.transfer(transferFileContext);
    }


    /**
     * 测试文件复制成功
     */
    @Test
    public void testCopyFileSuccess() {
        Long userId = register();
        UserInfoVO info = info(userId);

        CreateFolderContext context=new CreateFolderContext();
        context.setParentId(info.getRootFileId());
        context.setUserId(userId);
        context.setFolderName("folder1");
        Long folder1 = userFileService.createFolder(context);
        Assert.notNull(folder1);

        context.setFolderName("folder2");
        Long folder2 = userFileService.createFolder(context);
        Assert.notNull(folder2);

        CopyFileContext copyFileContext=new CopyFileContext();
        copyFileContext.setTargetParentId(folder1);
        copyFileContext.setFileIdList(Lists.newArrayList(folder2));
        copyFileContext.setUserId(userId);
        userFileService.copy(copyFileContext);
        QueryFileContext queryFileContext=new QueryFileContext();
        queryFileContext.setUserId(userId);
        queryFileContext.setParentId(folder1);
        queryFileContext.setDelFlag(DelFlagEnum.NO.getCode());
        List<RPanUserFileVo> fileList = userFileService.getFileList(queryFileContext);
        Assert.notEmpty(fileList);
    }

    /**
     * 测试文件复制失败，目标文件夹是要转移的文件列表中的文件夹或者是其子文件夹
     */
    @Test(expected = RPanBusinessException.class)
    public void testCopyFileFail() {
        Long userId = register();
        UserInfoVO info = info(userId);

        CreateFolderContext context=new CreateFolderContext();
        context.setParentId(info.getRootFileId());
        context.setUserId(userId);
        context.setFolderName("folder1");
        Long folder1 = userFileService.createFolder(context);
        Assert.notNull(folder1);

        context.setFolderName("folder2");
        context.setParentId(folder1);
        Long folder2 = userFileService.createFolder(context);
        Assert.notNull(folder2);

        CopyFileContext copyFileContext=new CopyFileContext();
        copyFileContext.setTargetParentId(folder2);
        copyFileContext.setFileIdList(Lists.newArrayList(folder1));
        copyFileContext.setUserId(userId);
        userFileService.copy(copyFileContext);
    }
    /**
     * 文件分片上传器
     */
    @AllArgsConstructor
    private static class ChunkUploader extends Thread{
        private CountDownLatch countDownLatch;
        private Integer chunk;
        private Integer chunks;
        private IUserFileService userFileService;
        private Long userId;
        private Long parentId;

        /**
         * 1、上传文件分片
         * 2、根据上传的结果调用文件分片合并
         */
        @Override
        public void run() {
            super.run();
            MultipartFile file = genarateMultipartFile();
            Long totalSize=file.getSize()*chunks;
            String filename="test.txt";
            String identifier="123456789";
            FileChunkUploadContext fileChunkUploadContext=new FileChunkUploadContext();
            fileChunkUploadContext.setFilename(filename);
            fileChunkUploadContext.setIdentifier(identifier);
            fileChunkUploadContext.setTotalChunks(chunks);
            fileChunkUploadContext.setChunkNumber(chunk);
            fileChunkUploadContext.setCurrentChunkSize(file.getSize());
            fileChunkUploadContext.setTotalSize(totalSize);
            fileChunkUploadContext.setFile(file);
            fileChunkUploadContext.setUserId(userId);
            FileChunkUploadVO fileChunkUploadVO = userFileService.chunkUpload(fileChunkUploadContext);
            if (Objects.equals(fileChunkUploadVO.getMergeFlag(),MergeFlagEnum.READY.getCode())){
                System.out.println("分片"+chunk+"检测到可以合并分片");
                FileChunkMergeContext fileChunkMergeContext=new FileChunkMergeContext();
                fileChunkMergeContext.setFilename(filename);
                fileChunkMergeContext.setIdentifier(identifier);
                fileChunkMergeContext.setTotalSize(totalSize);
                fileChunkMergeContext.setUserId(userId);
                fileChunkMergeContext.setParentId(parentId);
                userFileService.mergeFile(fileChunkMergeContext);
                countDownLatch.countDown();
            }else{
                countDownLatch.countDown();
            }
        }
    }
    /**
     * 生成模拟的网络文件实体
     * @return
     */
    private static MultipartFile genarateMultipartFile() {
        MultipartFile file=null;
        try {
            file=new MockMultipartFile("file", "test.txt", "multipart/form-data", "test".getBytes("UTF-8"));
        }catch (Exception e){
            e.printStackTrace();
        }
        return file;
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
