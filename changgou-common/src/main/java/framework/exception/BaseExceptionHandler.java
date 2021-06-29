package framework.exception;

import entity.Result;
import entity.StatusCode;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @description:
 * @author: Benson
 * @time: 2021/6/24 22:42
 */

@ControllerAdvice
public class BaseExceptionHandler {

    /**
     * @description: 异常处理
     * @param e 错误
     * @return: entity.Result
     * @author: Benson
     * @time: 2021/6/24 22:45
     */
    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public Result error(Exception e) {
        e.printStackTrace();
        return new Result(false, StatusCode.ERROR, e.getMessage());
    }
}
