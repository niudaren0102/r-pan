package xyz.xlls.rpan.server.modules.recycle.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.xlls.rpan.core.response.R;
import xyz.xlls.rpan.core.utils.IdUtil;
import xyz.xlls.rpan.server.common.utils.UserIdUtil;
import xyz.xlls.rpan.server.modules.file.vo.RPanUserFileVO;
import xyz.xlls.rpan.server.modules.recycle.context.QueryRecycleFileListContext;
import xyz.xlls.rpan.server.modules.recycle.service.IRecycleService;

import java.util.List;

/**
 * 回收站模块控制器
 */
@RestController
@Api(tags = "回收站模块")
@Validated
public class RecycleController {
    @Autowired
    private IRecycleService recycleService;

    @ApiOperation(
            value = "获取回收站文件列表",
            notes = "该接口提供了获取回收站文件列表的功能",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @GetMapping("recycles")
    public R<List<RPanUserFileVO>> recycles(){
        QueryRecycleFileListContext context=new QueryRecycleFileListContext();
        context.setUserId(UserIdUtil.get());
        List<RPanUserFileVO> result=recycleService.recycles(context);
        return R.data(result);
    }
}
