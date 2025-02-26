package cn.encmys.ykdz.forest.hyphascript.value;

import cn.encmys.ykdz.forest.hyphascript.exception.VariableException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Reference implements Cloneable {
    @Nullable
    private final String name;
    @NotNull
    private Value value;
    private boolean isConst;
    private boolean isExported;

    public Reference(@Nullable String name, @NotNull Value value) {
        this(name, value, false, false);
    }

    public Reference(@Nullable String name, @NotNull Value value, boolean isConst, boolean isExported) {
        this.name = name;
        this.value = value;
        this.isConst = isConst;
        this.isExported = isExported;
    }

    public Reference() {
        this(null, new Value());
    }

    public boolean isConst() {
        return isConst;
    }

    public void setConst(boolean isConst) {
        this.isConst = isConst;
    }

    public boolean isExported() {
        return isExported;
    }

    public void setExported(boolean isExported) {
        this.isExported = isExported;
    }

    @Nullable
    public String getName() {
        return name;
    }

    public @NotNull Value getReferedValue() {
        return value;
    }

    public void setReferredValue(@NotNull Value value) {
        if (isConst) throw new VariableException(this, "Tried to reassign-value for const variable");
        this.value = value;
    }

    @Override
    public Reference clone() {
        try {
            Reference cloned = (Reference) super.clone();
            cloned.value = this.value;
            cloned.isConst = this.isConst;
            cloned.isExported = this.isExported;
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    @Override
    public String toString() {
        return "Reference{" +
                "name='" + name + '\'' +
                ", value=" + value +
                ", isConst=" + isConst +
                ", isExported=" + isExported +
                '}';
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
