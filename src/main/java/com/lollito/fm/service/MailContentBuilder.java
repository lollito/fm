package com.lollito.fm.service;

import org.springframework.stereotype.Service;

@Service
public class MailContentBuilder {
 
    public MailContentBuilder() {
    }
 
    public String build(String message) {
        return "<html><body>" + message + "</body></html>";
    }
 
}
