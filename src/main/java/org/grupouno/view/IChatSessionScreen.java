package org.grupouno.view;

public interface IChatSessionScreen {
    void setVisible(boolean visible);

    String getCurrentConversationContact();

    String getTextInputArea();

    void setChatTitle(String s);

    void updateConversationList(String contact);

    void setChatAreaText(String messagesByContact);

    void resetTextInputArea();

    void appendNewMessageToChatArea(String s);

    void selectContactInList(String contact);
}
