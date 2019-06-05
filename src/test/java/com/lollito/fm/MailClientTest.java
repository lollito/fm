package com.lollito.fm;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.lollito.fm.service.MailClient;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MailClientTest {
 
     
    @Autowired
    private MailClient mailClient;
     
    @Test
    @Ignore
    public void shouldSendMail() throws Exception {
        //given
        String recipient = "";
        String message = "Test message content";
        //when
        mailClient.prepareAndSend(recipient, message);
        //then
    }
     
}