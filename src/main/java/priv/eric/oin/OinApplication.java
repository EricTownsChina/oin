package priv.eric.oin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author ErictownsChina@outlook.com
 */
@MapperScan("priv.eric.oin.dao")
@SpringBootApplication
public class OinApplication {

    public static void main(String[] args) {
        SpringApplication.run(OinApplication.class, args);
    }

}
