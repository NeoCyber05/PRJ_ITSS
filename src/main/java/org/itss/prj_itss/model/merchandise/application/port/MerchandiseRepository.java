package org.itss.prj_itss.model.merchandise.application.port;

import org.itss.prj_itss.model.merchandise.domain.Merchandise;

import java.util.List;

public interface MerchandiseRepository {
    List<Merchandise> findAll();
    List<Merchandise> findActive();
    Merchandise findById(int id);
    Merchandise findByCode(String code);
    int countAll();
    int create(Merchandise merchandise);
    boolean update(Merchandise merchandise);
    boolean setActive(int merchandiseId, boolean active);
}
