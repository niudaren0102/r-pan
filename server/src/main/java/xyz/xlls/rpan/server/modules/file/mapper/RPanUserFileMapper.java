package xyz.xlls.rpan.server.modules.file.mapper;

import org.apache.ibatis.annotations.Param;
import xyz.xlls.rpan.server.modules.file.context.FileSearchContext;
import xyz.xlls.rpan.server.modules.file.entity.RPanUserFile;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import xyz.xlls.rpan.server.modules.file.context.QueryFileContext;
import xyz.xlls.rpan.server.modules.file.vo.FileSearchResultVO;
import xyz.xlls.rpan.server.modules.file.vo.RPanUserFileVO;

import java.util.List;

/**
* @author Administrator
* @description 针对表【r_pan_user_file(用户文件信息表)】的数据库操作Mapper
* @createDate 2024-10-22 15:04:10
* @Entity xyz.xlls.rpan.server.modules.file.entity.RPanUserFile
*/
public interface RPanUserFileMapper extends BaseMapper<RPanUserFile> {
    /**
     * 查询用户的文件列表
     * @param context
     * @return
     */
    List<RPanUserFileVO> selectFileList(@Param("param") QueryFileContext context);

    /**
     * 搜索文件列表
     * @param context
     * @return
     */
    List<FileSearchResultVO> searchFile(@Param("param") FileSearchContext context);
}




