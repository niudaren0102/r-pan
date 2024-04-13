package xyz.xlls.rpan.swagger2;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import xyz.xlls.rpan.core.constants.RPanConstants;

/**
 * swagger2配置属性实体
 */
@Component
@ConfigurationProperties(prefix = "swagger2")
@Data
public class Swagger2ConfigProperties {
    private Boolean show=true;
    private String groupName="r-pan";
    private String basePackage= RPanConstants.BASE_COMPONENT_SCAN_PATH;
    private String title="r-pan-server";
    private String description="r-pan-server";
    private String termsOfServiceUrl="http://127.0.0.1:${server.port}";
    private String contactName="xkk";
    private String contactUrl="https://blog.xlls.xyz";
    private String contactEmail="zhangdehenshuaiyy@outlook.com";
    private String version="1.0";
}
