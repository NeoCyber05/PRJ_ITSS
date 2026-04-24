package org.itss.prj_itss.service;

import org.itss.prj_itss.entity.Merchandise;
import org.itss.prj_itss.repository.MerchandiseRepository;

import java.util.List;

public final class MerchandiseService {

    private final MerchandiseRepository merchandiseRepository;

    public MerchandiseService(MerchandiseRepository merchandiseRepository) {
        this.merchandiseRepository = merchandiseRepository;
    }

    public List<Merchandise> findAll() {
        return merchandiseRepository.findAll();
    }

    public Merchandise findById(int id) {
        return merchandiseRepository.findById(id);
    }

    public int countAll() {
        return merchandiseRepository.countAll();
    }
}
