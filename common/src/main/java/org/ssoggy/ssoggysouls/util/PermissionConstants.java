package org.ssoggy.ssoggysouls.util;

public final class PermissionConstants {
    private PermissionConstants() {}

    public static final String SECURITY_ERROR_HEADER = "Security Error: On the Limbo server, OP status cannot be used to execute this command.";
    public static final String SECURITY_ERROR_SUGGESTION_START = "Either ";
    public static final String SECURITY_ERROR_SUGGESTION_COMMAND = "/deop";
    public static final String SECURITY_ERROR_SUGGESTION_MIDDLE = " yourself on Limbo, ask an administrator to add you to the whitelist, or have them grant you the bypass permission ";
    public static final String SECURITY_ERROR_SUGGESTION_NODE = "ssoggysouls.bypass-limbo-op-security";
    public static final String SECURITY_ERROR_SUGGESTION_END = ".";
    public static final String SECURITY_ERROR_FALLBACK = "Either /deop yourself on Limbo, ask an administrator to add you to the whitelist, or have them grant you the bypass permission (ssoggysouls.bypass-limbo-op-security).";

    public static final String HOVER_DEOP = "Click to prepare /deop";
    public static final String HOVER_COPY = "Click to copy permission node";
}
