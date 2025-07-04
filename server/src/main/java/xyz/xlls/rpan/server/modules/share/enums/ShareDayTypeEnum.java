package xyz.xlls.rpan.server.modules.share.enums;

import cn.hutool.core.util.ObjectUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import xyz.xlls.rpan.core.constants.RPanConstants;

/**
 * 分享日期类型枚举类
 */
@AllArgsConstructor
@Getter
public enum ShareDayTypeEnum {
    PERMANENT_VALIDITY(0,"永久有效",0),
    SEVEN_DAYS_VALIDITY(1,"7天有效",7),
    THIRTY_DAY_VALIDITY(2,"30天有效",30);
    private Integer code;
    private String desc;
    private Integer days;

    /**
     * 根据传过来的分享天数的code获取对应的分享天数的数值
     * @param code
     * @return
     */
    public static Integer getShareDayByCode(Integer code){
        if(ObjectUtil.isNull(code)){
            return RPanConstants.MINUS_ONE_INT;
        }
        for (ShareDayTypeEnum value : ShareDayTypeEnum.values()) {
            if(value.getCode().equals(code)){
                return value.getDays();
            }
        }
        return RPanConstants.MINUS_ONE_INT;
    }
}
