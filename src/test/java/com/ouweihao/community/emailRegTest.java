package com.ouweihao.community;

import org.apache.commons.net.smtp.SMTPClient;
import org.apache.commons.net.smtp.SMTPReply;
import org.junit.Test;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.Type;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class emailRegTest {

    //    @Test
    public static void main(String args[]) {
        String str = "1@qq.com";
        String pattern = "^[A-Za-z\\d]+([-_.][A-Za-z\\d]+)*@([A-Za-z\\d]+[-.])+[A-Za-z\\d]{2,4}$";

        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(str);
        System.out.println(m.matches());
    }

    @Test
    public void testIsEmail() {
        String testEmail = "1203497772@qq.com";
        if (!testEmail.matches("[\\w\\.\\-]+@([\\w\\-]+\\.)+[\\w\\-]+")) {
            System.out.println("false");
        }
        String host = "";
        String hostName = testEmail.split("@")[1];
        Record[] result = null;
        SMTPClient client = new SMTPClient();

        try {
            // 查找MX记录
            Lookup lookup = new Lookup(hostName, Type.MX);
            lookup.run();
            if (lookup.getResult() != Lookup.SUCCESSFUL) {
                System.out.println("false");
            } else {
                result = lookup.getAnswers();
            }

            // 连接到邮箱服务器
            for (Record record : result) {
                host = record.getAdditionalName().toString();
                client.connect(host);
                if (!SMTPReply.isPositiveCompletion(client.getReplyCode())) {
                    client.disconnect();
                } else {
                    break;
                }
            }

            //以下2项自己填写快速的，有效的邮箱
            client.login("163.com");
            client.setSender("otestcomm@163.com");
            client.addRecipient(testEmail);
            if (250 == client.getReplyCode()) {
                System.out.println("true");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                client.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("false");
    }
}
