package com.biqasoft.users.auth;

import com.biqasoft.entity.core.DomainSettings;
import com.biqasoft.microservice.common.MicroserviceDomainSettings;
import com.biqasoft.users.domain.settings.DomainSettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * TODO: refactor; do not implement MicroserviceDomainSettings
 * Created by Nikita on 22.08.2016.
 */
@Service
@Primary
public class LocalDomainSettings implements MicroserviceDomainSettings {

    private final DomainSettingsRepository domainSettingsRepository;

    @Autowired
    public LocalDomainSettings(DomainSettingsRepository domainSettingsRepository) {
        this.domainSettingsRepository = domainSettingsRepository;
    }

    @Override
    public DomainSettings create(DomainSettings domainSettings) {
        return domainSettingsRepository.addDomainSettings(domainSettings);
    }

    @Override
    public void unsafeDelete(String domainSettings) {
        domainSettingsRepository.delete(domainSettings);
    }

    @Override
    public DomainSettings findDomainSetting() {
        return domainSettingsRepository.findDomainSettingsCurrentUser();
    }

    @Override
    public DomainSettings unsafeUpdateDomainSettings(DomainSettings domainSettings) {
        return domainSettingsRepository.update(domainSettings);
    }

    @Override
    public DomainSettings updateDomainSettings(DomainSettings domainSettings) {
        return domainSettingsRepository.updateDomainSettings(domainSettings);
    }

    @Override
    public DomainSettings unsafeFindDomainSettingsById(String domainSettings) {
        return domainSettingsRepository.findDomainSettingsById(domainSettings);
    }
}
