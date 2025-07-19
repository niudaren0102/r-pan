package xyz.xlls.rpan.server.modules.share.controller;

import com.google.common.base.Splitter;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import xyz.xlls.rpan.core.constants.RPanConstants;
import xyz.xlls.rpan.core.response.R;
import xyz.xlls.rpan.core.utils.IdUtil;
import xyz.xlls.rpan.server.common.annotation.LoginIgnore;
import xyz.xlls.rpan.server.common.annotation.NeedShareCode;
import xyz.xlls.rpan.server.common.utils.ShareIdUtil;
import xyz.xlls.rpan.server.common.utils.UserIdUtil;
import xyz.xlls.rpan.server.modules.file.vo.RPanUserFileVO;
import xyz.xlls.rpan.server.modules.share.Converter.ShareConverter;
import xyz.xlls.rpan.server.modules.share.context.*;
import xyz.xlls.rpan.server.modules.share.po.CancelShareUrlPO;
import xyz.xlls.rpan.server.modules.share.po.CheckShareCodePO;
import xyz.xlls.rpan.server.modules.share.po.CreateShareUrlPO;
import xyz.xlls.rpan.server.modules.share.service.IShareService;
import xyz.xlls.rpan.server.modules.share.vo.RPanShareUrlVO;
import xyz.xlls.rpan.server.modules.share.vo.RPanShareUrlListVO;
import xyz.xlls.rpan.server.modules.share.vo.ShareDetailVO;
import xyz.xlls.rpan.server.modules.share.vo.ShareSimpleDetailVO;

import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.stream.Collectors;

@Api(tags = "分享模块")
@RestController
@Validated
public class ShareController {
    @Autowired
    private IShareService shareService;
    @Autowired
    private ShareConverter shareConverter;

    @ApiOperation(value = "创建分享链接",
            notes = "该接口提供了分享链接的功能",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PostMapping("share")
    public R<RPanShareUrlVO> create(@Validated @RequestBody CreateShareUrlPO createShareUrlPO) {
        CreateShareUrlContext context = shareConverter.createShareUrlPO2CreateShareUrlContext(createShareUrlPO);
        String shareFileIds = createShareUrlPO.getShareFileIds();
        List<Long> shareFileIdList = Splitter.on(RPanConstants.COMMON_SEPARATOR).splitToList(shareFileIds).stream().map(IdUtil::decrypt).collect(Collectors.toList());
        context.setShareFileIdList(shareFileIdList);
        RPanShareUrlVO vo = shareService.create(context);
        return R.data(vo);
    }
    @ApiOperation(value = "查询分享链接列表",
            notes = "该接口提供了查询分享链接列表的功能",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @GetMapping("shares")
    public R<List<RPanShareUrlListVO>> getShares(){
        QueryShareUrlListContext context = new QueryShareUrlListContext();
        context.setUserId(UserIdUtil.get());
        List<RPanShareUrlListVO> result = shareService.getShares(context);
        return R.data(result);
    }
    @ApiOperation(value = "取消分享链接",
            notes = "该接口提供了取消分享链接的功能",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @DeleteMapping("share")
    public R cancelShare(@RequestBody @Validated CancelShareUrlPO po) {
        CancelShareUrlContext context = new CancelShareUrlContext();
        context.setUserId(UserIdUtil.get());
        String shareIds = po.getShareIds();
        List<Long> shareIdList = Splitter.on(RPanConstants.COMMON_SEPARATOR).splitToList(shareIds).stream().map(IdUtil::decrypt).collect(Collectors.toList());
        context.setShareIdList(shareIdList);
        shareService.cancelShare(context);
        return R.success();
    }
    @ApiOperation(value = "检验分享码",
            notes = "该接口提供了校验分享码的功能",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PostMapping("share/code/check")
    @LoginIgnore
    public R<String> checkShareCode(@Validated @RequestBody CheckShareCodePO checkShareCodePO){
        CheckShareCodeContext context = new CheckShareCodeContext();
        context.setShareId(IdUtil.decrypt(checkShareCodePO.getShareCode()));
        context.setShareCode(checkShareCodePO.getShareCode());
        String token=shareService.checkShareCode(context);
        return R.data(token);
    }
    @ApiOperation(
            value = "查询文件分享详情",
            notes = "该接口提供了查询文件分享详情的功能",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @LoginIgnore
    @NeedShareCode
    @GetMapping("share")
    public R<ShareDetailVO> detail(){
        QueryShareDetailContext context = new QueryShareDetailContext();
        context.setShareId(ShareIdUtil.get());
        ShareDetailVO vo=shareService.detail(context);
        return R.data( vo);
    }
    @ApiOperation(
            value = "查询文件分享的简单详情",
            notes = "该接口提供了查询文件分享简单详情的功能",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @LoginIgnore
    @GetMapping("share/simple")
    public R<ShareSimpleDetailVO> simpleDetail(
            @NotBlank(message="分享的ID不能为空") @RequestParam(value = "shareId",required = false) String shareId
    ){
        QueryShareSimpleDetailContext context = new QueryShareSimpleDetailContext();
        context.setShareId(IdUtil.decrypt(shareId));
        ShareSimpleDetailVO vo=shareService.simpleDetail(context);
        return R.data( vo);
    }
    @ApiOperation(
            value = "获取下一级文件列表",
            notes = "该接口提供了获取下一级文件列表的功能",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @GetMapping("share/file/list")
    @NeedShareCode
    @LoginIgnore
    public R<List<RPanUserFileVO>> fileList(
            @NotBlank(message="文件父ID不能为空") @RequestParam(value = "parentId",required = false) String parentId
    ){
        QueryChildFileListContext context = new QueryChildFileListContext();
        context.setShareId(ShareIdUtil.get());
        context.setParentId(IdUtil.decrypt(parentId));
        List<RPanUserFileVO> result= shareService.fileList(context);
        return R.data(result);
    }
}
