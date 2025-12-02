package cn.encmys.ykdz.forest.hyphascript.value;

import cn.encmys.ykdz.forest.hyphascript.HyphaScript;
import cn.encmys.ykdz.forest.hyphascript.exception.ValueException;
import cn.encmys.ykdz.forest.hyphascript.function.Function;
import cn.encmys.ykdz.forest.hyphascript.oop.ScriptObject;
import cn.encmys.ykdz.forest.hyphascript.utils.StringUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;

public class Value {
    @NotNull
    private final Type type;
    @Nullable
    private final Object value;

    /**
     * Construct a value with Type VOID
     * <p>
     * Try to get value of VOID value will throw ContextException
     *
     * @see #getValue()
     */
    public Value() {
        this.value = null;
        this.type = Type.VOID;
    }

    /**
     * Construct a value
     * <p>
     * The type of value will be inferred automatically
     *
     * @param value Value of value
     */
    public Value(@Nullable Object value) {
        this.type = calType(value);

        if (value != null && type == Type.ARRAY && value.getClass().isArray()) {
            final ScriptArray array = new ScriptArray();
            IntStream.range(0, Array.getLength(value)).forEach(i -> {
                Object elem = Array.get(value, i);
                if (elem instanceof Reference) {
                    array.put(i, (Reference) elem);
                } else {
                    array.put(i, new Reference(new Value(elem)));
                }
            });
            this.value = array;
        } else if (type == Type.SCRIPT_OBJECT && value instanceof Map) {
            final ScriptObject scriptObject = new ScriptObject();
            ((Map<?, ?>) value).forEach((k, v) -> {
                final String name = String.valueOf(k);
                final Reference ref = new Reference(new Value(v));
                scriptObject.declareMember(name, ref);
            });
            this.value = scriptObject;
        } else if (type == Type.STRING && value instanceof Character) {
            this.value = String.valueOf(value);
        } else {
            this.value = value;
        }
    }

    public Value(@NotNull Type type, @NotNull Object value) {
        this.type = type;
        this.value = value;
    }

    private static @NotNull Type calType(@Nullable Object value) {
        if (value instanceof Value) {
            throw new ValueException((Value) value, "Value or Reference in value is illegal.");
        } else if (value instanceof Reference) {
            throw new ValueException(((Reference) value).getReferredValue(), "Reference in value is illegal.");
        } else if (value == null) {
            return Type.NULL;
        } else if (value instanceof Number) {
            return Type.NUMBER;
        } else if (value instanceof Character) {
            return Type.STRING;
        } else if (value instanceof String) {
            return Type.STRING;
        } else if (value instanceof Boolean) {
            return Type.BOOLEAN;
        } else if (value instanceof Function) {
            return Type.FUNCTION;
        } else if (value instanceof MethodHandle[]) {
            return Type.JAVA_METHOD_HANDLES;
        } else if (value.getClass().isArray() || value instanceof ScriptArray) {
            return Type.ARRAY;
        } else if (value instanceof ScriptObject || value instanceof Map) {
            return Type.SCRIPT_OBJECT;
        } else if (value instanceof Class<?>) {
            return Type.JAVA_CLASS;
        } else if (value instanceof Component) {
            return Type.ADVENTURE_COMPONENT;
        } else {
            return Type.JAVA_OBJECT;
        }
    }

    public @Nullable Object getValue() throws ValueException {
        if (type == Type.VOID)
            throw new ValueException(this, "Impossible to get value of void.");
        return value;
    }

    public @NotNull Type getType() {
        return type;
    }

    public boolean isType(@NotNull Type expected) {
        return expected == this.type;
    }

    public boolean isType(@NotNull Type @NotNull ... expected) {
        for (Type t : expected) {
            if (this.type.equals(t)) {
                return true;
            }
        }
        return false;
    }

    public @NotNull BigDecimal getAsBigDecimal() throws ValueException {
        if (!isType(Type.NUMBER))
            throw new ValueException(this, "Value " + this + " is not a number but: " + type);
        assert value != null;
        if (value instanceof BigDecimal)
            return (BigDecimal) value;
        else {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        }
    }

    /**
     * 尽力将所有值转换为字符串
     *
     * @see Value#toReadableString()
     */
    public @NotNull String getAsString() {
        if (value instanceof Character)
            return String.valueOf((char) value);
        else if (value instanceof String)
            return (String) value;
        else
            return toReadableString();
    }

    public char getAsChar() throws ValueException {
        if (value == null) return '\0';

        if (isType(Type.STRING)) {
            String s = (String) value;
            return s.isEmpty() ? '\0' : s.charAt(0);
        }
        
        throw new ValueException(this, "Value " + this + " is not a string (for char conversion) but: " + type);
    }

    /**
     * 尽力将所有值转换为布尔值<br/>
     * 遵守 <a href="https://developer.mozilla.org/en-US/docs/Glossary/Truthy">JS 规则</a>
     *
     */
    public boolean getAsBoolean() {
        if (value == null)
            return false;

        return switch (type) {
            case BOOLEAN -> (boolean) value;
            case STRING -> !((String) value).isEmpty();
            case NUMBER -> getAsBigDecimal().compareTo(BigDecimal.ZERO) != 0;
            default -> true;
        };
    }

    public @NotNull ScriptArray getAsArray() throws ValueException {
        if (!isType(Type.ARRAY, Type.NULL))
            throw new ValueException(this, "Value " + this + " is not an array but: " + type);
        if (value != null && value instanceof ScriptArray) {
            return (ScriptArray) value;
        } else if (value == null) {
            return new ScriptArray();
        } else
            throw new ValueException(
                    this,
                    "Value has type " + type + ", but it is neither null nor a ScriptArray (actual class: "
                            + value.getClass() + ")");
    }

    public @NotNull ScriptObject getAsScriptObject() throws ValueException {
        if (!isType(Type.SCRIPT_OBJECT, Type.FUNCTION))
            throw new ValueException(this, "Value " + this + " is not a script object but: " + type);
        assert value != null;
        return (ScriptObject) value;
    }

    public @NotNull Function getAsFunction() throws ValueException {
        if (type != Type.FUNCTION)
            throw new ValueException(this, "Value " + this + " is not a function.");
        assert value != null;
        return (Function) value;
    }

    public @NotNull MethodHandle[] getAsMethodHandles() throws ValueException {
        if (type != Type.JAVA_METHOD_HANDLES)
            throw new ValueException(this, "Value " + this + " is not a method handle array.");
        return (MethodHandle[]) value;
    }

    public @NotNull Class<?> getAsClass() throws ValueException {
        if (type != Type.JAVA_CLASS)
            throw new ValueException(this, "Value " + this + " is not a class.");
        assert value != null;
        return (Class<?>) value;
    }

    /**
     * 尽力将所有值转换为组件<br/>
     * 对于 {@link Type#VOID} 和 {@link Type#NULL} 类型的值，返回
     * {@link Component#empty()}；<br />
     * 对于 {@link Type#ADVENTURE_COMPONENT} 类型的值，直接返回；<br />
     * 对于 {@link Type#STRING} 类型的值，将其视为 MiniMessage 并反序列化为组件再返回；<br />
     * 对于其他类型的值，视为纯文本，先用 {@link Value#toReadableString()} 转换为可读文本再用
     * {@link Component#text()} 包装为组件后返回；<br />
     *
     */
    public @NotNull Component getAsAdventureComponent() {
        if (value == null)
            return Component.empty();

        if (type == Type.ADVENTURE_COMPONENT)
            return (Component) value;
        else if (type == Type.STRING)
            return HyphaScript.miniMessage.deserialize((String) value).decorationIfAbsent(TextDecoration.ITALIC,
                    TextDecoration.State.FALSE);
        else
            return Component.text(toReadableString()).decorationIfAbsent(TextDecoration.ITALIC,
                    TextDecoration.State.FALSE);
    }

    public @NotNull Class<?> getClassOfValue() throws ValueException {
        if (value == null)
            throw new ValueException(this, "Impossible to get class of null value.");
        return value.getClass();
    }

    public @Nullable Object getAs(@NotNull Type type) {
        return switch (type) {
            case NULL, VOID -> null;
            case BOOLEAN -> getAsBoolean();
            case ARRAY -> getAsArray();
            case SCRIPT_OBJECT -> getAsScriptObject();
            case FUNCTION -> getAsFunction();
            case JAVA_OBJECT -> getValue();
            case JAVA_CLASS -> getAsClass();
            case STRING -> getAsString();
            case NUMBER -> getAsBigDecimal();
            case JAVA_METHOD_HANDLES -> getAsMethodHandles();
            case ADVENTURE_COMPONENT -> getAsAdventureComponent();
        };
    }

    public @NotNull String toReadableString() {
        // Both Type have null value
        if (type == Type.NULL)
            return "null";
        else if (type == Type.VOID)
            return "void";
        assert value != null;

        try {
            return switch (type) {
                case NUMBER -> StringUtils.toString((Number) value);
                case STRING -> "\"" + value + "\"";
                case BOOLEAN -> Boolean.toString((boolean) value);
                case SCRIPT_OBJECT -> ((ScriptObject) value).toString();
                case FUNCTION -> ((Function) value).toString();
                case JAVA_CLASS -> StringUtils.toString((Class<?>) value);
                case JAVA_METHOD_HANDLES -> StringUtils.toString((MethodHandle[]) value);
                case ARRAY -> StringUtils.toString((ScriptArray) value);
                case ADVENTURE_COMPONENT -> StringUtils.toString((Component) value);
                case JAVA_OBJECT -> StringUtils.toString(value);
                default -> throw new IllegalStateException("Unexpected value: " + type);
            };
        } catch (Exception e) {
            return "[Unreadable: " + type + " (" + e.getMessage() + ")]";
        }
    }

    @Override
    public String toString() {
        return "Value{" +
                "value=" + value +
                ", type=" + type +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Value value1 = (Value) o;
        return Objects.equals(value, value1.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    public enum Type {
        VOID(null),
        NULL(null),
        NUMBER(Number.class),
        STRING(String.class),
        BOOLEAN(Boolean.class),
        SCRIPT_OBJECT(ScriptObject.class),
        FUNCTION(Function.class),
        JAVA_OBJECT(Object.class),
        JAVA_CLASS(Class.class),
        JAVA_METHOD_HANDLES(MethodHandle[].class),
        ADVENTURE_COMPONENT(Component.class),
        ARRAY(ScriptArray.class);

        private final @Nullable Class<?> clazz;

        Type(@Nullable Class<?> clazz) {
            this.clazz = clazz;
        }

        public static @NotNull Type fromClass(@NotNull Class<?> clazz) {
            for (Type type : Type.values()) {
                if (clazz.equals(type.clazz)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown class " + clazz);
        }
    }
}