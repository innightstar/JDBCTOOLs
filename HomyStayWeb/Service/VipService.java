package com.HomyStayWeb.Service;

import com.HomyStayWeb.beans.Vip;
import com.HomyStayWeb.Dao.VipDao.VipDaoImpl;

import java.util.List;

public class VipService {
    VipDaoImpl vipDaoImpl =new VipDaoImpl();
    public boolean VipCreatService(Vip vip){

        return vipDaoImpl.CreatVip(vip);
    }
    public List<Vip> VipQueryService(){
           return vipDaoImpl.Query();
    }
    public List<Vip> VipQueryServiceByPhone(String Phone){
        if(Phone!=null&&!Phone.equals("")) {
            return vipDaoImpl.QueryByPhone(Phone);
        }
        else
            return vipDaoImpl.Query();
    }
    public  List<Vip> VipQueryServiceById(String idStr){
        int id=0;
        if(idStr!=null&&!idStr.isEmpty())
        {
            id=Integer.parseInt(idStr);
        }
        if(id==0) return null;
        return vipDaoImpl.QueryById(id);
    }
    public boolean VipServiceDeleteById(int id){
        return vipDaoImpl.DeleteById(id);
    }
    public boolean VipServiceUpdate(int id,Vip vip){
        return vipDaoImpl.Update(id,vip);
    }
}
