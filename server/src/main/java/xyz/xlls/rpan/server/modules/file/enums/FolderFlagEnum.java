package xyz.xlls.rpan.server.modules.file.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum FolderFlagEnum {
    /**
     * 非文件夹
     */
    NO(0),
    /**
     * 文件夹
     */
    YES(1);
    private Integer code;
}
