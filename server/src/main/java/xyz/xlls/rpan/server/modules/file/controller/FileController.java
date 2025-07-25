package xyz.xlls.rpan.server.modules.file.controller;

import com.google.common.base.Splitter;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import xyz.xlls.rpan.core.constants.RPanConstants;
import xyz.xlls.rpan.core.response.R;
import xyz.xlls.rpan.core.utils.IdUtil;
import xyz.xlls.rpan.server.common.utils.UserIdUtil;
import xyz.xlls.rpan.server.modules.file.constants.FileConstants;
import xyz.xlls.rpan.server.modules.file.context.*;
import xyz.xlls.rpan.server.modules.file.converter.FileConverter;
import xyz.xlls.rpan.server.modules.file.enums.DelFlagEnum;
import xyz.xlls.rpan.server.modules.file.po.*;
import xyz.xlls.rpan.server.modules.file.service.IUserFileService;
import xyz.xlls.rpan.server.modules.file.vo.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 文件模块控制器
 */
@RestController
@Validated
@Api(tags = "文件模块")
public class FileController {
    @Autowired
    private IUserFileService userFileService;
    @Autowired
    private FileConverter fileConverter;
    @ApiOperation(
            value = "查询文件列表",
            notes = "该接口提供了用户查询文件夹下面某些文件类型的文件列表的功能",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @GetMapping("files")
    public R<List<RPanUserFileVO>> files(
            @NotBlank(message = "父文件夹ID不能为空") @RequestParam(value = "parentId",required = false) String parentId,
            @RequestParam(value = "fileTypes",required = false,defaultValue = FileConstants.ALL_FILE_TYPE) String fileTypes
    ){
        Long realParentId = IdUtil.decrypt(parentId);
        List<Integer> fileTypeList=null;
        if(!Objects.equals(FileConstants.ALL_FILE_TYPE,fileTypes)){
            fileTypeList= Splitter.on(RPanConstants.COMMON_SEPARATOR).splitToList(fileTypes).stream().map(Integer::valueOf).collect(Collectors.toList());

        }
        QueryFileContext context=new QueryFileContext();
        context.setParentId(realParentId);
        context.setFileTypeArray(fileTypeList);
        context.setUserId(UserIdUtil.get());
        context.setDelFlag(DelFlagEnum.NO.getCode());
        List<RPanUserFileVO> result=userFileService.getFileList(context);
        return R.data(result);
    }
    @ApiOperation(
            value = "创建文件夹",
            notes = "该接口提供了创建文件夹的功能",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PostMapping("file/folder")
    public R<String> createFolder(
           @Validated @RequestBody CreateFolderPO createFolderPo
    ){
        CreateFolderContext context=fileConverter.createFolderPO2CreateFolderContext(createFolderPo);
        Long folder = userFileService.createFolder(context);
        return R.success(IdUtil.encrypt(folder));
    }
    @ApiOperation(
            value = "文件重命名",
            notes = "该接口提供了文件重命名的功能",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PutMapping("file")
    public R updateFilename(@Validated @RequestBody UpdateFilenamePO updateFilenamePO){
        UpdateFilenameContext context=fileConverter.updateFilenamePO2UpdateFilenameContext(updateFilenamePO);
        userFileService.updateFilename(context);
        return R.success();
    }
    @ApiOperation(
            value = "批量删除文件",
            notes = "该接口提供了批量删除文件的功能",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @DeleteMapping("file")
    public R deleteFile(@RequestBody @Validated DeleteFilePO deleteFilePO)
    {
        DeleteFileContext context = fileConverter.deleteFilePO2DeleteFileContext(deleteFilePO);
        String fileIds = deleteFilePO.getFileIds();
        List<Long> fileIdList = Splitter.on(RPanConstants.COMMON_SEPARATOR).splitToList(fileIds).stream().map(IdUtil::decrypt).collect(Collectors.toList());
        context.setFileIdList(fileIdList);
        userFileService.deleteFile(context);
        return R.success();
    }

    @ApiOperation(
            value = "文件秒传",
            notes = "该接口提供了文件秒传的功能",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PostMapping("file/sec-upload")
    public R secUpload(@RequestBody @Validated SecUploadPO secUploadPO) {
        SecUploadContext context = fileConverter.secUploadPO2SecUploadContext(secUploadPO);
        boolean success=userFileService.secUpload(context);
        if(success){
            return R.success();
        }
        return R.fail("文件唯一标识不存在，请手动执行文件上传的操作");
    }

    @ApiOperation(
            value = "单文件上传",
            notes = "该接口提供了单文件上传的功能",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @PostMapping("file/upload")
    public R upload(@Validated FileUploadPO fileUploadPO) {
        FileUploadContext context = fileConverter.fileUploadPO2FileUploadContext(fileUploadPO);
        userFileService.upload(context);
        return R.success();
    }
    @ApiOperation(
            value = "文件分片上传",
            notes = "该接口提供了文件分片上传的功能",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @PostMapping("file/chunk-upload")
    public R<FileChunkUploadVO> chunkUpload(@Validated FileChunkUploadPO fileChunkUploadPO) {
        FileChunkUploadContext context = fileConverter.chunkUploadPO2ChunkUploadContext(fileChunkUploadPO);
        FileChunkUploadVO vo=userFileService.chunkUpload(context);
        return R.data(vo);
    }
    @ApiOperation(
            value = "查询已经上传的文件分片列表",
            notes = "该接口提供了查询已经上传的文件分片列表的功能",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @GetMapping("file/chunk-upload")
    public R<UploadedChunksVO> getUploadedChunks(@Validated QueryUploadedChunksPO queryUploadedChunksPO){
        QueryUploadedChunksContext context=fileConverter.queryUploadedChunksPO2QueryUploadedChunksContext(queryUploadedChunksPO);
        UploadedChunksVO vo=userFileService.getUploadedChunks(context);
        return R.data(vo);
    }
    @ApiOperation(
            value = "文件分片合并",
            notes = "该接口提供了文件分片合并的功能",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PostMapping("file/merge")
    public R mergeFile(@Validated @RequestBody FileChunkMergePO fileChunkMergePO){
        FileChunkMergeContext context=fileConverter.fileChunkMergePO2FileChunkMergeContext(fileChunkMergePO);
        userFileService.mergeFile(context);
        return R.success();
    }
    @ApiOperation(
            value = "文件下载",
            notes = "该接口提供了文件下载的功能",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    @GetMapping("file/download")
    public void download(@NotBlank(message = "文件ID不能为空") @RequestParam(value = "fileId",required = false) String  fileId,
                      HttpServletResponse response){
        FileDownloadContext fileDownloadContext=new FileDownloadContext();
        fileDownloadContext.setFileId(IdUtil.decrypt(fileId));
        fileDownloadContext.setResponse(response);
        fileDownloadContext.setUserId(UserIdUtil.get());
        userFileService.download(fileDownloadContext);
    }

    @ApiOperation(
            value = "文件预览",
            notes = "该接口提供了文件预览的功能",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    @GetMapping("file/preview")
    public void preview(@NotBlank(message = "文件ID不能为空") @RequestParam(value = "fileId",required = false) String  fileId,
                         HttpServletResponse response){
        FilePreviewContext filePreviewContext=new FilePreviewContext();
        filePreviewContext.setFileId(IdUtil.decrypt(fileId));
        filePreviewContext.setResponse(response);
        filePreviewContext.setUserId(UserIdUtil.get());
        userFileService.preview(filePreviewContext);
    }

    @ApiOperation(
            value = "查询文件夹树",
            notes = "该接口提供了查询文件夹树的功能",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @GetMapping("file/folder/tree")
    public R<List<FolderTreeNodeVO>> getFolderTree(){
        QueryFolderTreeContext queryFolderTreeContext=new QueryFolderTreeContext();
        queryFolderTreeContext.setUserId(UserIdUtil.get());
        List<FolderTreeNodeVO> result=userFileService.getFolderTree(queryFolderTreeContext);
        return R.data(result);
    }
    @ApiOperation(
            value = "文件转移",
            notes = "该接口提供了文件转移的功能",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @PostMapping("file/transfer")
    public R transfer(@Validated @RequestBody TransferFilePO transferFilePO){
        String fileIds = transferFilePO.getFileIds();
        String targetParentId = transferFilePO.getTargetParentId();
        List<Long> fileIdList = Splitter.on(RPanConstants.COMMON_SEPARATOR).splitToList(fileIds).stream().map(IdUtil::decrypt).collect(Collectors.toList());
        TransferFileContext transferFileContext=new TransferFileContext();
        transferFileContext.setFileIdList(fileIdList);
        transferFileContext.setTargetParentId(IdUtil.decrypt(targetParentId));
        transferFileContext.setUserId(UserIdUtil.get());
        userFileService.transfer(transferFileContext);
        return R.success();
    }
    @ApiOperation(
            value = "文件复制",
            notes = "该接口提供了文件复制的功能",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @PostMapping("file/copy")
    public R copy(@Validated @RequestBody TransferFilePO copyFilePO){
        String fileIds = copyFilePO.getFileIds();
        String targetParentId = copyFilePO.getTargetParentId();
        List<Long> fileIdList = Splitter.on(RPanConstants.COMMON_SEPARATOR).splitToList(fileIds).stream().map(IdUtil::decrypt).collect(Collectors.toList());
        CopyFileContext context=new CopyFileContext();
        context.setFileIdList(fileIdList);
        context.setTargetParentId(IdUtil.decrypt(targetParentId));
        context.setUserId(UserIdUtil.get());
        userFileService.copy(context);
        return R.success();
    }
    @ApiOperation(
            value = "文件搜索",
            notes = "该接口提供了文件搜索的功能",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @PostMapping("file/search")
    public R<List<FileSearchResultVO>> search(@Validated  FileSearchPO fileSearchPO){
        FileSearchContext context=new FileSearchContext();
        context.setKeyword(fileSearchPO.getKeyword());
        context.setUserId(UserIdUtil.get());
        String fileTypes = fileSearchPO.getFileTypes();
        if(StringUtils.isNoneBlank(fileTypes)&&!Objects.equals(FileConstants.ALL_FILE_TYPE, fileTypes)){
            List<Integer> fileTypeArray = Splitter.on(RPanConstants.COMMON_SEPARATOR).splitToList(fileTypes).stream().map(Integer::valueOf).collect(Collectors.toList());
            context.setFileTypeArray(fileTypeArray);
        }
        List<FileSearchResultVO> result= userFileService.search(context);
        return R.data(result);
    }
    @ApiOperation(
            value = "查询面包屑列表",
            notes = "该接口提供了查询面包屑列表的功能",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @PostMapping("file/breadcrumbs")
    public R<List<BreadcrumbVO>> getBreadcrumbs(@NotBlank(message = "文件ID不能为空")@RequestParam(value = "fileId",required =false) String fileId){
        QueryBreadcrumbsContext context=new QueryBreadcrumbsContext();
        context.setFileId(IdUtil.decrypt(fileId));
        context.setUserId(UserIdUtil.get());
        List<BreadcrumbVO> result= userFileService.getBreadcrumbs(context);
        return R.data(result);
    }

}
