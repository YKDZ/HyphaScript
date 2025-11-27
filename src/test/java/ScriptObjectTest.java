import cn.encmys.ykdz.forest.hyphascript.oop.ScriptObject;
import cn.encmys.ykdz.forest.hyphascript.value.Value;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public class ScriptObjectTest {
    @Test
    void findMember() {
        ScriptObject obj = new ScriptObject();
        obj.declareMember("num", new Value(1));
        obj.declareMember("component", new Value(Component.text("Test")));
        obj.declareMember("string", new Value("String"));

        assertEquals(1, obj.findMember("num", Number.class).map(Number::intValue).orElse(0));
        assertEquals("Test", obj.findMember("component", Component.class).map(component -> ((TextComponent) component).content()).orElse(null));
        assertInstanceOf(String.class, obj.findMember("string", String.class).orElse(null));
    }
}
