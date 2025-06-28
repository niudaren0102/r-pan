package xyz.xlls.rpan.storage.engine.fastdfs.config;

import cn.hutool.core.collection.CollectionUtil;
import com.github.tobato.fastdfs.conn.ConnectionPoolConfig;
import com.github.tobato.fastdfs.conn.FdfsConnectionPool;
import com.github.tobato.fastdfs.conn.PooledConnectionFactory;
import com.github.tobato.fastdfs.conn.TrackerConnectionManager;
import lombok.Data;
import org.assertj.core.util.Lists;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableMBeanExport;
import org.springframework.jmx.support.RegistrationPolicy;
import xyz.xlls.rpan.core.exception.RPanBusinessException;

import java.util.List;

/**
 * fastdfs文件存储引擎配置类
 * 3、是为了防止和原来的配置类冲突
 * 4、扫描fastdfs下的组件
 */
@SpringBootConfiguration
@Data
@ConfigurationProperties(prefix = "xyz.xlls.rpan.storage.engine.fdfs")
@EnableMBeanExport(registration = RegistrationPolicy.IGNORE_EXISTING)
@ComponentScan(value = {"com.github.tobato.fastdfs.service","com.github.tobato.fastdfs.domain"})
public class FastDFSStorageEngineConfig {
    /**
     * 链接的超时时间
     */
    private Integer connectTimeout= 600;
    /**
     * 跟踪服务器地址列表
     */
    private List<String> trackerList= Lists.newArrayList();
    /**
     * 组名称
     */
    private String group= "group1";
    @Bean
    public PooledConnectionFactory pooledConnectionFactory(){
        PooledConnectionFactory factory=new PooledConnectionFactory();
        factory.setConnectTimeout(connectTimeout);
        return factory;
    }
    @Bean
    public ConnectionPoolConfig connectionPoolConfig(){
        return new ConnectionPoolConfig();
    }
    @Bean
    public FdfsConnectionPool fdfsConnectionPool( ConnectionPoolConfig connectionPoolConfig,PooledConnectionFactory pooledConnectionFactory){
        FdfsConnectionPool fdfsConnectionPool = new FdfsConnectionPool(pooledConnectionFactory, connectionPoolConfig);
        return fdfsConnectionPool;
    }
    @Bean
    public TrackerConnectionManager trackerConnectionManager(FdfsConnectionPool fdfsConnectionPool){
        TrackerConnectionManager trackerConnectionManager = new TrackerConnectionManager(fdfsConnectionPool);
        if(CollectionUtil.isEmpty(trackerList)){
            throw new RPanBusinessException("the tracker list is empty!");
        }
        trackerConnectionManager.setTrackerList(trackerList);
        return trackerConnectionManager;
    }
}
