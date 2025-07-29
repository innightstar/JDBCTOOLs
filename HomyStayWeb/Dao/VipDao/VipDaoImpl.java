package com.HomyStayWeb.Dao.VipDao;

import com.HomyStayWeb.Tools.SQLExecutor;
import com.HomyStayWeb.beans.Guest;
import com.HomyStayWeb.beans.Vip;

import java.sql.SQLException;
import java.util.List;

public class VipDaoImpl implements VipDao {
     SQLExecutor  sqlExecutor = new SQLExecutor();
     SQLExecutor.SQLPrepared sqlPrepared=null;
    int CountRow=0;
    @Override
    public boolean CreatVip(Vip vip) {
        String sql = "insert into tb_vip values(null,?,?,?,?,?,?,?,now(),now())";
        Object[] params = sqlExecutor.getParams(vip.getName(), vip.getSex(), vip.getCard(), vip.getPhone(), vip.getV_type(), vip.getStartTime(), vip.getEndTime());
        sqlPrepared = new SQLExecutor.SQLPrepared(sql, params);
        try {
            int count = sqlExecutor.executeUpdate(sqlPrepared);
            if (count > 0) return true;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return false;
    }
    public List<Vip> Query() {
        String sql = "select * from tb_vip";
        
        try {
            return sqlExecutor.executeQuery(sql,Vip.class);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public List<Vip> QueryByPhone   (String Phone) {
        String sql="select * from tb_vip where phone like '%"+Phone+"%'";
        
        try {
            return sqlExecutor.executeQuery(sql,Vip.class);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Vip> QueryById(int id) {
        
        try {
            return sqlExecutor.executeQuery("select * from tb_vip where id=?",Vip.class,id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public boolean DeleteById(int id) {
        
        String sql="select * from tb_vip where id=?";
        sqlPrepared=new SQLExecutor.SQLPrepared(sql,id);
        try {
            List<Vip> list=sqlExecutor.executeQuery(sqlPrepared,Vip.class);
            if(!list.isEmpty()){
                String name=list.get(0).getName();
                sql="select * from tb_guest where name=?";
                sqlPrepared=new SQLExecutor.SQLPrepared(sql,name);
                List<Guest> list2=sqlExecutor.executeQuery(sqlPrepared,Guest.class);
                if(!list2.isEmpty()) return false;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        sql = "delete from tb_vip where id=?";
         sqlPrepared = new SQLExecutor.SQLPrepared(sql,id);
        
        try {
            CountRow=sqlExecutor.executeUpdate(sqlPrepared);
            return CountRow > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        finally {
            if(sqlPrepared!=null) sqlPrepared=null;
            CountRow=0;
        }
    }
    @Override
    public boolean Update(int id, Vip vip){
        String sql="update tb_vip set name=?,sex=?,card=?,phone=?,v_type=?,startTime=?,endTime=?,updatetime=now() where id=?";
        Object[] params= sqlExecutor.getParams(vip.getName(),vip.getSex(),vip.getCard(),vip.getPhone(),vip.getV_type(),vip.getStartTime(),vip.getEndTime(),id);
        sqlPrepared=new SQLExecutor.SQLPrepared(sql,params);
        
        try {
            CountRow=sqlExecutor.executeUpdate(sqlPrepared);
            if(CountRow>0) return true;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }finally {
            if(sqlPrepared!=null) sqlPrepared=null;
            CountRow=0;
        }
        return false;
    }
}

