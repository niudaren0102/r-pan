package xyz.xlls.rpan.server.modules.file.context;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import xyz.xlls.rpan.server.modules.file.entity.RPanUserFile;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 文件重命名上下文对象
 */
@Data
public class UpdateFilenameContext implements Serializable {
    private static final long serialVersionUID = 3338103435772823745L;
    /**
     * 要更新的文件ID
     */
    private Long fileId;
    /**
     * 新的文件名
     */
    private String newFilename;
    /**
     * 当前的登录用户ID
     */
    private Long userId;
    /**
     * 要更新的文件记录实体
     */
    private RPanUserFile entity;
}
