import cn.encmys.ykdz.forest.hyphascript.annotions.Function;
import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.exception.ValueException;
import cn.encmys.ykdz.forest.hyphascript.oop.ScriptObject;
import cn.encmys.ykdz.forest.hyphascript.oop.internal.core.MathObject;
import cn.encmys.ykdz.forest.hyphascript.script.EvaluateResult;
import cn.encmys.ykdz.forest.hyphascript.script.Script;
import cn.encmys.ykdz.forest.hyphascript.value.Reference;
import cn.encmys.ykdz.forest.hyphascript.value.Value;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.*;

public class ScriptTest {
    private static @NotNull Value evaluate(@NotNull String script) throws ValueException {
        return evaluate(script, new Context());
    }

    private static @NotNull Value evaluate(@NotNull String script, @NotNull Context ctx) throws ValueException {
        EvaluateResult result = new Script(script).evaluate(ctx);
        if (result.type() != EvaluateResult.Type.SUCCESS) {
            System.out.println(result);
            throw new RuntimeException(result.cause());
        }
        return result.value();
    }

    @Test
    void operator() {
        assertEquals(9d, evaluate("1 + 8").getAsBigDecimal().intValue());
        assertEquals(7d, evaluate("-1 + 8").getAsBigDecimal().intValue());
        assertEquals(-9d, evaluate("-1 - 8").getAsBigDecimal().intValue());
        assertEquals(-8d, evaluate("-1 * 8").getAsBigDecimal().intValue());
        assertEquals(8d, evaluate("-1 * -8").getAsBigDecimal().intValue());
        assertEquals(-2d, evaluate("-4 / 2").getAsBigDecimal().intValue());
        assertEquals(0d, evaluate("-4 % 2").getAsBigDecimal().intValue());
        assertEquals(16d, evaluate("-4 ** 2").getAsBigDecimal().intValue());

        assertEquals(3.1f, evaluate("1.1 + 2").getAsBigDecimal().floatValue());
        assertEquals(-0.9f, evaluate("1.1 - 2").getAsBigDecimal().floatValue());
        assertEquals(2.2f, evaluate("1.1 * 2").getAsBigDecimal().floatValue());
        assertEquals(0.6f, evaluate("1.1 / 2").getAsBigDecimal().floatValue());
        assertEquals(0.55f, evaluate("1.10 / 2").getAsBigDecimal().floatValue());
        assertEquals(1.21f, evaluate("1.1 ** 2").getAsBigDecimal().floatValue());

        assertEquals("ab1", evaluate("\"ab\" + 1").getAsString());
        assertEquals("abed", evaluate("\"ab\" + \"ed\"").getAsString());

        assertEquals(5, evaluate("5 & 7").getAsBigDecimal().intValue());
        assertEquals(5, evaluate("5 | 1").getAsBigDecimal().intValue());
        assertEquals(-5, evaluate("~4").getAsBigDecimal().intValue());
        assertEquals(8, evaluate("2 << 2").getAsBigDecimal().intValue());
        assertEquals(2, evaluate("8 >> 2").getAsBigDecimal().intValue());
        assertEquals(10, evaluate("8 ^ 2").getAsBigDecimal().intValue());
    }

    @Test
    void array() {
        Context ctx = Context.Builder.create()
                .with("arr",
                        new Value(new Reference[]{new Reference(new Value(1)),
                                new Reference(new Value(2)),
                                new Reference(new Value(3)),
                                new Reference(new Value(4))}))
                .with("str", new Value("YKDZ Miao~"))
                .build();
        assertEquals(5d, evaluate("arr[0] + arr[3]", ctx).getAsBigDecimal().intValue());
        assertEquals(10d, evaluate("arr.sum()", ctx).getAsBigDecimal().intValue());
        assertEquals(4d, evaluate("arr.length()", ctx).getAsBigDecimal().intValue());
        assertEquals(10d, evaluate("arr[0:4].sum()", ctx).getAsBigDecimal().intValue());
        assertEquals(5d, evaluate("arr[1:3].sum()", ctx).getAsBigDecimal().intValue());
        assertEquals(3d, evaluate("arr[:2].sum()", ctx).getAsBigDecimal().intValue());
        assertEquals(3d, evaluate("arr[:-2].sum()", ctx).getAsBigDecimal().intValue());
        assertEquals(2d, evaluate("arr[-3:-2].sum()", ctx).getAsBigDecimal().intValue());
        assertEquals(4d, evaluate("arr[::2].sum()", ctx).getAsBigDecimal().intValue());
        assertEquals(6d, evaluate("arr[1::2].sum()", ctx).getAsBigDecimal().intValue());
        assertEquals(6d, evaluate("arr[1:100:2].sum()", ctx).getAsBigDecimal().intValue());
        assertEquals(0d, evaluate("arr[10::2].sum()", ctx).getAsBigDecimal().intValue());
        assertEquals(0d, evaluate("arr[3:1].sum()", ctx).getAsBigDecimal().intValue());
        assertEquals(7d, evaluate("arr[2:].sum()", ctx).getAsBigDecimal().intValue());
        assertEquals(4d, evaluate("arr[::-1][0]", ctx).getAsBigDecimal().intValue());

        assertEquals("Miao", evaluate("str[-5:-1]", ctx).getAsString());
        assertEquals("YKDZ", evaluate("str[0:4]", ctx).getAsString());
        assertEquals("Miao", evaluate("str[5:9]", ctx).getAsString());
        assertEquals("Miao~", evaluate("str[5:100]", ctx).getAsString());
        assertEquals("YKDZ", evaluate("str[:4]", ctx).getAsString());
        assertEquals("Miao~", evaluate("str[5:]", ctx).getAsString());
        assertEquals("YD io", evaluate("str[::2]", ctx).getAsString());
        assertEquals("~oaiM ZDKY", evaluate("str[::-1]", ctx).getAsString());

        assertEquals(45d, evaluate("""
                const result = []
                for (let i = 0; i < 10; i += 1) {
                    result[i] = i
                }
                result.sum()
                """).getAsBigDecimal().intValue());

        assertEquals(1d, evaluate("""
                [1, 2, 3, 4, 5].filter(v => v == 1).sum()
                """).getAsBigDecimal().intValue());
        assertEquals(55d, evaluate("""
                [1, 2, 3, 4, 5].map(v => v ** 2).sum()
                """).getAsBigDecimal().intValue());
        assertEquals(55d, evaluate("""
                const result = []
                [1, 2, 3, 4, 5].forEach(v => result.push(v ** 2))
                result.sum()
                """).getAsBigDecimal().intValue());
    }

    @Test
    void object() {
        Context ctx = Context.Builder.create()
                .with("obj", new Value(ScriptObject.Builder.create()
                        .with("a", new Value(1))
                        .with("b", new Value(2))
                        .with("c", new Value(ScriptObject.Builder.create()
                                .with("d", new Value(3))
                                .build()))
                        .build()))
                .build();
        assertEquals(4d, evaluate("obj.a + obj.c.d", ctx).getAsBigDecimal().intValue());
        assertEquals(3d, evaluate("obj.keys().length()", ctx).getAsBigDecimal().intValue());
        assertEquals(3d, evaluate("obj.values()[:2].sum()", ctx).getAsBigDecimal().intValue());
    }

    @Test
    void future() {
        assertEquals("YKDZ Miao~", evaluate("""
                Future.supply(() => sleep 50) // ms
                    .then(() => "YKDZ")
                    .then((result) => result + " Miao~")
                    .get()
                """).getAsString());
    }

    @Test
    void function() {
        assertEquals("YKDZ Miao~", evaluate("""
                function cat(str) {
                    return str + " Miao~"
                }
                cat("YKDZ")
                """).getAsString());
        assertEquals("YKDZ Miao~", evaluate("""
                ((str) => {
                    return str + " Miao~"
                })("YKDZ")
                """).getAsString());
        assertEquals("Cat YKDZ (age 12) say miao to you", evaluate("""
                const miao = (name, age) => {
                    return `Cat ${name} (age ${age}) say miao to you`
                }
                miao{name="YKDZ", age=12}
                """).getAsString());
    }

    @Test
    void loop() {
        assertEquals(45d, evaluate("""
                let sum = 0
                for (let i = 0; i < 10; i += 1) {
                    sum += i
                }
                sum
                """).getAsBigDecimal().intValue());
        assertEquals(50d, evaluate("""
                let sum = 0
                while (sum < 50) {
                    sum += 1
                }
                sum
                """).getAsBigDecimal().intValue());
        assertEquals(20d, evaluate("""
                let sum = 0
                while (sum < 50) {
                    sum += 1
                    if (sum == 20) break;
                }
                sum
                """).getAsBigDecimal().intValue());
    }

    @Test
    void logic() {
        assertFalse(evaluate("1 == 2").getAsBoolean());
        assertTrue(evaluate("1 != 2").getAsBoolean());
        assertTrue(evaluate("1 < 2").getAsBoolean());
        assertTrue(evaluate("3 > 2").getAsBoolean());
        assertFalse(evaluate("true && false").getAsBoolean());
        assertTrue(evaluate("true || false").getAsBoolean());
    }

    @Test
    void templateString() {
        assertEquals("Hello World 2", evaluate("""
                const world = "World"
                `Hello ${world} ${3 - 1}`
                """).getAsString());
        assertEquals(64d, evaluate("""
                const sum = (strings, ...values) => {
                    return values.sum()
                }
                sum`Hello ${-2} ${8 ** 2} ${3 - 1}`
                """).getAsBigDecimal().intValue());
        assertInstanceOf(Component.class, evaluate("MiniMessage.deser`<red>Test`").getValue());
    }

    @Test
    void assignment() {
        assertEquals(5d, evaluate("""
                const a = 5
                a
                """).getAsBigDecimal().intValue());
        assertEquals(7d, evaluate("""
                let a = 5
                a += 2
                a
                """).getAsBigDecimal().intValue());
        assertEquals(3d, evaluate("""
                let a = 5
                a -= 2
                a
                """).getAsBigDecimal().intValue());
        assertEquals(10d, evaluate("""
                let a = 5
                a *= 2
                a
                """).getAsBigDecimal().intValue());
        assertEquals(2.5f, evaluate("""
                let a = 5.0
                a /= 2
                a
                """).getAsBigDecimal().floatValue());
        assertEquals(25d, evaluate("""
                let a = 5
                a **= 2
                a
                """).getAsBigDecimal().intValue());
        assertEquals(1d, evaluate("""
                let a = 5
                a %= 2
                a
                """).getAsBigDecimal().intValue());
        assertEquals(5d, evaluate("""
                let a = 4
                a := 5
                """).getAsBigDecimal().intValue());
    }

    @Test
    void unpack() {
        assertEquals(3d, evaluate("""
                const arr = [1, 2, 3]
                let [a, b, c] = arr
                c
                """).getAsBigDecimal().intValue());
        assertEquals(4d, evaluate("""
                const obj = {
                    a: 1,
                    b: 2,
                    c: {
                        d: 3
                    }
                }
                let { a,  c: { d } } = obj
                a + d
                """).getAsBigDecimal().intValue());
    }

    @Test
    void component() {
        Context ctx = Context.Builder.create()
                .with("component",
                        new Value(Component.text("Hello World").decorate(TextDecoration.BOLD)
                                .color(TextColor.color(255, 255, 255))))
                .with(new Context.Config(RoundingMode.HALF_UP, RoundingMode.HALF_UP, false, false))
                .build();

        assertEquals("<bold><white>Hello World</white></bold><!italic> :)",
                evaluate("component + \" :)\"", ctx).toReadableString());
        assertEquals("<bold><white>Hello World</white></bold><!italic> :)",
                evaluate("`${component} :)`", ctx).toReadableString());

        ctx.setConfig(new Context.Config(RoundingMode.HALF_UP, RoundingMode.HALF_UP, false, true));

        assertEquals("<bold><white>Hello World<!italic> :)",
                evaluate("component + \" :)\"", ctx).toReadableString());
        assertEquals("<bold><white>Hello World<!italic> :)",
                evaluate("`${component} :)`", ctx).toReadableString());
    }

    @Test
    void config() {
        Value result = evaluate("""
                (() => {
                    return {
                        title: "Title"
                    }
                })()
                """);
        assertTrue(result.getAsScriptObject().findMember("title", Component.class).isPresent());
    }

    @Test
    void export() {
        int count = 0;
        for (Method method : MathObject.class.getDeclaredMethods()) {
            if (method.isAnnotationPresent(Function.class)) {
                count++;
            }
        }

        Context ctx = new Context();
        evaluate("""
                export const a = 5;
                export function test() {
                    return 5;
                }
                export let b = 5;
                export * from "Math";
                """, ctx);
        assertEquals(3 + count, ctx.getExportedMembers().size());
    }

    @Test
    void number() {
        assertEquals(3d, evaluate("(-3).abs()").getAsBigDecimal().intValue());
        assertEquals(-3.5f, evaluate("(-3.5).floatValue()").getAsBigDecimal().floatValue());
        assertEquals(-3d, evaluate("(-3.5).intValue()").getAsBigDecimal().intValue());
        assertEquals(-3.5, evaluate("(-3.5).doubleValue()").getAsBigDecimal().doubleValue());
    }

    @Test
    void forInAndOf() {
        assertEquals(6d, evaluate("""
                let sum = 0
                let arr = [1, 2, 3]
                for (let v of arr) {
                    sum += v
                }
                sum
                """).getAsBigDecimal().intValue());

        assertEquals(3d, evaluate("""
                let sum = 0
                let arr = [10, 20, 30]
                for (let i in arr) {
                    sum += i
                }
                sum
                """).getAsBigDecimal().intValue());

        assertEquals("ab", evaluate("""
                let keys = ""
                let obj = {a: 1, b: 2}
                for (let key in obj) {
                    keys += key
                }
                keys
                """).getAsString());

        assertEquals("[\"VAULT - 0\", \"EXP - 1\"]", evaluate("""
                let keys = ["VAULT", "EXP"]
                const result = []
                
                for (const [i, key] of keys.entries()) {
                    result[i] = key + " - " + i
                }
                
                result
                """).getAsString());
    }

    @Test
    void iterator() {
        assertEquals(6d, evaluate("""
                                const myIterable = {
                                    __iterator__: () => {
                                        let index = 0
                                        const data = [1, 2, 3]
                                        return {
                                            next: () => {
                                                if (index < data.length()) {
                                                    let val = data[index]
                                                    index += 1
                                                    return { value: val, done: false }
                                                } else {
                                                    return { value: null, done: true }
                                                }
                                            }
                                        }
                                    }
                                }
                
                                let sum = 0
                                for (let v of myIterable) {
                                    sum += v
                                }
                sum
                """).getAsBigDecimal().intValue());
    }

    @Test
    void string() {
        assertEquals("YKDZ", evaluate("'YKDZ'").getAsString());
        assertEquals('Y', evaluate("'YKDZ'").getAsChar());
        assertEquals("ABCD", evaluate("\"abcd\".upper()").getAsString());
        assertEquals("efgh", evaluate("\"EFGH\".lower()").getAsString());
        assertEquals("shopping-mode", evaluate("\"SHOPPING_MODE\".lower().replace(\"_\", \"-\")").getAsString());
    }

    @Test
    void bool() {
        assertTrue(evaluate("true").getAsBoolean());
        assertTrue(evaluate("[]").getAsBoolean());
        assertTrue(evaluate("[] != null").getAsBoolean());
        assertTrue(evaluate("null == null").getAsBoolean());
        assertFalse(evaluate("false").getAsBoolean());
        assertFalse(evaluate("\"\"").getAsBoolean());
        assertFalse(evaluate("0").getAsBoolean());
        assertFalse(evaluate("-0").getAsBoolean());
        assertFalse(evaluate("null").getAsBoolean());
    }
}