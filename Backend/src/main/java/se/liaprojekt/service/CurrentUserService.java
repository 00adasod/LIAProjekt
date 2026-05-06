package se.liaprojekt.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    // =========================
    // GET ENTRA ID (OID FROM JWT)
    // =========================
    public String getEntraId() {

        // =========================
        // FETCH AUTH FROM SPRING SECURITY CONTEXT
        // =========================
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null) {
            throw new IllegalStateException("No authentication found");
        }

        // =========================
        // AZURE AD JWT TOKEN
        // =========================
        if (auth instanceof JwtAuthenticationToken jwt) {

            // =========================
            // OID = UNIQUE USER ID IN AZURE AD
            // =========================
            String oid = jwt.getToken().getClaimAsString("oid");

            if (oid == null) {
                throw new IllegalStateException("OID claim missing in token");
            }

            return oid;
        }

        // =========================
        // FALLBACK (should rarely happen in prod)
        // =========================
        return auth.getName();
    }

//    public String getEntraId() {
//
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//
//        // =========================
//        // DEV FALLBACK
//        // =========================
//        if (auth == null || auth.getName() == null || auth.getName().equals("anonymousUser")) {
//            return "dev-user-1";
//        }
//
//        return auth.getName();
//    }
}