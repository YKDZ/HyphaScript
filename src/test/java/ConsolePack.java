import cn.encmys.ykdz.forest.hypha.annotions.FunctionParas;
import cn.encmys.ykdz.forest.hypha.annotions.FunctionReceivers;
import cn.encmys.ykdz.forest.hypha.annotions.Function;
import cn.encmys.ykdz.forest.hypha.annotions.PackNamespace;
import cn.encmys.ykdz.forest.hypha.context.Context;
import cn.encmys.ykdz.forest.hypha.pack.HyphaPack;
import org.jetbrains.annotations.NotNull;

@PackNamespace("console")
public class ConsolePack implements HyphaPack {
    @Function
    @FunctionReceivers()
    @FunctionParas({"message"})
    public static void log(@NotNull Context ctx) {
        System.out.println(ctx.findMember("message").getReferedValue().getValue());
    }
}
