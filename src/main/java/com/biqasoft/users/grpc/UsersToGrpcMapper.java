package com.biqasoft.users.grpc;

import com.biqasoft.auth.internal.grpc.UserOuterClass;
import com.biqasoft.users.useraccount.dbo.UserAccount;

class UsersToGrpcMapper {

    /**
     * Map MS type to grpc
     * @param msModel ms model
     * @return grpc model
     */
    static UserOuterClass.User mapMsModelToGrpc(UserAccount msModel) {
        UserOuterClass.User.Builder account = UserOuterClass.User.newBuilder();

        account.setStatus(msModel.getStatus());
        account.setId(msModel.getId());
        account.setEnabled(msModel.getEnabled());

        // you always must to check for null for grpc setters

        if (msModel.getFirstname() != null) {
            account.setFirstname(msModel.getFirstname());
        }

        if (msModel.getLastname() != null) {
            account.setLastname(msModel.getLastname());
        }

        if (msModel.getPatronymic() != null) {
            account.setPatronymic(msModel.getPatronymic());
        }

        if (msModel.getEmail() != null) {
            account.setEmail(msModel.getEmail());
        }

        if (msModel.getTelephone() != null) {
            account.setTelephone(msModel.getTelephone());
        }

        if (msModel.getName() != null) {
            account.setName(msModel.getName());
        }

        if (msModel.getAvatarUrl() != null) {
            account.setAvatarUrl(msModel.getAvatarUrl());
        }

//        if (msModel.getRoles() != null) {
//            account.addAllRoles(msModel.getRoles());
//        }

        if (msModel.getLanguage() != null) {
            account.setLanguage(msModel.getLanguage());
        }

        if (msModel.getLastOnline() != null) {
            account.setLastOnline(msModel.getLastOnline().getTime());
        }

        if (msModel.getUsername() != null) {
            account.setUsername(msModel.getUsername());
        }

        return account.build();
    }

//    TODO: map from grpc to user
//    static UserOuterClass.User.Builder mapUserToGrpc(UserAccount internalUser) {
//        UserOuterClass.User.Builder account = UserOuterClass.User.newBuilder();
//
//        account.setStatus(internalUser.getStatus());
//        account.setId(internalUser.getId());
//        account.setEnabled(internalUser.getEnabled());
//
//        if (internalUser.getFirstname() != null) {
//            account.setFirstname(internalUser.getFirstname());
//        }
//
//        if (internalUser.getLastname() != null) {
//            account.setLastname(internalUser.getLastname());
//        }
//
//        if (internalUser.getPatronymic() != null) {
//            account.setPatronymic(internalUser.getPatronymic());
//        }
//
//        if (internalUser.getEmail() != null) {
//            account.setEmail(internalUser.getEmail());
//        }
//
//        if (internalUser.getTelephone() != null) {
//            account.setTelephone(internalUser.getTelephone());
//        }
//
//        if (internalUser.getName() != null) {
//            account.setName(internalUser.getName());
//        }
//
//        if (internalUser.getAvatarUrl() != null) {
//            account.setAvatarUrl(internalUser.getAvatarUrl());
//        }
//
////            if (internalUser.getRoles() != null) {
////                account.addAllRoles(internalUser.getRoles());
////            }
//
//        if (internalUser.getLanguage() != null) {
//            account.setLanguage(internalUser.getLanguage());
//        }
//
//        if (internalUser.getLastOnline() != null) {
//            account.setLastOnline(internalUser.getLastOnline().getTime());
//        }
//
//        if (internalUser.getUsername() != null) {
//            account.setUsername(internalUser.getUsername());
//        }
//        return account;
//    }

}
