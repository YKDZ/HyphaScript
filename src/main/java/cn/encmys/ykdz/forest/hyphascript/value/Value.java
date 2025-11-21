package cn.encmys.ykdz.forest.hyphascript.value;

import cn.encmys.ykdz.forest.hyphascript.exception.ValueException;
import cn.encmys.ykdz.forest.hyphascript.function.Function;
import cn.encmys.ykdz.forest.hyphascript.oop.ScriptObject;
import cn.encmys.ykdz.forest.hyphascript.utils.StringUtils;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.Objects;

public class Value {
    @NotNull
    private final Type type;
    @Nullable
    private Object value;

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
     * @see #setValue(Object, boolean)
     */
    public Value(@Nullable Object value) {
        this.type = calType(value);
        setValue(this.type, value, false);
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
            return Type.CHAR;
        } else if (value instanceof String) {
            return Type.STRING;
        } else if (value instanceof Boolean) {
            return Type.BOOLEAN;
        } else if (value instanceof Function) {
            return Type.FUNCTION;
        } else if (value instanceof ScriptObject) {
            return Type.SCRIPT_OBJECT;
        } else if (value instanceof Class<?>) {
            return Type.JAVA_CLASS;
        } else if (value instanceof Component) {
            return Type.ADVENTURE_COMPONENT;
        } else if (value instanceof MethodHandle[]) {
            return Type.JAVA_METHOD_HANDLES;
        } else if (value.getClass().isArray()) {
            return Type.ARRAY;
        } else {
            return Type.JAVA_OBJECT;
        }
    }

    public @Nullable Object getValue() throws ValueException {
        if (type == Type.VOID) throw new ValueException(this, "Impossible to get value of void.");
        return value;
    }

    public void setValue(@Nullable Object value, boolean typeCheck) {
        final Type newType = calType(value);
        setValue(newType, value, typeCheck);
    }

    private void setValue(@NotNull Type type, @Nullable Object value, boolean typeCheck) {
        if (typeCheck && this.type != type)
            throw new ValueException(this, "Type error. New type is " + type + " but old type is " + this.type);

        if (value != null && type == Type.ARRAY && value.getClass().isArray()) {
            Class<?> componentType = value.getClass().getComponentType();

            if (Reference.class.equals(componentType)) {
                this.value = value;
                return;
            }

            int len = Array.getLength(value);
            Reference[] refs = new Reference[len];
            for (int i = 0; i < len; i++) {
                Object elem = Array.get(value, i);
                if (elem instanceof Reference) {
                    refs[i] = (Reference) elem;
                } else {
                    refs[i] = new Reference(new Value(elem));
                }
            }
            this.value = refs;
        } else {
            this.value = value;
        }
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
        if (value instanceof BigDecimal) return (BigDecimal) value;
        else {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        }
    }

    public @NotNull String getAsString() throws ValueException {
        if (value instanceof Character) return String.valueOf((char) value);
        else if (value instanceof String) return (String) value;
        else return toReadableString();
    }

    public char getAsChar() throws ValueException {
        if (!isType(Type.CHAR, Type.NULL))
            throw new ValueException(this, "Value " + this + " is not a char but: " + type);
        return value == null ? '\0' : (char) value;
    }

    public boolean getAsBoolean() {
        if (type == Type.NULL) return false;
        assert value != null;

        return switch (type) {
            case BOOLEAN -> (boolean) value;
            case ARRAY -> ((Reference[]) value).length > 0;
            case STRING -> !((String) value).isEmpty();
            case NUMBER -> getAsBigDecimal().compareTo(new BigDecimal("0")) != 0;
            default -> true;
        };
    }

    public @NotNull Reference[] getAsArray() throws ValueException {
        if (!isType(Type.ARRAY, Type.NULL))
            throw new ValueException(this, "Value " + this + " is not an array but: " + type);
        if (value != null && value instanceof Reference[]) {
            return (Reference[]) value;
        } else if (value == null) {
            return new Reference[0];
        } else
            throw new ValueException(
                    this,
                    "Value has type " + type + ", but it is neither null nor a Reference[] (actual class: " + value.getClass() + ")"
            );
    }

    public @NotNull ScriptObject getAsScriptObject() throws ValueException {
        if (!isType(Type.SCRIPT_OBJECT, Type.FUNCTION))
            throw new ValueException(this, "Value " + this + " is not a script object but: " + type);
        assert value != null;
        return (ScriptObject) value;
    }

    public @NotNull Function getAsFunction() throws ValueException {
        if (type != Type.FUNCTION) throw new ValueException(this, "Value " + this + " is not a function.");
        assert value != null;
        return (Function) value;
    }

    public @NotNull MethodHandle[] getAsMethodHandles() throws ValueException {
        if (type != Type.JAVA_METHOD_HANDLES)
            throw new ValueException(this, "Value " + this + " is not a method handle array.");
        return (MethodHandle[]) value;
    }

    public @NotNull Class<?> getAsClass() throws ValueException {
        if (type != Type.JAVA_CLASS) throw new ValueException(this, "Value " + this + " is not a class.");
        assert value != null;
        return (Class<?>) value;
    }

    public @NotNull Component getAsAdventureComponent() throws ValueException {
        if (type != Type.ADVENTURE_COMPONENT)
            throw new ValueException(this, "Value " + this + " is not a component but: " + type);
        assert value != null;
        return (Component) value;
    }

    public @NotNull Class<?> getClassOfValue() throws ValueException {
        if (value == null) throw new ValueException(this, "Impossible to get class of null value.");
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
            case CHAR -> getAsChar();
        };
    }

    public @NotNull String toReadableString() {
        // Both Type have null value
        if (type == Type.NULL) return "null";
        else if (type == Type.VOID) return "void";
        assert value != null;

        try {
            return switch (type) {
                case CHAR -> String.valueOf((char) value);
                case NUMBER -> StringUtils.toString((Number) value);
                case STRING -> "\"" + value + "\"";
                case BOOLEAN -> Boolean.toString((boolean) value);
                case SCRIPT_OBJECT -> ((ScriptObject) value).toString();
                case FUNCTION -> ((Function) value).toString();
                case JAVA_CLASS -> StringUtils.toString((Class<?>) value);
                case JAVA_METHOD_HANDLES -> StringUtils.toString((MethodHandle[]) value);
                case ARRAY -> StringUtils.toString((Reference[]) value);
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
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
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
        CHAR(Character.class),
        NUMBER(Number.class),
        STRING(String.class),
        BOOLEAN(Boolean.class),
        SCRIPT_OBJECT(ScriptObject.class),
        FUNCTION(Function.class),
        JAVA_OBJECT(Object.class),
        JAVA_CLASS(Class.class),
        JAVA_METHOD_HANDLES(MethodHandle[].class),
        ADVENTURE_COMPONENT(Component.class),
        ARRAY(Reference[].class);

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