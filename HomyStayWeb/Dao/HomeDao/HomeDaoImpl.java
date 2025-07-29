package com.HomyStayWeb.Dao.HomeDao;

import com.HomyStayWeb.Tools.SQLExecutor;
import com.HomyStayWeb.beans.Home;

import java.sql.SQLException;
import java.util.List;

public class HomeDaoImpl implements HomeDao {
    SQLExecutor sqlExecutor=new SQLExecutor();
    SQLExecutor.SQLPrepared sqlPrepared = null;

    public int addHome(Home home) {

        String sql = "insert into tb_home values(null,?,?,?,?,?,?)";
        Object[] params = sqlExecutor.getParams(home.getNum(), home.getH_Type(), home.getPrice(), home.getState(), home.getFile(), home.getText());
        try {
            return sqlExecutor.executeUpdate(sql, params);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Home> getAllHome(String num) {

        String sql = "select * from tb_home where Num=?";
        sqlPrepared = new SQLExecutor.SQLPrepared(sql, num);
        try {
            return sqlExecutor.executeQuery(sqlPrepared, Home.class);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Home> getAllHome() {

        String sql = "select * from tb_home";
        try {
            return sqlExecutor.executeQuery(sql, Home.class);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Home> getHomeById(int id) {
        String sql = "select * from tb_home where Id=?";
        sqlPrepared = new SQLExecutor.SQLPrepared(sql, id);
        try {
            return sqlExecutor.executeQuery(sqlPrepared, Home.class);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int UpDateById(int id, Home home) {

        String sql = "update tb_home set num=?,h_Type=?,price=?,state=?,file=?,text=? where Id=?";
        Object[] params = sqlExecutor.getParams(home.getNum(), home.getH_Type(), home.getPrice(), home.getState(), home.getFile(), home.getText(), id);
        sqlPrepared = new SQLExecutor.SQLPrepared(sql, params);
        try {
            return sqlExecutor.executeUpdate(sqlPrepared);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int DeleteById(int id) {

        String sql="select * from tb_home where Id=?";
        sqlPrepared = new SQLExecutor.SQLPrepared(sql, id);
        try {
            List<Home> list=sqlExecutor.executeQuery(sqlPrepared,Home.class);
            if(!list.isEmpty()) {
                if(list.get(0).getState().equals("已入住")) return 0;
            }else return 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        sql = "delete from tb_home where Id=?";
        sqlPrepared = new SQLExecutor.SQLPrepared(sql, id);
        try {
            return sqlExecutor.executeUpdate(sqlPrepared);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public List<Home> CheckByNum(int num) {

        String sql = "SELECT * FROM tb_home WHERE num LIKE ?";
        sqlPrepared = new SQLExecutor.SQLPrepared(sql, "%" + num + "%");
        try {
            return sqlExecutor.executeQuery(sqlPrepared,Home.class);
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
