package com.bookstore.identity.service.Impl;

import com.bookstore.identity.entity.User;
import com.bookstore.identity.entity.UserLoginHistory;
import com.bookstore.identity.repository.UserLoginHistoryRepository;
import com.bookstore.identity.service.IUserLoginHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserLoginLoginHistoryServiceImpl implements IUserLoginHistoryService {

    private final UserLoginHistoryRepository loginHistoryRepository;

    @Override
    public void saveLoginHistory(User user, String ipAddress, String userAgent, UserLoginHistory.LoginStatus status, String failureReason) {
        try {
            UserLoginHistory history = UserLoginHistory.builder()
                    .user(user)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .deviceType(parseDeviceType(userAgent))
                    .loginStatus(status)
                    .failureReason(failureReason)
                    .build();

            loginHistoryRepository.save(history);
        } catch (Exception e) {
            log.error("Failed to save login history: {}", e.getMessage());

        }
    }

    private UserLoginHistory.DeviceType parseDeviceType(String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) {
            return UserLoginHistory.DeviceType.UNKNOWN;
        }

        String ua = userAgent.toLowerCase();

        if (ua.contains("mobile") || ua.contains("android") || ua.contains("iphone")) {
            return UserLoginHistory.DeviceType.MOBILE;
        } else if (ua.contains("tablet") || ua.contains("ipad")) {
            return UserLoginHistory.DeviceType.TABLET;
        } else if (ua.contains("mozilla") || ua.contains("chrome") || ua.contains("safari") ||
                ua.contains("firefox") || ua.contains("edge") || ua.contains("opera")) {
            return UserLoginHistory.DeviceType.WEB;
        }

        return UserLoginHistory.DeviceType.UNKNOWN;
    }
}
