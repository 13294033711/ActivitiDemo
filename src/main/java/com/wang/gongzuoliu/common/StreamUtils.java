package com.wang.gongzuoliu.common;

import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 输入输出流工具类
 */
@Slf4j
public class StreamUtils {

    public static String readStream(InputStream is){
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = 0;
            while(( len = is.read(buffer))!=-1){
                baos.write(buffer, 0, len);
            }
            is.close();
            String result = baos.toString();

            if(result.contains("gb2312")){
                return baos.toString("gb2312");
            }else{
                return result;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void closeInputStream(InputStream inputStream) {
        try {
            inputStream.close();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        } finally {
            // 如果inputStream本身为空就不需要再进行关闭操作
            if (null != inputStream) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }
}
