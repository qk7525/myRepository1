package com.itheima.test;

import com.itheima.dao.BookDao;
import com.itheima.dao.impl.BookDaoImpl;
import com.itheima.domain.Book;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.Test;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: QK
 * @Date: 2019/6/16 9:18
 * @Version 1.0
 */
public class LuceneTest {

    private BookDao bookDao=new BookDaoImpl() ;


    //新增数据到索引库(单条document)==>indexWriter.addDocument(doc);
    @Test
    public void testCreateIndexOne() throws IOException {

        //创建分词器  ==>StandardAnalyzer标准分词器
        Analyzer analyzer=new StandardAnalyzer();
        //Analyzer analyzer=new IKAnalyzer();
        //IndexWriterConfig配置了IndexWriter对象的参数信息
        IndexWriterConfig config=new IndexWriterConfig(analyzer);
        //索引存储位置
        Directory directory=FSDirectory.open(new File("D:/luceneIndex").toPath());
        /*  Directory d,  IndexWriterConfig config  */
        //创建IndexWriter写入对象
        IndexWriter indexWriter=new IndexWriter(directory,config);

        //创建文档对象
        Document doc=new Document();
        doc.add(new TextField("id","1" , Field.Store.YES));
        doc.add(new TextField("name","论语" , Field.Store.YES));
        doc.add(new TextField("price","50.5" , Field.Store.YES));
        doc.add(new TextField("pic","2.jps" , Field.Store.YES));
        doc.add(new TextField("desc","经典著作" , Field.Store.YES));

        //将文档写入到索引库
        indexWriter.addDocument(doc);
        //提交操作
        indexWriter.commit();
        //回收资源
        indexWriter.close();
    }



    //新增数据到索引库(从数据库中)==>indexWriter.addDocuments(docList);
    @Test
    public void testCreateIndexFormDateBase() throws Exception {
        //从数据库中获取所有Book的集合
        List<Book> bookList= bookDao.queryBookList();

        List<Document> docList=new ArrayList<>();

        //bookList==>转化为==》docList
        for (Book book : bookList) {
            Document doc=new Document();
          /*  doc.add(new TextField("id",book.getId()+"" , Field.Store.YES ));
            doc.add(new TextField("name",book.getName()+"" , Field.Store.YES ));
            doc.add(new TextField("price",book.getPrice()+"" , Field.Store.YES ));
            doc.add(new TextField("pic",book.getPic()+"" , Field.Store.YES ));
            doc.add(new TextField("desc",book.getDesc()+"" , Field.Store.YES ));*/


          //使用分词器首先判断字段使用哪一种Field  --？
                    // 1.是否分词
                    //2.是否索引
                    //3.是否储存
            doc.add(new StoredField("id",book.getId()));  //使用StoredField 不分词/不索引/要储存
            doc.add(new TextField("name",book.getName()+"" , Field.Store.YES )); //TextField：要分词/要索引/要储存YES
            doc.add(new FloatField("price",book.getPrice() , Field.Store.YES )); //FloatField：要分词/要索引/要储存YES
            doc.add(new StoredField("pic",book.getPic()));                       //StoredField：不分词/不索引/要储存
            doc.add(new TextField("desc",book.getDesc()+"" , Field.Store.NO ));//TextField：要分词/要索引/不储存NO
            docList.add(doc);
        }


        //创建分词器
        //Analyzer analyzer=new StandardAnalyzer();
        Analyzer analyzer=new IKAnalyzer();  //中文ik分词器
        //IndexWriterConfig配置了IndexWriter对象的参数信息
        IndexWriterConfig config=new IndexWriterConfig(analyzer);
        //索引存储位置
        Directory directory=FSDirectory.open(new File("D:/luceneIndex").toPath());
        /*  Directory d,  IndexWriterConfig config  */
        //创建IndexWriter写入对象
        IndexWriter indexWriter=new IndexWriter(directory,config);


        //将文档集合写入到索引库
        indexWriter.addDocuments(docList);
        //提交操作
        indexWriter.commit();
        //回收资源
        indexWriter.close();
    }


    //查找索引 ==》indexSearcher.search(query, 10);
    @Test
    public void testSearchIndex() throws Exception {
        // 创建分词器
        Analyzer analyzer = new StandardAnalyzer();

        // 创建Query
        // 指定搜索的字段和分词器
        // 参数一：值默认搜索域（当搜索条件没有指定搜索域的时候，默认使用该搜索域搜索）
        // 参数二：指定分词器
        QueryParser queryParser = new QueryParser("desc",analyzer);
        // 指定搜索的内容（当指定搜索域为name字段的时候，默认搜索域将失效）
        Query query = queryParser.parse("desc:1");


        //创建Directory流对象，声明索引库位置
        Directory directory=FSDirectory.open(new File("D:/luceneIndex").toPath());

        //创建索引读取对象IndexReader
        IndexReader indexReader= DirectoryReader.open(directory);
        //创建索引搜索对象 IndexSearcher
        IndexSearcher indexSearcher=new IndexSearcher(indexReader);

        // indexSearcher执行查询,10表示查询前10条记录
        TopDocs topDocs = indexSearcher.search(query, 10);
        System.out.println("总记录条数："+topDocs.totalHits);

        // 获得查询的文档数据
        ScoreDoc[] docs = topDocs.scoreDocs;

        for (ScoreDoc scoreDoc : docs) {
            System.out.println("文档得分："+scoreDoc.score);
            System.out.println("文档唯一编号："+scoreDoc.doc);
            Document doc = indexSearcher.doc(scoreDoc.doc);

            System.out.println("bookId:" + doc.get("id"));
            System.out.println("name:" + doc.get("name"));
            System.out.println("price:" + doc.get("price"));
            System.out.println("pic:" + doc.get("pic"));
            System.out.println("=============================");
        }

        //关闭资源
        indexReader.close();

    }



    /**删除索引，根据Term项删除索引 indexWriter.deleteDocuments(new Term("name", "论语"));*/
    @Test
    public void testIndexDelete() throws Exception {
        // 创建Directory流对象
        Analyzer analyzer = new IKAnalyzer();
        Directory directory = FSDirectory.open(new File("D:/luceneIndex").toPath());
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        // 创建写入对象
        IndexWriter indexWriter = new IndexWriter(directory, config);

        // 根据Term删除索引库，以name:java作为条件
        indexWriter.deleteDocuments(new Term("name", "论语"));

        indexWriter.commit();
        // 释放资源
        indexWriter.close();
    }




    /**
     * 修改文档（先删除、再添加） indexWriter.updateDocument(new Term("name", "java"), document);
     * @throws Exception
     */
    @Test
    public void testIndexUpdate() throws Exception {
        // 创建分词器
        Analyzer analyzer = new IKAnalyzer();
        // 创建Directory流对象
        Directory directory = FSDirectory.open(new File("D:/luceneIndex").toPath());
        IndexWriterConfig config = new IndexWriterConfig( analyzer);
        // 创建写入对象
        IndexWriter indexWriter = new IndexWriter(directory, config);

        // 创建Document
        Document document = new Document();
        document.add(new StoredField("id", 1002));
        document.add(new TextField("name", "lucene测试test 002", Field.Store.YES));

        // 执行更新，会把所有符合条件的Document删除，再新增。
        indexWriter.updateDocument(new Term("name", "java"), document);

        indexWriter.commit();

        // 释放资源
        indexWriter.close();
    }


}
