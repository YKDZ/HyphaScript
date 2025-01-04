package cn.encmys.ykdz.forest.hypha.function;

import cn.encmys.ykdz.forest.hypha.context.Context;
import cn.encmys.ykdz.forest.hypha.value.Reference;
import cn.encmys.ykdz.forest.hypha.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface Function {
    @NotNull
    Reference call(@NotNull Context ctx, @NotNull List<Value> receiversArgs, @NotNull List<Value> args);

    @NotNull List<String> getReceivers();

    Context getCapturedContext();

    void setCapturedContext(@Nullable Context capturedContext);
}
