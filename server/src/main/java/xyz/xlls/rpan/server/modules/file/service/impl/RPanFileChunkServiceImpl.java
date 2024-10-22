package xyz.xlls.rpan.server.modules.file.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import xyz.xlls.rpan.server.modules.file.entity.RPanFileChunk;
import xyz.xlls.rpan.server.modules.file.service.RPanFileChunkService;
import xyz.xlls.rpan.server.modules.file.mapper.RPanFileChunkMapper;
import org.springframework.stereotype.Service;

/**
* @author Administrator
* @description 针对表【r_pan_file_chunk(文件分片信息表)】的数据库操作Service实现
* @createDate 2024-10-22 15:04:09
*/
@Service
public class RPanFileChunkServiceImpl extends ServiceImpl<RPanFileChunkMapper, RPanFileChunk>
    implements RPanFileChunkService{

}




