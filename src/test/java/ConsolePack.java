import cn.encmys.ykdz.forest.hyphascript.annotions.FunctionParas;
import cn.encmys.ykdz.forest.hyphascript.annotions.FunctionReceivers;
import cn.encmys.ykdz.forest.hyphascript.annotions.Function;
import cn.encmys.ykdz.forest.hyphascript.annotions.PackNamespace;
import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.pack.HyphaPack;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.Arrays;

@PackNamespace("console")
public class ConsolePack implements HyphaPack {
    @Function
    @FunctionReceivers()
    @FunctionParas({"message"})
    public static void log(@NotNull Context ctx) {
        Object str = ctx.findMember("message").getReferedValue().getValue();
        if (str != null && str.getClass().isArray()) System.out.println(Arrays.toString(toArray(str)));
        else System.out.println(ctx.findMember("message").getReferedValue().getValue());
    }

    public static Object[] toArray(Object obj) {
        if (obj == null) {
            throw new IllegalArgumentException("Input object is null");
        }

        if (!obj.getClass().isArray()) {
            throw new IllegalArgumentException("Input object is not an array");
        }

        int length = Array.getLength(obj);
        Object[] newArray = new Object[length];

        for (int i = 0; i < length; i++) {
            newArray[i] = Array.get(obj, i);
        }

        return newArray;
    }
}
