package org.itss.prj_itss.model.catalog.application.port;

import org.itss.prj_itss.model.catalog.domain.Merchandise;

import java.util.List;

public interface MerchandiseRepository {
    List<Merchandise> findAll();
    Merchandise findById(int id);
    Merchandise findByCode(String code);
    int countAll();
}
