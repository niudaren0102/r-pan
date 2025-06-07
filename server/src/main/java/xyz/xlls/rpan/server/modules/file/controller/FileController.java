package xyz.xlls.rpan.server.modules.file.controller;

import com.google.common.base.Splitter;
import io.swagger.annotations.ApiOperation;
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
import xyz.xlls.rpan.server.modules.file.vo.FileChunkUploadVO;
import xyz.xlls.rpan.server.modules.file.vo.RPanUserFileVo;
import xyz.xlls.rpan.server.modules.file.vo.UploadedChunksVO;

import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 文件模块控制器
 */
@RestController
@Validated
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
    public R<List<RPanUserFileVo>> files(
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
        List<RPanUserFileVo> result=userFileService.getFileList(context);
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
    @DeleteMapping("file/sec-upload")
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
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @DeleteMapping("file/upload")
    public R upload(@RequestBody @Validated FileUploadPO fileUploadPO) {
        FileUploadContext context = fileConverter.fileUploadPO2FileUploadContext(fileUploadPO);
        userFileService.upload(context);
        return R.success();
    }
    @ApiOperation(
            value = "文件分片上传",
            notes = "该接口提供了文件分片上传的功能",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PostMapping("file/chunk-upload")
    public R<FileChunkUploadVO> chunkUpload(@RequestBody @Validated FileChunkUploadPO fileChunkUploadPO) {
        FileChunkUploadContext context = fileConverter.chunkUploadPO2ChunkUploadContext(fileChunkUploadPO);
        FileChunkUploadVO vo=userFileService.chunkUpload(context);
        return R.data(vo);
    }
    @ApiOperation(
            value = "文件分片检查",
            notes = "该接口提供了文件分片检查的功能",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @GetMapping("file/chunk-upload")
    public R<UploadedChunksVO> getUploadedChunks(@Validated QueryUploadedChunksPO queryUploadedChunksPO){
    }
}
