package com.qb.zip.controller;

import com.qb.zip.config.ApplicationConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author zoulinjun
 * @title: ZipController
 * @projectName zip
 * @description: TODO
 * @date 2021/2/2 13:14
 */
@RestController
@Slf4j
public class ZipController {

    @Autowired
    private ApplicationConfig config;

    @RequestMapping(value = "/zip.htm")
    public String hello(HttpServletRequest request) {
        try{
            String path = config.getFilePath();
            Executor executor = Executors.newFixedThreadPool(20);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
            log.info(simpleDateFormat.format(new Date()));
            File file = new File(path);
            File[] picFile = file.listFiles();
            int i = 0;
            int errorCount = 0;
            for (File pic:
                    picFile) {
                if(pic.isFile()){
                    executor.execute(()->{
                        String picName  = pic.getName();
                        log.info("开始压缩" + picName);
                        String name = picName.substring(picName.lastIndexOf("."));
                        String fileName = "shop_" + getRandom() + "_"  + simpleDateFormat.format(new Date())  + name;
                        ZipUtil.compressAndCopyFile(pic,new File(path
                                + File.separator + "tmp" + File.separator + picName));
//                        ZipUtil.compressAndCopyFile(pic,new File(path + File.separator + fileName));
                        pic.delete();
                        log.info("压缩成功" + fileName);
//                        ZipUtil.compress(pic);
                    });

                    i++;
                }else{
                    log.error(pic.getAbsolutePath() + "is not a file");
                    errorCount++;
                    continue;
                }
            }
            log.info("一共成功压缩" + i + ",失败" + errorCount);
        }catch (Exception e){
            log.error("系统异常",e);
            return "FAIL";
        }
        return "SUCCESS";
    }

    private long getRandom(){
        return Math.round(Math.random()*1000000000);
    }

    public static void main(String[] args) {
        for (int i = 0;i < 100;i++){
            System.out.println(Math.round(Math.random()*10000));
        }
        System.out.println("shop_297535181_20210208112507443.jpg".length());
    }

}
