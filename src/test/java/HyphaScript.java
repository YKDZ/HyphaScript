import cn.encmys.ykdz.forest.hypha.context.Context;
import cn.encmys.ykdz.forest.hypha.pack.PackManager;
import cn.encmys.ykdz.forest.hypha.script.Script;
import cn.encmys.ykdz.forest.hypha.script.ScriptManager;
import cn.encmys.ykdz.forest.hypha.value.Value;

public class HyphaScript {
    public static void main(String[] args) {
        PackManager.registerPack(new ConsolePack());
        String mainStr = """
                import "console";
                console.log(`Hello World! ${person.getName()}.`);
                """;
        Context mainCtx = Context.Builder.create()
                .with("person", new Value(new Test("YKDZ", 18)))
                .build();
        Script main = ScriptManager.createScript("main", mainStr, mainCtx);
//        System.out.println(main.evaluate());
        main.evaluate();

//        // 创建一个包含多个 Value 对象的数组
//        Reference[] values = new Reference[] {
//                new Reference(null, new Value(1), false, false),
//                new Reference(null, new Value(2), false, false),
//                new Reference(null, new Value(3), false, false),
//        };
//
//        // 将数组包装为 Value 对象
//        Value arrayValue = new Value(values);
//
//        // 获取引用数组
//        Reference[] references = arrayValue.getAsArray();
//
//        // 修改引用数组中的 Value 内容
//        references[0].getReferedValue().setValue(100);
//
//        // 检查原数组是否被修改
//        System.out.println((arrayValue.getAsArray()[0])); // 输出 100
    }
}
