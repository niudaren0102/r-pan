package xyz.xlls.rpan.server.modules.share.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 分享状态枚举类
 */
@AllArgsConstructor
@Getter
public enum ShareStatusEnum {
    NORMAL(0,"正常"),
    FILE_DELETED(1,"有文件被删除");
    private Integer code;
    private String desc;
}
