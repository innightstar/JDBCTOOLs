package com.HomyStayWeb.Dao.VipDao;

import com.HomyStayWeb.beans.Vip;

import java.util.List;

public interface VipDao {
    boolean CreatVip(Vip vip);
    List<Vip> Query();
    List<Vip> QueryByPhone(String phone);
    List<Vip> QueryById(int id);
    boolean DeleteById(int id);
    boolean Update(int id, Vip vip);

}
