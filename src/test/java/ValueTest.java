import cn.encmys.ykdz.forest.hyphascript.value.Reference;
import cn.encmys.ykdz.forest.hyphascript.value.Value;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ValueTest {
    @Test
    void getAsBigDecimal() {
        assertEquals(0d, new Value(0d).getAsBigDecimal().floatValue());
        assertEquals(1d, new Value(1d).getAsBigDecimal().intValue());
        assertEquals(2d, new Value(2d).getAsBigDecimal().intValue());
        assertEquals(2.5f, new Value(2.5f).getAsBigDecimal().floatValue());
        assertEquals(2.5f, new Value(2.5f).getAsBigDecimal().floatValue());
    }

    @Test
    void getAsBoolean() {
        assertTrue(new Value(true).getAsBoolean());
        assertTrue(new Value(1).getAsBoolean());
        assertTrue(new Value(new String[]{"1"}).getAsBoolean());
        assertFalse(new Value(false).getAsBoolean());
        assertFalse(new Value(0).getAsBoolean());
        assertFalse(new Value("").getAsBoolean());
        assertFalse(new Value(null).getAsBoolean());
        assertTrue(new Value(new int[]{}).getAsBoolean());
    }

    @Test
    void getAsString() {
        assertEquals("1", new Value("1").getAsString());
        assertEquals("1", new Value('1').getAsString());
        assertEquals("1", new Value(1).getAsString());
        assertEquals("12", new Value(12).getAsString());
        assertEquals("null", new Value(null).getAsString());
        assertEquals("void", new Value().getAsString());
        assertEquals("true", new Value(true).getAsString());
        assertEquals("false", new Value(false).getAsString());
        assertEquals("[1, 2, 3]", new Value(new int[]{1, 2, 3}).getAsString());
    }

    @Test
    void getAsChar() {
        assertEquals('1', new Value('1').getAsChar());
        assertEquals('\0', new Value(null).getAsChar());
    }

    @Test
    void getAsArray() {
        Value value = new Value(new Reference[]{new Reference(new Value(1))});
        assertEquals(1d, value.getAsArray().get(0).getReferredValue().getAsBigDecimal().intValue());
    }

    @Test
    void getAsScriptObject() {
        Value value = new Value(Map.of(
                "a", 1d,
                "b", 2d,
                "c", Map.of("d", 3d)
        ));
        assertEquals(2d, value.getAsScriptObject().findMember("b")
                .getReferredValue().getAsBigDecimal().intValue());
    }
}
