package xyz.xlls.rpan.web.validator;

import lombok.extern.log4j.Log4j2;
import org.hibernate.validator.HibernateValidator;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;
import xyz.xlls.rpan.core.constants.RPanConstants;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

/**
 * 统一的参数校验器
 */
@SpringBootConfiguration
@Log4j2
public class WebValidatorConfig {
    private static final String FAIL_FAST_KEY="hibernate.validator.fail_fast";
    @Bean
    public MethodValidationPostProcessor methodValidationPostProcessor(){
        MethodValidationPostProcessor processor=new MethodValidationPostProcessor();
        processor.setValidator(rPanValidator());
        log.info("The hibernate validator is loaded successfully!");
        return processor;
    }

    /**
     * 构造项目的方法参数校验器
     * @return
     */
    private Validator rPanValidator() {
        ValidatorFactory factory= Validation.byProvider(HibernateValidator.class)
                .configure()
                .addProperty(FAIL_FAST_KEY, RPanConstants.TRUE_STR)
                .buildValidatorFactory();
        Validator validation=factory.getValidator();
        return validation;
    }
}
