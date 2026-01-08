package com.bookstore.identity.service;

import com.bookstore.identity.entity.User;
import com.bookstore.identity.entity.UserLoginHistory;

public interface IUserLoginHistoryService {
    void saveLoginHistory(User user, String ipAddress, String userAgent,
                          UserLoginHistory.LoginStatus status, String failureReason);

}
