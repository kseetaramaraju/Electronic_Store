package com.electronics_store.util;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.electronics_store.entity.User;
import com.electronics_store.repository.UserRepository;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Component
@Getter
@Setter
@AllArgsConstructor
public class ScheduleJobs {

    private UserRepository userRepository;

    @SuppressWarnings("null")
    @Scheduled(cron = "0 0 0 * * MON-SUN")
    public void deleteNonVerifiedUser() {
        List<User> listOfNonVerifiedUsers = userRepository.findByIsDeleted(true);
        userRepository.deleteAll(listOfNonVerifiedUsers);
    }
    
}
