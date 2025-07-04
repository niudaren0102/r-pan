package xyz.xlls.rpan.server.modules.share.controller;

import com.google.common.base.Splitter;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import xyz.xlls.rpan.core.constants.RPanConstants;
import xyz.xlls.rpan.core.response.R;
import xyz.xlls.rpan.core.utils.IdUtil;
import xyz.xlls.rpan.server.modules.share.Converter.ShareConverter;
import xyz.xlls.rpan.server.modules.share.context.CreateShareUrlContext;
import xyz.xlls.rpan.server.modules.share.po.CreateShareUrlPO;
import xyz.xlls.rpan.server.modules.share.service.IShareService;
import xyz.xlls.rpan.server.modules.share.vo.RPanShareUrlVO;

import java.util.List;
import java.util.stream.Collectors;

@Api(tags = "分享模块")
@RestController
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

}
