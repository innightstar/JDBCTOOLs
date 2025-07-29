package com.HomyStayWeb.Dao.HomeDao;

import com.HomyStayWeb.beans.Home;

import java.util.List;

public interface HomeDao {
    int addHome(Home home);

    List<Home> getAllHome(String num);

    List<Home> getAllHome();

    List<Home> getHomeById(int id);

    int UpDateById(int id, Home home);

    int DeleteById(int i);

    List<Home> CheckByNum(int i);
}
