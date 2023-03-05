package com.druh.community.entity;

/**
 * 封装分页相关的信息
 */
public class Page {

    // 当前页码
    private int current = 1;
    // 显示上限
    private int limit = 10;
    // 数据总数(用于计算总页数)
    public int rows;
    // 请求路径(用于在分页链接中复用，不记得了请看index.html中分页部分的代码)
    private String path;

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        if (current >= 1) {
            this.current = current;
        }
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        if (limit >= 1 && limit <= 100) {
            this.limit = limit;
        }
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        if (rows >= 0) {
            this.rows = rows;
        }
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    /**
     * 获取当前页的起始行
     * @return
     */
    public int getOffset() {
        //当前页的起始行就是前面所有页的总记录数
        return (current - 1) * limit;
    }

    /**
     * 获取总页数
     * @return
     */
    public int getTotal() {
        if (rows % limit == 0){
            return rows / limit;
        }else {
            return rows / limit + 1;
        }
    }

    /**
     * 获取起始页码（页面下方那个点击换页的那一条，首页、上一页、1、2、3、4、5、下一页、末页）
     * 这个例子中，起始页码就是1
     * @return
     */
    public int getFrom() {
        if (current - 2 >= 1){
            return current - 2;
        }else {
            return 1;
        }
    }

    /**
     * 获取结束页码，首页、上一页、1、2、3、4、5、下一页、末页
     * 这个例子中，结束页码就是5
     * @return
     */
    public int getTo() {
        int total = getTotal();
        if (current + 2 > total){
            return total;
        }else {
            return current + 2;
        }
    }
}
