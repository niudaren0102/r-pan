package xyz.xlls.rpan.server.modules.recycle.service;

import xyz.xlls.rpan.server.modules.file.vo.RPanUserFileVO;
import xyz.xlls.rpan.server.modules.recycle.context.QueryRecycleFileListContext;

import java.util.List;

public interface IRecycleService {
    /**
     * 查询用户的回收站文件列表
     * @param context
     * @return
     */
    List<RPanUserFileVO> recycles(QueryRecycleFileListContext context);
}
