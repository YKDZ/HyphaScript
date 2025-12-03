package cn.encmys.ykdz.forest.hyphascript.value;

import cn.encmys.ykdz.forest.hyphascript.exception.ReferenceException;
import org.jetbrains.annotations.NotNull;

public class Reference implements Cloneable {
    private @NotNull Value value;
    private boolean isConst;

    public Reference(@NotNull Value value) {
        this(value, false);
    }

    public Reference(@NotNull Value value, boolean isConst) {
        this.value = value;
        this.isConst = isConst;
    }

    public Reference() {
        this(new Value());
    }

    public boolean isConst() {
        return isConst;
    }

    public void setConst(boolean isConst) {
        this.isConst = isConst;
    }

    public @NotNull Value getReferredValue() {
        return value;
    }

    public void setReferredValue(@NotNull Value value, boolean typeCheck) {
        if (isConst) throw new ReferenceException(this, "Tried to reassign-value for const variable");
        if (typeCheck && this.value.type() != value.type())
            throw new ReferenceException(this, "Type error. New type is " + value.type() + " but old type is " + this.value.type());
        this.value = value;
    }

    /**
     * Value in reference will not be cloned
     *
     * @return Cloned
     */
    @Override
    public Reference clone() {
        try {
            Reference cloned = (Reference) super.clone();
            cloned.value = this.value;
            cloned.isConst = this.isConst;
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    @Override
    public String toString() {
        return "Reference{" +
                "value=" + value.toReadableString() +
                ", isConst=" + isConst +
                '}';
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
