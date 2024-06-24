package com.raft;


/*
RPC 是一种网络通信协议，允许程序调用远程服务器上的方法，就像调用本地方法一样。
在分布式系统中，RPC 广泛用于节点之前的通信。
属性：
1.term:long -当前任期号，用于判断请求或响应的有效性，并在需要时更新节点的任期号
2.type:String RPC 类型，包括 括AppendEntries、RequestVote等。用于区分不同的RPC请求和响应类型。
3。senderId: String-发送RPC请求的节点ID。用于标识请求的发起者。
4.receiverId: String 接收RPC请求的节点ID。用于标识请求的接收者。
5.logEntries: List<LogEntry> 包含的日志条目列表。在AppendEntries请求中，日志条目被用于复制到其他节点。
6.prevLogIndex: long 前一个日志条目的索引。用于日志一致性检查，确保接收者节点的日志与发送者节点的日志一致。
7.prevLogTerm: long 前一个日志条目的任期号。用于日志一致性检查。
8.leaderCommit: long Leader已知的已提交的最高日志条目的索引。用于更新Follower的已提交日志条目。
9.voteGranted: boolean 表示是否授予投票。在RequestVote响应中使用。


 */




import java.util.List;

public class RPC {
    private long term;
    private RPCType type;
    private String senderId;
    private String receiverId;
    private List<LogEntry> logEntries;
    private long prevLogIndex;
    private long prevLogTerm;
    private long leaderCommit;
    private boolean voteGranted;
    private long lastLogTerm; // 添加用于RequestVote RPC
    private long lastLogIndex; // 添加用于RequestVote RPC
    private boolean success; // AppendEntries RPC响应的结果

    public long getLogEntryIndex() {
        return logEntryIndex;
    }

    public void setLogEntryIndex(long logEntryIndex) {
        this.logEntryIndex = logEntryIndex;
    }

    private long logEntryIndex; // AppendEntries RPC响应的日志条目索引

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    // 构造函数
    public RPC(long term, RPCType type, String senderId, String receiverId, List<LogEntry> logEntries, long prevLogIndex, long prevLogTerm, long leaderCommit, boolean voteGranted, long lastLogTerm, long lastLogIndex,boolean success,long logEntryIndex) {
        this.term = term;
        this.type = type;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.logEntries = logEntries;
        this.prevLogIndex = prevLogIndex;
        this.prevLogTerm = prevLogTerm;
        this.leaderCommit = leaderCommit;
        this.voteGranted = voteGranted;
        this.lastLogTerm = lastLogTerm;
        this.lastLogIndex = lastLogIndex;
        this.success =true;
        this.logEntryIndex =logEntryIndex;
    }

    // Getter methods
    public long getTerm() {
        return term;
    }

    public RPCType getType() {
        return type;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public List<LogEntry> getLogEntries() {
        return logEntries;
    }

    public long getPrevLogIndex() {
        return prevLogIndex;
    }

    public long getPrevLogTerm() {
        return prevLogTerm;
    }

    public long getLeaderCommit() {
        return leaderCommit;
    }

    public boolean isVoteGranted() {
        return voteGranted;
    }

    public long getLastLogTerm() {
        return lastLogTerm;
    }

    public long getLastLogIndex() {
        return lastLogIndex;
    }

    // Setter methods
    public void setTerm(long term) {
        this.term = term;
    }

    public void setType(RPCType type) {
        this.type = type;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public void setLogEntries(List<LogEntry> logEntries) {
        this.logEntries = logEntries;
    }

    public void setPrevLogIndex(long prevLogIndex) {
        this.prevLogIndex = prevLogIndex;
    }

    public void setPrevLogTerm(long prevLogTerm) {
        this.prevLogTerm = prevLogTerm;
    }

    public void setLeaderCommit(long leaderCommit) {
        this.leaderCommit = leaderCommit;
    }

    public void setVoteGranted(boolean voteGranted) {
        this.voteGranted = voteGranted;
    }

    public void setLastLogTerm(long lastLogTerm) {
        this.lastLogTerm = lastLogTerm;
    }

    public void setLastLogIndex(long lastLogIndex) {
        this.lastLogIndex = lastLogIndex;
    }
}
