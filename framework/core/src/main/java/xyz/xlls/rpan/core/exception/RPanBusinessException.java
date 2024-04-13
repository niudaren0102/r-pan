package xyz.xlls.rpan.core.exception;

import lombok.Data;
import xyz.xlls.rpan.core.response.ResponseCode;

/**
 * 自定义全局业务异常类
 */
@Data
public class RPanBusinessException extends RuntimeException{
    /**
     * 错误码
     */
    public Integer code;
    /**
     * 错误信息
     */
    private String message;
    public  RPanBusinessException(ResponseCode responseCode){
        this.code=responseCode.getCode();
        this.message=responseCode.getDesc();
    }
    public RPanBusinessException(Integer code,String message){
        this.code=code;
        this.message=message;
    }
    public RPanBusinessException(String message){
        this.code=ResponseCode.ERROR_PARAM.getCode();
    }
    public RPanBusinessException(){
        this.code=ResponseCode.ERROR_PARAM.getCode();
        this.message=ResponseCode.ERROR_PARAM.getDesc();
    }

}
