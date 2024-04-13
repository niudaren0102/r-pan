package xyz.xlls.rpan.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.xlls.rpan.core.constants.RPanConstants;
import xyz.xlls.rpan.core.response.R;

@SpringBootApplication(scanBasePackages = RPanConstants.BASE_COMPONENT_SCAN_PATH)
@ServletComponentScan(basePackages= RPanConstants.BASE_COMPONENT_SCAN_PATH)
@RestController
public class RPanServerLauncher {
    public static void main(String[] args) {
        SpringApplication.run(RPanServerLauncher.class);
    }
    @GetMapping("/hello")
    public R<String> hello(String name){
        return R.success("hello "+name);
    }
}
