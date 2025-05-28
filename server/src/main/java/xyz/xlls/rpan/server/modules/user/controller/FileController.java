package xyz.xlls.rpan.server.modules.user.controller;

import com.google.common.base.Splitter;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import xyz.xlls.rpan.core.constants.RPanConstants;
import xyz.xlls.rpan.core.response.R;
import xyz.xlls.rpan.core.utils.IdUtil;
import xyz.xlls.rpan.server.modules.file.constants.FileConstants;
import xyz.xlls.rpan.server.modules.file.enums.DelFlagEnum;
import xyz.xlls.rpan.server.modules.file.service.IUserFileService;
import xyz.xlls.rpan.server.modules.user.context.QueryFileContext;
import xyz.xlls.rpan.server.modules.user.vo.RPanUserFileVo;

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
        context.setUserId(IdUtil.get());
        context.setDelFlag(DelFlagEnum.NO.getCode());
        List<RPanUserFileVo> result=userFileService.getFileList(context);
        return R.data(result);
    }
}
