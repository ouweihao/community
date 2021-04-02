package com.ouweihao.community.entity;

/**
 * 封装跟分页相关的信息
 * @author owh
 */

public class Page {

    // 当前页码

    private int current = 1;

    // 每页显示的数目

    private int limit = 10;

    // 数据总数(计算总的总的页数)，总的页数 = 数据总数 / 每页显示数目

    private int rows;

    // 查询路径（用于复用分页链接）

    private String path;

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        // 判断传入的参数是否符合要求
        if(current >= 1) {
            this.current = current;
        }
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        // 判断传入的limit是否符合要求
        if(limit >= 1 && limit <= 100) {
            this.limit = limit;
        }
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        // 判断rows
        if(rows >= 0) {
            this.rows = rows;
        }
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return "Page{" +
                "current=" + current +
                ", limit=" + limit +
                ", rows=" + rows +
                ", path='" + path + '\'' +
                '}';
    }

    /**
     * 获取当前页的起始行
     * @return 当前页的第一行
     */
    public int getOffSet(){
        return (current - 1) * limit;
    }

    /**
     * 获取总页数
     * @return 总页数
     */
    public int getTotal(){
        return rows % limit == 0 ? rows / limit : rows / limit + 1;
    }

    /**
     * 获取当前页码的前几页
     * @return
     */
    public int getFrom(){
        int from = current - 2;
        return Math.max(from, 1);
    }

    /**
     * 获取当前页码的后几页
     * @return
     */
    public int getTo(){
        int to = current + 2;
        return  Math.min(to, getTotal());
    }

}
