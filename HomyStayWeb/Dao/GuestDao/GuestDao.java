package com.HomyStayWeb.Dao.GuestDao;

import com.HomyStayWeb.beans.Guest;
import com.HomyStayWeb.beans.Home;
import com.HomyStayWeb.beans.Vip;

import java.util.List;

public interface GuestDao {
     boolean GuestAdd(Guest guest);
     List<Guest> GuestList();
     List<Vip> GuestQueryVipByName(String name);
     List<Home> GuestQueryHomeByNum(int num);
     List<Integer> QueryNumByH_TYPE(String hType);
     boolean GuestDelete(int id);
     List<Guest> GuestQueryGuestById(int id);
     boolean GuestUpdate(Guest guest,int id);
     List<Vip> QueryAll();
}
