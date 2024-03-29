package protocol;

import java.util.List;

public class ReqBatchGetContact {
    public final BaseRequest BaseRequest;
    public final int Count;
    public final List<Contact> List;

    public ReqBatchGetContact(BaseRequest baseRequest, List<Contact> contacts) {
        this.BaseRequest = baseRequest;
        this.Count = contacts.size();
        this.List = contacts;
    }

    public static class Contact {
        public final String UserName;
        public final String EncryChatRoomId;

        public Contact(String userName, String encryChatRoomId) {
            this.UserName = userName;
            this.EncryChatRoomId = encryChatRoomId;
        }
    }
}
