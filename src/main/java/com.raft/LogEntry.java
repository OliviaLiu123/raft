package com.raft;
/*
用于表示日志条目，日志条目包含了由客户端提交的命令和一些元数据 如日志条目的索引和它所属的任期
主要作用是为了记录日志条目
属性：索引，任期号和命令

 */
public class LogEntry {

    private long index;
    private long term;
    private String command;

    public long getIndex() {
        return index;
    }

    public long getTerm() {
        return term;
    }

    public String getCommand() {
        return command;
    }



    public LogEntry(long index, long term, String command) {
        this.index = index;
        this.term = term;
        this.command = command;
    }



}
