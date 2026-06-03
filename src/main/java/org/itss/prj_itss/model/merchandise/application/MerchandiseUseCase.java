package org.itss.prj_itss.model.merchandise.application;

import org.itss.prj_itss.model.merchandise.domain.Merchandise;
import org.itss.prj_itss.model.merchandise.application.port.MerchandiseRepository;

import java.util.List;

public final class MerchandiseUseCase {

    private final MerchandiseRepository merchandiseRepository;

    public MerchandiseUseCase(MerchandiseRepository merchandiseRepository) {
        this.merchandiseRepository = merchandiseRepository;
    }

    public List<Merchandise> findAll() {
        return merchandiseRepository.findAll();
    }

    public List<Merchandise> findActive() {
        return merchandiseRepository.findActive();
    }

    public Merchandise findById(int id) {
        return merchandiseRepository.findById(id);
    }

    public Merchandise findByCode(String code) {
        return merchandiseRepository.findByCode(code);
    }

    public int countAll() {
        return merchandiseRepository.countAll();
    }
}
