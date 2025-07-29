package com.HomyStayWeb.Service;

import com.HomyStayWeb.Dao.GuestDao.GuestDao;
import com.HomyStayWeb.Dao.GuestDao.GuestDaoImpl;
import com.HomyStayWeb.beans.Guest;
import com.HomyStayWeb.beans.Home;
import com.HomyStayWeb.beans.Vip;

import javax.swing.plaf.PanelUI;
import java.util.List;


public class GuestService {
    GuestDao guestDao=new GuestDaoImpl();
    public List<Guest> getGuests(){
        return guestDao.GuestList();
    }
    public boolean addGuest(Guest guest) {

        return guestDao.GuestAdd(guest);
    }
    public List<Vip> QueryListByName(String name){
        System.out.println(name);
        return guestDao.GuestQueryVipByName(name);
    }
    public List<Integer> QueryNumByH_Type(String h_type){
        if(h_type==null||h_type.equals("")){return null;}
        else return guestDao.QueryNumByH_TYPE(h_type);
    }
    public boolean Delete(String id) {
        if(id==null|| id.isEmpty()){return false;}
        else return guestDao.GuestDelete(Integer.parseInt(id));
    }
    public List<Guest> QueryGuestById(String id){
        if(id==null||id.isEmpty()){return null;}
        else return guestDao.GuestQueryGuestById(Integer.parseInt(id));
    }
    public boolean UpdateGuest(Guest guest,String id) {
        if(id==null||id.isEmpty()){return false;}
        else return guestDao.GuestUpdate(guest,Integer.parseInt(id));
    }
    public List<Vip> QueryAll(){
        return guestDao.QueryAll();
    }
    public List<Guest> GuestList(){
        return guestDao.GuestList();
    }
}
