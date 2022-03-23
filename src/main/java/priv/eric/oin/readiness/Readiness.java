package priv.eric.oin.readiness;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import priv.eric.oin.common.entity.Resp;

/**
 * Desc: 可读性检查
 *
 * @author EricTownsChina@outlook.com
 * create 2022/3/23 20:46
 */
@RestController
public class Readiness {

    @GetMapping("readiness")
    public Resp readiness() {
        return Resp.ok();
    }

}
