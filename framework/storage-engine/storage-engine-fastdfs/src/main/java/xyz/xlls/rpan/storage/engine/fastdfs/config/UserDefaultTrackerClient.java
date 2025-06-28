package xyz.xlls.rpan.storage.engine.fastdfs.config;

import com.github.tobato.fastdfs.domain.StorageNode;
import com.github.tobato.fastdfs.domain.StorageNodeInfo;
import com.github.tobato.fastdfs.service.DefaultTrackerClient;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component("userDefaultTrackerClient")
public class UserDefaultTrackerClient extends DefaultTrackerClient {
    @Override
    public StorageNode getStoreStorage() {
        StorageNode result=super.getStoreStorage();
        result.setPort(2212);
        return result;
    }

    @Override
    public StorageNodeInfo getUpdateStorage(String groupName, String filename) {
        StorageNodeInfo result = super.getUpdateStorage(groupName, filename);
        result.setPort(2212);
        return result;
    }

    @Override
    public StorageNode getStoreStorage(String groupName) {
        StorageNode storeStorage = super.getStoreStorage(groupName);
        storeStorage.setPort(2212);
        return storeStorage;
    }

    @Override
    public StorageNodeInfo getFetchStorage(String groupName, String filename) {
        StorageNodeInfo fetchStorage = super.getFetchStorage(groupName, filename);
        fetchStorage.setPort(2212);
        return fetchStorage;
    }
}
