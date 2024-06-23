# raft
Raft协议中节点角色和状态管理的具体实现：
1. Follower状态：默认状态，响应Leader和Candidate的请求。
超时未收到Leader消息时转变为Candidate。
2.Candidate状态：由Follower超时未收到Leader消息时转变。
发起选举，向其他节点发送RequestVote请求。
获得大多数票数后成为Leader。
3.Leader状态：处理所有客户端请求。
通过AppendEntries RPC复制日志到Followers，确保日志一致性。
确保大多数Followers确认日志条目后提交日志，并通知客户端。
4.选举机制：Follower超时未收到Leader心跳时发起选举，成为Candidate。
发送RequestVote RPC，获得多数票后成为Leader。
如果选举出现票数分散，采用随机化选举超时，避免多个Candidate同时发起选举。
5日志复制：Leader处理客户端请求，将请求作为新的日志条目添加到日志中。
通过AppendEntries RPC将日志条目复制到Followers。
大多数Followers确认日志条目后，Leader提交并应用这些条目。
安全性：

6.确保已提交的条目不会被覆盖或删除。Leader只有在日志与大多数Followers一致时才进行复制。
通过强制Followers复制Leader日志保持一致性。
7.持久化存储：每个节点需要将状态和日志持久化，以便在节点重启后恢复状态。
持久化信息包括currentTerm、votedFor和日志条目。
8.故障恢复：Leader故障时，Followers会在超时后转变为Candidate并发起选举。
新的Leader接管并继续处理客户端请求。
9.网络通信：处理RPC请求，处理超时和重试逻辑。
发送和接收RequestVote和AppendEntries RPC，维护集群状态。
10.客户端交互：客户端将请求发送给Leader，等待响应。
如果Leader故障，客户端重新发送请求到新的Leader。
