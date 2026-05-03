package org.itss.prj_itss.service;

import org.itss.prj_itss.entity.Merchandise;
import org.itss.prj_itss.repository.IMerchandiseRepository;

import java.util.List;

public final class MerchandiseService {

    private final IMerchandiseRepository merchandiseRepository;

    public MerchandiseService(IMerchandiseRepository merchandiseRepository) {
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
