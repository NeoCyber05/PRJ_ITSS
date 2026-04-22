package org.itss.prj_itss.dao;

import org.itss.prj_itss.entity.Site;

import java.util.List;

public interface ISiteDAO {
    List<Site> findAll();
    Site findById(int id);
    Site findBySiteCode(String siteCode);
    int countAll();
}
