package org.semantics.apigateway.service.auth;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenBlacklist {
    private final ConcurrentHashMap<String, Boolean> blacklistedTokens = new ConcurrentHashMap<>();

    public void blacklistToken(String token) {
        blacklistedTokens.put(token, true);
    }

    public boolean isTokenBlacklisted(String token) {
        return blacklistedTokens.getOrDefault(token, false);
    }
}
