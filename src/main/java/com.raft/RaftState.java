package com.raft;

public interface RaftState {

    void becomeFollower(String leaderId, long term);
    void becomeFollower(long term);
}
