import cn.encmys.ykdz.forest.hyphascript.annotions.*;
import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.oop.internal.InternalObject;
import org.jetbrains.annotations.NotNull;

@ObjectName("console")
public class ConsoleInternalObject extends InternalObject {
    @Static
    @Function("log")
    @FunctionParas({"message"})
    public static void log(@NotNull Context ctx) {
        System.out.println(ctx.findMember("message").getReferredValue().getAsString());
    }
}
