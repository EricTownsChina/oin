package priv.eric.oin.common.component;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Desc: 切面日志打印
 *
 * @author EricTownsChina@outlook.com
 * create 2022/3/14 0:10
 */
@Slf4j
@Aspect
@Component
public class AspectLog {

    /**
     * 换行符
     */
    private static final String LINE_SEPARATOR = System.lineSeparator();

    @Around("@annotation(org.springframework.web.bind.annotation.PostMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.GetMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.RequestMapping)")
    public Object accountController(ProceedingJoinPoint joinPoint) throws Throwable {
        return doAround(joinPoint);
    }

    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        // 开始打印请求日志
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        assert attributes != null;
        HttpServletRequest request = attributes.getRequest();
        // 获取 @WebLog 注解的描述信息
        /*String methodDescription = getAspectLogDescription(joinPoint);*/
        // 打印请求相关参数
        log.info("========================================== Start ==========================================");
        // 打印请求 url
        log.info("URL            : {}", request.getRequestURL().toString());
        // 打印描述信息
        /*log.info("Description    : {}", methodDescription);*/
        // 打印 Http method
        log.info("HTTP Method    : {}", request.getMethod());
        // 打印调用 controller 的全路径以及执行方法
        log.info("Class Method   : {}.{}", joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName());
        // 打印请求的 IP
        log.info("IP             : {}", request.getRemoteAddr());
        // 打印请求入参
        log.info("Request Args   : {}", getParams(joinPoint));

        long startTime = System.currentTimeMillis();
        Object result = null;
        try {
            result = joinPoint.proceed();
            // 打印出参
            log.info("Response Data  : {}", JSONObject.toJSONString(result));
        } catch (Throwable throwable) {
            log.info("Response ERROR : {}", throwable.getMessage());
            throw throwable;
        } finally {
            // 执行耗时
            log.info("Time-Consuming : {} ms", System.currentTimeMillis() - startTime);
            log.info("=========================================== End ===========================================" + LINE_SEPARATOR);
        }

        return result;
    }

    private String getParams(JoinPoint joinPoint) {
        JSONObject params = new JSONObject();
        // 参数名
        String[] argNames = ((MethodSignature) joinPoint.getSignature()).getParameterNames();
        // 参数值
        Object[] args = joinPoint.getArgs();
        if (args != null && args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                Object arg = args[i];
                if ((arg instanceof HttpServletResponse) || (arg instanceof HttpServletRequest)
                        || (arg instanceof MultipartFile) || (arg instanceof MultipartFile[])) {
                    continue;
                }
                try {
                    params.put(argNames[i], args[i]);
                } catch (Exception e1) {
                    log.error(e1.getMessage());
                }
            }
        }
        return params.toJSONString();
    }

}
