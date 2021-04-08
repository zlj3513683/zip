package com.qb.zip.config;

import lombok.Data;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author zoulinjun
 * @title: ApplicationConfig
 * @projectName zip
 * @description: TODO
 * @date 2021/2/2 15:23
 */
@Data
@Component
@Configurable
public class ApplicationConfig implements BeanNameAware {

    @Value("${file_path}")
    private String filePath;

    @Override
    public void setBeanName(String s) {
        System.out.println(filePath);
    }
}
