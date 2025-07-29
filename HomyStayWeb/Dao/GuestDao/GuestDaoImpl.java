package com.HomyStayWeb.Dao.GuestDao;

import com.HomyStayWeb.Tools.SQLExecutor;
import com.HomyStayWeb.beans.Guest;
import com.HomyStayWeb.beans.Home;
import com.HomyStayWeb.beans.Vip;

import java.sql.SQLException;
import java.util.List;

public class GuestDaoImpl implements GuestDao {
    SQLExecutor sqlExecutor = new SQLExecutor();
    SQLExecutor.SQLPrepared sqlPrepared=null;

    @Override
    public boolean GuestAdd(Guest guest) {

        String sql1 = "insert into tb_guest values(null,?,?,?,?,?,?,?,?)";
        Object[] params1 = sqlExecutor.getParams(guest.getName(), guest.getSex(), guest.getCard(), guest.getPhone(), guest.getEnterTime(), guest.getExitTime(), guest.getH_Type(), guest.getNum());
        String sql2 = "update tb_home set state='已入住' where num=?";
        Object[] params2 = sqlExecutor.getParams(guest.getNum());
        String[] SQLs = {sql1, sql2};
        Object[][] params = new Object[SQLs.length][2];
        params[0] = params1;
        params[1] = params2;
        try {
            return sqlExecutor.executeTransaction(SQLs, params);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Guest> GuestList() {
        String sql = "select * from tb_guest";
        try {
            return sqlExecutor.executeQuery(sql, Guest.class);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Vip> GuestQueryVipByName(String name) {

        String sql = "select * from tb_vip where name=?";
        Object[] params = sqlExecutor.getParams(name);
        sqlPrepared = new SQLExecutor.SQLPrepared(sql, params);
        try {
            return sqlExecutor.executeQuery(sqlPrepared, Vip.class);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public List<Home> GuestQueryHomeByNum(int num) {

        String sql = "select * from tb_home where num=?";
        try {
            return sqlExecutor.executeQuery(sql, Home.class);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Integer> QueryNumByH_TYPE(String hType) {

        String sql = "select num from tb_home where h_type=? and state='空房'";
        Object[] params = sqlExecutor.getParams(hType);
        System.out.println(hType);
        sqlPrepared = new SQLExecutor.SQLPrepared(sql, params);
        try {
            return sqlExecutor.executeQuery(sqlPrepared, Integer.class);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Guest> GuestQueryGuestById(int id) {

        String sql = "select * from tb_guest where id=?";
        Object[] params = sqlExecutor.getParams(id);
        sqlPrepared = new SQLExecutor.SQLPrepared(sql, params);
        try {
            System.out.println(sqlExecutor.executeQuery(sqlPrepared, Guest.class));
            return sqlExecutor.executeQuery(sqlPrepared, Guest.class);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean GuestUpdate(Guest guest, int id) {
        List<Guest> Old = GuestQueryGuestById(id);
        int num = Old.get(0).getNum();
        System.out.println(num);
        String sql1 = "update tb_home set state='空房' where num=?";
        Object[] params1 = sqlExecutor.getParams(num);
        String sql2 = "delete from tb_guest where id=?";
        Object[] param2 = sqlExecutor.getParams(id);
        String sql3 = "insert into tb_guest values(null,?,?,?,?,?,?,?,?)";
        Object[] params3 = sqlExecutor.getParams(guest.getName(), guest.getSex(), guest.getCard(), guest.getPhone(), guest.getEnterTime(), guest.getExitTime(), guest.getH_Type(), guest.getNum());
        String sql4 = "update tb_home set state='已入住' where num=?";
        Object[] params4 = sqlExecutor.getParams(guest.getNum());
        String[] SQLs = {sql1, sql2, sql3, sql4};
        Object[][] params = new Object[SQLs.length][];
        params[0] = params1;
        params[1] = param2;
        params[2] = params3;
        params[3] = params4;
        try {
            return sqlExecutor.executeTransaction(SQLs, params);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Vip> QueryAll() {
        String sql = "select * from tb_vip ";
        try {
            return sqlExecutor.executeQuery(sql, Vip.class);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean GuestDelete(int id) {

        Guest guest = GuestQueryGuestById(id).get(0);
        if (guest == null) return false;
        String sql1 = "delete from tb_guest where id=?";
        Object[] params1 = sqlExecutor.getParams(id);
        String sql2 = "update tb_home set state='未打扫' where num=?";
        Object[] params2 = sqlExecutor.getParams(guest.getNum());
        SQLExecutor.SQLPrepared sqlPrepared1 = new SQLExecutor.SQLPrepared(sql1, params1);
        SQLExecutor.SQLPrepared sqlPrepared2 = new SQLExecutor.SQLPrepared(sql2, params2);
        try {
            return sqlExecutor.executeTransaction(sqlPrepared1, sqlPrepared2);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }
}
