package cn.encmys.ykdz.forest.hyphascript.function;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@FunctionalInterface
public interface JavaFunctionBody {
    @Nullable Object apply(@NotNull Value target, @NotNull List<Value> parameters, @NotNull Context ctx);
}
