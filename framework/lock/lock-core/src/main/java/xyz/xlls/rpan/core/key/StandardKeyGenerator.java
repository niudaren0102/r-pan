package xyz.xlls.rpan.core.key;

import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Component;
import xyz.xlls.rpan.core.LockContext;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 标准的key生成器
 */
@Component
public class StandardKeyGenerator extends AbstractKeyGenerator{
    /**
     * 标准的key生成方法
     * 生成格式:className:methodName:parameter1:parameter2:parameter3...:value1:value2:value3...
     * @param lockContext
     * @param keyValueMap
     * @return
     */
    @Override
    protected String doGenerateKey(LockContext lockContext, Map<String, String> keyValueMap) {
        List<String> keyList= Lists.newArrayList();
        keyList.add(lockContext.getClassName());
        keyList.add(lockContext.getMethodName());
        Class[] parameterTypes = lockContext.getParameterTypes();
        if(ArrayUtils.isNotEmpty(parameterTypes)){
            Arrays.stream(parameterTypes).forEach(parameterType -> keyList.add(parameterType.toString()));
        }else{
            keyList.add(Void.class.toString());
        }
        Collection<String> values = keyValueMap.values();
        if(CollectionUtils.isNotEmpty( values)){
            values.stream().forEach(value->keyList.add(value));
        }
        return keyList.stream().collect(Collectors.joining(","));
    }
}
