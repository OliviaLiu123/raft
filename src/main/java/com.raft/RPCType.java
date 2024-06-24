package com.raft;

public enum RPCType {
    APPENDENTRIES, //用于日志条目的复制和心跳检测
    REQUESTVOTE,// 用于选举Leader
    APPENDENTRIESRESPONSE, //用于响应appendEntries 请求
    REQUESTVOTERESPONSE,// 用于响应requestVote 请求
    CLIENTREQUEST,
}
