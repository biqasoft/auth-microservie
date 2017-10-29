package com.biqasoft.users.grpc;

import com.biqasoft.users.useraccount.UserAccount;

class UserToGrpcMapper {

    static UserOuterClass.User transform(UserAccount internalUser) {
        UserOuterClass.User.Builder account = UserOuterClass.User.newBuilder();

        account.setStatus(internalUser.getStatus());
        account.setId(internalUser.getId());
        account.setEnabled(internalUser.getEnabled());

        if (internalUser.getFirstname() != null) {
            account.setFirstname(internalUser.getFirstname());
        }

        if (internalUser.getLastname() != null) {
            account.setLastname(internalUser.getLastname());
        }

        if (internalUser.getPatronymic() != null) {
            account.setPatronymic(internalUser.getPatronymic());
        }

        if (internalUser.getEmail() != null) {
            account.setEmail(internalUser.getEmail());
        }

        if (internalUser.getTelephone() != null) {
            account.setTelephone(internalUser.getTelephone());
        }

        if (internalUser.getName() != null) {
            account.setName(internalUser.getName());
        }

        if (internalUser.getAvatarUrl() != null) {
            account.setAvatarUrl(internalUser.getAvatarUrl());
        }

//        if (internalUser.getRoles() != null) {
//            account.addAllRoles(internalUser.getRoles());
//        }

        if (internalUser.getLanguage() != null) {
            account.setLanguage(internalUser.getLanguage());
        }

        if (internalUser.getLastOnline() != null) {
            account.setLastOnline(internalUser.getLastOnline().getTime());
        }

        if (internalUser.getUsername() != null) {
            account.setUsername(internalUser.getUsername());
        }

        return account.build();
    }

    static UserOuterClass.User.Builder mapUserToGrpc(UserAccount internalUser) {
        UserOuterClass.User.Builder account = UserOuterClass.User.newBuilder();

        account.setStatus(internalUser.getStatus());
        account.setId(internalUser.getId());
        account.setEnabled(internalUser.getEnabled());

        if (internalUser.getFirstname() != null) {
            account.setFirstname(internalUser.getFirstname());
        }

        if (internalUser.getLastname() != null) {
            account.setLastname(internalUser.getLastname());
        }

        if (internalUser.getPatronymic() != null) {
            account.setPatronymic(internalUser.getPatronymic());
        }

        if (internalUser.getEmail() != null) {
            account.setEmail(internalUser.getEmail());
        }

        if (internalUser.getTelephone() != null) {
            account.setTelephone(internalUser.getTelephone());
        }

        if (internalUser.getName() != null) {
            account.setName(internalUser.getName());
        }

        if (internalUser.getAvatarUrl() != null) {
            account.setAvatarUrl(internalUser.getAvatarUrl());
        }

//            if (internalUser.getRoles() != null) {
//                account.addAllRoles(internalUser.getRoles());
//            }

        if (internalUser.getLanguage() != null) {
            account.setLanguage(internalUser.getLanguage());
        }

        if (internalUser.getLastOnline() != null) {
            account.setLastOnline(internalUser.getLastOnline().getTime());
        }

        if (internalUser.getUsername() != null) {
            account.setUsername(internalUser.getUsername());
        }
        return account;
    }
}
