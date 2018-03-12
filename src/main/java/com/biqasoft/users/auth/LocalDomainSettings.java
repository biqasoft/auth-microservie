package com.biqasoft.users.auth;

import com.biqasoft.entity.core.DomainSettings;
import com.biqasoft.users.domain.settings.DomainSettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * TODO: refactor; do not implement MicroserviceDomainSettings
 * Created by Nikita on 22.08.2016.
 */
@Service
public class LocalDomainSettings {

    private final DomainSettingsRepository domainSettingsRepository;

    @Autowired
    public LocalDomainSettings(DomainSettingsRepository domainSettingsRepository) {
        this.domainSettingsRepository = domainSettingsRepository;
    }

    public DomainSettings create(DomainSettings domainSettings) {
        return domainSettingsRepository.addDomainSettings(domainSettings);
    }

    public void unsafeDelete(String domainSettings) {
        domainSettingsRepository.delete(domainSettings);
    }

    public DomainSettings findDomainSetting(CurrentUserCtx ctx) {
        return domainSettingsRepository.findDomainSettingsCurrentUser(ctx);
    }

    public DomainSettings unsafeUpdateDomainSettings(DomainSettings domainSettings) {
        return domainSettingsRepository.update(domainSettings);
    }

    public DomainSettings updateDomainSettings(DomainSettings domainSettings, CurrentUserCtx ctx) {
        return domainSettingsRepository.updateDomainSettings(domainSettings, ctx);
    }

    public DomainSettings unsafeFindDomainSettingsById(String domainSettings) {
        return domainSettingsRepository.findDomainSettingsById(domainSettings);
    }
}
