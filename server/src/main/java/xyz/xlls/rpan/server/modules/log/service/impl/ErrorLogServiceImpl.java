package xyz.xlls.rpan.server.modules.log.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import xyz.xlls.rpan.server.modules.log.entity.RPanErrorLog;
import xyz.xlls.rpan.server.modules.log.service.IErrorLogService;
import xyz.xlls.rpan.server.modules.log.mapper.RPanErrorLogMapper;
import org.springframework.stereotype.Service;

/**
* @author Administrator
* @description 针对表【r_pan_error_log(错误日志表)】的数据库操作Service实现
* @createDate 2024-10-22 15:06:28
*/
@Service
public class ErrorLogServiceImpl extends ServiceImpl<RPanErrorLogMapper, RPanErrorLog>
    implements IErrorLogService {

}




