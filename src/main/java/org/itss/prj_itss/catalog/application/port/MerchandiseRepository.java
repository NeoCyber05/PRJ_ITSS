package org.itss.prj_itss.catalog.application.port;

import org.itss.prj_itss.catalog.domain.Merchandise;

import java.util.List;

public interface MerchandiseRepository {
    List<Merchandise> findAll();
    Merchandise findById(int id);
    Merchandise findByCode(String code);
    int countAll();
}
