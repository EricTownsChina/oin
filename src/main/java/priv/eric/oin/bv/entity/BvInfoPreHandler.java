package priv.eric.oin.bv.entity;

import java.lang.annotation.*;

/**
 * Desc: BvInfo的预处理
 *
 * @author EricTownsChina@outlook.com
 * create 2022/4/5 20:24
 */
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Target(ElementType.METHOD)
public @interface BvInfoPreHandler {

    /**
     * 预处理顺序
     *
     * @return 预处理顺序, 且不能相同
     */
    int order() default 0;

    /**
     * 预处理描述
     *
     * @return 预处理描述
     */
    String desc() default "";

}
