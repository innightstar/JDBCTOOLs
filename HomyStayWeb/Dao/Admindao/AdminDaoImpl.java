package com.HomyStayWeb.Dao.Admindao;

import com.HomyStayWeb.Tools.SQLExecutor;
import com.HomyStayWeb.beans.Admin;

import java.sql.SQLException;

public class AdminDaoImpl implements AdminDao {
    SQLExecutor sqlExecutor=new SQLExecutor();
    @Override
    public boolean Login(String name, String password) {
        try {
            if(!sqlExecutor.executeQuery("select * from tb_admin where username=? AND password=?", Admin.class, name, password).isEmpty()) return true;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return false;
    }
}
