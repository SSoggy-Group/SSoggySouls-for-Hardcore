package org.ssoggy.ssoggysouls.hrm.dlc.shared;

public record DlcCommandResult(Status status, String message, String details) {

    public enum Status {
        FALSE,
        TRUE,
        INFO,
        RAW,
        MISSING_ARGS
    }

    public static DlcCommandResult missingArgs(String message, String suggestionCommand) {
        return new DlcCommandResult(Status.MISSING_ARGS, message, suggestionCommand);
    }

    public static DlcCommandResult success(String message) {
        return new DlcCommandResult(Status.TRUE, message, null);
    }

    public static DlcCommandResult fail(String message) {
        return new DlcCommandResult(Status.FALSE, message, null);
    }

    public static DlcCommandResult info(String message) {
        return new DlcCommandResult(Status.INFO, message, null);
    }

    public static DlcCommandResult raw(String message) {
        return new DlcCommandResult(Status.RAW, message, null);
    }
}
