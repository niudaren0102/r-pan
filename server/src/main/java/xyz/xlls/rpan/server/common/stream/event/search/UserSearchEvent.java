package xyz.xlls.rpan.server.common.stream.event.search;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * 用户搜索时间
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class UserSearchEvent implements Serializable {
    private static final long serialVersionUID = 3427739665150107123L;
    private String keyword;
    private Long userId;
    public UserSearchEvent(String keyword, Long userId) {
        this.keyword = keyword;
        this.userId = userId;
    }
}
