package se.liaprojekt.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

//    public String getEntraId() {
//        return SecurityContextHolder
//                .getContext()
//                .getAuthentication()
//                .getName();
//    }

    public String getEntraId() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // =========================
        // DEV FALLBACK
        // =========================
        if (auth == null || auth.getName() == null || auth.getName().equals("anonymousUser")) {
            return "dev-user-1";
        }

        return auth.getName();
    }
}