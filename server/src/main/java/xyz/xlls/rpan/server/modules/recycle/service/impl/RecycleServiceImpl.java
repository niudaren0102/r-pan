package xyz.xlls.rpan.server.modules.recycle.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xyz.xlls.rpan.server.modules.file.context.QueryFileContext;
import xyz.xlls.rpan.server.modules.file.enums.DelFlagEnum;
import xyz.xlls.rpan.server.modules.file.service.IUserFileService;
import xyz.xlls.rpan.server.modules.file.vo.RPanUserFileVO;
import xyz.xlls.rpan.server.modules.recycle.context.QueryRecycleFileListContext;
import xyz.xlls.rpan.server.modules.recycle.service.IRecycleService;

import java.util.Collections;
import java.util.List;

/**
 *回收站模块业务处理类
 */
@Service
public class RecycleServiceImpl implements IRecycleService {
    @Autowired
    private IUserFileService userFileService;
    @Override
    public List<RPanUserFileVO> recycles(QueryRecycleFileListContext context) {
        QueryFileContext fileContext=new QueryFileContext();
        fileContext.setUserId(context.getUserId());
        fileContext.setDelFlag(DelFlagEnum.YES.getCode());
        return  userFileService.getFileList(fileContext);
    }
}
