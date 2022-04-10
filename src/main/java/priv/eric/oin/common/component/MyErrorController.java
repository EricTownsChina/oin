package priv.eric.oin.common.component;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import priv.eric.oin.common.entity.ResponseCode;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * Desc: 覆盖boot默认异常处理
 *
 * @author EricTownsChina@outlook.com
 * create 2022/3/27 23:41
 */
@Slf4j
@Controller
public class MyErrorController extends BasicErrorController {
    public MyErrorController(ServerProperties serverProperties) {
        super(new DefaultErrorAttributes(), serverProperties.getError());
    }

    /**
     * 覆盖默认的Json响应
     */
    @Override
    public ResponseEntity<Map<String, Object>> error(HttpServletRequest request) {
        Map<String, Object> body = getErrorAttributes(request, ErrorAttributeOptions.defaults());
        HttpStatus status = getStatus(request);

        // 组装错误返回
        String message = String.valueOf(body.get("message"));
        message = message.isEmpty() ? ResponseCode.FAIL.getDesc() : message;

        Map<String, Object> resp = new HashMap<>();
        resp.put("code", status.value());
        resp.put("msg", message);
        resp.put("data", null);

        // 返回标准格式
        return new ResponseEntity<>(resp, status);
    }
}
