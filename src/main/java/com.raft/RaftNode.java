package com.raft;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

//一个Node 代表一个server
public class RaftNode implements RaftState{
    private String id; // 节点的唯一标识符
    private List<String> peers; // 集群中其他节点的ID列表
    private State state; // 节点状态
    private long electionTimeout; // 选举超时
    private long lastReceived; // 上次接收消息的时间戳
    private String leader; // 当前Leader的标识ID
    private Timer electionTimer; // 用于触发选举的定时器
    private long currentTerm; // 当前任期号
    private String votedFor; // 在当前任期中投票给的候选人ID
    private List<LogEntry> log; // 日志条目列表
    private int commitIndex; // 已提交的日志条目的最高索引
    private int lastApplied; // 最后应用到状态机的日志条目的索引
    private Map<String, Integer> nextIndex; // 对于每个Follower，下一个要发送的日志条目的索引
    private Map<String, Integer> matchIndex; // 对于每个Follower，已知的已复制日志的最高索引
    private int votes; // 当前节点在选举过程中收到的投票数量


    // 默认构造函数
    public RaftNode() {
        this.state = State.FOLLOWER;
        this.currentTerm = 0;
        this.votedFor = null;
        this.log = new ArrayList<>();
        this.commitIndex = 0;
        this.lastApplied = 0;
        this.electionTimeout =  randomElectionTimeout();
        this.lastReceived = 0;
        this.nextIndex = new HashMap<>();
        this.matchIndex = new HashMap<>();
        this.leader = "";
        this.votes = 0;
    }

    // 带参数的构造函数
    public RaftNode(String id, List<String> peers) {
        this(); // 调用默认构造函数初始化
        this.id = id;
        this.peers = peers;
        resetElectionTimeout();
    }



    // Getter 和 Setter 方法
     // pass

//=========================================================================================


    // 当 raftNode 的状态是Follower 时
    /*
    tick()`：处理时间流逝，检查心跳是否超时。
   receive_rpc()`：接收并处理来自其他节点（Leader or Candidate）RPC请求。
   1. 接收来自Leader 的心跳信息AppendEntries RPC, 如果收到心跳就 reset_election_timeout()`：重置选举超时计时器。
   2.接收来自其他candidate 的投票请求，就handle 投票

     */



//tick 主要是处理心跳超时
     public void tick(){
       long currentTime = getCurrentTime();
       if (state==State.FOLLOWER){
           if((currentTime-lastReceived) > electionTimeout){
               //超时转变为candidate 状态
               becomeCandidate();

           }
       }



     }

//receive_rpc 方法用于处理来自其他节点的RPC请求。
     public void FollwerReceivedRPC(RPC rpc){
         //  如果是 Follower 状态
            if (state == State.FOLLOWER){
                if (rpc.getType() == RPCType.APPENDENTRIES){
                    //处理appendentries RPC 来自Leader的心跳消息
                    lastReceived = getCurrentTime();
                    leader = rpc.getSenderId();
                    resetElectionTimeout();//重置选举时间

//                    // 处理附加日志条目逻辑
//                    handleAppendEntries(rpc);


                }else if (rpc.getType() == RPCType.REQUESTVOTE){
                    //处理requestvote RPC 来自Candidate的选举请求
                    // 处理RequestVote RPC
//                    handleRequestVote(rpc);

                }


            }


     }


     //helper function 重置选举超时
     public void resetElectionTimeout(){
       //取消现有的选举定时任务
         if(electionTimer !=null){
             electionTimer.cancel();
         }

         //创建一个新的任务
          electionTimer = new Timer();
         TimerTask task = new TimerTask() {
             @Override
             public void run() {
                 startElection();
             }
         };
// 重新调度选举定时器任务
         electionTimer.schedule(task, electionTimeout);

     }

     // 当raftNode 的状态变成 Candidate 时
    /*
       1.实现becomeCandidate()
       2.会向其他的所有节点发送RequestVoteRPC, 请求他们投票
       3.接收来自其他Candidate 的 RPC 请求
       -如果是来自leader 的心跳AppendEntries RPC ,并该请求的term 大于自己， 就变回Followr becomefollower()，否则忽略
       -如果来自其他candidate 如果小于等于当前任期号，拒绝投票
       4.出来投票结果
       5.启动选举计时器等待选举结果，如果大于半数就成为leader becomeleader()

     */

    //下面的实现是 变成了candidate 状态
    public void becomeCandidate(){
        state=State.CANDIDATE;
        currentTerm+=1; //递增当前任期号
        votedFor = id; //给自己投票

        sendRequestVoteToPeers();// 给其他节点发起投票
        resetElectionTimeout();// 重置选举超时

    }



 // 收到了来自其他candidate 发来的 RPC
    public void CandidateReceivedRPC(RPC rpc){
        //2. 如果是 Candidate 状态
        if (state == State.CANDIDATE){
            if (rpc.getType()==RPCType.REQUESTVOTERESPONSE){
                //处理requestVote 的响应
                if (rpc.isVoteGranted() ==true){
                    votes ++;
                    if(votes > peers.size()/2){
                        //获得多数票成为Leader
                        becomeLeader();
                    }
                }else{
                    // 请求投票被拒绝，可以继续等待其他响应
                }
            } else if (rpc.getType() == RPCType.APPENDENTRIES){
                // 接收来自Leader AppendEntries 的消息
                //转变为Follwer 状态，并更新leader 等信息
                becomeFollower(rpc.getSenderId(),rpc.getTerm());

            }
        }
    }



// 启动选举定时器
    public void startELectionTimer(){
        //设置一个定时器，超时后再次进行选举

 }
// 向其他node 发送投票请求
    public void sendRequestVoteToPeers() {
        int lastLogIndex = log.size() - 1;
        long lastLogTerm;

        if (lastLogIndex >= 0) {
            lastLogTerm = log.get(lastLogIndex).getTerm();
        } else {
            lastLogTerm = 0;
        }

        for (String peer : peers) {
            if (!peer.equals(this.id)) {
                RequestVoteRPC requestVoteRpc = new RequestVoteRPC(
                        currentTerm,
                        id,
                        lastLogIndex,
                        lastLogTerm
                );
                sendRPC(peer, requestVoteRpc);
            }
        }
    }

    public void sendRPC(String peer, RequestVoteRPC requestVoteRpc) {

    }


// 处理来自其他node 发送过来的投票
    public void handleRequestVoteResponse(RequestVoteResponse response){
                if (response.getTerm() > this.currentTerm){
                    becomeFollower(response.getTerm());
                }else if (response.isVoteGranted()){
                    tallyVotes();
                }
    }

    public void tallyVotes(){
        int grantedVotes =0;
        for(String peer:peers){
            if(votedFor.equals(id)){
                grantedVotes++;
            }if (grantedVotes > peers.size() / 2) {
                becomeLeader();
            } else {
                // Wait for more votes or handle timeout
            }
        }

    }


    //如果已经选举出来了，自己不是Leader 就变回 Follower 状态

    @Override
    public void becomeFollower(String leaderId, long term) {

    }

    @Override
    public void becomeFollower( long term){
        state = State.FOLLOWER;
        currentTerm = term;
        votedFor = null;
        resetElectionTimeout();
        //Reset any other follower-specific state


    }








    /*
    其他的实现方式
    这里是 Candidate 将要成为 Leader

     */


    public void startElection(){
        //向其他的node 发送投票请求

    }
    // 收到投票请求
    public void receiveRequestVote(String senderId,long term,String candidateId){
        if (term < currentTerm){
            //忽略旧的RequestVote 消息
        }
        if((state ==State.FOLLOWER) && votedFor ==""){
            //如果当前是Follower且还未投票，可以投票给候选人
            votedFor = candidateId;
            sentVoteGranted(senderId,term);


        }else if ((state ==State.CANDIDATE) && votedFor ==candidateId){
            // 如果当前是Candidate且已为该候选人投票，再次发送投票
            sentVoteGranted(senderId,term);

        }

    }
    //给别人发同意
    public String sentVoteGranted(String sender, long term){
        return "";
    }



    // 获得了投票同意
    public void receiveVoteGranted(String senderId, long term){
        if (term != currentTerm){
            //忽略当前任期不一样的投票

        }
        //更新收到的投票数
        votes++;
        //检查是否收到了大多数节点的投票, 如果是就成为leader
        if (votes > peers.size() ){
            becomeLeader();

        }
    }



 // 当 raftNode 变成了Leader
    /*
    2. 发送 RequestVote RPC 给指定节点
    3.启动心跳
    4.接收来自其他节点的RPC 请求 ，当state =LEADER
    5.处理AppendEntries的响应"
    6. 检查是否可以提交新的日志条目
    7.  应用已提交的日志条目到状态机
    8.处理客户端请求
    9.复制日志条目到所有Followers
    10.发送AppendEntries RPC给指定节点
     */
    public void becomeLeader(){
        //转变为Leader状态，并开始管理集群
        //初始化nextIndex 和matchIndex
        state=State.LEADER;
        nextIndex= new HashMap<>();
        matchIndex=new HashMap<>();
        for (String peer :peers){
            nextIndex.put(peer,log.size()+1);
            matchIndex.put(peer,0);
        }
        //初始化commitIndex为当前日志的最后一条已提交的索引
        commitIndex = matchIndex.values().stream().max(Integer::compare).orElse(0);
        startHeartBeatTimer();

    }



//启动心跳定时器
   public void startHeartBeatTimer(){
       //启动心跳定时器"""
    //设置一个定时器，定期发送AppendEntries RPCs


   }

   // 接收来自其他节点的请求
    public void LeaderReceiveRPC(RPC rpc ,ClientRequest request){
        if (state == State.LEADER) {
           if(rpc.getType() ==RPCType.APPENDENTRIES){
               // # 处理AppendEntries的响应
               handleAppendEntriesResponse( rpc);

           }else if(rpc.getType() ==RPCType.REQUESTVOTE){
               //# 在成为Leader之后仍然可能收到RequestVoteResponse，直接忽略
               //            pass

           }else if(rpc.getType() ==RPCType.CLIENTREQUEST){
               handleClientRequest(request);

           }
        }


    }
// 处理AppendENtries 响应
   public void handleAppendEntriesResponse(RPC response){
       String followerId = response.getSenderId();
       if (response.isSuccess()) {
           nextIndex.put(followerId, (int) (response.getLogEntryIndex() + 1));
           matchIndex.put(followerId, Math.max(matchIndex.get(followerId), (int) response.getLogEntryIndex()));

           // 检查是否可以提交新的日志条目
           maybeCommit();
       } else {
           // If the append entries failed, you might need to decrement nextIndex and retry
           nextIndex.put(followerId, nextIndex.get(followerId) - 1);
           // Optionally retry sending append entries
       }

   }

   // 检查是否可以提交新的日志条目
   public void maybeCommit(){
       int newCommitIndex = matchIndex.values().stream().max(Integer::compare).orElse(commitIndex);
       if (newCommitIndex > commitIndex) {
           commitIndex = newCommitIndex;
           // Apply committed entries to the state machine
           applyCommittedEntries();
       }

   }

// 应用已经提交的条目到状态机
  public void applyCommittedEntries(){
      for (int index = lastApplied + 1; index <= commitIndex; index++) {
          LogEntry entry = log.get(index);
          applyEntry(entry);
          lastApplied = index;
      }

  }

//处理客户请求
  public void handleClientRequest(ClientRequest request){
        //创建新的日志目录
      LogEntry newEntry = new LogEntry(log.size(), currentTerm, request.getCommand());
      log.add(newEntry);

      // 复制日志条目到所有Followers
      replicateLogEntries();
  }

//复制日志

  public void replicateLogEntries(){
        // # 发送AppendEntries RPC到所有Follower节点
      //    # RPC中包含新的日志条目和当前任期号等信息
      // 获取最后一条已提交的日志条目的索引和任期号
      int lastCommittedIndex = commitIndex;
      long lastCommittedTerm = (lastCommittedIndex < log.size()) ? log.get(lastCommittedIndex).getTerm() : 0;

      // 遍历所有的Follower节点
      for (Map.Entry<String, Integer> entry : nextIndex.entrySet()) {
          String serverId = entry.getKey();
          int nextIdx = entry.getValue();

          // 发送AppendEntries RPC
          int prevLogIndex = nextIdx - 1;
          long prevLogTerm = (prevLogIndex < log.size()) ? log.get(prevLogIndex).getTerm() : 0;
          List<LogEntry> entriesToSend = log.subList(nextIdx, log.size()); // 从Follower的nextIndex开始发送日志条目
          int leaderCommit = Math.min(lastApplied, lastCommittedIndex);
          sendAppendEntries(serverId,lastCommittedTerm, prevLogIndex, prevLogTerm, entriesToSend, leaderCommit);
      }




  }

// 发送AppendEntries RPC给指定节点
    public void sendAppendEntries(String nodeId, long term, long prevLogIndex, long prevLogTerm, List<LogEntry> entries, long leaderCommit){
//  """发送AppendEntries RPC给指定节点"""
//    # 构造并发送AppendEntries RPC
//    # ...
    }

    public void applyEntry(LogEntry entry) {
        // 应用日志条目到状态机

    }



    //helper function
    //获取当前时间的函数，需根据实现细节获取当前系统时间。
    public static long getCurrentTime(){
        return System.currentTimeMillis();

    }

    //生成随机的选举超时时间的函数，返回一个介于最小和最大超时之间的随机值
    public static long randomElectionTimeout() {
        int minTimeout = 150;//最小超时时间，单位毫秒
        int maxTimeout = 300;//最大超时时间，单位毫秒
        return ThreadLocalRandom.current().nextInt(minTimeout,maxTimeout+1);
    }


}
