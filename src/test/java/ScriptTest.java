import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.exception.ValueException;
import cn.encmys.ykdz.forest.hyphascript.script.EvaluateResult;
import cn.encmys.ykdz.forest.hyphascript.script.Script;
import cn.encmys.ykdz.forest.hyphascript.value.Value;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ScriptTest {
    private static @NotNull Value evaluate(@NotNull String script) throws ValueException {
        EvaluateResult result = new Script(script).evaluate(new Context());
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
        assertEquals(16d, evaluate("-4 ^ 2").getAsBigDecimal().intValue());

        assertEquals(3.1f, evaluate("1.1 + 2").getAsBigDecimal().floatValue());
        assertEquals(-0.9f, evaluate("1.1 - 2").getAsBigDecimal().floatValue());
        assertEquals(2.2f, evaluate("1.1 * 2").getAsBigDecimal().floatValue());
        assertEquals(0.6f, evaluate("1.1 / 2").getAsBigDecimal().floatValue());
        assertEquals(0.55f, evaluate("1.10 / 2").getAsBigDecimal().floatValue());
        assertEquals(1.21f, evaluate("1.1 ^ 2").getAsBigDecimal().floatValue());

        assertEquals("ab1", evaluate("\"ab\" + 1").getAsString());
        assertEquals("abed", evaluate("\"ab\" + \"ed\"").getAsString());

        assertEquals(5, evaluate("5 & 7").getAsBigDecimal().intValue());
        assertEquals(5, evaluate("5 | 1").getAsBigDecimal().intValue());
    }

    @Test
    void array() {
        assertEquals(5d, evaluate("""
                const arr = [1, 2, 3, 4];
                arr[0] + arr[3]
                """).getAsBigDecimal().intValue());
        assertEquals("YKDZ", evaluate("""
                const arr = ["YK", 2, "DZ", 4];
                arr[0] + arr[2]
                """).getAsString());
    }

    @Test
    void object() {
        assertEquals("YKDZ", evaluate("""
                const obj = {
                    a: "YK",
                    b: {
                        c: "DZ"
                    }
                }
                obj.a + obj.b.c
                """).getAsString());
    }

    @Test
    void function() {
        assertEquals("YKDZ Miao~", evaluate("""
                function cat(str) {
                    return str + " Miao~";
                }
                cat("YKDZ")
                """).getAsString());
        assertEquals("YKDZ Miao~", evaluate("""
                ((str) => {
                    return str + " Miao~";
                })("YKDZ")
                """).getAsString());
    }

    @Test
    void loop() {
        assertEquals(45d, evaluate("""
                let sum = 0;
                for (let i = 0; i < 10; i += 1) {
                    sum += i;
                }
                sum
                """).getAsBigDecimal().intValue());
        assertEquals(50d, evaluate("""
                let sum = 0;
                while (sum < 50) {
                    sum += 1;
                }
                sum
                """).getAsBigDecimal().intValue());
        assertEquals(20d, evaluate("""
                let  sum = 0;
                while (sum < 50) {
                    sum += 1;
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
                const world = "World";
                `Hello ${world} ${3 - 1}`
                """).getAsString());
    }

    @Test
    void assignment() {
        assertEquals(5d, evaluate("""
                const a = 5;
                a
                """).getAsBigDecimal().intValue());
        assertEquals(7d, evaluate("""
                let a = 5;
                a += 2;
                a
                """).getAsBigDecimal().intValue());
        assertEquals(3d, evaluate("""
                let a = 5;
                a -= 2;
                a
                """).getAsBigDecimal().intValue());
        assertEquals(10d, evaluate("""
                let a = 5;
                a *= 2;
                a
                """).getAsBigDecimal().intValue());
        assertEquals(2.5f, evaluate("""
                let a = 5.0;
                a /= 2;
                a
                """).getAsBigDecimal().floatValue());
        assertEquals(25d, evaluate("""
                let a = 5;
                a ^= 2;
                a
                """).getAsBigDecimal().intValue());
        assertEquals(1d, evaluate("""
                let a = 5;
                a %= 2;
                a
                """).getAsBigDecimal().intValue());
        assertEquals(1d, evaluate("""
                let a = 5;
                a %= 2;
                a
                """).getAsBigDecimal().intValue());
        assertEquals(5d, evaluate("""
                let a = 4;
                a := 5;
                """).getAsBigDecimal().intValue());
    }

    @Test
    void unpack() {
        assertEquals(3d, evaluate("""
                const arr = [1, 2, 3];
                let [a, b, c] = arr;
                c
                """).getAsBigDecimal().intValue());
        assertEquals(4d, evaluate("""
                const obj = {
                    a: 1,
                    b: 2,
                    c: {
                        d: 3
                    }
                };
                let { a,  c: { d } } = obj;
                a + d
                """).getAsBigDecimal().intValue());
    }
}
