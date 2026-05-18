package org.itss.prj_itss.catalog.application;

import org.itss.prj_itss.catalog.domain.Merchandise;
import org.itss.prj_itss.catalog.application.port.MerchandiseRepository;

import java.util.List;

public final class CatalogUseCase {

    private final MerchandiseRepository merchandiseRepository;

    public CatalogUseCase(MerchandiseRepository merchandiseRepository) {
        this.merchandiseRepository = merchandiseRepository;
    }

    public List<Merchandise> findAll() {
        return merchandiseRepository.findAll();
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
