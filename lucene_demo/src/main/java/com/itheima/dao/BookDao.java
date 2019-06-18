package com.itheima.dao;

import com.itheima.domain.Book;

import java.util.List;

/**
 * @Author: QK
 * @Date: 2019/6/16 9:07
 * @Version 1.0
 */
public interface BookDao {
    /**
     * 查询所有的book数据
     * @return
     */
    List<Book>  queryBookList() throws Exception;

}
