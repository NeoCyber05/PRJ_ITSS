package org.itss.prj_itss.repository;

import org.itss.prj_itss.entity.Merchandise;

import java.util.List;

public interface MerchandiseRepository {
    List<Merchandise> findAll();
    Merchandise findById(int id);
    Merchandise findByCode(String code);
    int countAll();
}
