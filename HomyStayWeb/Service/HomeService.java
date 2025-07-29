package com.HomyStayWeb.Service;

import com.HomyStayWeb.Dao.HomeDao.HomeDao;
import com.HomyStayWeb.Dao.HomeDao.HomeDaoImpl;
import com.HomyStayWeb.beans.Home;

import java.util.List;

public class HomeService {
    HomeDao homeDao = new HomeDaoImpl();

    public int addHome(Home home) {
        return homeDao.addHome(home);
    }

    public List<Home> getAllHome(String num) {
        if (num == null || num.equals("")) return homeDao.getAllHome();
        return homeDao.getAllHome(num);
    }

    public List<Home> getHomeById(String id) {
        return homeDao.getHomeById(Integer.parseInt(id));
    }

    public boolean UpdateById(String id, Home home) {
        System.out.println(home.getFile());
        if (homeDao.UpDateById(Integer.parseInt(id), home) > 0) return true;
        return false;
    }

    public boolean DeleteById(String id) {
        if (homeDao.DeleteById(Integer.parseInt(id)) > 0) return true;
        return false;
    }

    public List<Home> CheckByNum(String findByNum) {
        if (findByNum==null || findByNum.isEmpty()) return homeDao.getAllHome();
        return homeDao.CheckByNum(Integer.parseInt(findByNum));
    }
}
