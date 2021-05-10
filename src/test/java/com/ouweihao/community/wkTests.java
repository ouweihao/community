package com.ouweihao.community;

import java.io.IOException;

public class wkTests {

    public static void main(String[] args) {
        String cmd = "D:/wkhtmltopdf/bin/wkhtmltoimage --quality 75 https://www.baidu.com/ E:/IntellijIDEA/bishe/data/wk-images/3.png";
        try {
            Runtime.getRuntime().exec(cmd);
            System.out.println("ok");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
