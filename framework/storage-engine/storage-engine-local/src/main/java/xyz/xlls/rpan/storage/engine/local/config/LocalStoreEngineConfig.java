package xyz.xlls.rpan.storage.engine.local.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import xyz.xlls.rpan.core.utils.FileUtil;

@Component
@ConfigurationProperties(prefix = "xyz.xlls.rpan.storage.engine.local")
@Data
public class LocalStoreEngineConfig {
    /**
     * 实际存放路径的前缀
     */
    private String rootFilePath= FileUtil.generateDefaultStoreFileRealPath();
    /**
     * 实际存放文件分片的路径前缀
     */
    private String rootFileChunkPath= FileUtil.generateDefaultStoreFileChunkRealPath();
}
