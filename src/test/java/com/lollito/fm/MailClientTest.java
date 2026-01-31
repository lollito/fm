package com.lollito.fm;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;

import com.lollito.fm.service.MailClient;

@SpringBootTest
@WithMockUser(username = "lollito")
public class MailClientTest {
 
     
    @Autowired
    private MailClient mailClient;
     
    @Test
    public void shouldSendMail() throws Exception {
        //given
        String recipient = "";
        String message = "Test message content";
        //when
        mailClient.prepareAndSend(recipient, message);
        //then
    }
     
}