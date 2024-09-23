package com.accenture.challenge.service;

import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    public void notify(Long userId) {
        System.out.println("Your order is completed");
    };
}
