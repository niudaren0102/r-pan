package xyz.xlls.rpan.server.common.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import xyz.xlls.rpan.core.constants.RPanConstants;

@Component
@ConfigurationProperties(prefix = "xyz.xlls.pan.server")
@Data
public class PanServerConfig {
    /**
     * 文件分片上传的过期天数
     */
    private  Integer chunkFileExpirationDays= RPanConstants.ONE_INT;
}
