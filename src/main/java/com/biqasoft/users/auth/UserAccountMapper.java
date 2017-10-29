package com.biqasoft.users.auth;

import com.biqasoft.entity.core.useraccount.UserAccount;
import org.javers.common.collections.Lists;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Nikita on 9/12/2016.
 */
public class UserAccountMapper {

    public static List<UserAccount> transform (List<com.biqasoft.users.useraccount.UserAccount> internalUser){
        return internalUser.stream().map(UserAccountMapper::transform).collect(Collectors.toList());
    }

    /**
     * Map internal microservice model to DTO
     * @param msModel internal model
     * @return dto
     */
    public static UserAccount transform (com.biqasoft.users.useraccount.UserAccount msModel){
        UserAccount account = new UserAccount();
        account.setId(msModel.getId());

        account.setFirstname(msModel.getFirstname());
        account.setLastname(msModel.getLastname());
        account.setPatronymic(msModel.getPatronymic());
        account.setEmail(msModel.getEmail());
        account.setTelephone(msModel.getTelephone());

        account.setName(msModel.getName());
        account.setVersion(msModel.getVersion());
        account.setArchived(msModel.isArchived());
        account.setAvatarUrl(msModel.getAvatarUrl());
        account.setCreatedInfo(msModel.getCreatedInfo());
        account.setAlias(msModel.getAlias());

        account.setEnabled(msModel.getEnabled());
        account.setRoles(msModel.getRoles());
        account.setEffectiveRoles(msModel.getEffectiveRoles());
        account.setStatus(msModel.getStatus());
        account.setGroups(msModel.getGroups());
        account.setIpPattern(msModel.getIpPattern());

        account.setLanguage(msModel.getLanguage());
        account.setLastOnline(msModel.getLastOnline());
        account.setPersonalSettings(msModel.getPersonalSettings());
        account.setUsername(msModel.getUsername());
        account.setTwoStepEnabled(msModel.isTwoStepActivated());

        account.setDomains(List.of(msModel.getDomain()));
        return account;
    }

    /**
     * Map dto to internal microservice
     * @param dtoModel dto
     * @return internal model
     */
    public static com.biqasoft.users.useraccount.UserAccount transform (UserAccount dtoModel){
        com.biqasoft.users.useraccount.UserAccount msModel = new com.biqasoft.users.useraccount.UserAccount();
        msModel.setId(dtoModel.getId());

        msModel.setFirstname(dtoModel.getFirstname());
        msModel.setLastname(dtoModel.getLastname());
        msModel.setPatronymic(dtoModel.getPatronymic());
        msModel.setEmail(dtoModel.getEmail());
        msModel.setTelephone(dtoModel.getTelephone());

        msModel.setName(dtoModel.getName());
        msModel.setVersion(dtoModel.getVersion());
        msModel.setArchived(dtoModel.isArchived());
        msModel.setAvatarUrl(dtoModel.getAvatarUrl());
        msModel.setCreatedInfo(dtoModel.getCreatedInfo());
        msModel.setAlias(dtoModel.getAlias());

        msModel.setEnabled(dtoModel.getEnabled());
        msModel.setRoles(dtoModel.getRoles());
        msModel.setStatus(dtoModel.getStatus());
        msModel.setGroups(dtoModel.getGroups());
        msModel.setIpPattern(dtoModel.getIpPattern());

        msModel.setLanguage(dtoModel.getLanguage());
        msModel.setLastOnline(dtoModel.getLastOnline());
        msModel.setPersonalSettings(dtoModel.getPersonalSettings());
        msModel.setUsername(dtoModel.getUsername());

        msModel.setDomains(dtoModel.getDomains());

        return msModel;
    }

}
