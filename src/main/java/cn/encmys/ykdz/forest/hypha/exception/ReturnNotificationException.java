package cn.encmys.ykdz.forest.hypha.exception;

import cn.encmys.ykdz.forest.hypha.value.Reference;
import org.jetbrains.annotations.NotNull;

public class ReturnNotificationException extends RuntimeException {
    @NotNull
    private final Reference returnedReference;

    public ReturnNotificationException(@NotNull Reference returnedReference) {
        this.returnedReference = returnedReference;
    }

    @NotNull
    public Reference getReturnedReference() {
        return returnedReference;
    }
}
