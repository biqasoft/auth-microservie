package com.biqasoft.users.auth;

import com.biqasoft.entity.core.useraccount.UserAccount;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Nikita on 9/12/2016.
 */
public class TransformUserAccountEntity {

    public static List<UserAccount> transform (List<com.biqasoft.users.useraccount.UserAccount> internalUser){
        return internalUser.stream().map(TransformUserAccountEntity::transform).collect(Collectors.toList());
    }

    public static UserAccount transform (com.biqasoft.users.useraccount.UserAccount internalUser){
        UserAccount account = new UserAccount();
        account.setId(internalUser.getId());

        account.setFirstname(internalUser.getFirstname());
        account.setLastname(internalUser.getLastname());
        account.setPatronymic(internalUser.getPatronymic());
        account.setEmail(internalUser.getEmail());
        account.setTelephone(internalUser.getTelephone());

        account.setName(internalUser.getName());
        account.setVersion(internalUser.getVersion());
        account.setArchived(internalUser.isArchived());
        account.setAvatarUrl(internalUser.getAvatarUrl());
        account.setCreatedInfo(internalUser.getCreatedInfo());
        account.setAlias(internalUser.getAlias());

        account.setEnabled(internalUser.getEnabled());
        account.setRoles(internalUser.getRoles());
        account.setEffectiveRoles(internalUser.getEffectiveRoles());
        account.setStatus(internalUser.getStatus());
        account.setGroups(internalUser.getGroups());
        account.setIpPattern(internalUser.getIpPattern());

        account.setLanguage(internalUser.getLanguage());
        account.setLastOnline(internalUser.getLastOnline());
        account.setPersonalSettings(internalUser.getPersonalSettings());
        account.setUsername(internalUser.getUsername());
        account.setTwoStepEnabled(internalUser.isTwoStepActivated());

        account.setDomains(List.of(internalUser.getDomain()));

        return account;
    }

    public static com.biqasoft.users.useraccount.UserAccount transform (UserAccount internalUser){
        com.biqasoft.users.useraccount.UserAccount account = new com.biqasoft.users.useraccount.UserAccount();
        account.setId(internalUser.getId());

        account.setFirstname(internalUser.getFirstname());
        account.setLastname(internalUser.getLastname());
        account.setPatronymic(internalUser.getPatronymic());
        account.setEmail(internalUser.getEmail());
        account.setTelephone(internalUser.getTelephone());

        account.setName(internalUser.getName());
        account.setVersion(internalUser.getVersion());
        account.setArchived(internalUser.isArchived());
        account.setAvatarUrl(internalUser.getAvatarUrl());
        account.setCreatedInfo(internalUser.getCreatedInfo());
        account.setAlias(internalUser.getAlias());

        account.setEnabled(internalUser.getEnabled());
        account.setRoles(internalUser.getRoles());
        account.setStatus(internalUser.getStatus());
        account.setGroups(internalUser.getGroups());
        account.setIpPattern(internalUser.getIpPattern());

        account.setLanguage(internalUser.getLanguage());
        account.setLastOnline(internalUser.getLastOnline());
        account.setPersonalSettings(internalUser.getPersonalSettings());
        account.setUsername(internalUser.getUsername());

        account.setDomains(internalUser.getDomains());

        return account;
    }

}
