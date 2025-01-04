package cn.encmys.ykdz.forest.hypha.value;

import cn.encmys.ykdz.forest.hypha.exception.ValueException;
import cn.encmys.ykdz.forest.hypha.function.Function;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Value {
    public enum Type { VOID, NULL, CHAR, BIG_DECIMAL, STRING, BOOLEAN, NESTED_OBJECT, FUNCTION, JAVA_OBJECT, JAVA_CLASS, JAVA_FIELD, JAVA_METHOD_HANDLES, ARRAY }

    @Nullable
    private Object value;
    @NotNull
    private final Type type;

    /**
     * Construct a value with Type VOID
     * <p>
     * Try to get value of VOID value will throw ContextException
     * @see #getValue()
     */
    public Value() {
        this.value = null;
        this.type = Type.VOID;
    }

    /**
     * Construct a value with given isConst
     * <p>
     * The type of value will be inferred automatically
     * @param value Value of value
     * @see #setValue(Object)
     */
    public Value(@Nullable Object value) {
        this.value = value;
        this.type = calType(value);
    }

    @Nullable
    public Object getValue() throws ValueException {
        if (type == Type.VOID) throw new ValueException(this, "Impossible to get value of void.");
        return value;
    }

    @NotNull
    public Type getType() {
        return type;
    }

    public boolean isType(@NotNull Type expected) {
        return this.type == expected;
    }

    @NotNull
    public BigDecimal getAsBigDecimal() throws ValueException {
        if (type != Type.BIG_DECIMAL) throw new ValueException(this, "This value is not a big decimal but: " + type);
        if (value instanceof BigDecimal) return (BigDecimal) value;
        else if (value instanceof Double || value instanceof Float) {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        } else {
            // 已检查此值的类型为 Type.DECIMAL
            assert value != null;
            return new BigDecimal(value.toString());
        }
    }

    @NotNull
    public String getAsString() throws ValueException {
        if (type != Type.STRING && type != Type.CHAR) throw new ValueException(this, "This value is not a string but: " + type);
        assert value != null;
        if (value instanceof Character) return String.valueOf((char) value);
        return (String) value;
    }

    public char getAsChar() throws ValueException {
        if (type != Type.CHAR) throw new ValueException(this, "This value is not a char but: " + type);
        assert value != null;
        return (char) value;
    }

    public boolean getAsBoolean() throws ValueException {
        if (type != Type.BOOLEAN) throw new ValueException(this, "This value is not a boolean but: " + type);
        assert value != null;
        return (boolean) value;
    }

    @NotNull
    public Reference[] getAsArray() throws ValueException {
        if (type != Type.ARRAY) throw new ValueException(this, "This value is not an array but: " + type);
        assert value != null;
        if (value.getClass().isArray()) {
            int length = Array.getLength(value);
            Reference[] result = new Reference[length];
            for (int i = 0; i < length; i++) {
                result[i] = (Reference) Array.get(value, i);
            }
            return result;
        } else {
            throw new ValueException(this, "Value is not an array.");
        }
    }

    @SuppressWarnings("unchecked")
    @NotNull
    public Map<String, Reference> getAsNestedObject() throws ValueException {
        if (type != Type.NESTED_OBJECT) throw new ValueException(this, "This value is not a nested object but: " + type);
        assert value != null;
        return Collections.unmodifiableMap(((Map<String, Reference>) value));
    }

    @NotNull
    public Function getAsFunction() throws ValueException {
        if (type != Type.FUNCTION) throw new ValueException(this, "This value is not a function.");
        assert value != null;
        return (Function) value;
    }

    @NotNull
    public Field getAsField() throws ValueException {
        if (type != Type.JAVA_FIELD) throw new ValueException(this, "This value is not a field.");
        assert value != null;
        return (Field) value;
    }

    @NotNull
    public MethodHandle[] getAsMethodHandles() throws ValueException {
        if (type != Type.JAVA_METHOD_HANDLES) throw new ValueException(this, "This value is not a method handle array.");
        return (MethodHandle[]) value;
    }

    @NotNull
    public Class<?> getAsClass() throws ValueException {
        if (type != Type.JAVA_CLASS) throw new ValueException(this, "This value is not a class.");
        assert value != null;
        return (Class<?>) value;
    }

    @NotNull
    public Class<?> getValueClass() throws ValueException {
        if (value == null) throw new ValueException(this, "Impossible to get class of null value.");
        return value.getClass();
    }

    public void setValue(@Nullable Object value) {
        this.value = value;
        calType(value);
    }

    private static Type calType(@Nullable Object value) {
        if (value instanceof Value || value instanceof Reference) {
            throw new IllegalArgumentException("Value or Reference in value is illegal.");
        } else if (value == null) {
            return Type.NULL;
        } else if (value instanceof Number) {
            return Type.BIG_DECIMAL;
        } else if (value instanceof Character) {
            return Type.CHAR;
        } else if (value instanceof String) {
            return Type.STRING;
        } else if (value instanceof Boolean) {
            return Type.BOOLEAN;
        } else if (value instanceof HashMap<?,?>) {
            return Type.NESTED_OBJECT;
        } else if (value instanceof Function) {
            return Type.FUNCTION;
        } else if (value instanceof Class<?>) {
            return Type.JAVA_CLASS;
        } else if (value instanceof Field) {
            return Type.JAVA_FIELD;
        } else if (value instanceof MethodHandle[]) {
            return Type.JAVA_METHOD_HANDLES;
        } else if (value.getClass().isArray()) {
            return Type.ARRAY;
        } else {
            return Type.JAVA_OBJECT;
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
}