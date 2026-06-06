package org.itss.prj_itss.model.merchandise.application.port;

import org.itss.prj_itss.model.merchandise.domain.Merchandise;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public interface MerchandiseRepository {
    List<Merchandise> findAll();
    List<Merchandise> findActive();
    Merchandise findById(int id);
    Merchandise findByCode(String code);
    int countAll();
    int create(Merchandise merchandise);
    boolean update(Merchandise merchandise);
    boolean setActive(int merchandiseId, boolean active);

    default Map<Integer, Merchandise> findByIds(Collection<Integer> ids) {
        Map<Integer, Merchandise> merchandiseById = new LinkedHashMap<>();
        if (ids == null) {
            return merchandiseById;
        }
        for (Integer id : ids) {
            if (id == null) {
                continue;
            }
            Merchandise merchandise = findById(id);
            if (merchandise != null) {
                merchandiseById.put(id, merchandise);
            }
        }
        return merchandiseById;
    }
}
