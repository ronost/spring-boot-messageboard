package se.ronost.dev.messageboard;

public final class MessageBoardUtil {
    private MessageBoardUtil() {
    }

    public static boolean validUserNameFormat(String userName) {
        return userName != null && userName != "";
    }
}
