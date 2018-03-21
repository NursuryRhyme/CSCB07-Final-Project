package com.bank.messages;

public class Message {
  private String message;
  private int messageid;
  private int userid;
  private int viewed;
  
  /**
   * Constructor for Messages.
   * @param message the actual message being sent
   * @param messageid id of the message
   * @param userid of the person being sent this message
   * @param viewed number of times thie message was viewed
   */
  public Message(String message, int messageid, int userid, int viewed) {
    this.message = message;
    this.userid = userid;
    this.messageid = messageid;
    this.viewed = viewed;
  }

  public String getMessage() {
    return this.message;
  }
  
  public int getViewed() {
    return this.viewed;
  }
  
  public int getMessageid() {
    return this.messageid;
  }
  
  public  int getUserid() {
    return this.userid;
  }

}
