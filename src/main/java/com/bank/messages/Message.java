package com.bank.messages;

public class Message {
  private int id;
  private int userId;
  private String message;
  private int viewed;

  /**
   * Constructor for a message.
   * @param id of the message
   * @param userId of the user being sent this message
   * @param message the message
   * @param viewed status of the message
   */
  public Message(int id, int userId, String message, int viewed) {
    this.id = id;
    this.userId = userId;
    this.message = message;
    this.viewed = viewed;
  }

  public String getMessage() {
    return this.message;
  }

  public int getViewed() {
    return this.viewed;
  }

  public int getId() {
    return this.id;
  }

  public  int getUserId() {
    return this.userId;
  }
}
