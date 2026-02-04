package com.lollito.fm.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.lollito.fm.model.User;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendEmailVerification(String to, String name, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Verify your email");
        message.setText("Hello " + name + ",\n\nPlease verify your email using this token: " + token);
        mailSender.send(message);
    }

    public void sendWelcomeEmail(User user) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Welcome!");
        message.setText("Welcome " + user.getFirstName() + "!");
        mailSender.send(message);
    }

    public void sendBanNotificationEmail(User user, String reason) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Account Banned");
        message.setText("Your account has been banned. Reason: " + reason);
        mailSender.send(message);
    }

    public void sendUnbanNotificationEmail(User user) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Account Unbanned");
        message.setText("Your account has been unbanned.");
        mailSender.send(message);
    }

    public void sendTemporaryPasswordEmail(User user, String tempPassword) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Temporary Password");
        message.setText("Your temporary password is: " + tempPassword);
        mailSender.send(message);
    }

    public boolean sendPasswordResetEmail(User user, String token) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(user.getEmail());
            message.setSubject("Password Reset Request");
            message.setText("Use this token to reset your password: " + token);
            mailSender.send(message);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void sendPasswordResetConfirmationEmail(User user) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Password Changed");
        message.setText("Your password has been changed successfully.");
        mailSender.send(message);
    }
}
