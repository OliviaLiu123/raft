package com.raft;



    public class ClientRequest {
        private String command;

        public ClientRequest(String command) {
            this.command = command;
        }

        public String getCommand() {
            return command;
        }
    }


