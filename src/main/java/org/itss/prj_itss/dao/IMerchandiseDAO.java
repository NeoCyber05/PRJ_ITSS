package org.itss.prj_itss.dao;

import org.itss.prj_itss.entity.Merchandise;

import java.util.List;

public interface IMerchandiseDAO {
    List<Merchandise> findAll();
    Merchandise findById(int id);
    Merchandise findByCode(String code);
    int countAll();
}
