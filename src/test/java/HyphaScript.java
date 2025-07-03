import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.oop.internal.InternalObjectManager;
import cn.encmys.ykdz.forest.hyphascript.script.Script;
import cn.encmys.ykdz.forest.hyphascript.script.ScriptManager;

public class HyphaScript {
    public static void main(String[] args) {
        InternalObjectManager.registerObject(new ConsoleInternalObject());

        Context parentCtx = new Context();
        Context childCtx = new Context();

        Script parent = new Script("""
                export * from "console";
                """);

        Script child = new Script("""
                import { log } from "parent";
                log("yes");
                """);

        ScriptManager.putScript("parent", parent);
        ScriptManager.putScript("child", child);

        System.out.println("父: ");
        System.out.println(parent.evaluate(parentCtx));
        System.out.println(parent);
        System.out.println(parentCtx);

        System.out.println("子: ");
        System.out.println(child.evaluate(childCtx));
        System.out.println(childCtx);
    }
}
