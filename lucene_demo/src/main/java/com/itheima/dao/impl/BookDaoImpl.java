package com.itheima.dao.impl;

import com.itheima.dao.BookDao;
import com.itheima.domain.Book;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: QK
 * @Date: 2019/6/16 9:08
 * @Version 1.0
 */
public class BookDaoImpl implements BookDao {


    public List<Book>  queryBookList() throws Exception {
        Class.forName("com.mysql.jdbc.Driver");
        Connection connection = DriverManager.getConnection("jdbc:mysql:///ssm", "root", "root");

        String sql="select * from book";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);

        ResultSet resultSet = preparedStatement.executeQuery();
        List<Book> list = new ArrayList<Book>();
        while (resultSet.next()){
            Book book=new Book();
            book.setId(resultSet.getInt("id"));
            book.setName(resultSet.getString("name"));
            book.setPrice(resultSet.getFloat("price"));
            book.setPic(resultSet.getString("pic"));
            book.setDesc(resultSet.getString("desc"));
            list.add(book);
        }
        return list;
    }
}
